// Made from shadcn's badge.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.badge;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import org.jspecify.annotations.NullMarked;

/// shadcn's `Badge` (`data-slot="badge"`, badge.tsx), as a JavaFX Label.
/// The look is in `components.css` (style class `mfx-badge`).
///
/// Left out (JavaFX has no equivalent): `has-data-[icon=inline-end]:pr-1.5` `has-data-[icon=inline-start]:pl-1.5` `[&>svg]:pointer-events-none` `[&>svg]:size-3!` `[a]:hover:bg-primary/80` `[a]:hover:bg-secondary/80` `[a]:hover:bg-destructive/20` `[a]:hover:bg-muted` `[a]:hover:text-muted-foreground`
@NullMarked
public class BadgeMfx extends Label {

    /// The style class every BadgeMfx has.
    public static final String STYLE_CLASS = "mfx-badge";

    /// shadcn's `variant`: `default`, `secondary`, `destructive`, `outline`, `ghost`, `link`.
    public enum Variant {
        DEFAULT("mfx-badge-variant-default"),
        SECONDARY("mfx-badge-variant-secondary"),
        DESTRUCTIVE("mfx-badge-variant-destructive"),
        OUTLINE("mfx-badge-variant-outline"),
        GHOST("mfx-badge-variant-ghost"),
        LINK("mfx-badge-variant-link");

        private final String styleClass;

        Variant(String styleClass) {
            this.styleClass = styleClass;
        }

        /// The style class this value puts on the Badge.
        public String styleClass() {
            return styleClass;
        }
    }

    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", Variant.DEFAULT);

    public BadgeMfx variant(Variant value) {
        variant.set(value);
        return this;
    }

    public ObjectProperty<Variant> variantProperty() {
        return variant;
    }

    public Variant getVariant() {
        return variant.get();
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

    public BadgeMfx() {
        this("");
    }

    public BadgeMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
        getStyleClass().add(variant.get().styleClass());
        variant.addListener((obs, old, now) -> {
            getStyleClass().remove(old.styleClass());
            getStyleClass().add(now.styleClass());
        });
        Tw.apply(this, "w-fit shrink-0 overflow-hidden");
    }
}
