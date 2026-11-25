package io.joshuasalcedo.fx.ui.core.sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// A rectangle of cells from `start` (top left) to `end` (bottom right), both in it. Made from any two corners;
/// written `A1:C3`.
public record CellRange(CellAddress start, CellAddress end) {

    public CellRange {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        CellAddress topLeft = new CellAddress(Math.min(start.row(), end.row()), Math.min(start.column(), end.column()));
        CellAddress bottomRight = new CellAddress(Math.max(start.row(), end.row()), Math.max(start.column(), end.column()));
        start = topLeft;
        end = bottomRight;
    }

    public static CellRange of(int row1, int column1, int row2, int column2) {
        return new CellRange(new CellAddress(row1, column1), new CellAddress(row2, column2));
    }

    /// One cell.
    public static CellRange of(CellAddress cell) {
        return new CellRange(cell, cell);
    }

    /// Reads `A1:C3` (or one address, `B2`).
    public static CellRange parse(String a1) {
        String[] parts = a1.split(":");
        if (parts.length == 1) {
            return of(CellAddress.parse(parts[0]));
        }
        if (parts.length != 2) {
            throw new IllegalArgumentException("not an A1 range: " + a1);
        }
        return new CellRange(CellAddress.parse(parts[0]), CellAddress.parse(parts[1]));
    }

    public int rows() {
        return end.row() - start.row() + 1;
    }

    public int columns() {
        return end.column() - start.column() + 1;
    }

    public boolean contains(CellAddress cell) {
        return cell.row() >= start.row() && cell.row() <= end.row()
                && cell.column() >= start.column() && cell.column() <= end.column();
    }

    /// Its cells, row by row.
    public List<CellAddress> cells() {
        List<CellAddress> out = new ArrayList<>(rows() * columns());
        for (int r = start.row(); r <= end.row(); r++) {
            for (int c = start.column(); c <= end.column(); c++) {
                out.add(new CellAddress(r, c));
            }
        }
        return out;
    }

    @Override
    public String toString() {
        return start.equals(end) ? start.toString() : start + ":" + end;
    }
}
