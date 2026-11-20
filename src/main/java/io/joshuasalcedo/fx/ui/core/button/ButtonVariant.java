package io.joshuasalcedo.fx.ui.core.button;

/// How a button looks. Each variant is a style class from `shadcn.css`.
public enum ButtonVariant {
    PRIMARY("mfx-default"),
    SECONDARY("mfx-secondary"),
    GHOST("mfx-ghost"),
    DANGER("mfx-destructive"),
    SUCCESS("mfx-success"),
    LINK("mfx-link");

    private final String styleClass;

    ButtonVariant(String styleClass) {
        this.styleClass = styleClass;
    }

    /// The style class this variant puts on the button.
    public String styleClass() {
        return styleClass;
    }
}
