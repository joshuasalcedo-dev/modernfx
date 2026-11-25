package io.joshuasalcedo.fx.ui.core.data;

import java.util.Objects;

/// One sorted column: its key and direction. A list of them is a multi-column sort, the first one first.
public record SortSpec(String key, SortDirection direction) {

    public SortSpec {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(direction, "direction");
    }

    public static SortSpec ascending(String key) {
        return new SortSpec(key, SortDirection.ASCENDING);
    }

    public static SortSpec descending(String key) {
        return new SortSpec(key, SortDirection.DESCENDING);
    }
}
