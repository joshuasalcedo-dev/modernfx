package io.joshuasalcedo.fx.utils.state;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.System.Logger.Level;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/// A [Store] whose state is kept in a JSON file on this computer: read back when the app starts, written again
/// shortly after each change, and once more on [#close].
///
/// ```java
/// try (var store = PersistentStore.builder(Settings.class, Settings.DEFAULTS, Settings::reduce)
///         .app("Acme", "notes")
///         .open()) {
///     store.dispatch(new ThemeChanged("light"));
/// }
/// ```
///
/// The file is `{"version": n, "state": {...}}`. Saves go to a `.tmp` file that then replaces the real one, so a
/// crash mid-save leaves the old file whole. A file that can't be read is renamed to `<name>.broken` and the store
/// starts from the initial state.
///
/// Keep only what should outlive the app here (settings, drafts, window size); never secrets. When the state's
/// shape changes, raise [Builder#version] and add a [Builder#migrate] step from the old version.
///
/// @param <S> the state: a record (or other value) that Jackson can write and read
/// @param <A> the actions
@NullMarked
public final class PersistentStore<S, A> extends Store<S, A> implements AutoCloseable {

    private static final System.Logger LOG = System.getLogger(PersistentStore.class.getName());

    private final Path file;
    private final int version;
    private final ObjectMapper mapper;
    private final Duration saveDelay;
    private final ScheduledExecutorService saver;

    // Only touched on the saver thread.
    private @Nullable S pending;
    private @Nullable ScheduledFuture<?> scheduled;
    private boolean closed;

