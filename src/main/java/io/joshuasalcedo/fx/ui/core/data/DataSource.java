package io.joshuasalcedo.fx.ui.core.data;

import java.util.concurrent.CompletableFuture;

/// Where a collection's items come from. `load` returns at once; the page may come later (a request to a server)
/// or be there already ([CompletableFuture#completedFuture], as ListDataSource does). The collection shows the
/// page on the JavaFX thread, whichever thread completes the future.
///
/// For a source that fetches with a blocking call (an HTTP client), implement PagedDataSource instead.
@FunctionalInterface
public interface DataSource<T> {

    /// Starts loading the page `request` asks for.
    CompletableFuture<Page<T>> load(PageRequest request);
}
