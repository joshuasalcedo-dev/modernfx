package io.joshuasalcedo.fx.ui.core.treetable;

import io.joshuasalcedo.fx.ui.core.tree.LazyTreeLoader;
import io.joshuasalcedo.fx.ui.core.tree.TreeNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Rows that open to show their children, in columns (JavaFX's TreeTableView), with TableMfx's look: a line under
/// each row, a muted header. The rows are TreeNodes (lazy ones load with the LazyTreeLoader); the columns are
/// TreeTableColumnMfx, so the table renderers work here too. The first column has the open/close arrows.
///
/// ```java
/// var files = new TreeTableMfx<File>();
/// files.columns(
///         TreeTableColumnMfx.of("name", "Name", File::getName),
///         TreeTableColumnMfx.of("size", "Size", File::length).renderer(NumberCellRenderer.decimals(0)));
/// files.setRootNode(TreeNode.of(home, children));
/// ```
@NullMarked
public class TreeTableMfx<T> extends TreeTableView<T> {

    public static final String STYLE_CLASS = "mfx-tree-table";

    private final ObjectProperty<@Nullable LazyTreeLoader<T>> loader = new SimpleObjectProperty<>(this, "loader");
    private @Nullable TreeNode<T> rootNode;

    public TreeTableMfx() {
        getStyleClass().add(STYLE_CLASS);
        setShowRoot(false);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        setFixedCellSize(40);
    }

    /// Adds these columns.
    @SafeVarargs
    public final TreeTableMfx<T> columns(TreeTableColumn<T, ?>... columns) {
        getColumns().addAll(columns);
        return this;
    }

    /// Shows this tree; the root's children are the top rows.
    public void setRootNode(@Nullable TreeNode<T> node) {
        rootNode = node;
        if (node == null) {
            setRoot(null);
            return;
        }
        node.setExpanded(true);
        setRoot(node.toTreeItem(loader.get()));
    }

    public @Nullable TreeNode<T> getRootNode() {
        return rootNode;
    }

    /// Loads the children of lazy nodes. Set it before [#setRootNode].
    public final ObjectProperty<@Nullable LazyTreeLoader<T>> loaderProperty() { return loader; }
    public final void setLoader(@Nullable LazyTreeLoader<T> value) { loader.set(value); }
}
