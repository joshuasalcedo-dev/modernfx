package io.joshuasalcedo.fx.ui.core.sheet;

/// What kind of value a sheet cell holds: it decides how the value is shown, edited and sorted.
public enum CellType {
    TEXT,
    NUMBER,
    MONEY,
    PERCENT,
    DATE,
    BOOLEAN,
    EMPTY
}
