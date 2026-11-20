// Made from shadcn's avatar.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.avatar;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NullMarked;

/// shadcn's `AvatarGroup` (`data-slot="avatar-group"`, avatar.tsx), as a JavaFX HBox.
/// The look is in `components.css` (style class `mfx-avatar-group`).
///
/// Left out (JavaFX has no equivalent): `-space-x-2` `*:data-[slot=avatar]:ring-2` `*:data-[slot=avatar]:ring-background`
@NullMarked
public class AvatarGroupMfx extends HBox {

    /// The style class every AvatarGroupMfx has.
    public static final String STYLE_CLASS = "mfx-avatar-group";

    public AvatarGroupMfx(Node... children) {
        super(children);
        getStyleClass().add(STYLE_CLASS);
    }
}
