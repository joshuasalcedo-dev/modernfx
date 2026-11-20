package io.joshuasalcedo.fx.ui.core.button;

/// How big a button is. Each size is a style class.
public enum ButtonSize {
    SM("mfx-sm"),
    MD("mfx-md"),
    LG("mfx-lg");

    private final String styleClass;

    ButtonSize(String styleClass) {
        this.styleClass = styleClass;
    }

    /// The style class this size puts on the button.
    public String styleClass() {
        return styleClass;
    }
}
