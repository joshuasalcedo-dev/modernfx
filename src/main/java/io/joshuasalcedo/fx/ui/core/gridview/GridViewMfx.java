package io.joshuasalcedo.fx.ui.core.gridview;

import io.joshuasalcedo.fx.ui.core.data.KeyExtractor;
import io.joshuasalcedo.fx.ui.core.data.SelectionMode;
import io.joshuasalcedo.fx.ui.core.data.SelectionState;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Label;
import org.controlsfx.control.GridView;
import org.jspecify.annotations.NullMarked;

/// A virtualized grid of tiles, such as a gallery (ControlsFX's GridView): only the rows on screen are made. The
/// columns follow the width: as many tiles of at least `sizing.minTileWidth()` as fit, grown to fill the row.
/// Clicking a tile selects it (a check in its corner); the selection is a SelectionState, by key.
///
/// ```java
/// var photos = new GridViewMfx<Photo>(p -> new ImageView(p.thumbnail()));
/// photos.setSizing(new GridSizing(180, 4 / 3.0, 12));
/// photos.getItems().setAll(album);
/// ```
@NullMarked
public class GridViewMfx<T> extends GridView<T> {

    public static final String STYLE_CLASS = "mfx-grid-view";

    private final ObjectProperty<GridSizing> sizing = new SimpleObjectProperty<>(this, "sizing", GridSizing.DEFAULT) {
        @Override
        protected void invalidated() {
            resizeTiles();
        }
    };
    private final ObjectProperty<SelectionState<T>> selection = new SimpleObjectProperty<>(this, "selection",
            new SelectionState<>(KeyExtractor.identity(), SelectionMode.MULTIPLE));
    private final ObjectProperty<Function<T, Node>> tile;

    /// Tiles showing the item's `toString()`.
    public GridViewMfx() {
        this(item -> new Label(String.valueOf(item)));
    }

    public GridViewMfx(Function<T, Node> tile) {
        super(FXCollections.observableArrayList());
        this.tile = new SimpleObjectProperty<>(this, "tile", Objects.requireNonNull(tile, "tile"));
        getStyleClass().add(STYLE_CLASS);
        setCellFactory(grid -> new GridCellMfx<>(this.tile.get(), selection::get));
        // a new factory makes the cells again, with the new tile
        this.tile.addListener(o -> setCellFactory(grid -> new GridCellMfx<>(this.tile.get(), selection::get)));
        widthProperty().addListener(o -> resizeTiles());
        resizeTiles();
    }

    public GridViewMfx(Function<T, Node> tile, List<T> items) {
        this(tile);
        getItems().setAll(items);
    }

    /// Makes the content of a tile.
    public final ObjectProperty<Function<T, Node>> tileProperty() { return tile; }
    public final void setTile(Function<T, Node> value) { tile.set(Objects.requireNonNull(value)); }

    public final ObjectProperty<GridSizing> sizingProperty() { return sizing; }
    public final GridSizing getSizing() { return sizing.get(); }
    public final void setSizing(GridSizing value) { sizing.set(Objects.requireNonNull(value)); }

    /// What is selected, by key.
    public final ObjectProperty<SelectionState<T>> selectionProperty() { return selection; }
    public final SelectionState<T> getSelection() { return selection.get(); }
    public final void setSelection(SelectionState<T> value) { selection.set(Objects.requireNonNull(value)); }

    /// The width the tiles share: the grid's width less its padding and the scrollbar.
    private double usableWidth() {
        double scrollbar = 14;
        return Math.max(0, getWidth() - snappedLeftInset() - snappedRightInset() - scrollbar);
    }

    private void resizeTiles() {
        GridSizing s = sizing.get();
        double width = usableWidth();
        // GridView puts the spacing on each side of a tile, so each column takes cellWidth + gap
        setHorizontalCellSpacing(s.gap() / 2);
        setVerticalCellSpacing(s.gap() / 2);
        double cell = width <= 0 ? s.minTileWidth() : Math.max(1, Math.floor(width / s.columns(width) - s.gap()));
        setCellWidth(cell);
        setCellHeight(s.tileHeight(cell));
    }
}
