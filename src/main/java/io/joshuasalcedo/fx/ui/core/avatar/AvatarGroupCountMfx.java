// Made from shadcn's avatar.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.avatar;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NullMarked;

/// shadcn's `AvatarGroupCount` (`data-slot="avatar-group-count"`, avatar.tsx), as a JavaFX HBox.
/// The look is in `components.css` (style class `mfx-avatar-group-count`).
///
/// Left out (JavaFX has no equivalent): `group-has-data-[size=lg]/avatar-group:size-10` `group-has-data-[size=sm]/avatar-group:size-6` `[&>svg]:size-4` `group-has-data-[size=lg]/avatar-group:[&>svg]:size-5` `group-has-data-[size=sm]/avatar-group:[&>svg]:size-3`
@NullMarked
public class AvatarGroupCountMfx extends HBox {

    /// The style class every AvatarGroupCountMfx has.
    public static final String STYLE_CLASS = "mfx-avatar-group-count";

    public AvatarGroupCountMfx(Node... children) {
        super(children);
        getStyleClass().add(STYLE_CLASS);
        Tw.apply(this, "shrink-0 rounded-full");
    }
}
