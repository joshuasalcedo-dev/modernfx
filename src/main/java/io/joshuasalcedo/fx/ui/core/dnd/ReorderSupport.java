package io.joshuasalcedo.fx.ui.core.dnd;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Adds drag-to-reorder to a ListView or a TableView: drag a row, a line shows where it will land (`:drop-above` or
/// `:drop-below` on the row under the pointer), and dropping moves the item in the items list and calls
/// `onReorder`. It keeps working when the list's cell factory (or the table's row factory) is replaced.
///
/// ```java
/// ReorderSupport.install(tasks, e -> api.moveTask(e.item().id(), e.to()));
/// ```
///
/// Sorted tables can't be reordered by hand: clear the sort order first.
@NullMarked
public final class ReorderSupport<T> {

    private static final PseudoClass DROP_ABOVE = PseudoClass.getPseudoClass("drop-above");
    private static final PseudoClass DROP_BELOW = PseudoClass.getPseudoClass("drop-below");
    private static final PseudoClass DRAGGING = PseudoClass.getPseudoClass("dragging");

    private final Supplier<ObservableList<T>> items;
    private final IntConsumer select;
    private final @Nullable Consumer<ReorderEvent<T>> onReorder;
    private final BooleanProperty handleOnly = new SimpleBooleanProperty(this, "handleOnly");
    private int dragging = -1;

    private ReorderSupport(Supplier<ObservableList<T>> items, IntConsumer select, @Nullable Consumer<ReorderEvent<T>> onReorder) {
        this.items = items;
        this.select = select;
        this.onReorder = onReorder;
    }

    /// Lets the rows of `list` be dragged to reorder them.
    public static <T> ReorderSupport<T> install(ListView<T> list, @Nullable Consumer<ReorderEvent<T>> onReorder) {
        ReorderSupport<T> support = new ReorderSupport<>(list::getItems, i -> list.getSelectionModel().clearAndSelect(i), onReorder);
        support.wrapCells(list);
        list.cellFactoryProperty().addListener((obs, old, now) -> {
            if (!(now instanceof ReorderSupport<?>.Wrapped)) {
                support.wrapCells(list);
            }
        });
        return support;
    }

    /// Lets the rows of `table` be dragged to reorder them.
    public static <T> ReorderSupport<T> install(TableView<T> table, @Nullable Consumer<ReorderEvent<T>> onReorder) {
        ReorderSupport<T> support = new ReorderSupport<>(table::getItems, i -> table.getSelectionModel().clearAndSelect(i), onReorder);
        support.wrapRows(table);
        table.rowFactoryProperty().addListener((obs, old, now) -> {
            if (!(now instanceof ReorderSupport<?>.WrappedRows)) {
                support.wrapRows(table);
            }
        });
        return support;
    }

    /// When true, rows only start dragging from a DragHandleMfx inside them.
    public BooleanProperty handleOnlyProperty() {
        return handleOnly;
    }

    public ReorderSupport<T> handleOnly(boolean value) {
        handleOnly.set(value);
        return this;
    }

    /// Moves the item at `from` to `to` (an index in the list before the move: a drop above row `to`), as a drop
    /// does. Returns the event, or null if nothing moved.
    public @Nullable ReorderEvent<T> move(int from, int to) {
        ObservableList<T> list = items.get();
        if (from < 0 || from >= list.size()) {
            return null;
        }
        int target = Math.clamp(to, 0, list.size());
        if (from < target) {
            target--;
        }
        if (target == from) {
            return null;
        }
        T item = list.remove(from);
        list.add(target, item);
        select.accept(target);
        ReorderEvent<T> event = new ReorderEvent<>(from, target, item);
        if (onReorder != null) {
            onReorder.accept(event);
        }
        return event;
    }

    private final class Wrapped implements Callback<ListView<T>, ListCell<T>> {
        private final @Nullable Callback<ListView<T>, ListCell<T>> inner;

        Wrapped(@Nullable Callback<ListView<T>, ListCell<T>> inner) {
            this.inner = inner;
        }

        @Override
        public ListCell<T> call(ListView<T> view) {
            ListCell<T> cell = inner == null ? textCell() : inner.call(view);
            decorate(cell);
            return cell;
        }
    }

    private final class WrappedRows implements Callback<TableView<T>, TableRow<T>> {
        private final @Nullable Callback<TableView<T>, TableRow<T>> inner;

        WrappedRows(@Nullable Callback<TableView<T>, TableRow<T>> inner) {
            this.inner = inner;
        }

        @Override
        public TableRow<T> call(TableView<T> view) {
            TableRow<T> row = inner == null ? new TableRow<>() : inner.call(view);
            decorate(row);
            return row;
        }
    }

    private void wrapCells(ListView<T> list) {
        list.setCellFactory(new Wrapped(list.getCellFactory()));
    }

    private void wrapRows(TableView<T> table) {
        table.setRowFactory(new WrappedRows(table.getRowFactory()));
    }

    private static <T> ListCell<T> textCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(@Nullable T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        };
    }

    private void decorate(IndexedCell<T> cell) {
        cell.addEventHandler(MouseEvent.DRAG_DETECTED, e -> {
            if (cell.isEmpty() || (handleOnly.get() && !fromHandle(e, cell))) {
                return;
            }
            dragging = cell.getIndex();
            Dragboard board = cell.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(dragging));
            board.setContent(content);
            board.setDragView(cell.snapshot(null, null));
            cell.pseudoClassStateChanged(DRAGGING, true);
            e.consume();
        });
        cell.addEventHandler(DragEvent.DRAG_OVER, e -> {
            if (dragging < 0) {
                return;
            }
            e.acceptTransferModes(TransferMode.MOVE);
            boolean below = cell.isEmpty() || e.getY() > cell.getHeight() / 2;
            cell.pseudoClassStateChanged(DROP_ABOVE, !cell.isEmpty() && !below);
            cell.pseudoClassStateChanged(DROP_BELOW, !cell.isEmpty() && below);
            e.consume();
        });
        cell.addEventHandler(DragEvent.DRAG_EXITED, e -> clearDrop(cell));
        cell.addEventHandler(DragEvent.DRAG_DROPPED, e -> {
            if (dragging < 0) {
                return;
            }
            boolean below = cell.isEmpty() || e.getY() > cell.getHeight() / 2;
            int to = cell.isEmpty() ? items.get().size() : cell.getIndex() + (below ? 1 : 0);
            move(dragging, to);
            clearDrop(cell);
            e.setDropCompleted(true);
            e.consume();
        });
        cell.addEventHandler(DragEvent.DRAG_DONE, e -> {
            dragging = -1;
            cell.pseudoClassStateChanged(DRAGGING, false);
        });
    }

    private static void clearDrop(IndexedCell<?> cell) {
        cell.pseudoClassStateChanged(DROP_ABOVE, false);
        cell.pseudoClassStateChanged(DROP_BELOW, false);
    }

    private static boolean fromHandle(MouseEvent e, Node cell) {
        Node n = e.getPickResult().getIntersectedNode();
        while (n != null && n != cell) {
            if (n instanceof DragHandleMfx) {
                return true;
            }
            n = n.getParent();
        }
        return false;
    }
}