    private PersistentStore(Builder<S, A> b, S loaded) {
        super(loaded, b.reducer, b.updates);
        this.file = b.file();
        this.version = b.version;
        this.mapper = b.mapper;
        this.saveDelay = b.saveDelay;
        this.saver = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread t = new Thread(task, "modernfx-state-" + file.getFileName());
            t.setDaemon(true);
            return t;
        });
        state().addListener((obs, old, now) -> saver.execute(() -> changed(now)));
    }

    /// Starts building a store for the given state type.
    ///
    /// @param type    the state's class, for reading it back
    /// @param initial the state when there's no file yet, or it can't be read
    /// @param reducer current state + action -> next state
    public static <S, A> Builder<S, A> builder(Class<S> type, S initial, BiFunction<S, A, S> reducer) {
        return new Builder<>(type, initial, reducer);
    }

    /// The JSON file the state is kept in.
    public Path file() {
        return file;
    }

    /// Writes the latest state now, if it changed since the last save, and waits until it's on disk.
    public void flush() {
        try {
            saver.submit(this::saveNow).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw e.getCause() instanceof UncheckedIOException u ? u : new IllegalStateException(e.getCause());
        }
    }

    /// Writes the latest state and stops saving. Later changes stay in memory only.
    @Override
    public void close() {
        if (saver.isShutdown()) {
            return;
        }
        try {
            flush();
        } finally {
            saver.execute(() -> closed = true);
            saver.shutdown();
            try {
                saver.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void changed(S now) {
        if (closed) {
            return;
        }
        pending = now;
        if (scheduled != null) {
            scheduled.cancel(false);
        }
        scheduled = saver.schedule(this::saveQuietly, saveDelay.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void saveQuietly() {
        try {
            saveNow();
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "could not save the state to " + file + "; will try again on the next change", e);
        }
    }

    private void saveNow() {
        if (scheduled != null) {
            scheduled.cancel(false);
            scheduled = null;
        }
        S toSave = pending;
        if (toSave == null || closed) {
            return;
        }
        ObjectNode root = mapper.createObjectNode();
        root.put("version", version);
        root.set("state", mapper.valueToTree(toSave));
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, mapper.writeValueAsString(root));
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
            pending = null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /// Settings for a [PersistentStore]. Only [#app] or [#file] is required.
    public static final class Builder<S, A> {
        private final Class<S> type;
        private final S initial;
        private final BiFunction<S, A, S> reducer;
        private @Nullable Path file;
        private int version = 1;
        private final Map<Integer, UnaryOperator<ObjectNode>> migrations = new TreeMap<>();
        private Duration saveDelay = Duration.ofMillis(300);
        private Executor updates = Store.fxThread();
        private ObjectMapper mapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        private Builder(Class<S> type, S initial, BiFunction<S, A, S> reducer) {
            this.type = Objects.requireNonNull(type, "type");
            this.initial = Objects.requireNonNull(initial, "initial");
            this.reducer = Objects.requireNonNull(reducer, "reducer");
        }

        /// Keeps the state in `state.json` in the app's data folder (see [AppDirs#data]).
        public Builder<S, A> app(String vendor, String app) {
            return file(AppDirs.data(vendor, app).resolve("state.json"));
        }

        /// Keeps the state in this file.
        public Builder<S, A> file(Path file) {
            this.file = Objects.requireNonNull(file, "file");
            return this;
        }

        /// The version of the state's shape (starts at 1). Raise it when fields are renamed or change meaning.
        public Builder<S, A> version(int version) {
            if (version < 1) {
                throw new IllegalArgumentException("version starts at 1");
            }
            this.version = version;
            return this;
        }

        /// How to turn a saved state of version `from` into version `from + 1`. Steps run one after another, so a
        /// version 1 file reaches version 3 through the 1 and 2 steps.
        public Builder<S, A> migrate(int from, UnaryOperator<ObjectNode> step) {
            migrations.put(from, Objects.requireNonNull(step, "step"));
            return this;
        }

        /// How long to wait after the last change before saving (default 300 ms).
        public Builder<S, A> saveDelay(Duration saveDelay) {
            this.saveDelay = Objects.requireNonNull(saveDelay, "saveDelay");
            return this;
        }

        /// Where the state changes (default the JavaFX application thread; `Runnable::run` for tests).
        public Builder<S, A> updates(Executor updates) {
            this.updates = Objects.requireNonNull(updates, "updates");
            return this;
        }

        /// The Jackson mapper for the state, when it needs its own modules or settings.
        public Builder<S, A> mapper(ObjectMapper mapper) {
            this.mapper = Objects.requireNonNull(mapper, "mapper");
            return this;
        }

        /// Reads the saved state (or starts from the initial one) and opens the store.
        public PersistentStore<S, A> open() {
            return new PersistentStore<>(this, load());
        }

        private Path file() {
            if (file == null) {
                throw new IllegalStateException("say where to keep the state: app(\"vendor\", \"app\") or file(path)");
            }
            return file;
        }

        private S load() {
            Path f = file();
            if (!Files.exists(f)) {
                return initial;
            }
            try {
                JsonNode root = mapper.readTree(Files.readString(f));
                if (!(root instanceof ObjectNode obj) || !(obj.get("state") instanceof ObjectNode saved)) {
                    throw new IllegalStateException("no \"state\" object in the file");
                }
                int from = obj.path("version").asInt(1);
                if (from > version) {
                    throw new IllegalStateException("saved by a newer version of the app (state version " + from
                            + ", this app knows up to " + version + ")");
                }
                ObjectNode state = saved;
                for (int v = from; v < version; v++) {
                    UnaryOperator<ObjectNode> step = migrations.get(v);
                    if (step == null) {
                        throw new IllegalStateException("no migrate(" + v + ", ...) step to version " + (v + 1));
                    }
                    state = Objects.requireNonNull(step.apply(state), "a migration step returned null");
                }
                S loaded = mapper.treeToValue(state, type);
                return loaded != null ? loaded : initial;
            } catch (IOException | RuntimeException e) {
                Path broken = f.resolveSibling(f.getFileName() + ".broken");
                LOG.log(Level.WARNING, "could not read the state in " + f + "; moved it to " + broken
                        + " and started from the initial state", e);
                try {
                    Files.move(f, broken, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException moveFailed) {
                    LOG.log(Level.WARNING, "could not move " + f + " aside", moveFailed);
                }
                return initial;
            }
        }
    }
}
