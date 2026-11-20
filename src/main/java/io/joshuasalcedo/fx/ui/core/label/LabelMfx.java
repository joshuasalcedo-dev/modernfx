// Made from shadcn's label.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.label;

import javafx.scene.control.Label;
import org.jspecify.annotations.NullMarked;

/// shadcn's `Label` (`data-slot="label"`, label.tsx), as a JavaFX Label.
/// The look is in `components.css` (style class `mfx-label`).
///
/// Left out (JavaFX has no equivalent): `group-data-[disabled=true]:pointer-events-none` `group-data-[disabled=true]:opacity-50` `peer-disabled:cursor-not-allowed` `peer-disabled:opacity-50`
@NullMarked
public class LabelMfx extends Label {

    /// The style class every LabelMfx has.
    public static final String STYLE_CLASS = "mfx-label";

    public LabelMfx() {
        this("");
    }

    public LabelMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
    }
}
