package io.joshuasalcedo.fx.ui.core.table.renderer;

import io.joshuasalcedo.fx.ui.core.table.ColumnAlignment;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

/// A number in the locale's format (`1,240.5`), right-aligned.
@NullMarked
public class NumberCellRenderer<T> implements CellRenderer<T, Number> {

    private final NumberFormat format;

    /// The default locale's number format.
    public NumberCellRenderer() {
        this(NumberFormat.getNumberInstance(Locale.getDefault(Locale.Category.FORMAT)));
    }

    public NumberCellRenderer(NumberFormat format) {
        this.format = (NumberFormat) Objects.requireNonNull(format, "format").clone();
    }

    /// Always `decimals` digits after the point, in the default locale.
    public static <T> NumberCellRenderer<T> decimals(int decimals) {
        NumberFormat f = NumberFormat.getNumberInstance(Locale.getDefault(Locale.Category.FORMAT));
        f.setMinimumFractionDigits(decimals);
        f.setMaximumFractionDigits(decimals);
        return new NumberCellRenderer<>(f);
    }

    /// A percentage (0.25 is `25%`).
    public static <T> NumberCellRenderer<T> percent() {
        return new NumberCellRenderer<>(NumberFormat.getPercentInstance(Locale.getDefault(Locale.Category.FORMAT)));
    }

    @Override
    public String text(Number value) {
        synchronized (format) {
            return format.format(value);
        }
    }

    @Override
    public ColumnAlignment alignment() {
        return ColumnAlignment.RIGHT;
    }
}
