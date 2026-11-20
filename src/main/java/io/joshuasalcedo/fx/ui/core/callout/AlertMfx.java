// Made from shadcn's alert.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.callout;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;

/// shadcn's `Alert` (`data-slot="alert"`, alert.tsx), as a JavaFX VBox.
/// The look is in `components.css` (style class `mfx-alert`).
///
/// Left out (JavaFX has no equivalent): `has-data-[slot=alert-action]:relative` `has-data-[slot=alert-action]:pr-18` `has-[>svg]:grid-cols-[auto_1fr]` `has-[>svg]:gap-x-2.5` `*:[svg]:row-span-2` `*:[svg]:translate-y-0.5` `*:[svg]:text-current` `*:[svg:not([class*='size-'])]:size-4` `*:data-[slot=alert-description]:text-destructive/90`
@NullMarked
public class AlertMfx extends VBox {

    /// The style class every AlertMfx has.
    public static final String STYLE_CLASS = "mfx-alert";

    /// shadcn's `variant`: `default`, `destructive`.
    public enum Variant {
        DEFAULT("mfx-alert-variant-default"),
        DESTRUCTIVE("mfx-alert-variant-destructive");

        private final String styleClass;

        Variant(String styleClass) {
            this.styleClass = styleClass;
        }

        /// The style class this value puts on the Alert.
        public String styleClass() {
            return styleClass;
        }
    }

    private final ObjectProperty<Variant> variant = new SimpleObjectProperty<>(this, "variant", Variant.DEFAULT);

    public AlertMfx variant(Variant value) {
        variant.set(value);
        return this;
    }

    public ObjectProperty<Variant> variantProperty() {
        return variant;
    }

    public Variant getVariant() {
        return variant.get();
    }

    public AlertMfx(Node... children) {
        super(children);
        getStyleClass().add(STYLE_CLASS);
        getStyleClass().add(variant.get().styleClass());
        variant.addListener((obs, old, now) -> {
            getStyleClass().remove(old.styleClass());
            getStyleClass().add(now.styleClass());
        });
        Tw.apply(this, "w-full");
    }
}
