package io.joshuasalcedo.fx.ui.core.tooltip;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import org.jspecify.annotations.NullMarked;

/// shadcn's Tooltip: a small dark label that shows while the pointer rests on a node (JavaFX's Tooltip).
///
/// ```java
/// TooltipMfx.install(saveButton, "Save (Ctrl+S)");
/// ```
@NullMarked
public class TooltipMfx extends Tooltip {

    /// The style class every TooltipMfx has.
    public static final String STYLE_CLASS = "mfx-tooltip";

    public TooltipMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
        setShowDelay(Duration.millis(400));
        setHideDelay(Duration.ZERO);
    }

    /// Puts a tooltip with this text on the node.
    public static TooltipMfx install(Node node, String text) {
        TooltipMfx t = new TooltipMfx(text);
        Tooltip.install(node, t);
        return t;
    }
}
