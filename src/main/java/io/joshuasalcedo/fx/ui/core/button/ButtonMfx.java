// Made from shadcn's button.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.button;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Button;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// shadcn's `Button` (`data-slot="button"`, button.tsx), as a JavaFX Button.
/// The look is in `components.css` (style class `mfx-button`).
///
/// Left out (JavaFX has no equivalent): `active:not-aria-[haspopup]:translate-y-px` `[&_svg]:pointer-events-none` `[&_svg]:shrink-0` `[&_svg:not([class*='size-'])]:size-4` `hover:bg-[color-mix(in_oklch,var(--secondary),var(--foreground)_5%)]` `in-data-[slot=button-group]:rounded-md` `has-data-[icon=inline-end]:pr-2` `has-data-[icon=inline-start]:pl-2` `has-data-[icon=inline-end]:pr-1.5` `has-data-[icon=inline-start]:pl-1.5` `[&_svg:not([class*='size-'])]:size-3`
@NullMarked
public class ButtonMfx extends Button {

    /// The style class every ButtonMfx has.
    public static final String STYLE_CLASS = "mfx-button";

    /// shadcn's `variant`: `default`, `outline`, `secondary`, `ghost`, `destructive`, `link`.
    public enum Variant {
        DEFAULT("mfx-button-variant-default"),
        OUTLINE("mfx-button-variant-outline"),
        SECONDARY("mfx-button-variant-secondary"),
        GHOST("mfx-button-variant-ghost"),
        DESTRUCTIVE("mfx-button-variant-destructive"),
        LINK("mfx-button-variant-link");

        private final String styleClass;

        Variant(String styleClass) {
            this.styleClass = styleClass;
        }

        /// The style class this value puts on the Button.
        public String styleClass() {
            return styleClass;
        }
    }

    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", Variant.DEFAULT);

    public ButtonMfx variant(Variant value) {
        variant.set(value);
        return this;
    }

    public ObjectProperty<Variant> variantProperty() {
        return variant;
    }

    public Variant getVariant() {
        return variant.get();
    }

    /// shadcn's `size`: `default`, `xs`, `sm`, `lg`, `icon`, `icon-xs`, `icon-sm`, `icon-lg`.
    public enum Size {
        DEFAULT("mfx-button-size-default"),
        XS("mfx-button-size-xs"),
        SM("mfx-button-size-sm"),
        LG("mfx-button-size-lg"),
        ICON("mfx-button-size-icon"),
        ICON_XS("mfx-button-size-icon-xs"),
        ICON_SM("mfx-button-size-icon-sm"),
        ICON_LG("mfx-button-size-icon-lg");

        private final String styleClass;

        Size(String styleClass) {
            this.styleClass = styleClass;
        }

        /// The style class this value puts on the Button.
        public String styleClass() {
            return styleClass;
        }
    }

    private final ObjectProperty<Size> sizeVariant = new SimpleObjectProperty<>(this, "size", Size.DEFAULT);

    public ButtonMfx size(Size value) {
        sizeVariant.set(value);
        return this;
    }

    public ObjectProperty<Size> sizeProperty() {
        return sizeVariant;
    }

    public Size getSize() {
        return sizeVariant.get();
    }

    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");

    private final BooleanProperty invalid = new SimpleBooleanProperty(this, "invalid") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(INVALID, get());
        }
    };

    /// shadcn's `aria-invalid`: the red border and ring of a field in error.
    public BooleanProperty invalidProperty() {
        return invalid;
    }

    public void setInvalid(boolean value) {
        invalid.set(value);
    }

    public ButtonMfx() {
        this("");
    }

    public ButtonMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
        getStyleClass().remove("button");
        getStyleClass().add(variant.get().styleClass());
        variant.addListener((obs, old, now) -> {
            getStyleClass().remove(old.styleClass());
            getStyleClass().add(now.styleClass());
        });
        getStyleClass().add(sizeVariant.get().styleClass());
        sizeVariant.addListener((obs, old, now) -> {
            getStyleClass().remove(old.styleClass());
            getStyleClass().add(now.styleClass());
        });
        Tw.apply(this, "shrink-0");
    }

    public ButtonMfx(String text, @Nullable Node graphic) {
        super(text, graphic);
        getStyleClass().add(STYLE_CLASS);
        getStyleClass().remove("button");
        getStyleClass().add(variant.get().styleClass());
        variant.addListener((obs, old, now) -> {
            getStyleClass().remove(old.styleClass());
            getStyleClass().add(now.styleClass());
        });
        getStyleClass().add(sizeVariant.get().styleClass());
        sizeVariant.addListener((obs, old, now) -> {
            getStyleClass().remove(old.styleClass());
            getStyleClass().add(now.styleClass());
        });
        Tw.apply(this, "shrink-0");
    }
}
