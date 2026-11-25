package io.joshuasalcedo.fx.ui.core.table;

import io.joshuasalcedo.fx.ui.core.table.renderer.CellRenderer;
import javafx.scene.Node;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A table cell that shows its value through a CellRenderer (or its `toString()`): text that doesn't fit ends in an
/// ellipsis, and then the whole text is in a tooltip.
@NullMarked
public class TableCellMfx<T, V> extends TableCell<T, V> {

    public static final String STYLE_CLASS = "mfx-table-cell";

    private final @Nullable CellRenderer<T, V> renderer;
    private final Text measure = new Text();
    private @Nullable String full;

    public TableCellMfx() {
        this(null);
    }

    public TableCellMfx(@Nullable CellRenderer<T, V> renderer) {
        this.renderer = renderer;
        getStyleClass().add(STYLE_CLASS);
        setTextOverrun(OverrunStyle.ELLIPSIS);
        widthProperty().addListener(o -> updateTooltip());
    }

    @Override
    protected void updateItem(@Nullable V value, boolean empty) {
        super.updateItem(value, empty);
        if (empty || value == null) {
            full = null;
            setText(null);
            setGraphic(null);
            setTooltip(null);
            return;
        }
        String text = renderer == null ? String.valueOf(value) : renderer.text(value);
        TableRow<T> row = getTableRow();
        T item = row == null ? null : row.getItem();
        Node node = renderer == null || item == null ? null : renderer.render(item, value);
        if (node == null) {
            setGraphic(null);
            setText(text);
        } else {
            setText(null);
            setGraphic(node);
        }
        full = text;
        updateTooltip();
    }

    private void updateTooltip() {
        String text = full;
        if (text == null || isEmpty()) {
            setTooltip(null);
            return;
        }
        double available = getWidth() - snappedLeftInset() - snappedRightInset();
        double needed;
        Node graphic = getGraphic();
        if (graphic == null) {
            measure.setFont(getFont());
            measure.setText(text);
            needed = measure.getLayoutBounds().getWidth();
        } else {
            needed = graphic.prefWidth(-1);
        }
        if (available > 0 && needed > available + 0.5) {
            Tooltip tip = getTooltip();
            if (tip == null || !text.equals(tip.getText())) {
                setTooltip(new Tooltip(text));
            }
        } else {
            setTooltip(null);
        }
    }
}
