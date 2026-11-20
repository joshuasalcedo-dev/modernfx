package io.joshuasalcedo.fx.ui.core.fab;

import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/// The floating action button: a round button for the screen's main action, floating in a corner over the content.
/// Put it in a StackPane on top of the content; `position` picks the corner. With `extended` it shows its text too.
///
/// ```java
/// var fab = new FabMfx(new FontIcon(Feather.PLUS), "New chat");
/// var root = new StackPane(content, fab);
/// ```
public class FabMfx extends Button {

    public static final String STYLE_CLASS = "mfx-fab";

    private final ObjectProperty<Pos> position = new SimpleObjectProperty<>(this, "position", Pos.BOTTOM_RIGHT);
    private final BooleanProperty extended = new SimpleBooleanProperty(this, "extended");

    public FabMfx(Node icon, String text) {
        super(text, Objects.requireNonNull(icon, "icon"));
        getStyleClass().add(STYLE_CLASS);
        setTooltip(new Tooltip(text));
        setAccessibleText(text);
        // TODO: StackPane.setAlignment(this, position) and a margin; show the text only when extended
    }

    public final ObjectProperty<Pos> positionProperty() { return position; }
    public final Pos getPosition() { return position.get(); }
    public final void setPosition(Pos value) { position.set(value); }

    /// When true the button shows its text beside the icon.
    public final BooleanProperty extendedProperty() { return extended; }
    public final boolean isExtended() { return extended.get(); }
    public final void setExtended(boolean value) { extended.set(value); }
}
