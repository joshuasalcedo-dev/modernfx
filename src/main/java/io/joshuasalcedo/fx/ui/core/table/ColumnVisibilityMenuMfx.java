package io.joshuasalcedo.fx.ui.core.table;

import io.joshuasalcedo.fx.ui.core.button.ButtonMfx;
import io.joshuasalcedo.fx.ui.core.button.ButtonSize;
import io.joshuasalcedo.fx.ui.core.checkbox.CheckboxMfx;
import io.joshuasalcedo.fx.ui.core.iconbutton.IconButtonMfx;
import io.joshuasalcedo.fx.ui.core.menu.ContextMenuMfx;
import io.joshuasalcedo.fx.ui.core.menu.MenuItemsMfx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.jspecify.annotations.NullMarked;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// The "Columns" button of shadcn's data table: a menu with a checkbox per column to show or hide it, arrows to move
/// it, and Reset to go back to how the table started. Columns that can't be hidden (`hideable(false)`, the selection
/// and actions columns) aren't in it.
///
/// ```java
/// var columns = new ColumnVisibilityMenuMfx(table);
/// ```
@NullMarked
public class ColumnVisibilityMenuMfx extends ButtonMfx {

    public static final String STYLE_CLASS = "mfx-column-menu";

    private final TableView<?> table;
    private final List<TableColumn<?, ?>> startOrder = new ArrayList<>();
    private final Map<TableColumn<?, ?>, Boolean> startVisible = new HashMap<>();

    public ColumnVisibilityMenuMfx(TableView<?> table) {
        super("Columns", new FontIcon(Feather.CHEVRON_DOWN));
        this.table = Objects.requireNonNull(table, "table");
        getStyleClass().add(STYLE_CLASS);
        variant(Variant.OUTLINE).size(Size.SM);
        setContentDisplay(ContentDisplay.RIGHT);
        setOnAction(e -> open());
    }

    /// True for the columns the menu lists.
    public static boolean listed(TableColumn<?, ?> column) {
        if (column instanceof SelectionColumnMfx<?> || column instanceof RowActionsColumnMfx<?>) {
            return false;
        }
        if (column instanceof TableColumnMfx<?, ?> c) {
            return c.isHideable();
        }
        return column.getText() != null && !column.getText().isBlank();
    }

    private void open() {
        if (startOrder.isEmpty()) {
            startOrder.addAll(table.getColumns());
            for (TableColumn<?, ?> c : startOrder) {
                startVisible.put(c, c.isVisible());
            }
        }
        ContextMenu menu = new ContextMenuMfx();
        menu.getItems().add(MenuItemsMfx.label("Columns"));
        for (TableColumn<?, ?> column : table.getColumns()) {
            if (listed(column)) {
                menu.getItems().add(row(column, menu));
            }
        }
        menu.getItems().addAll(MenuItemsMfx.separator(), MenuItemsMfx.item("Reset", null, null, this::reset));
        menu.show(this, Side.BOTTOM, 0, 4);
    }

    private CustomMenuItem row(TableColumn<?, ?> column, ContextMenu menu) {
        CheckboxMfx shown = new CheckboxMfx();
        shown.setText(column.getText());
        shown.setSelected(column.isVisible());
        shown.selectedProperty().addListener((obs, old, on) -> column.setVisible(on));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        IconButtonMfx up = new IconButtonMfx(new FontIcon(Feather.ARROW_UP), "Move up").size(ButtonSize.SM);
        IconButtonMfx down = new IconButtonMfx(new FontIcon(Feather.ARROW_DOWN), "Move down").size(ButtonSize.SM);
        up.setOnAction(e -> {
            move(column, -1);
            menu.hide();
            open();
        });
        down.setOnAction(e -> {
            move(column, 1);
            menu.hide();
            open();
        });
        HBox box = new HBox(4, shown, spacer, up, down);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinWidth(220);
        CustomMenuItem item = new CustomMenuItem(box, false);
        item.getStyleClass().add("mfx-column-menu-item");
        return item;
    }

    /// Moves a column one place left (-1) or right (1) among the listed columns.
    private void move(TableColumn<?, ?> column, int by) {
        @SuppressWarnings("unchecked")
        ObservableList<TableColumn<?, ?>> columns = (ObservableList<TableColumn<?, ?>>) (ObservableList<?>) table.getColumns();
        int at = columns.indexOf(column);
        int to = at + by;
        while (to >= 0 && to < columns.size() && !listed(columns.get(to))) {
            to += by;
        }
        if (at < 0 || to < 0 || to >= columns.size()) {
            return;
        }
        columns.remove(at);
        columns.add(to, column);
    }

    private void reset() {
        @SuppressWarnings("unchecked")
        ObservableList<TableColumn<?, ?>> columns = (ObservableList<TableColumn<?, ?>>) (ObservableList<?>) table.getColumns();
        List<TableColumn<?, ?>> now = new ArrayList<>(columns);
        List<TableColumn<?, ?>> order = new ArrayList<>(startOrder);
        order.retainAll(now);
        now.removeAll(order);
        order.addAll(now);
        columns.setAll(order);
        startVisible.forEach(TableColumn::setVisible);
    }
}
