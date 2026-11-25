package io.joshuasalcedo.fx.ui.core.data;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/// What a collection asks its DataSource for: which items (`offset` and `limit`, or the `cursor` the last page
/// gave), in what order (`sort`), and matching what (`filter`, the search text; empty for everything).
public record PageRequest(int offset, int limit, List<SortSpec> sort, String filter, @Nullable String cursor) {

    /// The page size collections use unless told otherwise.
    public static final int DEFAULT_LIMIT = 50;

    public PageRequest {
        if (offset < 0) {
            throw new IllegalArgumentException("offset < 0: " + offset);
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit <= 0: " + limit);
        }
        sort = List.copyOf(sort);
        Objects.requireNonNull(filter, "filter");
    }

    /// The first `limit` items, unsorted and unfiltered.
    public static PageRequest first(int limit) {
        return new PageRequest(0, limit, List.of(), "", null);
    }

    /// The page after this one. With a cursor-based source, pass the `nextCursor` of the page this one returned.
    public PageRequest next(@Nullable String nextCursor) {
        return new PageRequest(offset + limit, limit, sort, filter, nextCursor);
    }

    /// The page with this index (0 is the first), with the same sort and filter.
    public PageRequest page(int index) {
        return new PageRequest(index * limit, limit, sort, filter, null);
    }

    /// Sorted this way, from the first page again.
    public PageRequest withSort(List<SortSpec> value) {
        return new PageRequest(0, limit, value, filter, null);
    }

    /// Filtered by this text, from the first page again.
    public PageRequest withFilter(String value) {
        return new PageRequest(0, limit, sort, value.strip(), null);
    }

    /// The same request with another page size, from the first page again.
    public PageRequest withLimit(int value) {
        return new PageRequest(0, value, sort, filter, null);
    }

    /// The index of this page (0 is the first).
    public int pageIndex() {
        return offset / limit;
    }
}
