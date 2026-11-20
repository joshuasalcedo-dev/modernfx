// Made from shadcn's avatar.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.avatar;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NullMarked;

/// shadcn's `Avatar` (`data-slot="avatar"`, avatar.tsx), as a JavaFX StackPane.
/// The look is in `components.css` (style class `mfx-avatar`).
///
/// Left out (JavaFX has no equivalent): `after:absolute` `after:inset-0` `after:rounded-full` `after:border` `after:border-border` `after:mix-blend-darken` `dark:after:mix-blend-lighten`
@NullMarked
public class AvatarMfx extends StackPane {

    /// The style class every AvatarMfx has.
    public static final String STYLE_CLASS = "mfx-avatar";

    /// shadcn's `size`: `default`, `sm`, `lg`.
    public enum Size {
        DEFAULT("mfx-avatar-size-default"),
        SM("mfx-avatar-size-sm"),
        LG("mfx-avatar-size-lg");

        private final String styleClass;

        Size(String styleClass) {
            this.styleClass = styleClass;
        }

        /// The style class this value puts on the Avatar.
        public String styleClass() {
            return styleClass;
        }
    }

    private final ObjectProperty<Size> sizeVariant = new SimpleObjectProperty<>(this, "size", Size.DEFAULT);

    public AvatarMfx size(Size value) {
        sizeVariant.set(value);
        return this;
    }

    public ObjectProperty<Size> sizeProperty() {
        return sizeVariant;
    }

    public Size getSize() {
        return sizeVariant.get();
    }

    public AvatarMfx(Node... children) {
        super(children);
        getStyleClass().add(STYLE_CLASS);
        getStyleClass().add(sizeVariant.get().styleClass());
        sizeVariant.addListener((obs, old, now) -> {
            getStyleClass().remove(old.styleClass());
            getStyleClass().add(now.styleClass());
        });
        Tw.apply(this, "shrink-0 rounded-full");
    }
}
