// Made from shadcn's switch.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.toggle;

import atlantafx.base.controls.ToggleSwitch;
import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import org.jspecify.annotations.NullMarked;

/// shadcn's `Switch` (`data-slot="switch"`, switch.tsx), as a JavaFX ToggleSwitch.
/// The look is in `components.css` (style class `mfx-switch`).
///
/// Its parts: `switch-thumb` styles `> .thumb`.
///
/// Left out (JavaFX has no equivalent): `group-has-[:focus-visible]/field-label:border-transparent` `group-has-[:focus-visible]/field-label:ring-0` `after:absolute` `after:-inset-x-3` `after:-inset-y-2` `group-data-[size=default]/switch:data-checked:translate-x-[calc(100%-2px)]` `group-data-[size=sm]/switch:data-checked:translate-x-[calc(100%-2px)]`
@NullMarked
public class SwitchMfx extends ToggleSwitch {

    /// The style class every SwitchMfx has.
    public static final String STYLE_CLASS = "mfx-switch";

    /// shadcn's `size`: `default`, `sm`.
    public enum Size {
        DEFAULT("mfx-switch-size-default"),
        SM("mfx-switch-size-sm");

        private final String styleClass;

        Size(String styleClass) {
            this.styleClass = styleClass;
        }

        /// The style class this value puts on the Switch.
        public String styleClass() {
            return styleClass;
        }
    }

    private final ObjectProperty<Size> sizeVariant = new SimpleObjectProperty<>(this, "size", Size.DEFAULT);

    public SwitchMfx size(Size value) {
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

    public SwitchMfx() {
        this("");
    }

    public SwitchMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
        getStyleClass().add(sizeVariant.get().styleClass());
        sizeVariant.addListener((obs, old, now) -> {
            getStyleClass().remove(old.styleClass());
            getStyleClass().add(now.styleClass());
        });
        Tw.apply(this, "shrink-0 rounded-full");
    }
}
