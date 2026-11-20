package io.joshuasalcedo.fx.ui.core.splitbutton;

import io.joshuasalcedo.fx.ui.core.button.ButtonSize;
import io.joshuasalcedo.fx.ui.core.button.ButtonVariant;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import org.jspecify.annotations.Nullable;

/// A main action with a dropdown arrow beside it for the other actions. Clicking the text runs `onAction`; clicking
/// the arrow opens the menu.
///
/// ```java
/// var send = new SplitButtonMfx("Send");
/// send.setOnAction(e -> send());
/// send.getItems().addAll(new MenuItem("Send later"), new MenuItem("Send to all"));
/// ```
public class SplitButtonMfx extends Control {

    public static final String STYLE_CLASS = "mfx-split-button";

    private final StringProperty text = new SimpleStringProperty(this, "text", "");
    private final ObjectProperty<ButtonVariant> variant =
            new SimpleObjectProperty<>(this, "variant", ButtonVariant.PRIMARY);
    private final ObjectProperty<ButtonSize> size = new SimpleObjectProperty<>(this, "size", ButtonSize.MD);
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction");
    private final ObservableList<MenuItem> items = FXCollections.observableArrayList();

    public SplitButtonMfx() {
        this("");
    }

    public SplitButtonMfx(String text) {
        getStyleClass().add(STYLE_CLASS);
        setText(text);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SplitButtonSkinMfx(this);
    }

    public final StringProperty textProperty() { return text; }
    public final String getText() { return text.get(); }
    public final void setText(String value) { text.set(value); }

    public final ObjectProperty<ButtonVariant> variantProperty() { return variant; }
    public final ButtonVariant getVariant() { return variant.get(); }
    public final void setVariant(ButtonVariant value) { variant.set(value); }

    public final ObjectProperty<ButtonSize> sizeProperty() { return size; }
    public final ButtonSize getSize() { return size.get(); }
    public final void setSize(ButtonSize value) { size.set(value); }

    /// Runs when the main part is clicked.
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() { return onAction; }
    public final @Nullable EventHandler<ActionEvent> getOnAction() { return onAction.get(); }
    public final void setOnAction(@Nullable EventHandler<ActionEvent> value) { onAction.set(value); }

    /// The menu the arrow opens.
    public final ObservableList<MenuItem> getItems() {
        return items;
    }

    /// Runs the main action, as a click on the main part does.
    public void fire() {
        // TODO: unless disabled, fireEvent(new ActionEvent()) and route it to onAction
    }

    /// Opens the menu under the arrow.
    public void show() {
        if (getSkin() instanceof SplitButtonSkinMfx skin) {
            skin.showMenu();
        }
    }
}
