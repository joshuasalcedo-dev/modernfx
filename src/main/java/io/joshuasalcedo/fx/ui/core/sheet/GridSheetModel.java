package io.joshuasalcedo.fx.ui.core.sheet;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javafx.collections.ObservableList;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.GridChange;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// The SheetModel over a ControlsFX Grid: reads cells as CellValues, and tells its listeners about the user's edits
/// (the Grid's GridChange events) and its own sets.
@NullMarked
final class GridSheetModel implements SheetModel {

    private final Grid grid;
    private final List<Consumer<Change>> listeners = new CopyOnWriteArrayList<>();
    private boolean setting;

    GridSheetModel(Grid grid) {
        this.grid = Objects.requireNonNull(grid, "grid");
        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, e -> {
            if (!setting) {
                CellAddress at = new CellAddress(e.getRow(), e.getColumn());
                SpreadsheetCell cell = cell(at);
                if (cell != null) {
                    CellValue before = SheetCellMfx.valueOf(cell.getStyleClass(), e.getOldValue());
                    fire(new Change(at, before, SheetCellMfx.valueOf(cell)));
                }
            }
        });
    }

    Grid grid() {
        return grid;
    }

    @Override
    public int rowCount() {
        return grid.getRowCount();
    }

    @Override
    public int columnCount() {
        return grid.getColumnCount();
    }

    @Override
    public CellValue get(CellAddress address) {
        SpreadsheetCell cell = cell(address);
        return cell == null ? CellValue.empty() : SheetCellMfx.valueOf(cell);
    }

    @Override
    public void set(CellAddress address, CellValue value) {
        SpreadsheetCell cell = cell(address);
        if (cell == null) {
            return;
        }
        CellValue before = SheetCellMfx.valueOf(cell);
        if (before.equals(value)) {
            return;
        }
        setting = true;
        try {
            Object item = SheetCellMfx.itemFor(cell, value);
            if (item != null || value.isEmpty()) {
                grid.setCellValue(address.row(), address.column(), item);
            } else {
                // the cell's type can't hold it (text into a number cell): a new cell of the right type
                ObservableList<SpreadsheetCell> row = grid.getRows().get(address.row());
                row.set(address.column(), SheetCellMfx.create(address.row(), address.column(), value, null));
            }
        } finally {
            setting = false;
        }
        fire(new Change(address, before, get(address)));
    }

    @Override
    public void addListener(Consumer<Change> listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    @Override
    public void removeListener(Consumer<Change> listener) {
        listeners.remove(listener);
    }

    private @Nullable SpreadsheetCell cell(CellAddress at) {
        if (at.row() >= grid.getRowCount() || at.column() >= grid.getColumnCount()) {
            return null;
        }
        return grid.getRows().get(at.row()).get(at.column());
    }

    private void fire(Change change) {
        for (Consumer<Change> l : listeners) {
            l.accept(change);
        }
    }
}
