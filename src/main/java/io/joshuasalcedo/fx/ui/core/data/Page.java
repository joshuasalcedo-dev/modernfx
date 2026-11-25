package io.joshuasalcedo.fx.ui.core.data;

import java.util.List;
import org.jspecify.annotations.Nullable;

/// One page of items, as a DataSource returns it: the items, how many there are in all (`total`, or
/// [#UNKNOWN_TOTAL] when the source can't tell), and for cursor-based sources the cursor of the next page.
public record Page<T>(List<T> items, long total, @Nullable String nextCursor) {

    /// The `total` of a source that doesn't count.
    public static final long UNKNOWN_TOTAL = -1;

    public Page {
        items = List.copyOf(items);
    }

    /// A page from a source that knows the total.
    public static <T> Page<T> of(List<T> items, long total) {
        return new Page<>(items, total, null);
    }

    /// A page from a cursor-based source: `nextCursor` is null on the last page.
    public static <T> Page<T> ofCursor(List<T> items, @Nullable String nextCursor) {
        return new Page<>(items, UNKNOWN_TOTAL, nextCursor);
    }

    public static <T> Page<T> empty() {
        return new Page<>(List.of(), 0, null);
    }

    /// True when the total is known.
    public boolean hasTotal() {
        return total >= 0;
    }

    /// True when there are items after this page (asked for with `request`).
    public boolean hasMore(PageRequest request) {
        if (nextCursor != null) {
            return true;
        }
        if (hasTotal()) {
            return request.offset() + items.size() < total;
        }
        return items.size() >= request.limit();
    }
}
