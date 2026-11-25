package io.joshuasalcedo.fx.ui.core.list;

import java.util.Objects;
import java.util.function.Function;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A list row: a ListItemLayout filled by a ListCellFactory, with the hover, selected and focus looks
/// (`mfx-list-cell`). With `sectionBy`, the first item of each section has its ListSectionMfx header above it.
@NullMarked
public class ListCellMfx<T> extends ListCell<T> {

    public static final String STYLE_CLASS = "mfx-list-cell";

    private final ListCellFactory<T> factory;
    private final @Nullable Function<T, String> sectionBy;
    private final ListItemLayout layout = new ListItemLayout();
    private final ListSectionMfx header = new ListSectionMfx();
    private final VBox withHeader = new VBox();

    public ListCellMfx(ListCellFactory<T> factory) {
        this(factory, null);
    }

    public ListCellMfx(ListCellFactory<T> factory, @Nullable Function<T, String> sectionBy) {
        this.factory = Objects.requireNonNull(factory, "factory");
        this.sectionBy = sectionBy;
        getStyleClass().add(STYLE_CLASS);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        withHeader.getStyleClass().add("mfx-list-cell-sectioned");
    }

    /// The row this cell shows.
    public ListItemLayout itemLayout() {
        return layout;
    }

    @Override
    protected void updateItem(@Nullable T item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }
        layout.clear();
        factory.fill(layout, item);
        String section = sectionStarting(item);
        if (section == null) {
            withHeader.getChildren().clear();
            setGraphic(layout);
        } else {
            header.setText(section);
            withHeader.getChildren().setAll(header, layout);
            setGraphic(withHeader);
        }
    }

    /// The section this item starts, or null if it's in the same section as the item above it.
    private @Nullable String sectionStarting(T item) {
        if (sectionBy == null) {
            return null;
        }
        String section = sectionBy.apply(item);
        ListView<T> list = getListView();
        int index = getIndex();
        if (list == null || index <= 0 || index > list.getItems().size()) {
            return section;
        }
        T previous = list.getItems().get(index - 1);
        return previous != null && Objects.equals(sectionBy.apply(previous), section) ? null : section;
    }
}
