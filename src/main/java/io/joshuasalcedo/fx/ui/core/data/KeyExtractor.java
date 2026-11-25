package io.joshuasalcedo.fx.ui.core.data;

/// Gets a stable key from an item (its id), so selection and focus survive a reload, when the items are new
/// objects for the same records.
///
/// ```java
/// KeyExtractor<Invoice> key = Invoice::id;
/// ```
@FunctionalInterface
public interface KeyExtractor<T> {

    /// The item's key. Two items with equal keys are the same record.
    Object key(T item);

    /// The item itself is the key: for items with a proper `equals`, such as records or strings.
    static <T> KeyExtractor<T> identity() {
        return item -> item;
    }
}
