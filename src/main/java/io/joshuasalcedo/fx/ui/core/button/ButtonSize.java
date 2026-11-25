package io.joshuasalcedo.fx.ui.core.button;

/// How big a button is: one of ButtonMfx's size classes, for a button with text and for a square icon button.
public enum ButtonSize {
    SM("mfx-button-size-sm", "mfx-button-size-icon-sm"),
    MD("mfx-button-size-default", "mfx-button-size-icon"),
    LG("mfx-button-size-lg", "mfx-button-size-icon-lg");

    private final String styleClass;
    private final String iconStyleClass;

    ButtonSize(String styleClass, String iconStyleClass) {
        this.styleClass = styleClass;
        this.iconStyleClass = iconStyleClass;
    }

    /// The style class this size puts on a button with text.
    public String styleClass() {
        return styleClass;
    }

    /// The style class this size puts on an icon-only button: as wide as it is high.
    public String iconStyleClass() {
        return iconStyleClass;
    }
}
