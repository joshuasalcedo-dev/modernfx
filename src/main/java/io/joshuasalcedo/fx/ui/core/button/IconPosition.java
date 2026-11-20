package io.joshuasalcedo.fx.ui.core.button;

import javafx.scene.control.ContentDisplay;

/// Which side of the text the icon goes on.
public enum IconPosition {
    LEADING(ContentDisplay.LEFT),
    TRAILING(ContentDisplay.RIGHT);

    private final ContentDisplay contentDisplay;

    IconPosition(ContentDisplay contentDisplay) {
        this.contentDisplay = contentDisplay;
    }

    /// The JavaFX content display that puts the icon on this side.
    public ContentDisplay contentDisplay() {
        return contentDisplay;
    }
}
