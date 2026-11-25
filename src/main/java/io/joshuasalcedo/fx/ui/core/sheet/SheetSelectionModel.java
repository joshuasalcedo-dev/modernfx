package io.joshuasalcedo.fx.ui.core.sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TablePosition;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.controlsfx.control.spreadsheet.SpreadsheetViewSelectionModel;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A sheet's selection in CellAddresses and CellRanges: the active cell (where typing goes) and the selected
/// cells. Clicking, Shift+click, dragging and Ctrl+A are SpreadsheetView's; this reads and sets what they select,
/// in the sheet's own rows and columns (not the rows on screen, which sorting and hiding change).
@NullMarked
public class SheetSelectionModel {

    private final SpreadsheetView view;

    public SheetSelectionModel(SpreadsheetView view) {
        this.view = Objects.requireNonNull(view, "view");
    }

    private SpreadsheetViewSelectionModel selection() {
        return view.getSelectionModel();
    }

    /// The cell with the focus, or null.
    public @Nullable CellAddress activeCell() {
        TablePosition<?, ?> at = selection().getFocusedCell();
        if (at == null || at.getRow() < 0 || at.getColumn() < 0) {
            return null;
        }
        return new CellAddress(view.getModelRow(at.getRow()), view.getModelColumn(at.getColumn()));
    }

    /// Moves the focus to this cell and selects only it.
    public void setActiveCell(CellAddress cell) {
        selection().clearAndSelect(view.getViewRow(cell.row()), view.getColumns().get(cell.column()));
    }

    /// The selected cells.
    public List<CellAddress> selectedCells() {
        List<CellAddress> out = new ArrayList<>();
        for (TablePosition<?, ?> p : selection().getSelectedCells()) {
            if (p.getRow() >= 0 && p.getColumn() >= 0) {
                out.add(new CellAddress(view.getModelRow(p.getRow()), view.getModelColumn(p.getColumn())));
            }
        }
        return out;
    }

    /// The smallest range holding every selected cell, or null when nothing is selected.
    public @Nullable CellRange selectedRange() {
        List<CellAddress> cells = selectedCells();
        if (cells.isEmpty()) {
            CellAddress active = activeCell();
            return active == null ? null : CellRange.of(active);
        }
        int top = Integer.MAX_VALUE;
        int left = Integer.MAX_VALUE;
        int bottom = 0;
        int right = 0;
        for (CellAddress c : cells) {
            top = Math.min(top, c.row());
            left = Math.min(left, c.column());
            bottom = Math.max(bottom, c.row());
            right = Math.max(right, c.column());
        }
        return CellRange.of(top, left, bottom, right);
    }

    /// Selects a range.
    public void select(CellRange range) {
        selection().clearSelection();
        selection().selectRange(view.getViewRow(range.start().row()), view.getColumns().get(range.start().column()),
                view.getViewRow(range.end().row()), view.getColumns().get(range.end().column()));
    }

    public void selectAll() {
        selection().selectAll();
    }

    public void clear() {
        selection().clearSelection();
    }

    /// Runs `listener` each time the selection changes.
    public void addListener(Runnable listener) {
        selection().getSelectedCells().addListener((ListChangeListener<TablePosition>) c -> listener.run());
    }
}
