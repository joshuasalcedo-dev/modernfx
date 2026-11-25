package io.joshuasalcedo.fx.ui.core.table;

import java.util.Locale;

/// Where a column's values sit: numbers and money go right, so their digits line up. Each is a style class on the
/// column (`mfx-align-right`), which JavaFX puts on its header and its cells.
public enum ColumnAlignment {
    LEFT,
    CENTER,
    RIGHT;

    /// The style class this alignment puts on a column.
    public String styleClass() {
        return "mfx-align-" + name().toLowerCase(Locale.ROOT);
    }
}
