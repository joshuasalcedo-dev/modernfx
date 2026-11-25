package io.joshuasalcedo.fx.ui.core.pagination;

/// How a collection moves between pages.
public enum PaginationStyle {
    /// Previous, next and the page numbers (PaginationMfx).
    NUMBERED,
    /// Previous and next around the current page number (PaginationMfx).
    SIMPLE,
    /// A "Load more" button under the items (ListMfx); InfiniteListMfx loads them as you scroll instead.
    LOAD_MORE
}
