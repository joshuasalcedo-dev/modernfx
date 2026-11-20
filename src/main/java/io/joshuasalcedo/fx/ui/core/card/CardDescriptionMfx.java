// Made from shadcn's card.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.card;

import javafx.scene.control.Label;
import org.jspecify.annotations.NullMarked;

/// shadcn's `CardDescription` (`data-slot="card-description"`, card.tsx), as a JavaFX Label.
/// The look is in `components.css` (style class `mfx-card-description`).
@NullMarked
public class CardDescriptionMfx extends Label {

    /// The style class every CardDescriptionMfx has.
    public static final String STYLE_CLASS = "mfx-card-description";

    public CardDescriptionMfx() {
        this("");
    }

    public CardDescriptionMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
    }
}
