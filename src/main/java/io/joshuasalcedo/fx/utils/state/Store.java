package io.joshuasalcedo.fx.utils.state;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import org.jspecify.annotations.NullMarked;

/// Holds an app's state in one place. The only way to change it is [#dispatch]: the reducer turns the current
/// state and the action into the next state. Bind the UI to [#state], or to a part of it with
/// `store.state().map(AppState::loading)`.
///
/// The state should be a record (or another value that is never changed in place), the reducer a plain function
/// with no side effects. Do the slow work (HTTP, files) outside, then dispatch what came of it.
///
/// @param <S> the state
/// @param <A> the actions, best a sealed interface of records
@NullMarked
public class Store<S, A> {

    private final ReadOnlyObjectWrapper<S> state;
    private final BiFunction<S, A, S> reducer;
    private final Executor updates;

    /// A store whose state changes on the JavaFX application thread, so `dispatch` is safe from any thread.
    public Store(S initial, BiFunction<S, A, S> reducer) {
        this(initial, reducer, fxThread());
    }

    /// A store whose state changes on the given executor. Pass `Runnable::run` to change it right away on the
    /// calling thread (tests, or code with no JavaFX).
    public Store(S initial, BiFunction<S, A, S> reducer, Executor updates) {
        this.state = new ReadOnlyObjectWrapper<>(this, "state", Objects.requireNonNull(initial, "initial"));
        this.reducer = Objects.requireNonNull(reducer, "reducer");
        this.updates = Objects.requireNonNull(updates, "updates");
    }

    /// The current state, to bind to or listen on.
    public ReadOnlyObjectProperty<S> state() {
        return state.getReadOnlyProperty();
    }

    /// The current state, now.
    public S get() {
        return state.get();
    }

    /// Runs the action through the reducer and sets the new state. Safe to call from any thread.
    public void dispatch(A action) {
        Objects.requireNonNull(action, "action");
        updates.execute(() -> {
            S next = Objects.requireNonNull(reducer.apply(state.get(), action),
                    () -> "the reducer returned null for " + action);
            state.set(next);
        });
    }

    /// Runs the task on the JavaFX application thread: right away when already on it, otherwise soon after.
    public static Executor fxThread() {
        return task -> {
            if (Platform.isFxApplicationThread()) {
                task.run();
            } else {
                Platform.runLater(task);
            }
        };
    }
}
