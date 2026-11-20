// Made from shadcn's progress.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.progress;

import javafx.scene.control.ProgressBar;
import org.jspecify.annotations.NullMarked;

/// shadcn's `Progress` (`data-slot="progress"`, progress.tsx), as a JavaFX ProgressBar.
/// The look is in `components.css` (style class `mfx-progress`).
///
/// Its parts: `progress-track` styles `> .track`, `progress-indicator` styles `> .bar`.
@NullMarked
public class ProgressMfx extends ProgressBar {

    /// The style class every ProgressMfx has.
    public static final String STYLE_CLASS = "mfx-progress";

    public ProgressMfx() {
        this(0);
    }

    public ProgressMfx(double value) {
        super(value);
        getStyleClass().add(STYLE_CLASS);
    }
}
