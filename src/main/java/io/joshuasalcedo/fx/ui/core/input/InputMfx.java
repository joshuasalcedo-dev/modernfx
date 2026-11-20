// Made from shadcn's input.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.input;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NullMarked;

/// shadcn's `Input` (`data-slot="input"`, input.tsx), as a JavaFX TextField.
/// The look is in `components.css` (style class `mfx-input`).
///
/// Left out (JavaFX has no equivalent): `file:inline-flex` `file:h-7` `file:border-0` `file:bg-transparent` `file:text-sm` `file:font-medium` `file:text-foreground` `md:text-sm`
@NullMarked
public class InputMfx extends TextField {

    /// The style class every InputMfx has.
    public static final String STYLE_CLASS = "mfx-input";

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

    public InputMfx() {
        getStyleClass().add(STYLE_CLASS);
        getStyleClass().remove("text-field");
        Tw.apply(this, "w-full rounded-md");
    }

    public InputMfx(String prompt) {
        this();
        setPromptText(prompt);
    }
}
