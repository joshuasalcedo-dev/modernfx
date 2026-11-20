package io.joshuasalcedo.fx.ui.core.internal;

import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import org.jspecify.annotations.Nullable;

/// Puts a button in and out of its loading state. It swaps the graphic for a spinner, keeps the width so the layout
/// doesn't jump, sets `:loading`, and blocks clicks until the state ends.
public final class ButtonLoadingState {

    private final ButtonBase button;
    private @Nullable Node savedGraphic;
    private boolean active;

    public ButtonLoadingState(ButtonBase button) {
        this.button = button;
    }

    /// Shows the spinner and blocks clicks.
    public void start() {
        if (active) {
            return;
        }
        active = true;
        savedGraphic = button.getGraphic();
        // TODO: put a spinner in the graphic, pin the min width, filter out ActionEvent and mouse presses
        button.pseudoClassStateChanged(ButtonPseudoClasses.LOADING, true);
    }

    /// Puts the graphic back and lets clicks through again.
    public void stop() {
        if (!active) {
            return;
        }
        active = false;
        // TODO: remove the spinner and the click filter, release the min width
        button.setGraphic(savedGraphic);
        savedGraphic = null;
        button.pseudoClassStateChanged(ButtonPseudoClasses.LOADING, false);
    }

    public boolean isActive() {
        return active;
    }
}
