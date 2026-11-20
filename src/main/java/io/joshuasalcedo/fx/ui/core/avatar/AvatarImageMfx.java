// Made from shadcn's avatar.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.avatar;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// shadcn's `AvatarImage` (`data-slot="avatar-image"`, avatar.tsx), as a JavaFX ImageView.
/// The look is in `components.css` (style class `mfx-avatar-image`).
@NullMarked
public class AvatarImageMfx extends ImageView {

    /// The style class every AvatarImageMfx has.
    public static final String STYLE_CLASS = "mfx-avatar-image";

    public AvatarImageMfx() {
        this(null);
    }

    public AvatarImageMfx(@Nullable Image image) {
        super(image);
        getStyleClass().add(STYLE_CLASS);
        Tw.apply(this, "size-full rounded-full");
    }
}
