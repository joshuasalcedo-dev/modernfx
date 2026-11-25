package io.joshuasalcedo.fx.ui.core.sheet;

import java.math.BigDecimal;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Checks a cell edit: returns why it's refused, or null to accept it. SheetMfx puts a refused cell back as it was
/// and shows the message.
///
/// ```java
/// sheet.setValidation(SheetValidation.numberBetween(CellRange.parse("B2:B50"), BigDecimal.ZERO, BigDecimal.valueOf(80))
///         .and(SheetValidation.required(CellRange.parse("A2:A50"))));
/// ```
@NullMarked
@FunctionalInterface
public interface SheetValidation {

    /// Why `value` can't go at `at`, or null if it can.
    @Nullable String validate(CellAddress at, CellValue value);

    /// Both checks: the first refusal wins.
    default SheetValidation and(SheetValidation other) {
        Objects.requireNonNull(other, "other");
        return (at, value) -> {
            String first = validate(at, value);
            return first != null ? first : other.validate(at, value);
        };
    }

    /// Cells in `range` can't be emptied.
    static SheetValidation required(CellRange range) {
        return (at, value) -> range.contains(at) && value.isEmpty() ? at + " can't be empty" : null;
    }

    /// Cells in `range` hold numbers from `min` to `max` (or nothing).
    static SheetValidation numberBetween(CellRange range, BigDecimal min, BigDecimal max) {
        return (at, value) -> {
            if (!range.contains(at) || value.isEmpty()) {
                return null;
            }
            BigDecimal n = value.asNumber();
            if (n == null) {
                return at + " takes a number";
            }
            return n.compareTo(min) < 0 || n.compareTo(max) > 0
                    ? at + " takes a number from " + min.toPlainString() + " to " + max.toPlainString() : null;
        };
    }
}
