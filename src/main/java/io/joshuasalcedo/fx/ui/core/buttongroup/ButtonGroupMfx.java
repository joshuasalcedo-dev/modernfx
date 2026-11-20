package io.joshuasalcedo.fx.ui.core.buttongroup;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.Pane;

/// Buttons joined into one bar, with only the outer corners rounded. The group puts `mfx-first`, `mfx-middle` and
/// `mfx-last` (or `mfx-only`) on its buttons, and keeps them right as buttons are added, removed or hidden.
///
/// ```java
/// new ButtonGroupMfx(new ButtonMfx("Day"), new ButtonMfx("Week"), new ButtonMfx("Month"));
/// ```
public class ButtonGroupMfx extends Pane {

    public static final String STYLE_CLASS = "mfx-button-group";

    private final ObservableList<ButtonBase> buttons = FXCollections.observableArrayList();
    private final ObjectProperty<GroupOrientation> orientation =
            new SimpleObjectProperty<>(this, "orientation", GroupOrientation.HORIZONTAL);

    public ButtonGroupMfx(ButtonBase... buttons) {
        getStyleClass().add(STYLE_CLASS);
        // TODO: mirror the buttons into getChildren(); update the position classes when the list or visibility changes
        this.buttons.addAll(buttons);
    }

    /// The buttons, in order. Change this list, not getChildren().
    public final ObservableList<ButtonBase> getButtons() {
        return buttons;
    }

    public final ObjectProperty<GroupOrientation> orientationProperty() { return orientation; }
    public final GroupOrientation getOrientation() { return orientation.get(); }
    public final void setOrientation(GroupOrientation value) { orientation.set(value); }

    @Override
    protected void layoutChildren() {
        // TODO: lay the buttons edge to edge along the orientation, stretched across the other axis
        super.layoutChildren();
    }
}
