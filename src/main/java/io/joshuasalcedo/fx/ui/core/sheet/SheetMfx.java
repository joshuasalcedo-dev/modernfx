package io.joshuasalcedo.fx.ui.core.sheet;

import io.joshuasalcedo.fx.ui.core.toast.ToastMfx;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A spreadsheet: ControlsFX's [SpreadsheetView] with the shadcn look (style class `mfx-sheet`, in
/// `components/sheet.css`): muted headers, thin grid lines, `-primary-10` selection and a ring on the active cell.
/// Everything SpreadsheetView does works as is: editing, fixed rows and columns, spans, filters. On top of it:
/// the cells as CellValues ([#model]), copy and paste as tab-separated text (Excel, Google Sheets), undo and redo
/// (Ctrl+Z, Ctrl+Y), checks on edits ([#setValidation]), and the selection as CellAddresses ([#selection]).
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

    private @Nullable GridSheetModel model;
    private @Nullable SheetUndoStack undo;
    private @Nullable SheetSelectionModel selection;
    private @Nullable SheetValidation validation;
    private boolean reverting;

    /// A sheet showing this grid.
    public SheetMfx(Grid grid) {
        super(checked(grid));
        getStyleClass().add(STYLE_CLASS);
        model();
        addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (UNDO.match(e)) {
                undoStack().undo();
                e.consume();
            } else if (REDO.match(e) || REDO_SHIFT.match(e)) {
                undoStack().redo();
                e.consume();
            }
        });
    }

    private static final KeyCombination UNDO = new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination REDO = new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN);
    private static final KeyCombination REDO_SHIFT =
            new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    /// The cells as CellValues. A new grid (setGrid) gets a new model, and a new undo stack.
    public SheetModel model() {
        Grid grid = getGrid();
        GridSheetModel m = model;
        if (m == null || m.grid() != grid) {
            m = new GridSheetModel(grid);
            model = m;
            SheetUndoStack stack = new SheetUndoStack(m, false);
            undo = stack;
            GridSheetModel current = m;
            m.addListener(change -> changed(current, stack, change));
        }
        return m;
    }

    /// Undo and redo (Ctrl+Z, Ctrl+Y).
    public SheetUndoStack undoStack() {
        model();
        return java.util.Objects.requireNonNull(undo);
    }

    /// The selection, as cell addresses.
    public SheetSelectionModel selection() {
        if (selection == null) {
            selection = new SheetSelectionModel(this);
        }
        return selection;
    }

    /// Checks each edit; a refused one is put back and its message shown. Null accepts everything.
    public void setValidation(@Nullable SheetValidation value) {
        validation = value;
    }

    public @Nullable SheetValidation getValidation() {
        return validation;
    }

    private void changed(SheetModel m, SheetUndoStack stack, SheetModel.Change change) {
        if (reverting) {
            return;
        }
        SheetValidation check = validation;
        String error = check == null ? null : check.validate(change.address(), change.after());
        if (error != null) {
            reverting = true;
            try {
                m.set(change.address(), change.before());
            } finally {
                reverting = false;
            }
            ToastMfx.show(this, "Not accepted", error);
            return;
        }
        stack.record(change);
    }

    /// Copies the selected cells as tab-separated text, for Excel and Google Sheets.
    @Override
    public void copyClipboard() {
        CellRange range = selection().selectedRange();
        if (range != null) {
            SheetClipboard.copy(model(), range);
        }
    }

    /// Pastes tab-separated text from the active cell down and right, as one undo step.
    @Override
    public void pasteClipboard() {
        CellAddress at = selection().activeCell();
        if (at == null) {
            return;
        }
        if (!Clipboard.getSystemClipboard().hasString()) {
            super.pasteClipboard();
            return;
        }
        SheetModel m = model();
        undoStack().compound(() -> SheetClipboard.paste(m, at));
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
