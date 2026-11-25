package io.joshuasalcedo.fx.ui.core.table;

import io.joshuasalcedo.fx.ui.core.button.ButtonSize;
import io.joshuasalcedo.fx.ui.core.iconbutton.IconButtonMfx;
import io.joshuasalcedo.fx.ui.core.menu.ContextMenuMfx;
import io.joshuasalcedo.fx.ui.core.menu.MenuItemsMfx;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// The last column of a table: icon buttons for each row (`inline`), and a "⋯" button that opens a menu with the
/// rest (`menu`, `dangerMenu`). Each action gets the row's item. It stays the last column.
///
/// ```java
/// table.getColumns().add(new RowActionsColumnMfx<Invoice>()
///         .inline(Feather.EDIT_2, "Edit", this::edit)
///         .menu("Duplicate", this::duplicate)
///         .dangerMenu("Delete", this::delete));
/// ```
@NullMarked
public class RowActionsColumnMfx<T> extends TableColumn<T, T> {

    public static final String STYLE_CLASS = "mfx-row-actions-column";

    private record Action<T>(@Nullable Ikon icon, String label, Consumer<T> run, boolean danger) {
    }

    private final List<Action<T>> inline = new ArrayList<>();
    private final List<Action<T>> menu = new ArrayList<>();

    public RowActionsColumnMfx() {
        setId("mfx-actions");
        getStyleClass().addAll(STYLE_CLASS, ColumnAlignment.RIGHT.styleClass());
        setSortable(false);
        setReorderable(false);
        setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue()));
        setCellFactory(c -> new Cell());
        updateWidth();
    }

    /// An icon button on each row.
    public RowActionsColumnMfx<T> inline(Ikon icon, String label, Consumer<T> run) {
        inline.add(new Action<>(Objects.requireNonNull(icon), label, run, false));
        updateWidth();
        return this;
    }

    /// An item in the row's "⋯" menu.
    public RowActionsColumnMfx<T> menu(String label, Consumer<T> run) {
        menu.add(new Action<>(null, label, run, false));
        updateWidth();
        return this;
    }

    /// A red item in the row's "⋯" menu, for actions such as Delete.
    public RowActionsColumnMfx<T> dangerMenu(String label, Consumer<T> run) {
        menu.add(new Action<>(null, label, run, true));
        updateWidth();
        return this;
    }

    private void updateWidth() {
        int buttons = inline.size() + (menu.isEmpty() ? 0 : 1);
        double w = Math.max(48, 16 + buttons * 32 + Math.max(0, buttons - 1) * 4);
        ColumnWidth.fixed(w).apply(this, w);
    }

    private final class Cell extends TableCell<T, T> {

        private final HBox box = new HBox(4);

        Cell() {
            getStyleClass().add("mfx-row-actions-cell");
            box.setAlignment(Pos.CENTER_RIGHT);
            for (Action<T> a : inline) {
                IconButtonMfx b = new IconButtonMfx(new FontIcon(a.icon()), a.label()).size(ButtonSize.SM);
                b.setOnAction(e -> run(a));
                box.getChildren().add(b);
            }
            if (!menu.isEmpty()) {
                IconButtonMfx more = new IconButtonMfx(new FontIcon(Feather.MORE_HORIZONTAL), "More actions").size(ButtonSize.SM);
                more.setOnAction(e -> {
                    ContextMenu popup = new ContextMenuMfx();
                    for (Action<T> a : menu) {
                        popup.getItems().add(a.danger()
                                ? MenuItemsMfx.destructive(a.label(), null, () -> run(a))
                                : MenuItemsMfx.item(a.label(), null, null, () -> run(a)));
                    }
                    popup.show(more, Side.BOTTOM, 0, 4);
                });
                box.getChildren().add(more);
            }
        }

        private void run(Action<T> action) {
            T item = getItem();
            if (item != null && !isEmpty()) {
                action.run().accept(item);
            }
        }

        @Override
        protected void updateItem(@Nullable T item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            setGraphic(empty || item == null ? null : box);
        }
    }
}
