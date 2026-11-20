// Made from shadcn's checkbox.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.checkbox;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.CheckBox;
import org.jspecify.annotations.NullMarked;

/// shadcn's `Checkbox` (`data-slot="checkbox"`, checkbox.tsx), as a JavaFX CheckBox.
/// The look is in `components.css` (style class `mfx-checkbox`).
///
/// Its parts: `checkbox-indicator` styles `> .box > .mark`.
///
/// Left out (JavaFX has no equivalent): `group-has-disabled/field:opacity-50` `group-has-[:focus-visible]/field-label:ring-0` `group-has-[:focus-visible]/field-label:not-data-checked:border-input` `after:absolute` `after:-inset-x-3` `after:-inset-y-2` `group-has-[:focus-visible]/field-label:data-checked:border-primary` `[&>svg]:size-3.5`
@NullMarked
public class CheckboxMfx extends CheckBox {

    /// The style class every CheckboxMfx has.
    public static final String STYLE_CLASS = "mfx-checkbox";

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

    public CheckboxMfx() {
        this("");
    }

    public CheckboxMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
        Tw.apply(this, "shrink-0");
    }
}
