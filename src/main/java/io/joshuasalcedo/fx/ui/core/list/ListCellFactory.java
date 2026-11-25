package io.joshuasalcedo.fx.ui.core.list;

import java.util.function.Function;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Fills a list row for an item. The row (a ListItemLayout) is reused as the list scrolls, so `fill` sets every
/// slot it uses; the others start empty.
///
/// ```java
/// ListCellFactory<Contact> rows = (row, c) -> row
///         .leading(AvatarCellRenderer.avatar(c.name(), c.photo()))
///         .title(c.name())
///         .subtitle(c.email());
/// ```
@NullMarked
@FunctionalInterface
public interface ListCellFactory<T> {

    /// Fills `row` for `item`.
    void fill(ListItemLayout row, T item);

    /// Rows with only a title.
    static <T> ListCellFactory<T> title(Function<T, @Nullable String> title) {
        return (row, item) -> row.title(title.apply(item));
    }

    /// Rows with a title and a subtitle.
    static <T> ListCellFactory<T> of(Function<T, @Nullable String> title, Function<T, @Nullable String> subtitle) {
        return (row, item) -> row.title(title.apply(item)).subtitle(subtitle.apply(item));
    }
}
