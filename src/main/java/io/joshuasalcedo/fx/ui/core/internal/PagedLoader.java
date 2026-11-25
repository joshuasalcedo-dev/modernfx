package io.joshuasalcedo.fx.ui.core.internal;

import io.joshuasalcedo.fx.ui.core.data.DataSource;
import io.joshuasalcedo.fx.ui.core.data.LoadState;
import io.joshuasalcedo.fx.ui.core.data.Page;
import io.joshuasalcedo.fx.ui.core.data.PageRequest;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Loads a collection's items from a DataSource, a page at a time, into its ObservableList, and keeps its LoadState.
/// A reload drops whatever an older load returns later. Without a source, the state follows the list (EMPTY or
/// LOADED).
@NullMarked
public final class PagedLoader<T> {

    private final ObservableList<T> items;
    private final ReadOnlyObjectWrapper<LoadState> state = new ReadOnlyObjectWrapper<>(this, "state", LoadState.IDLE);
    private final ReadOnlyLongWrapper total = new ReadOnlyLongWrapper(this, "total", Page.UNKNOWN_TOTAL);
    private final ReadOnlyBooleanWrapper hasMore = new ReadOnlyBooleanWrapper(this, "hasMore");
    private final ReadOnlyStringWrapper error = new ReadOnlyStringWrapper(this, "error", "");
    private @Nullable DataSource<T> source;
    private PageRequest request = PageRequest.first(PageRequest.DEFAULT_LIMIT);
    private PageRequest lastRequest = request;
    private @Nullable Page<T> lastPage;
    private int generation;
    private Runnable afterLoad = () -> { };

    public PagedLoader(ObservableList<T> items) {
        this.items = items;
        items.addListener((ListChangeListener<T>) c -> {
            if (source == null) {
                localState();
            }
        });
        localState();
    }

    public ReadOnlyObjectProperty<LoadState> stateProperty() { return state.getReadOnlyProperty(); }
    public LoadState getState() { return state.get(); }
    public ReadOnlyLongProperty totalProperty() { return total.getReadOnlyProperty(); }
    public ReadOnlyBooleanProperty hasMoreProperty() { return hasMore.getReadOnlyProperty(); }
    public ReadOnlyStringProperty errorProperty() { return error.getReadOnlyProperty(); }

    public @Nullable DataSource<T> getSource() {
        return source;
    }

    /// Runs after each page is in the list (to select again what was selected).
    public void setAfterLoad(Runnable run) {
        this.afterLoad = Objects.requireNonNull(run);
    }

    /// Loads from this source from the first page (or, for null, stops loading and keeps the items).
    public void setSource(@Nullable DataSource<T> value) {
        source = value;
        reload();
    }

    /// The request of the first page: its size, sort and filter. Reloads.
    public void setRequest(PageRequest value) {
        request = Objects.requireNonNull(value);
        reload();
    }

    public PageRequest getRequest() {
        return request;
    }

    /// Loads the first page again.
    public void reload() {
        int gen = ++generation;
        DataSource<T> s = source;
        if (s == null) {
            hasMore.set(false);
            total.set(items.size());
            localState();
            return;
        }
        state.set(LoadState.LOADING);
        error.set("");
        PageRequest r = request;
        start(s, r, gen, false);
    }

    /// Loads the page after the last one, under the items already there. Does nothing while loading or at the end.
    public void loadMore() {
        DataSource<T> s = source;
        Page<T> last = lastPage;
        if (s == null || last == null || state.get().isBusy() || !hasMore.get()) {
            return;
        }
        state.set(LoadState.LOADING_MORE);
        start(s, lastRequest.next(last.nextCursor()), generation, true);
    }

    private void start(DataSource<T> s, PageRequest r, int gen, boolean append) {
        try {
            s.load(r).whenComplete((page, err) -> onFx(() -> done(gen, r, page, err, append)));
        } catch (RuntimeException e) {
            done(gen, r, null, e, append);
        }
    }

    private void done(int gen, PageRequest r, @Nullable Page<T> page, @Nullable Throwable err, boolean append) {
        if (gen != generation) {
            return;
        }
        if (err != null || page == null) {
            Throwable cause = err instanceof CompletionException && err.getCause() != null ? err.getCause() : err;
            error.set(cause == null || cause.getMessage() == null ? "" : cause.getMessage());
            state.set(append ? LoadState.LOADED : LoadState.ERROR);
            return;
        }
        lastRequest = r;
        lastPage = page;
        if (append) {
            items.addAll(page.items());
        } else {
            items.setAll(page.items());
        }
        total.set(page.hasTotal() ? page.total() : Page.UNKNOWN_TOTAL);
        hasMore.set(page.hasMore(r));
        state.set(items.isEmpty() ? LoadState.EMPTY : LoadState.LOADED);
        afterLoad.run();
    }

    private void localState() {
        if (!state.get().isBusy()) {
            state.set(items.isEmpty() ? LoadState.EMPTY : LoadState.LOADED);
        }
    }

    private static void onFx(Runnable run) {
        if (Platform.isFxApplicationThread()) {
            run.run();
        } else {
            Platform.runLater(run);
        }
    }
}
