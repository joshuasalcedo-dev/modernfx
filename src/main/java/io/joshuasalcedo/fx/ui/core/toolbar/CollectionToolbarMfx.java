package io.joshuasalcedo.fx.ui.core.toolbar;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// The bar above a collection: the search field, then the filters (chips, selects), then on the right the view
/// mode switch and any other buttons (such as the Columns menu). While items are selected, the bulk action bar
/// takes its place.
///
/// ```java
/// var toolbar = new CollectionToolbarMfx();
/// list.filterProperty().bind(toolbar.search().searchProperty());
/// toolbar.getFilters().add(statusSelect);
/// toolbar.setViewModeToggle(new ViewModeToggleMfx());
/// toolbar.setBulkActions(new BulkActionBarMfx(selection));
/// ```
@NullMarked
public class CollectionToolbarMfx extends StackPane {

    public static final String STYLE_CLASS = "mfx-collection-toolbar";

    private final SearchFieldMfx search = new SearchFieldMfx();
    private final HBox filters = new HBox(8);
    private final HBox trailing = new HBox(8);
    private final StackPane viewToggleSlot = new StackPane();
    private final HBox row;
    private final ObjectProperty<@Nullable ViewModeToggleMfx> viewModeToggle =
            new SimpleObjectProperty<>(this, "viewModeToggle") {
                @Override
                protected void invalidated() {
                    ViewModeToggleMfx toggle = get();
                    if (toggle == null) {
                        viewToggleSlot.getChildren().clear();
                    } else {
                        viewToggleSlot.getChildren().setAll(toggle);
                    }
                    viewToggleSlot.setManaged(toggle != null);
                    viewToggleSlot.setVisible(toggle != null);
                }
            };
    private final ObjectProperty<@Nullable BulkActionBarMfx> bulkActions =
            new SimpleObjectProperty<>(this, "bulkActions") {
                @Override
                protected void invalidated() {
                    showBulk(get());
                }
            };

    public CollectionToolbarMfx() {
        getStyleClass().add(STYLE_CLASS);
        search.setPrefWidth(260);
        search.setMaxWidth(320);
        filters.setAlignment(Pos.CENTER_LEFT);
        trailing.setAlignment(Pos.CENTER_RIGHT);
        viewToggleSlot.setManaged(false);
        viewToggleSlot.setVisible(false);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row = new HBox(8, search, filters, spacer, trailing, viewToggleSlot);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("mfx-collection-toolbar-row");
        getChildren().add(row);
    }

    /// The search field.
    public SearchFieldMfx search() {
        return search;
    }

    /// The filters, after the search field.
    public ObservableList<Node> getFilters() {
        return filters.getChildren();
    }

    /// Buttons on the right, before the view mode switch.
    public ObservableList<Node> getTrailing() {
        return trailing.getChildren();
    }

    public final ObjectProperty<@Nullable ViewModeToggleMfx> viewModeToggleProperty() { return viewModeToggle; }
    public final void setViewModeToggle(@Nullable ViewModeToggleMfx value) { viewModeToggle.set(value); }

    /// The bar that replaces the toolbar while items are selected.
    public final ObjectProperty<@Nullable BulkActionBarMfx> bulkActionsProperty() { return bulkActions; }
    public final void setBulkActions(@Nullable BulkActionBarMfx value) { bulkActions.set(value); }

    private void showBulk(@Nullable BulkActionBarMfx bulk) {
        getChildren().removeIf(n -> n instanceof BulkActionBarMfx);
        row.visibleProperty().unbind();
        if (bulk == null) {
            row.setVisible(true);
            return;
        }
        getChildren().add(bulk);
        row.visibleProperty().bind(bulk.visibleProperty().not());
    }
}
