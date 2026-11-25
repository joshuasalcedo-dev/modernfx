package io.joshuasalcedo.fx.ui.core.tree;

import io.joshuasalcedo.fx.ui.core.data.KeyExtractor;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A tree (JavaFX's TreeView) of TreeNodes, with indent guides, icons, children loaded on first open
/// (LazyTreeLoader), and the open nodes remembered by key (TreeExpansionState). Selection and the keyboard are
/// TreeView's: the arrows move, Right opens, Left closes. The root isn't shown: its children are the top level.
///
/// ```java
/// var files = new TreeMfx<File>();
/// files.setText(File::getName);
/// files.setIcon(f -> new FontIcon(f.isDirectory() ? Feather.FOLDER : Feather.FILE));
/// files.setLoader(LazyTreeLoader.blocking(dir -> list(dir)));
/// files.setRootNode(TreeNode.lazy(home));
/// ```
@NullMarked
public class TreeMfx<T> extends TreeView<T> {

    public static final String STYLE_CLASS = "mfx-tree";

    private final ObjectProperty<Function<T, String>> text = new SimpleObjectProperty<>(this, "text", String::valueOf);
    private final ObjectProperty<Function<T, @Nullable Node>> icon = new SimpleObjectProperty<>(this, "icon", v -> null);
    private final ObjectProperty<@Nullable LazyTreeLoader<T>> loader = new SimpleObjectProperty<>(this, "loader");
    private final ObjectProperty<KeyExtractor<T>> keyExtractor =
            new SimpleObjectProperty<>(this, "keyExtractor", KeyExtractor.identity());
    private final ObjectProperty<@Nullable TreeExpansionState> expansionState =
            new SimpleObjectProperty<>(this, "expansionState");
    private @Nullable TreeNode<T> rootNode;

    public TreeMfx() {
        getStyleClass().add(STYLE_CLASS);
        setShowRoot(false);
        updateCells();
        text.addListener(o -> updateCells());
        icon.addListener(o -> updateCells());
        expansionState.addListener(o -> track());
    }

    public TreeMfx(TreeNode<T> root) {
        this();
        setRootNode(root);
    }

    /// Shows this tree. Lazy nodes load with the loader set at the time.
    public void setRootNode(@Nullable TreeNode<T> node) {
        rootNode = node;
        if (node == null) {
            setRoot(null);
            return;
        }
        node.setExpanded(true);
        TreeItem<T> item = node.toTreeItem(loader.get());
        setRoot(item);
        track();
    }

    public @Nullable TreeNode<T> getRootNode() {
        return rootNode;
    }

    /// The label of a node.
    public final ObjectProperty<Function<T, String>> textProperty() { return text; }
    public final void setText(Function<T, String> value) { text.set(Objects.requireNonNull(value)); }

    /// The icon of a node, or null for none.
    public final ObjectProperty<Function<T, @Nullable Node>> iconProperty() { return icon; }
    public final void setIcon(Function<T, @Nullable Node> value) { icon.set(Objects.requireNonNull(value)); }

    /// Loads the children of lazy nodes. Set it before [#setRootNode].
    public final ObjectProperty<@Nullable LazyTreeLoader<T>> loaderProperty() { return loader; }
    public final void setLoader(@Nullable LazyTreeLoader<T> value) { loader.set(value); }

    /// A node's key, for the expansion state.
    public final ObjectProperty<KeyExtractor<T>> keyExtractorProperty() { return keyExtractor; }
    public final void setKeyExtractor(KeyExtractor<T> value) { keyExtractor.set(Objects.requireNonNull(value)); }

    /// Where the open nodes are remembered.
    public final ObjectProperty<@Nullable TreeExpansionState> expansionStateProperty() { return expansionState; }
    public final void setExpansionState(@Nullable TreeExpansionState value) { expansionState.set(value); }

    private void updateCells() {
        Function<T, String> t = text.get();
        Function<T, @Nullable Node> i = icon.get();
        setCellFactory(tree -> new TreeCellMfx<>(t, i));
    }

    private void track() {
        TreeExpansionState state = expansionState.get();
        TreeItem<T> root = getRoot();
        if (state != null && root != null) {
            state.track(root, keyExtractor.get());
        }
    }
}
