package io.joshuasalcedo.fx.ui.core.treetable;

import io.joshuasalcedo.fx.ui.core.internal.StyleClasses;
import io.joshuasalcedo.fx.ui.core.table.ColumnAlignment;
import io.joshuasalcedo.fx.ui.core.table.ColumnWidth;
import io.joshuasalcedo.fx.ui.core.table.renderer.CellRenderer;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A column of a TreeTableMfx, described as a TableColumnMfx is: key, title, value, CellRenderer, width and
/// alignment. The same renderers work in both.
///
/// ```java
/// TreeTableColumnMfx.of("size", "Size", File::length).renderer(NumberCellRenderer.decimals(0));
/// ```
@NullMarked
public class TreeTableColumnMfx<T, V> extends TreeTableColumn<T, V> {

    public static final String STYLE_CLASS = "mfx-tree-table-column";

    private final Function<T, @Nullable V> value;
    private final ObjectProperty<@Nullable CellRenderer<T, V>> renderer = new SimpleObjectProperty<>(this, "renderer");
    private final ObjectProperty<@Nullable ColumnAlignment> alignment = new SimpleObjectProperty<>(this, "alignment");
    private final ObjectBinding<ColumnAlignment> shownAlignment;

    public TreeTableColumnMfx(String key, String title, Function<T, @Nullable V> value) {
        super(title);
        setId(Objects.requireNonNull(key, "key"));
        this.value = Objects.requireNonNull(value, "value");
        getStyleClass().add(STYLE_CLASS);
        setCellValueFactory(c -> {
            T row = c.getValue() == null ? null : c.getValue().getValue();
            return new ReadOnlyObjectWrapper<>(row == null ? null : value.apply(row));
        });
        setCellFactory(c -> new Cell<>(renderer.get()));
        renderer.addListener(o -> setCellFactory(c -> new Cell<>(renderer.get())));
        shownAlignment = Bindings.createObjectBinding(() -> {
            ColumnAlignment a = alignment.get();
            CellRenderer<T, V> r = renderer.get();
            return a != null ? a : r != null ? r.alignment() : ColumnAlignment.LEFT;
        }, alignment, renderer);
        StyleClasses.bind(this, shownAlignment, ColumnAlignment::styleClass);
        ColumnWidth.flex(1).apply(this, 0);
    }

    public static <T, V> TreeTableColumnMfx<T, V> of(String key, String title, Function<T, @Nullable V> value) {
        return new TreeTableColumnMfx<>(key, title, value);
    }

    public String key() {
        return Objects.requireNonNull(getId());
    }

    public TreeTableColumnMfx<T, V> renderer(@Nullable CellRenderer<T, V> r) {
        renderer.set(r);
        return this;
    }

    public TreeTableColumnMfx<T, V> align(@Nullable ColumnAlignment a) {
        alignment.set(a);
        return this;
    }

    public TreeTableColumnMfx<T, V> width(ColumnWidth w) {
        w.apply(this, getPrefWidth());
        return this;
    }

    public final ObjectProperty<@Nullable CellRenderer<T, V>> rendererProperty() { return renderer; }
    public final ObjectProperty<@Nullable ColumnAlignment> alignmentProperty() { return alignment; }

    /// The value this column shows for a row.
    public @Nullable V valueOf(T row) {
        return value.apply(row);
    }

    /// A cell showing the value through the renderer, text ending in an ellipsis.
    private static final class Cell<T, V> extends TreeTableCell<T, V> {

        private final @Nullable CellRenderer<T, V> renderer;

        Cell(@Nullable CellRenderer<T, V> renderer) {
            this.renderer = renderer;
            getStyleClass().add("mfx-tree-table-cell");
            setTextOverrun(OverrunStyle.ELLIPSIS);
        }

        @Override
        protected void updateItem(@Nullable V v, boolean empty) {
            super.updateItem(v, empty);
            if (empty || v == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            TreeTableRow<T> row = getTableRow();
            T item = row == null ? null : row.getItem();
            Node node = renderer == null || item == null ? null : renderer.render(item, v);
            if (node == null) {
                setGraphic(null);
                setText(renderer == null ? String.valueOf(v) : renderer.text(v));
            } else {
                setText(null);
                setGraphic(node);
            }
        }
    }
}
