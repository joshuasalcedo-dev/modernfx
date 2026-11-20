package io.joshuasalcedo.fx.ui.core.divider;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import org.jspecify.annotations.NullMarked;

/// shadcn's Resizable: panels side by side (or stacked) with a thin line between them that can be dragged
/// (JavaFX's SplitPane). With a handle, the line shows a small grip.
///
/// ```java
/// new ResizableMfx(Orientation.HORIZONTAL, sidebar, editor).handle(true);
/// ```
@NullMarked
public class ResizableMfx extends SplitPane {

    /// The style class every ResizableMfx has.
    public static final String STYLE_CLASS = "mfx-resizable";

    public ResizableMfx(Orientation orientation, Node... panels) {
        super(panels);
        setOrientation(orientation);
        getStyleClass().add(STYLE_CLASS);
    }

    /// Shows a small grip on the line (shadcn's withHandle).
    public ResizableMfx handle(boolean show) {
        getStyleClass().remove("mfx-resizable-handle");
        if (show) {
            getStyleClass().add("mfx-resizable-handle");
        }
        return this;
    }
}
