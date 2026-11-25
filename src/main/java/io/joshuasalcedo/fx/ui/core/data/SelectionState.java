package io.joshuasalcedo.fx.ui.core.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

/// What is selected in a collection, by key, so the selection survives paging and reloading: an item selected on
/// page 1 is still selected when the user comes back to it, even as a new object.
///
/// ```java
/// var selection = new SelectionState<Contact>(Contact::id, SelectionMode.MULTIPLE);
/// selection.countProperty().subscribe(n -> System.out.println(n + " selected"));
/// ```
public class SelectionState<T> {

    private final KeyExtractor<T> key;
    private final SelectionMode mode;
    private final ObservableSet<Object> keys = FXCollections.observableSet();
    private final ObservableSet<Object> readOnlyKeys = FXCollections.unmodifiableObservableSet(keys);
    private final IntegerBinding count = Bindings.size(keys);
    private final BooleanBinding empty = count.isEqualTo(0);

    /// A multiple selection keyed by the items themselves.
    public SelectionState() {
        this(KeyExtractor.identity(), SelectionMode.MULTIPLE);
    }

    public SelectionState(KeyExtractor<T> key, SelectionMode mode) {
        this.key = Objects.requireNonNull(key, "key");
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    public SelectionMode mode() {
        return mode;
    }

    public KeyExtractor<T> keyExtractor() {
        return key;
    }

    /// The selected keys: save them, or listen to them. Change them through the methods here.
    public ObservableSet<Object> keys() {
        return readOnlyKeys;
    }

    /// How many are selected.
    public IntegerBinding countProperty() {
        return count;
    }

    public int count() {
        return count.get();
    }

    /// True when nothing is selected.
    public BooleanBinding emptyProperty() {
        return empty;
    }

    public boolean isSelected(T item) {
        return keys.contains(key.key(item));
    }

    /// Selects the item. In SINGLE mode it replaces what was selected; in NONE mode nothing happens.
    public void select(T item) {
        switch (mode) {
            case NONE -> { }
            case SINGLE -> {
                Object k = key.key(item);
                if (!(keys.size() == 1 && keys.contains(k))) {
                    keys.clear();
                    keys.add(k);
                }
            }
            case MULTIPLE -> keys.add(key.key(item));
        }
    }

    public void deselect(T item) {
        keys.remove(key.key(item));
    }

    /// Selects the item if it isn't, deselects it if it is.
    public void toggle(T item) {
        if (isSelected(item)) {
            deselect(item);
        } else {
            select(item);
        }
    }

    /// Sets whether the item is selected.
    public void set(T item, boolean selected) {
        if (selected) {
            select(item);
        } else {
            deselect(item);
        }
    }

    /// Selects all these items (MULTIPLE mode; in SINGLE mode, the last one).
    public void selectAll(Collection<? extends T> items) {
        if (mode == SelectionMode.MULTIPLE) {
            for (T item : items) {
                keys.add(key.key(item));
            }
        } else if (mode == SelectionMode.SINGLE && !items.isEmpty()) {
            select(List.<T>copyOf(items).getLast());
        }
    }

    /// Deselects all these items, leaving the others.
    public void deselectAll(Collection<? extends T> items) {
        for (T item : items) {
            keys.remove(key.key(item));
        }
    }

    /// Selects the items from index `from` to `to` (both included, either order), as shift-click does.
    public void selectRange(List<? extends T> items, int from, int to) {
        int lo = Math.max(0, Math.min(from, to));
        int hi = Math.min(items.size() - 1, Math.max(from, to));
        if (lo > hi) {
            return;
        }
        selectAll(items.subList(lo, hi + 1));
    }

    /// True when every one of these items is selected (false when there are none).
    public boolean containsAll(Collection<? extends T> items) {
        if (items.isEmpty()) {
            return false;
        }
        for (T item : items) {
            if (!isSelected(item)) {
                return false;
            }
        }
        return true;
    }

    /// True when some, but not all, of these items are selected: the select-all checkbox's indeterminate state.
    public boolean containsSome(Collection<? extends T> items) {
        boolean any = false;
        boolean all = true;
        for (T item : items) {
            if (isSelected(item)) {
                any = true;
            } else {
                all = false;
            }
        }
        return any && !all;
    }

    /// The selected ones among these items, in their order.
    public List<T> selectedIn(Collection<? extends T> items) {
        List<T> out = new ArrayList<>();
        for (T item : items) {
            if (isSelected(item)) {
                out.add(item);
            }
        }
        return out;
    }

    /// Deselects everything.
    public void clear() {
        keys.clear();
    }

    /// Selects exactly these keys (a saved selection).
    public void restore(Collection<?> saved) {
        keys.clear();
        if (mode == SelectionMode.MULTIPLE) {
            keys.addAll(saved);
        } else if (mode == SelectionMode.SINGLE && !saved.isEmpty()) {
            keys.add(saved.iterator().next());
        }
    }
}
