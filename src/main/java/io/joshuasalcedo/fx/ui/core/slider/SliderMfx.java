package io.joshuasalcedo.fx.ui.core.slider;

import javafx.scene.control.Slider;
import org.jspecify.annotations.NullMarked;

/// shadcn's Slider: pick a number by dragging (JavaFX's Slider): a thin muted track, the filled part in the
/// primary color, a round thumb.
@NullMarked
public class SliderMfx extends Slider {

    /// The style class every SliderMfx has.
    public static final String STYLE_CLASS = "mfx-slider";

    public SliderMfx() {
        this(0, 100, 50);
    }

    public SliderMfx(double min, double max, double value) {
        super(min, max, value);
        getStyleClass().add(STYLE_CLASS);
    }
}
