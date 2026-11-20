// Made from shadcn's alert.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.callout;

import javafx.scene.control.Label;
import org.jspecify.annotations.NullMarked;

/// shadcn's `AlertTitle` (`data-slot="alert-title"`, alert.tsx), as a JavaFX Label.
/// The look is in `components.css` (style class `mfx-alert-title`).
///
/// Left out (JavaFX has no equivalent): `group-has-[>svg]/alert:col-start-2` `[&_a]:underline` `[&_a]:underline-offset-3` `[&_a]:hover:text-foreground`
@NullMarked
public class AlertTitleMfx extends Label {

    /// The style class every AlertTitleMfx has.
    public static final String STYLE_CLASS = "mfx-alert-title";

    public AlertTitleMfx() {
        this("");
    }

    public AlertTitleMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
    }
}
