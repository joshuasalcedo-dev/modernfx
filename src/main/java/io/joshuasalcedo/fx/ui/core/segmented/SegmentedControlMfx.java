package io.joshuasalcedo.fx.ui.core.segmented;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A row of options where exactly one is selected, such as `All | Unread | VIP`. Clicking the selected option keeps
/// it selected; the left and right arrow keys move the selection. It looks like shadcn's TabsList: a muted track,
/// the selected option raised.
///
/// ```java
/// var filter = new SegmentedControlMfx<>(List.of("All", "Unread", "VIP"));
/// filter.valueProperty().subscribe(this::reload);
/// ```
///
/// @param <T> the option type; the converter (by default its `toString()`) gives the label
@NullMarked
public class SegmentedControlMfx<T> extends Control {

    public static final String STYLE_CLASS = "mfx-segmented-control";

    private final ObservableList<T> items = FXCollections.observableArrayList();
    private final ObjectProperty<@Nullable T> value = new SimpleObjectProperty<>(this, "value");
    private final ObjectProperty<StringConverter<T>> converter =
            new SimpleObjectProperty<>(this, "converter", new StringConverter<>() {
                @Override
                public String toString(@Nullable T object) {
                    return object == null ? "" : object.toString();
                }

                @Override
                public T fromString(String string) {
                    throw new UnsupportedOperationException();
                }
            });
    private final ObjectProperty<@Nullable Function<T, @Nullable Node>> graphicFactory =
            new SimpleObjectProperty<>(this, "graphicFactory");
    private final BooleanProperty showLabels = new SimpleBooleanProperty(this, "showLabels", true);

    public SegmentedControlMfx() {
        getStyleClass().add(STYLE_CLASS);
        setFocusTraversable(true);
        // as wide as its options: a squeezed row would cut the labels to "…"
        setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        // the value is always one of the items: the first when nothing (or something gone) is selected
        items.addListener((ListChangeListener<T>) c -> {
            if (items.isEmpty()) {
                setValue(null);
            } else if (getValue() == null || !items.contains(getValue())) {
                setValue(items.getFirst());
            }
        });
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.LEFT) {
                move(-1);
                e.consume();
            } else if (e.getCode() == KeyCode.RIGHT) {
                move(1);
                e.consume();
            }
        });
    }

    public SegmentedControlMfx(List<T> items) {
        this();
        this.items.setAll(items);
    }

    /// The options, in order.
    public final ObservableList<T> getItems() {
        return items;
    }

    /// The selected option.
    public final ObjectProperty<@Nullable T> valueProperty() { return value; }
    public final @Nullable T getValue() { return value.get(); }
    public final void setValue(@Nullable T v) { value.set(v); }

    /// Turns an option into its label.
    public final ObjectProperty<StringConverter<T>> converterProperty() { return converter; }
    public final StringConverter<T> getConverter() { return converter.get(); }
    public final void setConverter(StringConverter<T> value) { converter.set(Objects.requireNonNull(value)); }

    /// Makes an icon for an option (none by default).
    public final ObjectProperty<@Nullable Function<T, @Nullable Node>> graphicFactoryProperty() { return graphicFactory; }
    public final void setGraphicFactory(@Nullable Function<T, @Nullable Node> value) { graphicFactory.set(value); }

    /// When false, options with an icon show only the icon, and the label becomes their tooltip.
    public final BooleanProperty showLabelsProperty() { return showLabels; }
    public final boolean isShowLabels() { return showLabels.get(); }
    public final void setShowLabels(boolean value) { showLabels.set(value); }

    private void move(int by) {
        if (items.isEmpty()) {
            return;
        }
        int at = Math.max(0, items.indexOf(getValue()));
        setValue(items.get(Math.floorMod(at + by, items.size())));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SegmentedSkin<>(this);
    }

    /// The options as ToggleButtons in one ToggleGroup, in an HBox.
    private static final class SegmentedSkin<T> extends SkinBase<SegmentedControlMfx<T>> {

        private final HBox box = new HBox();
        private final ToggleGroup group = new ToggleGroup();
        private boolean syncing;

        SegmentedSkin(SegmentedControlMfx<T> control) {
            super(control);
            box.getStyleClass().add("mfx-segmented-items");
            getChildren().add(box);
            rebuild();
            control.getItems().addListener((ListChangeListener<T>) c -> rebuild());
            control.converterProperty().addListener(o -> rebuild());
            control.graphicFactoryProperty().addListener(o -> rebuild());
            control.showLabelsProperty().addListener(o -> rebuild());
            control.valueProperty().addListener(o -> selectValue());
            group.selectedToggleProperty().addListener((obs, old, now) -> {
                if (syncing) {
                    return;
                }
                if (now == null) {
                    // the selection can't be cleared: clicking the selected option keeps it
                    if (old != null) {
                        syncing = true;
                        group.selectToggle(old);
                        syncing = false;
                    }
                    return;
                }
                @SuppressWarnings("unchecked")
                T picked = (T) now.getUserData();
                control.setValue(picked);
            });
        }

        private void rebuild() {
            SegmentedControlMfx<T> control = getSkinnable();
            syncing = true;
            group.getToggles().clear();
            box.getChildren().clear();
            for (T item : control.getItems()) {
                String label = control.getConverter().toString(item);
                Function<T, @Nullable Node> icons = control.graphicFactoryProperty().get();
                Node icon = icons == null ? null : icons.apply(item);
                ToggleButton b = new ToggleButton(label, icon);
                b.getStyleClass().setAll("mfx-segmented-item");
                b.setMinWidth(USE_PREF_SIZE);
                b.setUserData(item);
                b.setFocusTraversable(false);
                b.setToggleGroup(group);
                if (icon != null && !control.isShowLabels()) {
                    b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    b.setTooltip(new Tooltip(label));
                    b.setAccessibleText(label);
                }
                box.getChildren().add(b);
            }
            syncing = false;
            selectValue();
        }

        private void selectValue() {
            T value = getSkinnable().getValue();
            syncing = true;
            group.getToggles().stream()
                    .filter(t -> Objects.equals(t.getUserData(), value))
                    .findFirst()
                    .ifPresentOrElse(group::selectToggle, () -> group.selectToggle(null));
            syncing = false;
        }
    }
}
