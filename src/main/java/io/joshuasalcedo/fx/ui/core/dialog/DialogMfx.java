package io.joshuasalcedo.fx.ui.core.dialog;

import io.joshuasalcedo.fx.ui.core.button.ButtonMfx;
import io.joshuasalcedo.fx.ui.core.modal.ModalMfx;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// shadcn's Dialog: a box over the screen with a title, a description, content and buttons, on a dimmed
/// background (AtlantaFX's ModalPane). Clicking outside, Escape or the X closes it.
///
/// ```java
/// var dialog = new DialogMfx("Edit profile", "Make changes to your profile here.")
///         .content(nameField)
///         .footer(new ButtonMfx("Save changes"));
/// dialog.show(anyNodeInTheScene);
/// ```
///
/// It's an ordinary node too: put it in a layout to show it inline.
@NullMarked
public class DialogMfx extends StackPane {

    /// The style class every DialogMfx has.
    public static final String STYLE_CLASS = "mfx-dialog";

    private final Label title = new Label();
    private final Label description = new Label();
    private final VBox content = new VBox();
    private final HBox footer = new HBox();
    private final ButtonMfx close = new ButtonMfx("", new FontIcon(Feather.X));
    private boolean persistent;

    public DialogMfx(String title, String description) {
        getStyleClass().add(STYLE_CLASS);
        this.title.setText(title);
        this.title.getStyleClass().add("mfx-dialog-title");
        this.description.setText(description);
        this.description.getStyleClass().add("mfx-dialog-description");
        this.description.setWrapText(true);
        this.description.managedProperty().bind(this.description.textProperty().isNotEmpty());
        this.description.visibleProperty().bind(this.description.textProperty().isNotEmpty());
        content.getStyleClass().add("mfx-dialog-content");
        footer.getStyleClass().add("mfx-dialog-footer");
        footer.managedProperty().bind(footer.visibleProperty());
        footer.setVisible(false);
        content.managedProperty().bind(content.visibleProperty());
        content.setVisible(false);

        VBox header = new VBox(this.title, this.description);
        header.getStyleClass().add("mfx-dialog-header");
        VBox box = new VBox(header, content, footer);
        box.getStyleClass().add("mfx-dialog-box");

        close.variant(ButtonMfx.Variant.GHOST).size(ButtonMfx.Size.ICON_SM);
        close.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        close.getStyleClass().add("mfx-dialog-close");
        close.setOnAction(e -> close());
        StackPane.setAlignment(close, Pos.TOP_RIGHT);
        getChildren().addAll(box, close);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
    }

    public DialogMfx content(Node... nodes) {
        content.getChildren().addAll(nodes);
        content.setVisible(true);
        return this;
    }

    /// The buttons at the bottom, right-aligned (shadcn puts the main action last).
    public DialogMfx footer(Node... nodes) {
        footer.getChildren().addAll(nodes);
        footer.setVisible(true);
        return this;
    }

    /// Hides the X in the corner.
    public DialogMfx closeButton(boolean show) {
        close.setVisible(show);
        close.setManaged(show);
        return this;
    }

    /// A persistent dialog closes only through its buttons (no click outside, no Escape).
    public DialogMfx persistent(boolean value) {
        persistent = value;
        return this;
    }

    /// Shows the dialog over the scene that `owner` is in.
    public void show(Node owner) {
        ModalMfx.open(owner, this, Pos.CENTER, null, persistent);
    }

    /// Closes it (when shown with [#show]).
    public void close() {
        if (getScene() != null) {
            ModalMfx.close(this);
        }
    }

    public Label getTitle() {
        return title;
    }

    public Label getDescription() {
        return description;
    }

    public VBox getContent() {
        return content;
    }

    public HBox getFooter() {
        return footer;
    }
}
