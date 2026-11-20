// Made from shadcn's card.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.card;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NullMarked;

/// shadcn's `CardAction` (`data-slot="card-action"`, card.tsx), as a JavaFX StackPane.
/// The look is in `components.css` (style class `mfx-card-action`).
@NullMarked
public class CardActionMfx extends StackPane {

    /// The style class every CardActionMfx has.
    public static final String STYLE_CLASS = "mfx-card-action";

    public CardActionMfx(Node... children) {
        super(children);
        getStyleClass().add(STYLE_CLASS);
    }
}
