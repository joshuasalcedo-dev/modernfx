package io.joshuasalcedo.fx.ui.core.table;

/// Which edge a column stays at. TableMfx keeps LEFT columns first and RIGHT columns last, whatever order the
/// others are dragged into. (JavaFX's TableView can't freeze columns while scrolling sideways; SheetMfx can.)
public enum ColumnPin {
    NONE,
    LEFT,
    RIGHT
}
