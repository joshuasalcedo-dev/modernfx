package io.joshuasalcedo.fx.ui.core.segmented;

import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import org.jspecify.annotations.Nullable;

/// A row of options where exactly one is selected, such as `All | Unread | VIP`. Clicking the selected option keeps
/// it selected; the arrow keys move the selection.
///
/// ```java
/// var filter = new SegmentedControlMfx<>(List.of("All", "Unread", "VIP"));
/// filter.valueProperty().subscribe(this::reload);
/// ```
///
/// @param <T> the option type; its `toString()` is the label
public class SegmentedControlMfx<T> extends Control {

    public static final String STYLE_CLASS = "mfx-segmented-control";

    private final ObservableList<T> items = FXCollections.observableArrayList();
    private final ObjectProperty<@Nullable T> value = new SimpleObjectProperty<>(this, "value");

    public SegmentedControlMfx() {
        getStyleClass().add(STYLE_CLASS);
    }

    public SegmentedControlMfx(List<T> items) {
        this();
        this.items.setAll(items);
        // TODO: select the first item
    }

    /// The options, in order.
    public final ObservableList<T> getItems() {
        return items;
    }

    /// The selected option.
    public final ObjectProperty<@Nullable T> valueProperty() { return value; }
    public final @Nullable T getValue() { return value.get(); }
    public final void setValue(@Nullable T v) { value.set(v); }

    // TODO: a converter property (StringConverter<T>) for the labels
    // TODO: createDefaultSkin(): SwToggleButtons in a ToggleGroup inside an ButtonGroupMfx; the selection can't be cleared
}
