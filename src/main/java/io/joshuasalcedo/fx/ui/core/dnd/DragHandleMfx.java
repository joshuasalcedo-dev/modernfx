package io.joshuasalcedo.fx.ui.core.dnd;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NullMarked;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;

/// The grip (six dots) to drag a row by. Put it in a row; with ReorderSupport's `handleOnly`, rows only start
/// dragging from it, so the rest of the row can still be clicked and selected.
@NullMarked
public class DragHandleMfx extends StackPane {

    public static final String STYLE_CLASS = "mfx-drag-handle";

    public DragHandleMfx() {
        getStyleClass().add(STYLE_CLASS);
        getChildren().add(new FontIcon(Material2AL.DRAG_INDICATOR));
        setCursor(Cursor.OPEN_HAND);
        setAccessibleText("Drag to move");
        addEventHandler(MouseEvent.MOUSE_PRESSED, e -> setCursor(Cursor.CLOSED_HAND));
        addEventHandler(MouseEvent.MOUSE_RELEASED, e -> setCursor(Cursor.OPEN_HAND));
    }
}
