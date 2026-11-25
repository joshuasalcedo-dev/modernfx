package io.joshuasalcedo.fx.ui.core.tree;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.NullMarked;

/// Loads a node's children the first time it's opened (a folder's files, a team's members). The tree shows
/// "Loading…" under the node until the future completes, on whichever thread.
///
/// ```java
/// LazyTreeLoader<Folder> folders = LazyTreeLoader.blocking(f -> api.children(f.id()).stream()
///         .map(c -> c.isFolder() ? TreeNode.lazy(c) : TreeNode.leaf(c)).toList());
/// ```
@NullMarked
@FunctionalInterface
public interface LazyTreeLoader<T> {

    /// Starts loading the children of `parent`.
    CompletableFuture<List<TreeNode<T>>> children(T parent);

    /// A loader that makes a blocking call (an HTTP request), run on a virtual thread.
    static <T> LazyTreeLoader<T> blocking(BlockingChildren<T> fetch) {
        return parent -> {
            CompletableFuture<List<TreeNode<T>>> out = new CompletableFuture<>();
            Thread.ofVirtual().start(() -> {
                try {
                    out.complete(fetch.children(parent));
                } catch (Throwable e) {
                    out.completeExceptionally(e);
                }
            });
            return out;
        };
    }

    /// A blocking call that returns a node's children.
    @FunctionalInterface
    interface BlockingChildren<T> {
        List<TreeNode<T>> children(T parent) throws Exception;
    }
}
