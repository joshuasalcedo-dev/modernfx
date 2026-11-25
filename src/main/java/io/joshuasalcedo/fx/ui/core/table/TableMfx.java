package io.joshuasalcedo.fx.ui.core.table;

import io.joshuasalcedo.fx.ui.core.data.Density;
import io.joshuasalcedo.fx.ui.core.internal.StyleClasses;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// shadcn's Table (and the base of its Data Table): rows with a line under each, a muted header, a muted row
/// on hover and selection (JavaFX's TableView). Sorting, selection and resizing are TableView's.
///
/// With TableColumnMfx columns it also keeps pinned columns at their edge (a SelectionColumnMfx first, a
/// RowActionsColumnMfx last), sizes AUTO columns to their content when the rows change, and takes its row height
/// from its Density.
///
/// ```java
/// var invoices = new TableMfx<Invoice>()
///         .column("Invoice", Invoice::id)
///         .column("Status", Invoice::status)
///         .column("Amount", Invoice::amount);
/// invoices.getItems().setAll(list);
/// ```
@NullMarked
public class TableMfx<T> extends TableView<T> {

    /// The style class every TableMfx has.
    public static final String STYLE_CLASS = "mfx-table";

    private final ObjectProperty<Density> density = new SimpleObjectProperty<>(this, "density", Density.COMFORTABLE) {
        @Override
        protected void invalidated() {
            setFixedCellSize(get().rowHeight());
        }
    };
    private final InvalidationListener pinChanged = o -> arrange();
    private boolean arranging;

    public TableMfx() {
        getStyleClass().add(STYLE_CLASS);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        setFixedCellSize(density.get().rowHeight());
        StyleClasses.bind(this, density, Density::styleClass);
        getColumns().addListener((ListChangeListener<TableColumn<T, ?>>) change -> {
            while (change.next()) {
                for (TableColumn<T, ?> c : change.getRemoved()) {
                    if (c instanceof TableColumnMfx<?, ?> mfx) {
                        mfx.pinProperty().removeListener(pinChanged);
                    }
                }
                for (TableColumn<T, ?> c : change.getAddedSubList()) {
                    if (c instanceof TableColumnMfx<?, ?> mfx) {
                        mfx.pinProperty().addListener(pinChanged);
                        fit(mfx);
                    }
                }
            }
            arrange();
        });
        getItems().addListener((ListChangeListener<T>) c -> fitAll());
        itemsProperty().addListener((obs, old, now) -> {
            now.addListener((ListChangeListener<T>) c -> fitAll());
            fitAll();
        });
    }

    public TableMfx(List<T> items) {
        this();
        getItems().setAll(items);
    }

    /// Adds a column showing `value` of each row.
    public <V> TableMfx<T> column(String title, Function<T, @Nullable V> value) {
        TableColumn<T, V> c = new TableColumn<>(title);
        c.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(value.apply(cell.getValue())));
        getColumns().add(c);
        return this;
    }

    /// Adds these columns.
    @SafeVarargs
    public final TableMfx<T> columns(TableColumn<T, ?>... columns) {
        getColumns().addAll(columns);
        return this;
    }

    /// How tall the rows are.
    public final ObjectProperty<Density> densityProperty() { return density; }
    public final Density getDensity() { return density.get(); }
    public final void setDensity(Density value) { density.set(Objects.requireNonNull(value)); }

    /// The edge a column is pinned to.
    public static ColumnPin pinOf(TableColumn<?, ?> column) {
        return switch (column) {
            case SelectionColumnMfx<?> s -> ColumnPin.LEFT;
            case RowActionsColumnMfx<?> a -> ColumnPin.RIGHT;
            case TableColumnMfx<?, ?> c -> c.getPin();
            default -> ColumnPin.NONE;
        };
    }

    /// Puts the LEFT-pinned columns first and the RIGHT-pinned last, keeping the order within each.
    private void arrange() {
        if (arranging) {
            return;
        }
        List<TableColumn<T, ?>> left = new ArrayList<>();
        List<TableColumn<T, ?>> middle = new ArrayList<>();
        List<TableColumn<T, ?>> right = new ArrayList<>();
        for (TableColumn<T, ?> c : getColumns()) {
            switch (pinOf(c)) {
                case LEFT -> left.add(c);
                case RIGHT -> right.add(c);
                case NONE -> middle.add(c);
            }
        }
        left.addAll(middle);
        left.addAll(right);
        if (!left.equals(getColumns())) {
            arranging = true;
            try {
                getColumns().setAll(left);
            } finally {
                arranging = false;
            }
        }
    }

    private void fitAll() {
        for (TableColumn<T, ?> c : getColumns()) {
            if (c instanceof TableColumnMfx<?, ?> mfx) {
                fit(mfx);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void fit(TableColumnMfx<?, ?> column) {
        ((TableColumnMfx<T, ?>) column).fitToContent(getItems());
    }
}
