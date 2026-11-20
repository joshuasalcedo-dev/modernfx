// Made from shadcn's card.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.card;

import javafx.scene.control.Label;
import org.jspecify.annotations.NullMarked;

/// shadcn's `CardTitle` (`data-slot="card-title"`, card.tsx), as a JavaFX Label.
/// The look is in `components.css` (style class `mfx-card-title`).
@NullMarked
public class CardTitleMfx extends Label {

    /// The style class every CardTitleMfx has.
    public static final String STYLE_CLASS = "mfx-card-title";

    public CardTitleMfx() {
        this("");
    }

    public CardTitleMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
    }
}
