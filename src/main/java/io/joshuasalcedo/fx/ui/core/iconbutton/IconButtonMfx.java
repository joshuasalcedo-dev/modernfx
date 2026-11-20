package io.joshuasalcedo.fx.ui.core.iconbutton;

import io.joshuasalcedo.fx.ui.core.button.ButtonSize;
import io.joshuasalcedo.fx.ui.core.button.ButtonVariant;
import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/// A button that shows only an icon. It has no text, so the tooltip is required: it says what the button does, and
/// screen readers read it.
///
/// ```java
/// new IconButtonMfx(new FontIcon(Feather.TRASH), "Delete");
/// ```
public class IconButtonMfx extends Button {

    public static final String STYLE_CLASS = "mfx-icon-button";

    private final ObjectProperty<ButtonVariant> variant =
            new SimpleObjectProperty<>(this, "variant", ButtonVariant.GHOST);
    private final ObjectProperty<ButtonSize> size = new SimpleObjectProperty<>(this, "size", ButtonSize.MD);
    private final ObjectProperty<IconButtonShape> buttonShape =
            new SimpleObjectProperty<>(this, "buttonShape", IconButtonShape.SQUARE);

    public IconButtonMfx(Node icon, String tooltip) {
        super(null, Objects.requireNonNull(icon, "icon"));
        Objects.requireNonNull(tooltip, "tooltip");
        getStyleClass().add(STYLE_CLASS);
        setTooltip(new Tooltip(tooltip));
        setAccessibleText(tooltip);
        // TODO: keep width == height for the size; swap the variant, size and buttonShape style classes
    }

    public final ObjectProperty<ButtonVariant> variantProperty() { return variant; }
    public final ButtonVariant getVariant() { return variant.get(); }
    public final void setVariant(ButtonVariant value) { variant.set(value); }

    public final ObjectProperty<ButtonSize> sizeProperty() { return size; }
    public final ButtonSize getSize() { return size.get(); }
    public final void setSize(ButtonSize value) { size.set(value); }

    public final ObjectProperty<IconButtonShape> buttonShapeProperty() { return buttonShape; }
    public final IconButtonShape getButtonShape() { return buttonShape.get(); }
    public final void setButtonShape(IconButtonShape value) { buttonShape.set(value); }
}
