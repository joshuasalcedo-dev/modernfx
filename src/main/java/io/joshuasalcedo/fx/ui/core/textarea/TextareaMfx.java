// Made from shadcn's textarea.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.textarea;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.TextArea;
import org.jspecify.annotations.NullMarked;

/// shadcn's `Textarea` (`data-slot="textarea"`, textarea.tsx), as a JavaFX TextArea.
/// The look is in `components.css` (style class `mfx-textarea`).
///
/// Left out (JavaFX has no equivalent): `md:text-sm`
@NullMarked
public class TextareaMfx extends TextArea {

    /// The style class every TextareaMfx has.
    public static final String STYLE_CLASS = "mfx-textarea";

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

    public TextareaMfx() {
        getStyleClass().add(STYLE_CLASS);
        Tw.apply(this, "w-full rounded-md");
    }

    public TextareaMfx(String prompt) {
        this();
        setPromptText(prompt);
    }
}
