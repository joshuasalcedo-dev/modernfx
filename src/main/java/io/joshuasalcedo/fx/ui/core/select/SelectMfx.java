package io.joshuasalcedo.fx.ui.core.select;

import java.util.Collection;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NullMarked;

/// shadcn's Select: pick one option from a list (JavaFX's ComboBox, with the input's look and a popover list).
///
/// ```java
/// var fruit = new SelectMfx<>("Select a fruit", List.of("Apple", "Banana", "Blueberry"));
/// ```
@NullMarked
public class SelectMfx<T> extends ComboBox<T> {

    /// The style class every SelectMfx has.
    public static final String STYLE_CLASS = "mfx-select";

    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");

    private final BooleanProperty invalid = new SimpleBooleanProperty(this, "invalid") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(INVALID, get());
        }
    };

    public SelectMfx() {
        getStyleClass().add(STYLE_CLASS);
    }

    /// A select showing `placeholder` until something is picked.
    public SelectMfx(String placeholder, Collection<T> items) {
        this();
        setPromptText(placeholder);
        getItems().setAll(items);
    }

    public BooleanProperty invalidProperty() {
        return invalid;
    }

    public void setInvalid(boolean value) {
        invalid.set(value);
    }
}
