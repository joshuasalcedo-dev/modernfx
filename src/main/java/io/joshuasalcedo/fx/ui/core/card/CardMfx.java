// Made from shadcn's card.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.card;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;

/// shadcn's `Card` (`data-slot="card"`, card.tsx), as a JavaFX VBox.
/// The look is in `components.css` (style class `mfx-card`).
///
/// Left out (JavaFX has no equivalent): `has-[>img:first-child]:pt-0` `*:[img:first-child]:rounded-t-xl` `*:[img:last-child]:rounded-b-xl`
@NullMarked
public class CardMfx extends VBox {

    /// The style class every CardMfx has.
    public static final String STYLE_CLASS = "mfx-card";

    /// shadcn's `size`: `default`, `sm`.
    public enum Size {
        DEFAULT("mfx-card-size-default"),
        SM("mfx-card-size-sm");

        private final String styleClass;

        Size(String styleClass) {
            this.styleClass = styleClass;
        }

        /// The style class this value puts on the Card.
        public String styleClass() {
            return styleClass;
        }
    }

    private final ObjectProperty<Size> sizeVariant = new SimpleObjectProperty<>(this, "size", Size.DEFAULT);

    public CardMfx size(Size value) {
        sizeVariant.set(value);
        return this;
    }

    public ObjectProperty<Size> sizeProperty() {
        return sizeVariant;
    }

    public Size getSize() {
        return sizeVariant.get();
    }

    public CardMfx(Node... children) {
        super(children);
        getStyleClass().add(STYLE_CLASS);
        getStyleClass().add(sizeVariant.get().styleClass());
        sizeVariant.addListener((obs, old, now) -> {
            getStyleClass().remove(old.styleClass());
            getStyleClass().add(now.styleClass());
        });
        Tw.apply(this, "overflow-hidden rounded-xl");
    }
}
