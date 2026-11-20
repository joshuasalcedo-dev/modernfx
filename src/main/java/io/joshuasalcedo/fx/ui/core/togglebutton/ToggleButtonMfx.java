package io.joshuasalcedo.fx.ui.core.togglebutton;

import io.joshuasalcedo.fx.ui.core.button.ButtonSize;
import io.joshuasalcedo.fx.ui.core.button.ButtonVariant;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ToggleButton;

/// A toggle button with the same variants and sizes as ButtonMfx. When selected it shows as `:selected`.
public class ToggleButtonMfx extends ToggleButton {

    public static final String STYLE_CLASS = "mfx-toggle-button";

    private final ObjectProperty<ButtonVariant> variant =
            new SimpleObjectProperty<>(this, "variant", ButtonVariant.GHOST);
    private final ObjectProperty<ButtonSize> size = new SimpleObjectProperty<>(this, "size", ButtonSize.MD);

    public ToggleButtonMfx() {
        this("");
    }

    public ToggleButtonMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
        // TODO: swap the variant and size style classes when they change
    }

    public final ObjectProperty<ButtonVariant> variantProperty() { return variant; }
    public final ButtonVariant getVariant() { return variant.get(); }
    public final void setVariant(ButtonVariant value) { variant.set(value); }

    public final ObjectProperty<ButtonSize> sizeProperty() { return size; }
    public final ButtonSize getSize() { return size.get(); }
    public final void setSize(ButtonSize value) { size.set(value); }
}
