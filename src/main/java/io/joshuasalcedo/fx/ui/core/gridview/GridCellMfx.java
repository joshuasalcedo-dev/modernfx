package io.joshuasalcedo.fx.ui.core.gridview;

import io.joshuasalcedo.fx.ui.core.data.SelectionState;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.GridCell;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// A tile in a GridViewMfx: the tile's content with a check in the corner when it's selected (`:selected`).
/// Clicking toggles it; the check shows on hover too, so the user sees tiles can be picked.
@NullMarked
public class GridCellMfx<T> extends GridCell<T> {

    public static final String STYLE_CLASS = "mfx-grid-cell";

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private final Function<T, Node> tile;
    private final Supplier<SelectionState<T>> selection;
    private final StackPane frame = new StackPane();
    private final StackPane check = new StackPane(new FontIcon(Feather.CHECK));
    private final SetChangeListener<Object> keys = c -> updateSelected();
    private @Nullable SelectionState<T> listening;

    public GridCellMfx(Function<T, Node> tile, Supplier<SelectionState<T>> selection) {
        this.tile = Objects.requireNonNull(tile, "tile");
        this.selection = Objects.requireNonNull(selection, "selection");
        getStyleClass().add(STYLE_CLASS);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        frame.getStyleClass().add("mfx-grid-tile");
        check.getStyleClass().add("mfx-grid-check");
        check.setMouseTransparent(true);
        StackPane.setAlignment(check, Pos.TOP_RIGHT);
        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            T item = getItem();
            if (e.getButton() == MouseButton.PRIMARY && item != null && !isEmpty()) {
                selection.get().toggle(item);
            }
        });
    }

    @Override
    protected void updateItem(@Nullable T item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        SelectionState<T> state = selection.get();
        if (listening != state) {
            if (listening != null) {
                listening.keys().removeListener(keys);
            }
            state.keys().addListener(keys);
            listening = state;
        }
        if (empty || item == null) {
            setGraphic(null);
        } else {
            frame.getChildren().setAll(tile.apply(item), check);
            setGraphic(frame);
        }
        updateSelected();
    }

    private void updateSelected() {
        T item = getItem();
        pseudoClassStateChanged(SELECTED, item != null && !isEmpty() && selection.get().isSelected(item));
    }
}
