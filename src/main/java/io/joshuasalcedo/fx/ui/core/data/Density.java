package io.joshuasalcedo.fx.ui.core.data;

import java.util.Locale;

/// How tight the rows of a list or table are. Each density is a row height, a padding and a style class
/// (`mfx-density-compact`...) the stylesheets match.
public enum Density {
    COMPACT(32, 4),
    COMFORTABLE(40, 8),
    SPACIOUS(52, 12);

    private final double rowHeight;
    private final double padding;

    Density(double rowHeight, double padding) {
        this.rowHeight = rowHeight;
        this.padding = padding;
    }

    /// The height of one row, in pixels.
    public double rowHeight() {
        return rowHeight;
    }

    /// The space above and below a row's content, in pixels.
    public double padding() {
        return padding;
    }

    /// The style class this density puts on the collection.
    public String styleClass() {
        return "mfx-density-" + name().toLowerCase(Locale.ROOT);
    }
}
