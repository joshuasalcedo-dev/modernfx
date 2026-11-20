// Made from shadcn's progress.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.progress;

import javafx.scene.control.Label;
import org.jspecify.annotations.NullMarked;

/// shadcn's `ProgressValue` (`data-slot="progress-value"`, progress.tsx), as a JavaFX Label.
/// The look is in `components.css` (style class `mfx-progress-value`).
///
/// Left out (JavaFX has no equivalent): `ml-auto`
@NullMarked
public class ProgressValueMfx extends Label {

    /// The style class every ProgressValueMfx has.
    public static final String STYLE_CLASS = "mfx-progress-value";

    public ProgressValueMfx() {
        this("");
    }

    public ProgressValueMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
    }
}
