package io.joshuasalcedo.fx.ui.core.tree;

import io.joshuasalcedo.fx.ui.core.data.KeyExtractor;
import java.util.Collection;
import java.util.Objects;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import javafx.scene.control.TreeItem;
import org.jspecify.annotations.NullMarked;

/// Remembers which nodes of a tree are open, by key, so they open again after a reload (new TreeItems for the
/// same records) or the next time the app runs (save [#keys], then [#restore] them).
///
/// ```java
/// var open = new TreeExpansionState();
/// tree.setExpansionState(open);
/// store.dispatch(new SaveOpenFolders(Set.copyOf(open.keys())));
/// ```
@NullMarked
public class TreeExpansionState {

    private final ObservableSet<Object> keys = FXCollections.observableSet();

    /// The keys of the open nodes.
    public ObservableSet<Object> keys() {
        return keys;
    }

    public boolean isExpanded(Object key) {
        return keys.contains(key);
    }

    /// Sets the open keys (saved ones). Trees tracked from now on open these nodes.
    public void restore(Collection<?> saved) {
        keys.clear();
        keys.addAll(saved);
    }

    /// Follows the tree under `root`: nodes whose key is here open, and opening or closing a node (now or later,
    /// children added later too) adds or removes its key.
    public <T> void track(TreeItem<T> root, KeyExtractor<T> key) {
        Objects.requireNonNull(key, "key");
        follow(root, key);
    }

    private <T> void follow(TreeItem<T> item, KeyExtractor<T> key) {
        T value = item.getValue();
        if (value != null) {
            Object k = key.key(value);
            if (keys.contains(k) && !item.isLeaf()) {
                item.setExpanded(true);
            } else if (item.isExpanded()) {
                keys.add(k);
            }
            item.expandedProperty().addListener((obs, was, open) -> {
                if (open) {
                    keys.add(k);
                } else {
                    keys.remove(k);
                }
            });
        }
        for (TreeItem<T> child : item.getChildren()) {
            follow(child, key);
        }
        item.getChildren().addListener((ListChangeListener<TreeItem<T>>) c -> {
            while (c.next()) {
                for (TreeItem<T> added : c.getAddedSubList()) {
                    follow(added, key);
                }
            }
        });
    }
}
