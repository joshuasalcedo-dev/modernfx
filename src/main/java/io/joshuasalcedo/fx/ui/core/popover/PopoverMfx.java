package io.joshuasalcedo.fx.ui.core.popover;

import atlantafx.base.controls.Popover;
import javafx.scene.Node;
import javafx.util.Duration;
import org.jspecify.annotations.NullMarked;

/// shadcn's Popover: a small floating box under a control (AtlantaFX's Popover, without its arrow).
///
/// ```java
/// var popover = new PopoverMfx(dimensionsForm);
/// trigger.setOnAction(e -> popover.showBelow(trigger));
/// ```
@NullMarked
public class PopoverMfx extends Popover {

    /// The style class every PopoverMfx has.
    public static final String STYLE_CLASS = "mfx-popover";

    public PopoverMfx(Node content) {
        super(content);
        getStyleClass().add(STYLE_CLASS);
        setArrowSize(0);
        setArrowIndent(0);
        setArrowLocation(ArrowLocation.TOP_LEFT);
        setHeaderAlwaysVisible(false);
        setDetachable(false);
        setAnimated(true);
        setFadeInDuration(Duration.millis(120));
        setFadeOutDuration(Duration.millis(120));
    }

    /// Shows it under `owner`, left edges lined up, 4px apart.
    public void showBelow(Node owner) {
        var b = owner.localToScreen(owner.getBoundsInLocal());
        if (b == null) {
            show(owner);
            return;
        }
        show(owner, b.getMinX(), b.getMaxY() + 4);
    }

    /// Shows it, or hides it when it's showing.
    public void toggle(Node owner) {
        if (isShowing()) {
            hide();
        } else {
            showBelow(owner);
        }
    }
}
