package io.joshuasalcedo.fx.ui.core.iconbutton;

/// The outline of an icon button.
public enum IconButtonShape {
    SQUARE("mfx-square"),
    CIRCLE("mfx-circle");

    private final String styleClass;

    IconButtonShape(String styleClass) {
        this.styleClass = styleClass;
    }

    /// The style class this shape puts on the button.
    public String styleClass() {
        return styleClass;
    }
}
