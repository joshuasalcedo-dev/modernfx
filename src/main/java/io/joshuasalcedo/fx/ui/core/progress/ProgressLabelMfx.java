// Made from shadcn's progress.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.progress;

import javafx.scene.control.Label;
import org.jspecify.annotations.NullMarked;

/// shadcn's `ProgressLabel` (`data-slot="progress-label"`, progress.tsx), as a JavaFX Label.
/// The look is in `components.css` (style class `mfx-progress-label`).
@NullMarked
public class ProgressLabelMfx extends Label {

    /// The style class every ProgressLabelMfx has.
    public static final String STYLE_CLASS = "mfx-progress-label";

    public ProgressLabelMfx() {
        this("");
    }

    public ProgressLabelMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
    }
}
