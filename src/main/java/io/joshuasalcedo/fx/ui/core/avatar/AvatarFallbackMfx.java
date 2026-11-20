// Made from shadcn's avatar.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.avatar;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.scene.control.Label;
import org.jspecify.annotations.NullMarked;

/// shadcn's `AvatarFallback` (`data-slot="avatar-fallback"`, avatar.tsx), as a JavaFX Label.
/// The look is in `components.css` (style class `mfx-avatar-fallback`).
@NullMarked
public class AvatarFallbackMfx extends Label {

    /// The style class every AvatarFallbackMfx has.
    public static final String STYLE_CLASS = "mfx-avatar-fallback";

    public AvatarFallbackMfx() {
        this("");
    }

    public AvatarFallbackMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
        Tw.apply(this, "size-full rounded-full");
    }
}
