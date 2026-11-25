package io.joshuasalcedo.fx.ui.core.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jspecify.annotations.Nullable;

/// A DataSource over items in memory (an ObservableList), sorted, filtered and paged here. Each sort key is a
/// comparator given with [#sortBy]; the filter matches the search text with [#filterBy] (by default, the item's
/// `toString()` contains it, ignoring case).
///
/// ```java
/// var source = new ListDataSource<>(contacts)
///         .sortBy("name", Contact::name)
///         .filterBy((c, text) -> c.name().toLowerCase().contains(text.toLowerCase()));
/// ```
public class ListDataSource<T> implements DataSource<T> {

    private final ObservableList<T> items;
    private final Map<String, Comparator<? super T>> comparators = new LinkedHashMap<>();
    private BiPredicate<T, String> filter = (item, text) ->
            String.valueOf(item).toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT));

    /// An empty source: add to [#getItems].
    public ListDataSource() {
        this(FXCollections.<T>observableArrayList());
    }

    /// A source over this list. Changes to the list show the next time the collection loads.
    public ListDataSource(ObservableList<T> items) {
        this.items = Objects.requireNonNull(items, "items");
    }

    /// A source over a copy of these items.
    public ListDataSource(List<T> items) {
        this(FXCollections.observableArrayList(items));
    }

    /// The items, unsorted and unfiltered.
    public ObservableList<T> getItems() {
        return items;
    }

    /// Lets the collection sort by `key` with this comparator (ascending; descending reverses it).
    public ListDataSource<T> sortBy(String key, Comparator<? super T> comparator) {
        comparators.put(key, Objects.requireNonNull(comparator, "comparator"));
        return this;
    }

    /// Lets the collection sort by `key`, comparing what `value` returns. Nulls go last.
    public <V extends Comparable<? super V>> ListDataSource<T> sortBy(String key, Function<T, @Nullable V> value) {
        return sortBy(key, Comparator.comparing(value, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    /// How an item matches the search text (never called with an empty text).
    public ListDataSource<T> filterBy(BiPredicate<T, String> matches) {
        this.filter = Objects.requireNonNull(matches, "matches");
        return this;
    }

    /// The items matching the request's filter, in its order, all of them (no paging).
    public List<T> query(PageRequest request) {
        List<T> out = new ArrayList<>();
        String text = request.filter();
        for (T item : items) {
            if (text.isEmpty() || filter.test(item, text)) {
                out.add(item);
            }
        }
        Comparator<T> order = comparator(request.sort());
        if (order != null) {
            out.sort(order);
        }
        return out;
    }

    @Override
    public CompletableFuture<Page<T>> load(PageRequest request) {
        try {
            List<T> all = query(request);
            int from = Math.min(request.offset(), all.size());
            int to = Math.min(from + request.limit(), all.size());
            return CompletableFuture.completedFuture(Page.of(all.subList(from, to), all.size()));
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private @Nullable Comparator<T> comparator(List<SortSpec> sort) {
        Comparator<T> out = null;
        for (SortSpec spec : sort) {
            if (spec.direction() == SortDirection.NONE) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Comparator<T> c = (Comparator<T>) comparators.get(spec.key());
            if (c == null) {
                throw new IllegalArgumentException("no sort key " + spec.key() + "; add it with sortBy");
            }
            if (spec.direction() == SortDirection.DESCENDING) {
                c = c.reversed();
            }
            out = out == null ? c : out.thenComparing(c);
        }
        return out;
    }
}
