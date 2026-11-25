package io.joshuasalcedo.fx.ui.core.sheet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Makes SheetMfx cells (ControlsFX SpreadsheetCells) from CellValues: the cell type follows the value's type, so
/// SpreadsheetView edits each with the right editor (a number field, a date picker, TRUE/FALSE), and the CellStyle
/// becomes its classes, format and background. Turns cells back into CellValues too.
@NullMarked
public final class SheetCellMfx {

    /// The number format of MONEY cells unless the style gives one.
    public static final String MONEY_FORMAT = "#,##0.00";

    /// The number format of PERCENT cells unless the style gives one.
    public static final String PERCENT_FORMAT = "0.##%";

    private static final List<String> BOOLEANS = List.of("TRUE", "FALSE");

    private SheetCellMfx() {
    }

    /// A cell at (`row`, `column`) holding `value`, looking like `style` (null for the default).
    public static SpreadsheetCell create(int row, int column, CellValue value, @Nullable CellStyle style) {
        SpreadsheetCell cell = switch (value.type()) {
            case NUMBER, MONEY, PERCENT -> SpreadsheetCellType.DOUBLE.createCell(row, column, 1, 1,
                    value.asNumber() == null ? null : value.asNumber().doubleValue());
            case DATE -> SpreadsheetCellType.DATE.createCell(row, column, 1, 1, (LocalDate) value.value());
            case BOOLEAN -> SpreadsheetCellType.LIST(BOOLEANS).createCell(row, column, 1, 1, value.asText());
            case TEXT, EMPTY -> SpreadsheetCellType.STRING.createCell(row, column, 1, 1, value.asText());
        };
        cell.getStyleClass().add("mfx-cell-" + value.type().name().toLowerCase(java.util.Locale.ROOT));
        CellStyle s = style == null ? CellStyle.DEFAULT : style;
        String format = s.numberFormat() != null ? s.numberFormat()
                : value.type() == CellType.MONEY ? MONEY_FORMAT
                : value.type() == CellType.PERCENT ? PERCENT_FORMAT : null;
        if (format != null) {
            cell.setFormat(format);
        }
        cell.getStyleClass().addAll(s.styleClasses());
        if (!s.inlineStyle().isEmpty()) {
            cell.setStyle(s.inlineStyle());
        }
        return cell;
    }

    /// The value a cell holds now. MONEY and PERCENT cells (by their class) stay MONEY and PERCENT.
    public static CellValue valueOf(SpreadsheetCell cell) {
        return valueOf(cell.getStyleClass(), cell.getItem());
    }

    /// The value `item` is in a cell with these style classes.
    static CellValue valueOf(Set<String> classes, @Nullable Object item) {
        return switch (item) {
            case null -> CellValue.empty();
            case Double d when classes.contains("mfx-cell-money") -> CellValue.money(BigDecimal.valueOf(d));
            case Double d when classes.contains("mfx-cell-percent") -> CellValue.percent(BigDecimal.valueOf(d));
            case Integer i -> CellValue.number(BigDecimal.valueOf(i));
            case Number n -> CellValue.number(new BigDecimal(n.toString()));
            case LocalDate d -> CellValue.date(d);
            case String s when classes.contains("mfx-cell-boolean") -> CellValue.bool("TRUE".equalsIgnoreCase(s));
            case String s -> CellValue.text(s);
            default -> CellValue.text(item.toString());
        };
    }

    /// The item to put in `cell` for `value`, or null if the cell's type can't hold it.
    static @Nullable Object itemFor(SpreadsheetCell cell, CellValue value) {
        SpreadsheetCellType<?> type = cell.getCellType();
        Object item = switch (value.type()) {
            case EMPTY -> null;
            case NUMBER, MONEY, PERCENT -> type == SpreadsheetCellType.STRING ? value.asText()
                    : value.asNumber() == null ? null : value.asNumber().doubleValue();
            case DATE -> type == SpreadsheetCellType.STRING ? value.asText() : value.value();
            case BOOLEAN, TEXT -> value.asText();
        };
        if (item == null) {
            return type == SpreadsheetCellType.STRING ? "" : null;
        }
        return type.match(item) ? type.convertValue(item) : null;
    }
}
