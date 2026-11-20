package io.joshuasalcedo.fx.ui.core.sheet;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A spreadsheet: ControlsFX's [SpreadsheetView] with the shadcn look (style class `mfx-sheet`, in
/// `components/sheet.css`): muted headers, thin grid lines, `-primary-10` selection and a ring on the active cell.
/// Everything SpreadsheetView does works as is: editing, copy and paste, fixed rows and columns, spans, filters.
///
/// On the module path (a modular app, or running from an IDE as a module), SpreadsheetView needs two JavaFX
/// internals opened to ControlsFX. Add these JVM options (the classpath needs nothing):
///
/// ```
/// --add-exports=javafx.base/com.sun.javafx.event=org.controlsfx.controls
/// --add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=org.controlsfx.controls
/// ```
///
/// ```java
/// SheetMfx sheet = SheetMfx.of(List.of("Name", "Hours", "Rate"), List.of(
///         List.of("Ana", 38, 12.5),
///         List.of("Ben", 41, 13.0)));
/// ```
@NullMarked
public class SheetMfx extends SpreadsheetView {

    /// The style class every SheetMfx has.
    public static final String STYLE_CLASS = "mfx-sheet";

    /// Row height of sheets made with [#of], in pixels (shadcn's tables use roomy rows).
    public static final double ROW_HEIGHT = 32;

    /// The narrowest column [#of] makes, in pixels.
    public static final double MIN_COLUMN_WIDTH = 80;

    /// An empty sheet.
    public SheetMfx() {
        this(new GridBase(0, 0));
    }

    /// The JVM options SpreadsheetView needs on the module path.
    public static final String JVM_OPTIONS = "--add-exports=javafx.base/com.sun.javafx.event=org.controlsfx.controls "
            + "--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=org.controlsfx.controls";

    /// A sheet showing this grid.
    public SheetMfx(Grid grid) {
        super(checked(grid));
        getStyleClass().add(STYLE_CLASS);
    }

    /// Fails with the JVM options to add when ControlsFX can't reach the JavaFX internals it needs.
    public static void checkAccess() {
        Module controlsfx = SpreadsheetView.class.getModule();
        if (!controlsfx.isNamed()) {
            return;
        }
        boolean events = javafx.event.Event.class.getModule().isExported("com.sun.javafx.event", controlsfx);
        boolean behavior = javafx.scene.control.Control.class.getModule()
                .isExported("com.sun.javafx.scene.control.behavior", controlsfx);
        if (!events || !behavior) {
            throw new IllegalStateException("SheetMfx (ControlsFX's SpreadsheetView) needs these JVM options on the "
                    + "module path: " + JVM_OPTIONS);
        }
    }

    private static Grid checked(Grid grid) {
        checkAccess();
        return grid;
    }

    /// A sheet from rows of values, with these column names. Numbers become number cells (edited as numbers),
    /// everything else text; `null` is an empty text cell.
    public static SheetMfx of(List<String> columns, List<? extends List<?>> rows) {
        checkAccess();
        GridBase grid = new GridBase(rows.size(), columns.size());
        ObservableList<ObservableList<SpreadsheetCell>> cells = FXCollections.observableArrayList();
        for (int r = 0; r < rows.size(); r++) {
            List<?> values = rows.get(r);
            List<SpreadsheetCell> row = new ArrayList<>();
            for (int c = 0; c < columns.size(); c++) {
                row.add(cell(r, c, c < values.size() ? values.get(c) : null));
            }
            cells.add(FXCollections.observableArrayList(row));
        }
        grid.setRows(cells);
        grid.getColumnHeaders().setAll(columns);
        grid.setRowHeightCallback(row -> ROW_HEIGHT);
        SheetMfx sheet = new SheetMfx(grid);
        sheet.setShowRowHeader(true);
        sheet.setShowColumnHeader(true);
        // wide enough for the longest value or name in each column (SpreadsheetView's default is narrow)
        for (int c = 0; c < columns.size(); c++) {
            int chars = columns.get(c).length();
            for (List<?> row : rows) {
                chars = Math.max(chars, c < row.size() && row.get(c) != null ? row.get(c).toString().length() : 0);
            }
            sheet.getColumns().get(c).setPrefWidth(Math.max(MIN_COLUMN_WIDTH, chars * 8 + 24));
        }
        return sheet;
    }

    private static SpreadsheetCell cell(int row, int column, @Nullable Object value) {
        if (value instanceof Integer i) {
            return SpreadsheetCellType.INTEGER.createCell(row, column, 1, 1, i);
        }
        if (value instanceof Number n) {
            return SpreadsheetCellType.DOUBLE.createCell(row, column, 1, 1, n.doubleValue());
        }
        return SpreadsheetCellType.STRING.createCell(row, column, 1, 1, value == null ? "" : value.toString());
    }
}
