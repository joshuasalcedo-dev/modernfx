package io.joshuasalcedo.fx.ui.core.internal;

import java.util.function.Function;
import javafx.beans.value.ObservableValue;
import javafx.css.Styleable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Keeps a node's (or a table column's) style class in step with a property: the class for the current value is on
/// it, the one for the old value is taken off.
@NullMarked
public final class StyleClasses {

    private StyleClasses() {
    }

    /// Puts `styleClass.apply(value)` on `target` now and each time `value` changes.
    public static <E> void bind(Styleable target, ObservableValue<@Nullable E> value, Function<E, String> styleClass) {
        E now = value.getValue();
        if (now != null) {
            target.getStyleClass().add(styleClass.apply(now));
        }
        value.addListener((obs, old, next) -> {
            if (old != null) {
                target.getStyleClass().remove(styleClass.apply(old));
            }
            if (next != null) {
                target.getStyleClass().add(styleClass.apply(next));
            }
        });
    }
}
