package io.joshuasalcedo.fx.ui.core.button;

/// How a button looks: the variants of the action controls (IconButtonMfx, ToggleButtonMfx, SplitButtonMfx).
/// Each is one of ButtonMfx's style classes in `components.css`, so they look the same as ButtonMfx.
public enum ButtonVariant {
    PRIMARY("mfx-button-variant-default"),
    SECONDARY("mfx-button-variant-secondary"),
    OUTLINE("mfx-button-variant-outline"),
    GHOST("mfx-button-variant-ghost"),
    DANGER("mfx-button-variant-destructive"),
    SUCCESS("mfx-button-variant-success"),
    LINK("mfx-button-variant-link");

    private final String styleClass;

    ButtonVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// The style class this variant puts on the button.
    public String styleClass() {
        return styleClass;
    }
}
