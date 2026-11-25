package io.joshuasalcedo.fx.ui.core.table;

import io.joshuasalcedo.fx.ui.core.data.SelectionState;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// The line under a table: how many rows there are ("1,240 rows", or "3 of 1,240 selected") on the left, and totals
/// on the right ("Total $12,400.00"). Everything updates as the rows change. When the table shows one page of a
/// server's rows, set `total` to the count of all of them.
///
/// ```java
/// new TableFooterMfx<>(table, selection)
///         .sum("Total", Invoice::amount, NumberFormat.getCurrencyInstance(Locale.US));
/// ```
@NullMarked
public class TableFooterMfx<T> extends HBox {

    public static final String STYLE_CLASS = "mfx-table-footer";

    private record Summary<T>(String title, Function<List<T>, String> compute, Label label) {
    }

    private final TableView<T> table;
    private final @Nullable SelectionState<T> selection;
    private final Label count = new Label();
    private final HBox totals = new HBox(16);
    private final List<Summary<T>> summaries = new ArrayList<>();
    private final LongProperty total = new SimpleLongProperty(this, "total", -1);
    private final NumberFormat integers = NumberFormat.getIntegerInstance(Locale.getDefault(Locale.Category.FORMAT));

    public TableFooterMfx(TableView<T> table) {
        this(table, null);
    }

    public TableFooterMfx(TableView<T> table, @Nullable SelectionState<T> selection) {
        this.table = Objects.requireNonNull(table, "table");
        this.selection = selection;
        getStyleClass().add(STYLE_CLASS);
        setAlignment(Pos.CENTER_LEFT);
        count.getStyleClass().add("mfx-table-footer-count");
        totals.setAlignment(Pos.CENTER_RIGHT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(count, spacer, totals);
        table.getItems().addListener((ListChangeListener<T>) c -> update());
        table.itemsProperty().addListener((obs, old, now) -> {
            now.addListener((ListChangeListener<T>) c -> update());
            update();
        });
        if (selection != null) {
            selection.keys().addListener((SetChangeListener<Object>) c -> update());
        }
        total.addListener(o -> update());
        update();
    }

    /// A total on the right: `title` and the sum of `value` over the rows.
    public TableFooterMfx<T> sum(String title, ToDoubleFunction<T> value, NumberFormat format) {
        NumberFormat f = (NumberFormat) format.clone();
        return summary(title, rows -> f.format(rows.stream().mapToDouble(value).sum()));
    }

    /// A figure on the right: `title` and what `compute` makes of the rows.
    public TableFooterMfx<T> summary(String title, Function<List<T>, String> compute) {
        Label label = new Label();
        label.getStyleClass().add("mfx-table-footer-total");
        summaries.add(new Summary<>(title, compute, label));
        totals.getChildren().add(label);
        update();
        return this;
    }

    /// The count of all the rows, when the table shows only some (−1: the table's rows are all of them).
    public final LongProperty totalProperty() { return total; }
    public final void setTotal(long value) { total.set(value); }

    private void update() {
        List<T> rows = table.getItems();
        long all = total.get() >= 0 ? total.get() : rows.size();
        int selected = selection == null ? 0 : selection.count();
        count.setText(selected > 0
                ? integers.format(selected) + " of " + integers.format(all) + " selected"
                : integers.format(all) + (all == 1 ? " row" : " rows"));
        for (Summary<T> s : summaries) {
            s.label().setText(s.title() + " " + s.compute().apply(rows));
        }
    }
}
