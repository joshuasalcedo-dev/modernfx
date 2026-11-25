package io.joshuasalcedo.fx.ui.core.togglebutton;

import io.joshuasalcedo.fx.ui.core.button.ButtonSize;
import io.joshuasalcedo.fx.ui.core.button.ButtonVariant;
import io.joshuasalcedo.fx.ui.core.internal.StyleClasses;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A toggle button with the same variants and sizes as ButtonMfx (shadcn's Toggle). When selected it shows as
/// `:selected`: the accent background.
///
/// ```java
/// var bold = new ToggleButtonMfx("Bold");
/// bold.selectedProperty().subscribe(on -> editor.setBold(on));
/// ```
@NullMarked
public class ToggleButtonMfx extends ToggleButton {

    public static final String STYLE_CLASS = "mfx-toggle-button";

    private final ObjectProperty<ButtonVariant> variant =
            new SimpleObjectProperty<>(this, "variant", ButtonVariant.GHOST);
    private final ObjectProperty<ButtonSize> size = new SimpleObjectProperty<>(this, "size", ButtonSize.MD);

    public ToggleButtonMfx() {
        this("");
    }

    public ToggleButtonMfx(String text) {
        this(text, null);
    }

    public ToggleButtonMfx(String text, @Nullable Node graphic) {
        super(text, graphic);
        getStyleClass().remove("toggle-button");
        getStyleClass().addAll("mfx-button", STYLE_CLASS);
        StyleClasses.bind(this, variant, ButtonVariant::styleClass);
        StyleClasses.bind(this, size, ButtonSize::styleClass);
    }

    /// Sets the variant and returns this button.
    public ToggleButtonMfx variant(ButtonVariant value) {
        setVariant(value);
        return this;
    }

    /// Sets the size and returns this button.
    public ToggleButtonMfx size(ButtonSize value) {
        setSize(value);
        return this;
    }

    public final ObjectProperty<ButtonVariant> variantProperty() { return variant; }
    public final ButtonVariant getVariant() { return variant.get(); }
    public final void setVariant(ButtonVariant value) { variant.set(value); }

    public final ObjectProperty<ButtonSize> sizeProperty() { return size; }
    public final ButtonSize getSize() { return size.get(); }
    public final void setSize(ButtonSize value) { size.set(value); }
}
