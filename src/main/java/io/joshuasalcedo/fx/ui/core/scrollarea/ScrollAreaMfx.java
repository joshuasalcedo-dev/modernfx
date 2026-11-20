package io.joshuasalcedo.fx.ui.core.scrollarea;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import org.jspecify.annotations.NullMarked;

/// shadcn's ScrollArea: scrolling content with thin rounded scrollbars and no background (JavaFX's
/// ScrollPane). It fits its content to its width, so only long content scrolls, downwards.
@NullMarked
public class ScrollAreaMfx extends ScrollPane {

    /// The style class every ScrollAreaMfx has.
    public static final String STYLE_CLASS = "mfx-scroll-area";

    public ScrollAreaMfx(Node content) {
        super(content);
        getStyleClass().add(STYLE_CLASS);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
    }
}
