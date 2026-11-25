package io.joshuasalcedo.fx.ui.core.table;

import io.joshuasalcedo.fx.ui.core.data.SortDirection;
import io.joshuasalcedo.fx.ui.core.data.SortSpec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jspecify.annotations.Nullable;

/// How the user left a table: the column order, widths, hidden columns and sort, by column key (the column's id,
/// or its title when it has none). It's a plain record, so a PersistentStore can keep it between runs.
///
/// ```java
/// var saved = TableLayoutState.capture(table);   // when the window closes
/// saved.apply(table);                            // when it opens again
/// ```
public record TableLayoutState(List<String> order, Map<String, Double> widths, Set<String> hidden, List<SortSpec> sort) {

    public TableLayoutState {
        order = List.copyOf(order);
        widths = Map.copyOf(widths);
        hidden = Set.copyOf(hidden);
        sort = List.copyOf(sort);
    }

    /// The layout of `table` now.
    public static TableLayoutState capture(TableView<?> table) {
        List<String> order = new ArrayList<>();
        Map<String, Double> widths = new LinkedHashMap<>();
        Set<String> hidden = new HashSet<>();
        for (TableColumn<?, ?> c : table.getColumns()) {
            String key = key(c);
            if (key == null) {
                continue;
            }
            order.add(key);
            widths.put(key, c.getWidth() > 0 ? c.getWidth() : c.getPrefWidth());
            if (!c.isVisible()) {
                hidden.add(key);
            }
        }
        List<SortSpec> sort = new ArrayList<>();
        for (TableColumn<?, ?> c : table.getSortOrder()) {
            String key = key(c);
            if (key != null) {
                sort.add(new SortSpec(key, c.getSortType() == TableColumn.SortType.ASCENDING
                        ? SortDirection.ASCENDING : SortDirection.DESCENDING));
            }
        }
        return new TableLayoutState(order, widths, hidden, sort);
    }

    /// Puts `table` back in this layout. Columns it doesn't know keep their place after the known ones.
    public <T> void apply(TableView<T> table) {
        ObservableList<TableColumn<T, ?>> columns = table.getColumns();
        Map<String, TableColumn<T, ?>> byKey = new LinkedHashMap<>();
        for (TableColumn<T, ?> c : columns) {
            String key = key(c);
            if (key != null) {
                byKey.put(key, c);
            }
        }
        List<TableColumn<T, ?>> arranged = new ArrayList<>();
        for (String key : order) {
            TableColumn<T, ?> c = byKey.get(key);
            if (c != null) {
                arranged.add(c);
            }
        }
        for (TableColumn<T, ?> c : columns) {
            if (!arranged.contains(c)) {
                arranged.add(c);
            }
        }
        columns.setAll(arranged);
        for (TableColumn<T, ?> c : columns) {
            String key = key(c);
            if (key == null) {
                continue;
            }
            Double w = widths.get(key);
            if (w != null && c.isResizable()) {
                c.setPrefWidth(w);
            }
            c.setVisible(!hidden.contains(key));
        }
        List<TableColumn<T, ?>> sorted = new ArrayList<>();
        for (SortSpec s : sort) {
            TableColumn<T, ?> c = byKey.get(s.key());
            if (c != null && s.direction() != SortDirection.NONE) {
                c.setSortType(s.direction() == SortDirection.ASCENDING
                        ? TableColumn.SortType.ASCENDING : TableColumn.SortType.DESCENDING);
                sorted.add(c);
            }
        }
        table.getSortOrder().setAll(sorted);
    }

    private static @Nullable String key(TableColumn<?, ?> c) {
        if (c.getId() != null && !c.getId().isBlank()) {
            return c.getId();
        }
        return c.getText() == null || c.getText().isBlank() ? null : c.getText();
    }
}
