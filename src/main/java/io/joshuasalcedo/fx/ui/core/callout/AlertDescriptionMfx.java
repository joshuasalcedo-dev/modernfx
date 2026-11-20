// Made from shadcn's alert.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.callout;

import javafx.scene.control.Label;
import org.jspecify.annotations.NullMarked;

/// shadcn's `AlertDescription` (`data-slot="alert-description"`, alert.tsx), as a JavaFX Label.
/// The look is in `components.css` (style class `mfx-alert-description`).
///
/// Left out (JavaFX has no equivalent): `md:text-pretty` `[&_a]:underline` `[&_a]:underline-offset-3` `[&_a]:hover:text-foreground` `[&_p:not(:last-child)]:mb-4`
@NullMarked
public class AlertDescriptionMfx extends Label {

    /// The style class every AlertDescriptionMfx has.
    public static final String STYLE_CLASS = "mfx-alert-description";

    public AlertDescriptionMfx() {
        this("");
    }

    public AlertDescriptionMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
    }
}
