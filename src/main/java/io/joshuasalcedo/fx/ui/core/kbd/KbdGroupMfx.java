// Made from shadcn's kbd.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.kbd;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NullMarked;

/// shadcn's `KbdGroup` (`data-slot="kbd-group"`, kbd.tsx), as a JavaFX HBox.
/// The look is in `components.css` (style class `mfx-kbd-group`).
@NullMarked
public class KbdGroupMfx extends HBox {

    /// The style class every KbdGroupMfx has.
    public static final String STYLE_CLASS = "mfx-kbd-group";

    public KbdGroupMfx(Node... children) {
        super(children);
        getStyleClass().add(STYLE_CLASS);
    }
}
