package io.joshuasalcedo.fx.ui.core.data;

/// Where a collection is in loading its items. CollectionPlaceholderMfx shows a skeleton, an empty state or an
/// error by it.
public enum LoadState {
    /// Nothing asked for yet.
    IDLE,
    /// The first page is loading: there is nothing to show yet.
    LOADING,
    /// More items are loading under the ones already shown.
    LOADING_MORE,
    LOADED,
    /// Loaded, and there are no items.
    EMPTY,
    ERROR;

    /// True while a load is running.
    public boolean isBusy() {
        return this == LOADING || this == LOADING_MORE;
    }
}
