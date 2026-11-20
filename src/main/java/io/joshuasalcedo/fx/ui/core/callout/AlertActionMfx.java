// Made from shadcn's alert.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.callout;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NullMarked;

/// shadcn's `AlertAction` (`data-slot="alert-action"`, alert.tsx), as a JavaFX StackPane.
/// The look is in `components.css` (style class `mfx-alert-action`).
@NullMarked
public class AlertActionMfx extends StackPane {

    /// The style class every AlertActionMfx has.
    public static final String STYLE_CLASS = "mfx-alert-action";

    public AlertActionMfx(Node... children) {
        super(children);
        getStyleClass().add(STYLE_CLASS);
    }
}
