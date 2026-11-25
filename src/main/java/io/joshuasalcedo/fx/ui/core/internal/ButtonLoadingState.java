package io.joshuasalcedo.fx.ui.core.internal;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.input.MouseEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// Puts a button in and out of its loading state. It swaps the graphic for a spinner, keeps the width so the layout
/// doesn't jump, sets `:loading`, and blocks clicks until the state ends.
@NullMarked
public final class ButtonLoadingState {

    private final ButtonBase button;
    private final EventHandler<Event> blocker = Event::consume;
    private @Nullable Node savedGraphic;
    private double savedMinWidth;
    private boolean active;

    public ButtonLoadingState(ButtonBase button) {
        this.button = button;
    }

    /// A spinning loader icon, as the loading state shows.
    public static Node spinner() {
        FontIcon icon = new FontIcon(Feather.LOADER);
        icon.getStyleClass().add("mfx-spinner");
        Tw.apply(icon, "animate-spin");
        return icon;
    }

    /// Shows the spinner and blocks clicks.
    public void start() {
        if (active) {
            return;
        }
        active = true;
        savedGraphic = button.getGraphic();
        savedMinWidth = button.getMinWidth();
        if (button.getWidth() > 0) {
            button.setMinWidth(button.getWidth());
        }
        button.setGraphic(spinner());
        button.addEventFilter(ActionEvent.ACTION, blocker);
        button.addEventFilter(MouseEvent.MOUSE_PRESSED, blocker);
        button.addEventFilter(MouseEvent.MOUSE_RELEASED, blocker);
        button.pseudoClassStateChanged(ButtonPseudoClasses.LOADING, true);
    }

    /// Puts the graphic back and lets clicks through again.
    public void stop() {
        if (!active) {
            return;
        }
        active = false;
        button.removeEventFilter(ActionEvent.ACTION, blocker);
        button.removeEventFilter(MouseEvent.MOUSE_PRESSED, blocker);
        button.removeEventFilter(MouseEvent.MOUSE_RELEASED, blocker);
        button.setMinWidth(savedMinWidth);
        button.setGraphic(savedGraphic);
        savedGraphic = null;
        button.pseudoClassStateChanged(ButtonPseudoClasses.LOADING, false);
    }

    /// Starts or stops.
    public void set(boolean loading) {
        if (loading) {
            start();
        } else {
            stop();
        }
    }

    public boolean isActive() {
        return active;
    }
}
