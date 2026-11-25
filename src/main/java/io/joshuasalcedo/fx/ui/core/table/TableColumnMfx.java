package io.joshuasalcedo.fx.ui.core.table;

import io.joshuasalcedo.fx.ui.core.internal.StyleClasses;
import io.joshuasalcedo.fx.ui.core.table.renderer.CellRenderer;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A table column described in one place: its `key` (what layouts and sorting use; also its id), its title, how to
/// get its value from a row, how to show it (a CellRenderer), its width, alignment, whether it sorts, whether the
/// user can hide it, and which edge it is pinned to.
///
/// ```java
/// table.getColumns().addAll(
///         TableColumnMfx.of("customer", "Customer", Invoice::customer).renderer(new AvatarCellRenderer<>()),
///         TableColumnMfx.of("amount", "Amount", Invoice::amount)
///                 .renderer(new MoneyCellRenderer<>(Currency.getInstance("USD")))
///                 .width(ColumnWidth.fixed(120)));
/// ```
@NullMarked
public class TableColumnMfx<T, V> extends TableColumn<T, V> {

    public static final String STYLE_CLASS = "mfx-table-column";

    /// How many rows AUTO widths measure.
    private static final int MEASURED_ROWS = 200;

    private final Function<T, @Nullable V> value;
    private final ObjectProperty<@Nullable CellRenderer<T, V>> renderer = new SimpleObjectProperty<>(this, "renderer");
    private final ObjectProperty<@Nullable ColumnAlignment> alignment = new SimpleObjectProperty<>(this, "alignment");
    private final ObjectBinding<ColumnAlignment> shownAlignment;
    private final ObjectProperty<ColumnWidth> width = new SimpleObjectProperty<>(this, "width", ColumnWidth.flex(1)) {
        @Override
        protected void invalidated() {
            get().apply(TableColumnMfx.this, getPrefWidth());
        }
    };
    private final BooleanProperty hideable = new SimpleBooleanProperty(this, "hideable", true);
    private final ObjectProperty<ColumnPin> pin = new SimpleObjectProperty<>(this, "pin", ColumnPin.NONE);

    public TableColumnMfx(String key, String title, Function<T, @Nullable V> value) {
        super(title);
        setId(Objects.requireNonNull(key, "key"));
        this.value = Objects.requireNonNull(value, "value");
        getStyleClass().add(STYLE_CLASS);
        setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue() == null ? null : value.apply(c.getValue())));
        setCellFactory(c -> new TableCellMfx<>(renderer.get()));
        renderer.addListener(o -> setCellFactory(c -> new TableCellMfx<>(renderer.get())));
        shownAlignment = Bindings.createObjectBinding(() -> {
            ColumnAlignment a = alignment.get();
            CellRenderer<T, V> r = renderer.get();
            return a != null ? a : r != null ? r.alignment() : ColumnAlignment.LEFT;
        }, alignment, renderer);
        StyleClasses.bind(this, shownAlignment, ColumnAlignment::styleClass);
        width.get().apply(this, getPrefWidth());
    }

    /// A column showing `value` of each row.
    public static <T, V> TableColumnMfx<T, V> of(String key, String title, Function<T, @Nullable V> value) {
        return new TableColumnMfx<>(key, title, value);
    }

    /// The column's key (its id).
    public String key() {
        return Objects.requireNonNull(getId());
    }

    /// The value this column shows for a row.
    public @Nullable V valueOf(T row) {
        return value.apply(row);
    }

    public TableColumnMfx<T, V> renderer(@Nullable CellRenderer<T, V> r) {
        renderer.set(r);
        return this;
    }

    public TableColumnMfx<T, V> align(@Nullable ColumnAlignment a) {
        alignment.set(a);
        return this;
    }

    public TableColumnMfx<T, V> width(ColumnWidth w) {
        width.set(Objects.requireNonNull(w));
        return this;
    }

    public TableColumnMfx<T, V> sortable(boolean on) {
        setSortable(on);
        return this;
    }

    public TableColumnMfx<T, V> hideable(boolean on) {
        hideable.set(on);
        return this;
    }

    public TableColumnMfx<T, V> pin(ColumnPin p) {
        pin.set(Objects.requireNonNull(p));
        return this;
    }

    public final ObjectProperty<@Nullable CellRenderer<T, V>> rendererProperty() { return renderer; }

    /// The alignment, or null to use the renderer's.
    public final ObjectProperty<@Nullable ColumnAlignment> alignmentProperty() { return alignment; }

    /// The alignment the column shows: its own, else the renderer's, else LEFT.
    public final ColumnAlignment getShownAlignment() { return shownAlignment.get(); }

    public final ObjectProperty<ColumnWidth> widthSpecProperty() { return width; }
    public final ColumnWidth getWidthSpec() { return width.get(); }

    /// Whether the column menu lets the user hide this column.
    public final BooleanProperty hideableProperty() { return hideable; }
    public final boolean isHideable() { return hideable.get(); }

    public final ObjectProperty<ColumnPin> pinProperty() { return pin; }
    public final ColumnPin getPin() { return pin.get(); }

    /// For an AUTO width: makes the column as wide as its title and the values of the first rows.
    public void fitToContent(List<T> rows) {
        if (width.get().kind() != ColumnWidth.Kind.AUTO) {
            return;
        }
        Text measure = new Text(getText());
        measure.setFont(Font.font(null, FontWeight.MEDIUM, 14));
        double widest = measure.getLayoutBounds().getWidth();
        measure.setFont(Font.font(14));
        CellRenderer<T, V> r = renderer.get();
        for (int i = 0; i < Math.min(rows.size(), MEASURED_ROWS); i++) {
            V v = value.apply(rows.get(i));
            if (v != null) {
                measure.setText(r == null ? String.valueOf(v) : r.text(v));
                widest = Math.max(widest, measure.getLayoutBounds().getWidth());
            }
        }
        width.get().apply(this, Math.ceil(widest) + 24);
    }
}
