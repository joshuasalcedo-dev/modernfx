package io.joshuasalcedo.fx.ui.core.sheet;

import java.util.function.Consumer;
import org.controlsfx.control.spreadsheet.Grid;
import org.jspecify.annotations.NullMarked;

/// A sheet's cells as CellValues: how many rows and columns, get and set by address, and a listener for every
/// change (the user's edits and `set` alike). SheetMfx's model is over its Grid ([#of]); the clipboard, the undo
/// stack and validation work through it.
@NullMarked
public interface SheetModel {

    /// One cell changed from `before` to `after`.
    record Change(CellAddress address, CellValue before, CellValue after) {
    }

    int rowCount();

    int columnCount();

    /// The value at `address` (EMPTY outside the sheet).
    CellValue get(CellAddress address);

    /// Sets the value at `address` (ignored outside the sheet).
    void set(CellAddress address, CellValue value);

    void addListener(Consumer<Change> listener);

    void removeListener(Consumer<Change> listener);

    /// The model of a ControlsFX Grid.
    static SheetModel of(Grid grid) {
        return new GridSheetModel(grid);
    }
}
