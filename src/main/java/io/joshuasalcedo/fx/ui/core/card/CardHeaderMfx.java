// Made from shadcn's card.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.card;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;

/// shadcn's `CardHeader` (`data-slot="card-header"`, card.tsx), as a JavaFX VBox.
/// The look is in `components.css` (style class `mfx-card-header`).
///
/// Left out (JavaFX has no equivalent): `rounded-t-xl` `has-data-[slot=card-action]:grid-cols-[1fr_auto]` `has-data-[slot=card-description]:grid-rows-[auto_auto]` `[.border-b]:pb-(--card-spacing)`
@NullMarked
public class CardHeaderMfx extends VBox {

    /// The style class every CardHeaderMfx has.
    public static final String STYLE_CLASS = "mfx-card-header";

    public CardHeaderMfx(Node... children) {
        super(children);
        getStyleClass().add(STYLE_CLASS);
    }
}
