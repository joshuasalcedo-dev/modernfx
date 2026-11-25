package io.joshuasalcedo.fx.ui.core.table;

import io.joshuasalcedo.fx.ui.core.checkbox.CheckboxMfx;
import io.joshuasalcedo.fx.ui.core.data.SelectionState;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// The checkbox column of shadcn's data table: a checkbox on each row, and one in the header that selects or
/// clears all the rows (indeterminate when some are selected). The selection is a SelectionState, by key, so it
/// survives sorting, paging and reloading. It stays the first column.
///
/// ```java
/// var selection = new SelectionState<Invoice>(Invoice::id, SelectionMode.MULTIPLE);
/// table.getColumns().addFirst(new SelectionColumnMfx<>(selection));
/// ```
@NullMarked
public class SelectionColumnMfx<T> extends TableColumn<T, T> {

    public static final String STYLE_CLASS = "mfx-selection-column";

    private final SelectionState<T> selection;
    private final CheckboxMfx all = new CheckboxMfx();
    private final ListChangeListener<T> itemsChanged = c -> updateHeader();
    private boolean updating;

    public SelectionColumnMfx(SelectionState<T> selection) {
        this.selection = Objects.requireNonNull(selection, "selection");
        setId("mfx-select");
        getStyleClass().addAll(STYLE_CLASS, ColumnAlignment.CENTER.styleClass());
        setSortable(false);
        setReorderable(false);
        setResizable(false);
        ColumnWidth.fixed(44).apply(this, 44);
        all.setAccessibleText("Select all");
        setGraphic(all);
        all.setOnAction(e -> {
            List<T> items = items();
            if (selection.containsAll(items)) {
                selection.deselectAll(items);
            } else {
                selection.selectAll(items);
            }
        });
        setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        setCellFactory(c -> new Cell());
        selection.keys().addListener((SetChangeListener<Object>) c -> updateHeader());
        tableViewProperty().addListener((obs, old, now) -> {
            if (old != null) {
                old.getItems().removeListener(itemsChanged);
            }
            if (now != null) {
                now.getItems().addListener(itemsChanged);
            }
            updateHeader();
        });
    }

    public SelectionState<T> selection() {
        return selection;
    }

    private List<T> items() {
        TableView<T> table = getTableView();
        return table == null ? List.of() : table.getItems();
    }

    private void updateHeader() {
        if (updating) {
            return;
        }
        updating = true;
        ObservableList<T> items = getTableView() == null ? null : getTableView().getItems();
        List<T> rows = items == null ? List.of() : items;
        all.setIndeterminate(selection.containsSome(rows));
        all.setSelected(selection.containsAll(rows));
        updating = false;
    }

    private final class Cell extends TableCell<T, T> {

        private final CheckboxMfx box = new CheckboxMfx();
        // held here so the weak listener lives as long as the cell
        private final SetChangeListener<Object> keysChanged = c -> sync();

        Cell() {
            getStyleClass().add("mfx-selection-cell");
            box.setAccessibleText("Select row");
            box.setOnAction(e -> {
                T item = getItem();
                if (item != null) {
                    selection.set(item, box.isSelected());
                }
            });
            selection.keys().addListener(new WeakSetChangeListener<>(keysChanged));
        }

        @Override
        protected void updateItem(@Nullable T item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            setGraphic(empty || item == null ? null : box);
            sync();
        }

        private void sync() {
            T item = getItem();
            box.setSelected(item != null && !isEmpty() && selection.isSelected(item));
        }
    }
}
