package io.joshuasalcedo.fx.ui.core.data;

/// Which way a column is sorted.
public enum SortDirection {
    ASCENDING,
    DESCENDING,
    /// Not sorted: the order the source gives.
    NONE;

    /// The next direction when a header is clicked: ascending, descending, then none.
    public SortDirection next() {
        return switch (this) {
            case NONE -> ASCENDING;
            case ASCENDING -> DESCENDING;
            case DESCENDING -> NONE;
        };
    }
}
