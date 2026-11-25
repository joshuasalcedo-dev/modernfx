package io.joshuasalcedo.fx.ui.core.data;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/// A DataSource that pages, sorts and filters on a server: `fetch` makes the call (a REST endpoint with
/// `?offset=&limit=&sort=&q=`) and may block. `load` runs it on a virtual thread, never on the JavaFX thread.
///
/// ```java
/// PagedDataSource<Contact> contacts = request -> api.contacts(request.offset(), request.limit(), request.filter());
/// list.setDataSource(contacts);
/// ```
@FunctionalInterface
public interface PagedDataSource<T> extends DataSource<T> {

    /// Fetches one page. Runs off the JavaFX thread; throw to report an error (the collection shows it).
    Page<T> fetch(PageRequest request) throws Exception;

    /// Where `fetch` runs: one virtual thread per call unless overridden.
    default Executor executor() {
        return VirtualThreads.EXECUTOR;
    }

    @Override
    default CompletableFuture<Page<T>> load(PageRequest request) {
        CompletableFuture<Page<T>> page = new CompletableFuture<>();
        executor().execute(() -> {
            try {
                page.complete(fetch(request));
            } catch (Throwable e) {
                page.completeExceptionally(e);
            }
        });
        return page;
    }
}

/// The default executor of PagedDataSource, made once.
final class VirtualThreads {

    static final Executor EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private VirtualThreads() {
    }
}
