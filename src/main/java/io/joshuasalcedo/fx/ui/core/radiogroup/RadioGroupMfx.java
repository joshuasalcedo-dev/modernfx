package io.joshuasalcedo.fx.ui.core.radiogroup;

import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// shadcn's RadioGroup: a column of options where one is picked (JavaFX's RadioButtons in a ToggleGroup).
///
/// ```java
/// var density = new RadioGroupMfx<>(List.of("Default", "Comfortable", "Compact"));
/// density.setValue("Comfortable");
/// ```
///
/// @param <T> the option type; its `toString()` is the label
@NullMarked
public class RadioGroupMfx<T> extends VBox {

    /// The style class every RadioGroupMfx has.
    public static final String STYLE_CLASS = "mfx-radio-group";

    private final ToggleGroup group = new ToggleGroup();
    private final ObjectProperty<@Nullable T> value = new SimpleObjectProperty<>(this, "value");

    public RadioGroupMfx(List<T> options) {
        getStyleClass().add(STYLE_CLASS);
        for (T option : options) {
            RadioButton b = new RadioButton(String.valueOf(option));
            b.setUserData(option);
            b.setToggleGroup(group);
            getChildren().add(b);
        }
        group.selectedToggleProperty().addListener((obs, old, now) -> {
            @SuppressWarnings("unchecked")
            T v = now == null ? null : (T) now.getUserData();
            value.set(v);
        });
        value.addListener((obs, old, now) -> group.getToggles().stream()
                .filter(t -> java.util.Objects.equals(t.getUserData(), now)).findFirst()
                .ifPresentOrElse(t -> t.setSelected(true), () -> group.selectToggle(null)));
    }

    /// The option picked (null: none).
    public ObjectProperty<@Nullable T> valueProperty() {
        return value;
    }

    public @Nullable T getValue() {
        return value.get();
    }

    public void setValue(@Nullable T v) {
        value.set(v);
    }
}
