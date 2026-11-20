// Made from shadcn's card.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.card;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NullMarked;

/// shadcn's `CardFooter` (`data-slot="card-footer"`, card.tsx), as a JavaFX HBox.
/// The look is in `components.css` (style class `mfx-card-footer`).
///
/// Left out (JavaFX has no equivalent): `rounded-b-xl` `[.border-t]:pt-(--card-spacing)`
@NullMarked
public class CardFooterMfx extends HBox {

    /// The style class every CardFooterMfx has.
    public static final String STYLE_CLASS = "mfx-card-footer";

    public CardFooterMfx(Node... children) {
        super(children);
        getStyleClass().add(STYLE_CLASS);
    }
}
