package io.joshuasalcedo.fx.ui.core.table;

import java.util.Objects;
import javafx.scene.control.TableColumnBase;

/// How wide a column is: FIXED pixels, a FLEX share of the space left (a weight: 2 is twice as wide as 1), or AUTO
/// (as wide as its content, measured when the items are set), each between `min` and `max`.
public record ColumnWidth(Kind kind, double value, double min, double max) {

    public enum Kind { FIXED, FLEX, AUTO }

    /// The width a FLEX column of weight 1 starts at, in pixels.
    public static final double FLEX_UNIT = 120;

    /// TableColumn's own maximum width, used for "no maximum".
    private static final double DEFAULT_MAX = 5000;

    public ColumnWidth {
        Objects.requireNonNull(kind, "kind");
        if (value < 0 || min < 0 || max < min) {
            throw new IllegalArgumentException("value and min >= 0, max >= min");
        }
    }

    public static ColumnWidth fixed(double pixels) {
        return new ColumnWidth(Kind.FIXED, pixels, pixels, pixels);
    }

    public static ColumnWidth flex(double weight) {
        return new ColumnWidth(Kind.FLEX, weight, 40, Double.MAX_VALUE);
    }

    public static ColumnWidth auto() {
        return new ColumnWidth(Kind.AUTO, 0, 40, 480);
    }

    public ColumnWidth withMin(double value) {
        return new ColumnWidth(kind, this.value, value, Math.max(value, max));
    }

    public ColumnWidth withMax(double value) {
        return new ColumnWidth(kind, this.value, Math.min(min, value), value);
    }

    /// Sets the column's widths. For AUTO, `contentWidth` is the measured width of its content.
    public void apply(TableColumnBase<?, ?> column, double contentWidth) {
        column.setMinWidth(min);
        column.setMaxWidth(max == Double.MAX_VALUE ? DEFAULT_MAX : max);
        double pref = switch (kind) {
            case FIXED -> value;
            case FLEX -> value * FLEX_UNIT;
            case AUTO -> contentWidth;
        };
        column.setPrefWidth(Math.clamp(pref, min, max));
        column.setResizable(kind != Kind.FIXED);
    }
}
