// Made from shadcn's separator.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.separator;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Region;
import org.jspecify.annotations.NullMarked;

/// shadcn's `Separator` (`data-slot="separator"`, separator.tsx), as a JavaFX Region.
/// The look is in `components.css` (style class `mfx-separator`).
@NullMarked
public class SeparatorMfx extends Region {

    /// The style class every SeparatorMfx has.
    public static final String STYLE_CLASS = "mfx-separator";

    /// shadcn's `orientation`: `horizontal`, `vertical`.
    public enum Orientation {
        HORIZONTAL("mfx-separator-orientation-horizontal"),
        VERTICAL("mfx-separator-orientation-vertical");

        private final String styleClass;

        Orientation(String styleClass) {
            this.styleClass = styleClass;
        }

        /// The style class this value puts on the Separator.
        public String styleClass() {
            return styleClass;
        }
    }

    private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL);

    public SeparatorMfx orientation(Orientation value) {
        orientation.set(value);
        return this;
    }

    public ObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    public Orientation getOrientation() {
        return orientation.get();
    }

    public SeparatorMfx() {
        getStyleClass().add(STYLE_CLASS);
        getStyleClass().add(orientation.get().styleClass());
        orientation.addListener((obs, old, now) -> {
            getStyleClass().remove(old.styleClass());
            getStyleClass().add(now.styleClass());
        });
        Tw.apply(this, "shrink-0");
    }
}
