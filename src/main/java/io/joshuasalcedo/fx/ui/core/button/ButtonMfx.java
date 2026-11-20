package io.joshuasalcedo.fx.ui.core.button;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import org.jspecify.annotations.Nullable;

/// The main button: a variant, a size, an optional icon and a loading state.
///
/// ```java
/// ButtonMfx save = new ButtonMfx("Save", ButtonVariant.PRIMARY);
/// save.setIcon(new FontIcon(Feather.SAVE));
/// save.setLoading(true);   // shows a spinner, ignores clicks
/// ```
public class ButtonMfx extends Button {

    /// The style class every ButtonMfx has.
    public static final String STYLE_CLASS = "mfx-button";

    private final ObjectProperty<ButtonVariant> variant =
            new SimpleObjectProperty<>(this, "variant", ButtonVariant.SECONDARY);
    private final ObjectProperty<ButtonSize> size = new SimpleObjectProperty<>(this, "size", ButtonSize.MD);
    private final ObjectProperty<@Nullable Node> icon = new SimpleObjectProperty<>(this, "icon");
    private final ObjectProperty<IconPosition> iconPosition =
            new SimpleObjectProperty<>(this, "iconPosition", IconPosition.LEADING);
    private final BooleanProperty loading = new SimpleBooleanProperty(this, "loading");

    public ButtonMfx() {
        this("");
    }

    public ButtonMfx(String text) {
        this(text, ButtonVariant.SECONDARY);
    }

    public ButtonMfx(String text, ButtonVariant variant) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
        setVariant(variant);
        // TODO: swap the variant and size style classes when they change
        // TODO: put the icon in the graphic and apply iconPosition
        // TODO: drive a ButtonLoadingState from the loading property
        // TODO: set ButtonPseudoClasses.FOCUS_VISIBLE only on keyboard focus
    }

    public final ObjectProperty<ButtonVariant> variantProperty() { return variant; }
    public final ButtonVariant getVariant() { return variant.get(); }
    public final void setVariant(ButtonVariant value) { variant.set(value); }

    public final ObjectProperty<ButtonSize> sizeProperty() { return size; }
    public final ButtonSize getSize() { return size.get(); }
    public final void setSize(ButtonSize value) { size.set(value); }

    public final ObjectProperty<@Nullable Node> iconProperty() { return icon; }
    public final @Nullable Node getIcon() { return icon.get(); }
    public final void setIcon(@Nullable Node value) { icon.set(value); }

    public final ObjectProperty<IconPosition> iconPositionProperty() { return iconPosition; }
    public final IconPosition getIconPosition() { return iconPosition.get(); }
    public final void setIconPosition(IconPosition value) { iconPosition.set(value); }

    /// While true the button shows a spinner instead of its icon and ignores clicks.
    public final BooleanProperty loadingProperty() { return loading; }
    public final boolean isLoading() { return loading.get(); }
    public final void setLoading(boolean value) { loading.set(value); }
}
