package io.joshuasalcedo.fx.ui.core.dnd;

import javafx.geometry.Orientation;
import javafx.scene.layout.Region;
import org.jspecify.annotations.NullMarked;

/// The line that shows where a dragged item will land: 2 pixels of `-primary`, across a column (horizontal) or
/// between tiles (vertical). BoardMfx puts it between cards; ReorderSupport draws the same line on rows.
@NullMarked
public class DropIndicatorMfx extends Region {

    public static final String STYLE_CLASS = "mfx-drop-indicator";

    private final Orientation orientation;

    /// A horizontal line.
    public DropIndicatorMfx() {
        this(Orientation.HORIZONTAL);
    }

    public DropIndicatorMfx(Orientation orientation) {
        this.orientation = orientation;
        getStyleClass().addAll(STYLE_CLASS, orientation == Orientation.HORIZONTAL ? "mfx-horizontal" : "mfx-vertical");
        setMouseTransparent(true);
        if (orientation == Orientation.HORIZONTAL) {
            setMinHeight(2);
            setPrefHeight(2);
            setMaxHeight(2);
            setMaxWidth(Double.MAX_VALUE);
        } else {
            setMinWidth(2);
            setPrefWidth(2);
            setMaxWidth(2);
            setMaxHeight(Double.MAX_VALUE);
        }
    }

    public Orientation orientation() {
        return orientation;
    }
}
