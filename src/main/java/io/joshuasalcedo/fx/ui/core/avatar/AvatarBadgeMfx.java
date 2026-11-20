// Made from shadcn's avatar.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.avatar;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NullMarked;

/// shadcn's `AvatarBadge` (`data-slot="avatar-badge"`, avatar.tsx), as a JavaFX HBox.
/// The look is in `components.css` (style class `mfx-avatar-badge`).
///
/// Left out (JavaFX has no equivalent): `group-data-[size=sm]/avatar:[&>svg]:hidden` `group-data-[size=default]/avatar:[&>svg]:size-2` `group-data-[size=lg]/avatar:[&>svg]:size-2`
@NullMarked
public class AvatarBadgeMfx extends HBox {

    /// The style class every AvatarBadgeMfx has.
    public static final String STYLE_CLASS = "mfx-avatar-badge";

    public AvatarBadgeMfx(Node... children) {
        super(children);
        getStyleClass().add(STYLE_CLASS);
    }
}
