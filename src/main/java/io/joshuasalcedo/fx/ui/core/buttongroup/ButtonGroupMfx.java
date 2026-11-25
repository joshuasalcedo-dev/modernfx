package io.joshuasalcedo.fx.ui.core.buttongroup;

import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.Pane;
import org.jspecify.annotations.NullMarked;

/// Buttons joined into one bar, with only the outer corners rounded. The group puts `mfx-first`, `mfx-middle` and
/// `mfx-last` (or `mfx-only`) on its buttons, and keeps them right as buttons are added, removed or hidden. A
/// vertical group is `:vertical`.
///
/// ```java
/// new ButtonGroupMfx(new ButtonMfx("Day"), new ButtonMfx("Week"), new ButtonMfx("Month"));
/// ```
@NullMarked
public class ButtonGroupMfx extends Pane {

    public static final String STYLE_CLASS = "mfx-button-group";

    /// The position classes the group puts on its buttons.
    public static final String FIRST = "mfx-first";
    public static final String MIDDLE = "mfx-middle";
    public static final String LAST = "mfx-last";
    public static final String ONLY = "mfx-only";

    private static final PseudoClass VERTICAL = PseudoClass.getPseudoClass("vertical");
    private static final List<String> POSITIONS = List.of(FIRST, MIDDLE, LAST, ONLY);

    private final ObservableList<ButtonBase> buttons = FXCollections.observableArrayList();
    private final InvalidationListener visibility = obs -> updatePositions();
    private final ObjectProperty<GroupOrientation> orientation =
            new SimpleObjectProperty<>(this, "orientation", GroupOrientation.HORIZONTAL) {
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(VERTICAL, get() == GroupOrientation.VERTICAL);
                    requestLayout();
                }
            };

    public ButtonGroupMfx(ButtonBase... buttons) {
        getStyleClass().add(STYLE_CLASS);
        this.buttons.addListener((ListChangeListener<ButtonBase>) change -> {
            while (change.next()) {
                for (ButtonBase removed : change.getRemoved()) {
                    removed.visibleProperty().removeListener(visibility);
                    removed.getStyleClass().removeAll(POSITIONS);
                }
                for (ButtonBase added : change.getAddedSubList()) {
                    added.visibleProperty().addListener(visibility);
                }
            }
            getChildren().setAll(this.buttons);
            updatePositions();
        });
        this.buttons.addAll(buttons);
    }

    /// The buttons, in order. Change this list, not getChildren().
    public final ObservableList<ButtonBase> getButtons() {
        return buttons;
    }

    public final ObjectProperty<GroupOrientation> orientationProperty() { return orientation; }
    public final GroupOrientation getOrientation() { return orientation.get(); }
    public final void setOrientation(GroupOrientation value) { orientation.set(value); }

    private List<ButtonBase> shown() {
        return buttons.stream().filter(ButtonBase::isVisible).toList();
    }

    private void updatePositions() {
        List<ButtonBase> shown = shown();
        for (ButtonBase b : buttons) {
            b.getStyleClass().removeAll(POSITIONS);
        }
        for (int i = 0; i < shown.size(); i++) {
            String position = shown.size() == 1 ? ONLY : i == 0 ? FIRST : i == shown.size() - 1 ? LAST : MIDDLE;
            shown.get(i).getStyleClass().add(position);
        }
        requestLayout();
    }

    private boolean horizontal() {
        return getOrientation() == GroupOrientation.HORIZONTAL;
    }

    @Override
    protected double computePrefWidth(double height) {
        Insets in = getInsets();
        double w = 0;
        for (ButtonBase b : shown()) {
            w = horizontal() ? w + b.prefWidth(-1) : Math.max(w, b.prefWidth(-1));
        }
        return in.getLeft() + w + in.getRight();
    }

    @Override
    protected double computePrefHeight(double width) {
        Insets in = getInsets();
        double h = 0;
        for (ButtonBase b : shown()) {
            h = horizontal() ? Math.max(h, b.prefHeight(-1)) : h + b.prefHeight(-1);
        }
        return in.getTop() + h + in.getBottom();
    }

    @Override
    protected double computeMinWidth(double height) {
        return computePrefWidth(height);
    }

    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    @Override
    protected void layoutChildren() {
        Insets in = getInsets();
        double x = in.getLeft();
        double y = in.getTop();
        double w = getWidth() - in.getLeft() - in.getRight();
        double h = getHeight() - in.getTop() - in.getBottom();
        for (ButtonBase b : buttons) {
            if (!b.isVisible()) {
                b.resizeRelocate(0, 0, 0, 0);
                continue;
            }
            if (horizontal()) {
                double bw = snapSizeX(b.prefWidth(h));
                b.resizeRelocate(snapPositionX(x), y, bw, h);
                x += bw;
            } else {
                double bh = snapSizeY(b.prefHeight(w));
                b.resizeRelocate(x, snapPositionY(y), w, bh);
                y += bh;
            }
        }
    }
}
