// Made from shadcn's kbd.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.kbd;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.scene.control.Label;
import org.jspecify.annotations.NullMarked;

/// shadcn's `Kbd` (`data-slot="kbd"`, kbd.tsx), as a JavaFX Label.
/// The look is in `components.css` (style class `mfx-kbd`).
///
/// Left out (JavaFX has no equivalent): `in-data-[slot=tooltip-content]:bg-background/20` `in-data-[slot=tooltip-content]:text-background` `dark:in-data-[slot=tooltip-content]:bg-background/10` `[&_svg:not([class*='size-'])]:size-3`
@NullMarked
public class KbdMfx extends Label {

    /// The style class every KbdMfx has.
    public static final String STYLE_CLASS = "mfx-kbd";

    public KbdMfx() {
        this("");
    }

    public KbdMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
        Tw.apply(this, "w-fit rounded-sm");
    }
}
