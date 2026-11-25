package io.joshuasalcedo.fx.ui.core.tree;

import io.joshuasalcedo.fx.ui.core.internal.ButtonLoadingState;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// A tree row: an indent guide per level (a thin line), a chevron that turns when the node opens, an optional
/// icon, and the label. The cell draws its own indent and chevron (`-fx-indent: 0`, no disclosure node), so the
/// guides line up. A row with a null value is the "Loading…" row of a lazy node.
@NullMarked
public class TreeCellMfx<T> extends TreeCell<T> {

    public static final String STYLE_CLASS = "mfx-tree-cell";

    /// The width of one level of indent, in pixels.
    public static final double INDENT = 16;

    /// The width TreeCellSkin keeps for a disclosure arrow.
    private static final double SKIN_DISCLOSURE = 18;

    private final Function<T, String> text;
    private final Function<T, @Nullable Node> icon;
    private final HBox row = new HBox();
    private final HBox guides = new HBox();
    private final StackPane chevron = new StackPane();
    private final StackPane iconBox = new StackPane();
    private final Label label = new Label();
    private final FontIcon chevronIcon = new FontIcon(Feather.CHEVRON_RIGHT);
    // the chevron turns as the node opens; held here so the weak listener lives as long as the cell
    private final InvalidationListener expanded = o -> {
        TreeItem<T> item = getTreeItem();
        chevronIcon.setRotate(item != null && item.isExpanded() ? 90 : 0);
    };
    private final WeakInvalidationListener weakExpanded = new WeakInvalidationListener(expanded);

    public TreeCellMfx(Function<T, String> text, Function<T, @Nullable Node> icon) {
        this.text = Objects.requireNonNull(text, "text");
        this.icon = Objects.requireNonNull(icon, "icon");
        getStyleClass().add(STYLE_CLASS);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        // TreeCellSkin keeps 18 px before leaves for the arrow; an empty node as wide puts every row at the same x,
        // and the row moves back over it
        Region disclosure = new Region();
        disclosure.setMinWidth(SKIN_DISCLOSURE);
        disclosure.setPrefWidth(SKIN_DISCLOSURE);
        disclosure.setMaxWidth(SKIN_DISCLOSURE);
        setDisclosureNode(disclosure);
        row.setTranslateX(-SKIN_DISCLOSURE);
        row.getStyleClass().add("mfx-tree-row");
        row.setAlignment(Pos.CENTER_LEFT);
        guides.getStyleClass().add("mfx-tree-guides");
        chevron.getStyleClass().add("mfx-tree-chevron");
        chevron.setMinSize(INDENT, INDENT);
        chevron.setPrefSize(INDENT, INDENT);
        chevron.setMaxSize(INDENT, INDENT);
        iconBox.getStyleClass().add("mfx-tree-icon");
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        label.setMinWidth(0);
        row.getChildren().addAll(guides, chevron, iconBox, label);
        treeItemProperty().addListener((obs, old, now) -> {
            if (old != null) {
                old.expandedProperty().removeListener(weakExpanded);
            }
            if (now != null) {
                now.expandedProperty().addListener(weakExpanded);
            }
        });
        chevron.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            TreeItem<T> item = getTreeItem();
            if (e.getButton() == MouseButton.PRIMARY && item != null && !item.isLeaf()) {
                item.setExpanded(!item.isExpanded());
                e.consume();
            }
        });
    }

    @Override
    protected void updateItem(@Nullable T value, boolean empty) {
        super.updateItem(value, empty);
        setText(null);
        TreeItem<T> item = getTreeItem();
        if (empty || item == null) {
            setGraphic(null);
            return;
        }
        int level = level(item);
        guides.getChildren().clear();
        for (int i = 0; i < level; i++) {
            Region guide = new Region();
            guide.getStyleClass().add("mfx-tree-guide");
            guide.setMinWidth(INDENT);
            guide.setPrefWidth(INDENT);
            guide.setMaxHeight(Double.MAX_VALUE);
            guides.getChildren().add(guide);
        }
        if (value == null) {
            chevron.getChildren().clear();
            iconBox.getChildren().setAll(ButtonLoadingState.spinner());
            label.setText("Loading…");
            label.getStyleClass().add("mfx-tree-loading");
        } else {
            label.getStyleClass().remove("mfx-tree-loading");
            if (item.isLeaf()) {
                chevron.getChildren().clear();
            } else {
                chevronIcon.setRotate(item.isExpanded() ? 90 : 0);
                chevron.getChildren().setAll(chevronIcon);
            }
            Node graphic = icon.apply(value);
            iconBox.getChildren().setAll(graphic == null ? new Node[0] : new Node[] {graphic});
            label.setText(text.apply(value));
        }
        iconBox.setManaged(!iconBox.getChildren().isEmpty());
        iconBox.setVisible(iconBox.isManaged());
        setGraphic(row);
    }

    private int level(TreeItem<T> item) {
        TreeView<T> tree = getTreeView();
        if (tree == null) {
            return 0;
        }
        int level = tree.getTreeItemLevel(item);
        return Math.max(0, tree.isShowRoot() ? level : level - 1);
    }
}
