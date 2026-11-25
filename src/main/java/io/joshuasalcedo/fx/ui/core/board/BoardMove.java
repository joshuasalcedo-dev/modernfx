package io.joshuasalcedo.fx.ui.core.board;

/// A card the user moved: from column `fromColumn` at `fromIndex` to column `toColumn` at `toIndex` (where it is
/// now). The board has moved it already; save it in `onMove`.
public record BoardMove<T>(T item, String fromColumn, int fromIndex, String toColumn, int toIndex) {

    /// True when the card changed column, not only its place in the column.
    public boolean changedColumn() {
        return !fromColumn.equals(toColumn);
    }
}
