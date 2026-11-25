package io.joshuasalcedo.fx.ui.core.dnd;

/// An item the user dragged: it was at index `from` and is now at index `to`.
public record ReorderEvent<T>(int from, int to, T item) {
}
