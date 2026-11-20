package io.joshuasalcedo.fx.ui.core.drawer;

import io.joshuasalcedo.fx.ui.core.button.ButtonMfx;
import io.joshuasalcedo.fx.ui.core.modal.ModalMfx;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// shadcn's Sheet and Drawer: a panel that slides in from an edge of the window, with a title, a description,
/// content and buttons at the bottom. (Named Drawer here: SheetMfx is the spreadsheet.)
///
/// ```java
/// new DrawerMfx(Side.RIGHT, "Edit profile", "Make changes to your profile here.")
///         .content(form)
///         .footer(new ButtonMfx("Save changes"))
///         .show(anyNodeInTheScene);
/// ```
@NullMarked
public class DrawerMfx extends VBox {

    /// The style class every DrawerMfx has.
    public static final String STYLE_CLASS = "mfx-drawer";

    private final Side side;
    private final Label title = new Label();
    private final Label description = new Label();
    private final VBox content = new VBox();
    private final HBox footer = new HBox();

    public DrawerMfx(Side side, String title, String description) {
        this.side = side;
        getStyleClass().addAll(STYLE_CLASS, "mfx-drawer-" + side.name().toLowerCase());
        this.title.setText(title);
        this.title.getStyleClass().add("mfx-drawer-title");
        this.description.setText(description);
        this.description.getStyleClass().add("mfx-drawer-description");
        this.description.setWrapText(true);
        content.getStyleClass().add("mfx-drawer-content");
        footer.getStyleClass().add("mfx-drawer-footer");
        footer.managedProperty().bind(footer.visibleProperty());
        footer.setVisible(false);

        ButtonMfx close = new ButtonMfx("", new FontIcon(Feather.X)).variant(ButtonMfx.Variant.GHOST)
                .size(ButtonMfx.Size.ICON_SM);
        close.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        close.setOnAction(e -> close());
        VBox text = new VBox(this.title, this.description);
        text.getStyleClass().add("mfx-drawer-text");
        HBox.setHgrow(text, Priority.ALWAYS);
        HBox header = new HBox(text, close);
        header.getStyleClass().add("mfx-drawer-header");
        VBox.setVgrow(content, Priority.ALWAYS);
        getChildren().addAll(header, content, footer);

        boolean across = side == Side.LEFT || side == Side.RIGHT;
        if (across) {
            setPrefWidth(384);
            setMaxWidth(USE_PREF_SIZE);
            setMaxHeight(Double.MAX_VALUE);
        } else {
            setMaxWidth(Double.MAX_VALUE);
            setMaxHeight(USE_PREF_SIZE);
        }
    }

    public DrawerMfx content(Node... nodes) {
        content.getChildren().addAll(nodes);
        return this;
    }

    public DrawerMfx footer(Node... nodes) {
        footer.getChildren().addAll(nodes);
        footer.setVisible(true);
        return this;
    }

    /// Slides the panel in over the scene that `owner` is in.
    public void show(Node owner) {
        Pos at = switch (side) {
            case LEFT -> Pos.CENTER_LEFT;
            case RIGHT -> Pos.CENTER_RIGHT;
            case TOP -> Pos.TOP_CENTER;
            case BOTTOM -> Pos.BOTTOM_CENTER;
        };
        ModalMfx.open(owner, this, at, side, false);
    }

    public void close() {
        if (getScene() != null) {
            ModalMfx.close(this);
        }
    }

    public Side getSide() {
        return side;
    }

    public VBox getContent() {
        return content;
    }

    public HBox getFooter() {
        return footer;
    }

    static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }
}
