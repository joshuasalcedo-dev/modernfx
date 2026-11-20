package io.joshuasalcedo.fx.ui.core.buttongroup;

import javafx.geometry.Orientation;

/// Which way a button group runs.
public enum GroupOrientation {
    HORIZONTAL(Orientation.HORIZONTAL),
    VERTICAL(Orientation.VERTICAL);

    private final Orientation orientation;

    GroupOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    /// The JavaFX orientation.
    public Orientation orientation() {
        return orientation;
    }
}
