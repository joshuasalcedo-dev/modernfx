package io.joshuasalcedo.fx.ui.core.fab;

import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NullMarked;

/// The floating action button: a round button for the screen's main action, floating in a corner over the content.
/// Put it in a StackPane on top of the content; `position` picks the corner. With `extended` it shows its text too.
///
/// ```java
/// var fab = new FabMfx(new FontIcon(Feather.PLUS), "New chat");
/// var root = new StackPane(content, fab);
/// ```
@NullMarked
public class FabMfx extends Button {

    public static final String STYLE_CLASS = "mfx-fab";

    /// The space between the button and the edges of the StackPane, in pixels.
    public static final double MARGIN = 24;

    private static final PseudoClass EXTENDED = PseudoClass.getPseudoClass("extended");

    private final ObjectProperty<Pos> position = new SimpleObjectProperty<>(this, "position", Pos.BOTTOM_RIGHT) {
        @Override
        protected void invalidated() {
            StackPane.setAlignment(FabMfx.this, get());
        }
    };
    private final BooleanProperty extended = new SimpleBooleanProperty(this, "extended") {
        @Override
        protected void invalidated() {
            setContentDisplay(get() ? ContentDisplay.LEFT : ContentDisplay.GRAPHIC_ONLY);
            pseudoClassStateChanged(EXTENDED, get());
        }
    };

    public FabMfx(Node icon, String text) {
        super(text, Objects.requireNonNull(icon, "icon"));
        getStyleClass().remove("button");
        getStyleClass().add(STYLE_CLASS);
        setTooltip(new Tooltip(text));
        setAccessibleText(text);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        StackPane.setAlignment(this, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(this, new Insets(MARGIN));
    }

    public final ObjectProperty<Pos> positionProperty() { return position; }
    public final Pos getPosition() { return position.get(); }
    public final void setPosition(Pos value) { position.set(value); }

    /// When true the button shows its text beside the icon.
    public final BooleanProperty extendedProperty() { return extended; }
    public final boolean isExtended() { return extended.get(); }
    public final void setExtended(boolean value) { extended.set(value); }
}
