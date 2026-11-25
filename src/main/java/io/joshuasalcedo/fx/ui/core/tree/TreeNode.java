package io.joshuasalcedo.fx.ui.core.tree;

import java.util.List;
import java.util.Objects;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A node of a tree: its value, its children, whether it is a leaf (no children, ever) and whether it is open. A
/// `lazy` node has children nobody has loaded yet: a LazyTreeLoader loads them the first time it opens.
///
/// ```java
/// var root = TreeNode.of("Projects",
///         TreeNode.of("ModernFX", TreeNode.leaf("README.md"), TreeNode.leaf("pom.xml")),
///         TreeNode.lazy("Archive"));
/// ```
@NullMarked
public class TreeNode<T> {

    private final T value;
    private final ObservableList<TreeNode<T>> children = FXCollections.observableArrayList();
    private final BooleanProperty leaf = new SimpleBooleanProperty(this, "leaf");
    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded");
    private final ReadOnlyBooleanWrapper loaded = new ReadOnlyBooleanWrapper(this, "loaded");

    private TreeNode(T value, boolean leaf, boolean loaded) {
        this.value = Objects.requireNonNull(value, "value");
        this.leaf.set(leaf);
        this.loaded.set(loaded);
    }

    /// A node with these children (a branch, even with none).
    @SafeVarargs
    public static <T> TreeNode<T> of(T value, TreeNode<T>... children) {
        TreeNode<T> node = new TreeNode<>(value, false, true);
        node.children.addAll(children);
        return node;
    }

    /// A node with these children.
    public static <T> TreeNode<T> of(T value, List<TreeNode<T>> children) {
        TreeNode<T> node = new TreeNode<>(value, false, true);
        node.children.addAll(children);
        return node;
    }

    /// A node that never has children.
    public static <T> TreeNode<T> leaf(T value) {
        return new TreeNode<>(value, true, true);
    }

    /// A node whose children a LazyTreeLoader loads when it's first opened.
    public static <T> TreeNode<T> lazy(T value) {
        return new TreeNode<>(value, false, false);
    }

    public T getValue() {
        return value;
    }

    /// The children. Adding to them makes the node loaded.
    public ObservableList<TreeNode<T>> getChildren() {
        return children;
    }

    public BooleanProperty leafProperty() { return leaf; }
    public boolean isLeaf() { return leaf.get(); }

    /// Whether the node is open. The tree keeps it in step both ways.
    public BooleanProperty expandedProperty() { return expanded; }
    public boolean isExpanded() { return expanded.get(); }
    public void setExpanded(boolean value) { expanded.set(value); }

    /// True once the children are known (always, except for a lazy node not loaded yet).
    public ReadOnlyBooleanProperty loadedProperty() { return loaded.getReadOnlyProperty(); }
    public boolean isLoaded() { return loaded.get(); }

    /// Sets the children (as a loader returns them) and marks the node loaded.
    public void setChildren(List<TreeNode<T>> value) {
        children.setAll(value);
        loaded.set(true);
    }

    /// Forgets the children of a lazy node, so they load again the next time it opens.
    public void unload() {
        children.clear();
        loaded.set(false);
        expanded.set(false);
    }

    /// A TreeItem showing this node and, as it opens, its children: for a TreeView or TreeTableView. A lazy node
    /// shows a child with a null value ("Loading…") while `loader` runs.
    public TreeItem<T> toTreeItem(@Nullable LazyTreeLoader<T> loader) {
        return new NodeItem<>(this, loader);
    }

    /// The TreeItem of a TreeNode.
    private static final class NodeItem<T> extends TreeItem<T> {

        private final TreeNode<T> node;
        private final @Nullable LazyTreeLoader<T> loader;
        private boolean loading;

        NodeItem(TreeNode<T> node, @Nullable LazyTreeLoader<T> loader) {
            super(node.getValue());
            this.node = node;
            this.loader = loader;
            setExpanded(node.isExpanded());
            expandedProperty().bindBidirectional(node.expandedProperty());
            node.getChildren().addListener((ListChangeListener<TreeNode<T>>) c -> {
                if (!node.isLoaded() && !node.getChildren().isEmpty()) {
                    node.loaded.set(true);
                }
                mapChildren();
            });
            if (node.isLoaded()) {
                mapChildren();
            }
            expandedProperty().addListener((obs, was, open) -> {
                if (open) {
                    loadIfNeeded();
                }
            });
            if (isExpanded()) {
                loadIfNeeded();
            }
        }

        @Override
        public boolean isLeaf() {
            return node.isLeaf() || (node.isLoaded() && node.getChildren().isEmpty());
        }

        private void mapChildren() {
            getChildren().setAll(node.getChildren().stream().map(c -> (TreeItem<T>) new NodeItem<>(c, loader)).toList());
        }

        private void loadIfNeeded() {
            if (node.isLoaded() || loading || loader == null) {
                return;
            }
            loading = true;
            getChildren().setAll(new TreeItem<>());
            loader.children(node.getValue()).whenComplete((list, err) -> fx(() -> {
                loading = false;
                if (err != null || list == null) {
                    getChildren().clear();
                    setExpanded(false);
                } else {
                    // the children listener maps them
                    node.setChildren(list);
                }
            }));
        }

        private static void fx(Runnable run) {
            if (Platform.isFxApplicationThread()) {
                run.run();
            } else {
                Platform.runLater(run);
            }
        }
    }
}
