package io.joshuasalcedo.fx.ui.core.iconbutton;

import io.joshuasalcedo.fx.ui.core.button.ButtonSize;
import io.joshuasalcedo.fx.ui.core.button.ButtonVariant;
import io.joshuasalcedo.fx.ui.core.internal.ButtonLoadingState;
import io.joshuasalcedo.fx.ui.core.internal.StyleClasses;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.NullMarked;

/// A button that shows only an icon. It has no text, so the tooltip is required: it says what the button does, and
/// screen readers read it. It looks like ButtonMfx (same variants), square or round, as wide as it is high.
///
/// ```java
/// new IconButtonMfx(new FontIcon(Feather.TRASH), "Delete");
/// ```
@NullMarked
public class IconButtonMfx extends Button {

    public static final String STYLE_CLASS = "mfx-icon-button";

    private final ObjectProperty<ButtonVariant> variant =
            new SimpleObjectProperty<>(this, "variant", ButtonVariant.GHOST);
    private final ObjectProperty<ButtonSize> size = new SimpleObjectProperty<>(this, "size", ButtonSize.MD);
    private final ObjectProperty<IconButtonShape> buttonShape =
            new SimpleObjectProperty<>(this, "buttonShape", IconButtonShape.SQUARE);
    private final ButtonLoadingState loadingState = new ButtonLoadingState(this);
    private final BooleanProperty loading = new SimpleBooleanProperty(this, "loading") {
        @Override
        protected void invalidated() {
            loadingState.set(get());
        }
    };

    public IconButtonMfx(Node icon, String tooltip) {
        super(null, Objects.requireNonNull(icon, "icon"));
        Objects.requireNonNull(tooltip, "tooltip");
        getStyleClass().remove("button");
        getStyleClass().addAll("mfx-button", STYLE_CLASS);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        setTooltip(new Tooltip(tooltip));
        setAccessibleText(tooltip);
        StyleClasses.bind(this, variant, ButtonVariant::styleClass);
        StyleClasses.bind(this, size, ButtonSize::iconStyleClass);
        StyleClasses.bind(this, buttonShape, IconButtonShape::styleClass);
    }

    /// Sets the variant and returns this button.
    public IconButtonMfx variant(ButtonVariant value) {
        setVariant(value);
        return this;
    }

    /// Sets the size and returns this button.
    public IconButtonMfx size(ButtonSize value) {
        setSize(value);
        return this;
    }

    /// Sets the shape and returns this button.
    public IconButtonMfx shape(IconButtonShape value) {
        setButtonShape(value);
        return this;
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

    /// While true the icon is a spinner and clicks do nothing.
    public final BooleanProperty loadingProperty() { return loading; }
    public final boolean isLoading() { return loading.get(); }
    public final void setLoading(boolean value) { loading.set(value); }
}
