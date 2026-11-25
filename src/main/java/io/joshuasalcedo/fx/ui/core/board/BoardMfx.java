package io.joshuasalcedo.fx.ui.core.board;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A Kanban board: columns side by side (scrolling sideways when they don't fit), each a list of cards. Drag a card
/// to another place in its column or to another column: the board moves it and tells `onMove`, where the app saves
/// it. With `onAdd`, each column has a + button that passes the column's key.
///
/// ```java
/// var board = new BoardMfx<Task>(t -> new Label(t.title()));
/// board.addColumn(BoardColumnSpec.of("todo", "To do"));
/// board.addColumn(BoardColumnSpec.of("doing", "Doing").withWipLimit(3));
/// board.addColumn(BoardColumnSpec.of("done", "Done"));
/// board.setItems(tasks, Task::status);
/// board.setOnMove(m -> api.moveTask(m.item().id(), m.toColumn(), m.toIndex()));
/// ```
@NullMarked
public class BoardMfx<T> extends ScrollPane {

    public static final String STYLE_CLASS = "mfx-board";

    private final HBox row = new HBox();
    private final ObservableList<BoardColumnMfx<T>> columns = FXCollections.observableArrayList();
    private final ObjectProperty<Function<T, Node>> cardFactory;
    private final ObjectProperty<@Nullable Consumer<BoardMove<T>>> onMove = new SimpleObjectProperty<>(this, "onMove");
    private final ObjectProperty<@Nullable Consumer<String>> onAdd = new SimpleObjectProperty<>(this, "onAdd");
    private @Nullable T dragged;

    /// A board whose cards show the item's `toString()`.
    public BoardMfx() {
        this(item -> new Label(String.valueOf(item)));
    }

    public BoardMfx(Function<T, Node> card) {
        cardFactory = new SimpleObjectProperty<>(this, "cardFactory", Objects.requireNonNull(card, "card"));
        getStyleClass().addAll(STYLE_CLASS, "mfx-scroll-area");
        row.getStyleClass().add("mfx-board-columns");
        setContent(row);
        setFitToHeight(true);
        setVbarPolicy(ScrollBarPolicy.NEVER);
        cardFactory.addListener(o -> columns.forEach(BoardColumnMfx::refresh));
    }

    /// Adds a column at the right and returns it.
    public BoardColumnMfx<T> addColumn(BoardColumnSpec spec) {
        if (column(spec.key()) != null) {
            throw new IllegalArgumentException("there is a column " + spec.key() + " already");
        }
        BoardColumnMfx<T> column = new BoardColumnMfx<>(this, spec);
        columns.add(column);
        row.getChildren().add(column);
        return column;
    }

    /// Removes a column and its cards.
    public void removeColumn(String key) {
        BoardColumnMfx<T> column = column(key);
        if (column != null) {
            columns.remove(column);
            row.getChildren().remove(column);
        }
    }

    /// The columns, left to right.
    public List<BoardColumnMfx<T>> getColumns() {
        return FXCollections.unmodifiableObservableList(columns);
    }

    /// The column with this key, or null.
    public @Nullable BoardColumnMfx<T> column(String key) {
        for (BoardColumnMfx<T> c : columns) {
            if (c.key().equals(key)) {
                return c;
            }
        }
        return null;
    }

    /// Puts each item in the column `columnOf` gives, in order; items whose column isn't there are left out.
    public void setItems(Collection<? extends T> items, Function<T, String> columnOf) {
        for (BoardColumnMfx<T> c : columns) {
            c.getItems().clear();
        }
        for (T item : items) {
            BoardColumnMfx<T> c = column(columnOf.apply(item));
            if (c != null) {
                c.getItems().add(item);
            }
        }
    }

    /// Adds an item at the bottom of a column.
    public void add(String columnKey, T item) {
        BoardColumnMfx<T> c = Objects.requireNonNull(column(columnKey), () -> "no column " + columnKey);
        c.getItems().add(item);
    }

    /// Moves an item to `toIndex` in column `toColumn` (an index before the move) and tells `onMove`, as a drag does.
    /// Returns false if the item or the column isn't on the board.
    public boolean move(T item, String toColumn, int toIndex) {
        BoardColumnMfx<T> target = column(toColumn);
        return target != null && drop(item, target, toIndex);
    }

    /// Makes the content of a card. Changing it remakes the cards.
    public final ObjectProperty<Function<T, Node>> cardFactoryProperty() { return cardFactory; }

    /// Runs after a card is moved, with where it was and where it is now.
    public final ObjectProperty<@Nullable Consumer<BoardMove<T>>> onMoveProperty() { return onMove; }
    public final void setOnMove(@Nullable Consumer<BoardMove<T>> value) { onMove.set(value); }

    /// Runs when a column's + button is clicked, with the column's key. Without it there are no + buttons.
    public final ObjectProperty<@Nullable Consumer<String>> onAddProperty() { return onAdd; }
    public final void setOnAdd(@Nullable Consumer<String> value) { onAdd.set(value); }

    Function<T, Node> cardFactory() {
        return cardFactory.get();
    }

    @Nullable T dragged() {
        return dragged;
    }

    void dragStarted(T item) {
        dragged = item;
    }

    void dragEnded() {
        dragged = null;
    }

    boolean drop(T item, BoardColumnMfx<T> target, int index) {
        BoardColumnMfx<T> source = null;
        for (BoardColumnMfx<T> c : columns) {
            if (c.getItems().contains(item)) {
                source = c;
                break;
            }
        }
        if (source == null) {
            return false;
        }
        int from = source.getItems().indexOf(item);
        int to = Math.clamp(index, 0, target.getItems().size());
        if (source == target) {
            if (from < to) {
                to--;
            }
            if (from == to) {
                return true;
            }
        }
        source.getItems().remove(from);
        target.getItems().add(to, item);
        Consumer<BoardMove<T>> listener = onMove.get();
        if (listener != null) {
            listener.accept(new BoardMove<>(item, source.key(), from, target.key(), to));
        }
        return true;
    }
}
