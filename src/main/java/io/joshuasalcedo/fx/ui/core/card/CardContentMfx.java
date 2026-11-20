// Made from shadcn's card.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.card;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;

/// shadcn's `CardContent` (`data-slot="card-content"`, card.tsx), as a JavaFX VBox.
/// The look is in `components.css` (style class `mfx-card-content`).
@NullMarked
public class CardContentMfx extends VBox {

    /// The style class every CardContentMfx has.
    public static final String STYLE_CLASS = "mfx-card-content";

    public CardContentMfx(Node... children) {
        super(children);
        getStyleClass().add(STYLE_CLASS);
    }
}
