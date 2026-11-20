package io.joshuasalcedo.fx.ui.core.table;

import java.util.List;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// shadcn's Table (and the base of its Data Table): rows with a line under each, a muted header, a muted row
/// on hover and selection (JavaFX's TableView). Sorting, selection and resizing are TableView's.
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

    public TableMfx() {
        getStyleClass().add(STYLE_CLASS);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        setFixedCellSize(40);
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
}
