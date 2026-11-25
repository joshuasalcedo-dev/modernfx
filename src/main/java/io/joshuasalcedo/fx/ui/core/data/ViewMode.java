package io.joshuasalcedo.fx.ui.core.data;

/// How a collection shows its items. ViewModeToggleMfx switches between them.
public enum ViewMode {
    LIST("List"),
    GRID("Grid"),
    TABLE("Table"),
    BOARD("Board");

    private final String label;

    ViewMode(String label) {
        this.label = label;
    }

    /// The name shown to the user (`List`, `Grid`...).
    public String label() {
        return label;
    }
}
