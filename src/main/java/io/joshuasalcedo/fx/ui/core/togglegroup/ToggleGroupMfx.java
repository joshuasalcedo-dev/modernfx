package io.joshuasalcedo.fx.ui.core.togglegroup;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NullMarked;

/// shadcn's ToggleGroup: buttons joined in a row that stay pressed (JavaFX's ToggleButtons). SINGLE keeps at
/// most one pressed, MULTIPLE lets any be pressed; OUTLINE gives them a border.
///
/// ```java
/// var format = new ToggleGroupMfx(ToggleGroupMfx.Type.MULTIPLE, "Bold", "Italic", "Underline");
/// ```
@NullMarked
public class ToggleGroupMfx extends HBox {

    /// The style class every ToggleGroupMfx has.
    public static final String STYLE_CLASS = "mfx-toggle-group";

    public enum Type { SINGLE, MULTIPLE }

    public enum Variant {
        DEFAULT, OUTLINE;

        public String styleClass() {
            return "mfx-toggle-group-variant-" + name().toLowerCase();
        }
    }

    private final List<ToggleButton> buttons = new ArrayList<>();
    private final ToggleGroup single = new ToggleGroup();
    private Variant variant = Variant.DEFAULT;

    public ToggleGroupMfx(Type type, String... labels) {
        getStyleClass().addAll(STYLE_CLASS, Variant.DEFAULT.styleClass());
        for (int i = 0; i < labels.length; i++) {
            ToggleButton b = new ToggleButton(labels[i]);
            b.getStyleClass().add("mfx-toggle-group-item");
            if (i == 0) {
                b.getStyleClass().add("mfx-first");
            }
            if (i == labels.length - 1) {
                b.getStyleClass().add("mfx-last");
            }
            if (type == Type.SINGLE) {
                b.setToggleGroup(single);
            }
            buttons.add(b);
        }
        getChildren().setAll(buttons);
    }

    public ToggleGroupMfx variant(Variant v) {
        getStyleClass().remove(variant.styleClass());
        variant = v;
        getStyleClass().add(v.styleClass());
        return this;
    }

    /// The buttons, in order: listen to their `selectedProperty()`.
    public ObservableList<ToggleButton> getButtons() {
        return FXCollections.unmodifiableObservableList(FXCollections.observableList(buttons));
    }
}
