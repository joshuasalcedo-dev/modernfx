package io.joshuasalcedo.fx.ui.core.collapsible;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;

/// shadcn's Collapsible: a trigger that shows and hides the content under it. Unstyled, as in shadcn: the
/// trigger and the content look like whatever you give it.
///
/// ```java
/// new CollapsibleMfx(new ButtonMfx("Show 3 more").variant(GHOST), moreRepositories);
/// ```
@NullMarked
public class CollapsibleMfx extends VBox {

    /// The style class every CollapsibleMfx has.
    public static final String STYLE_CLASS = "mfx-collapsible";

    private final BooleanProperty open = new SimpleBooleanProperty(this, "open");

    public CollapsibleMfx(Node trigger, Node content) {
        super(trigger, content);
        getStyleClass().add(STYLE_CLASS);
        content.visibleProperty().bind(open);
        content.managedProperty().bind(open);
        trigger.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> toggle());
        if (trigger instanceof javafx.scene.control.ButtonBase b) {
            // a button fires on Space and Enter too; MOUSE_CLICKED already covers the mouse
            b.addEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, e -> {
                if (e.getCode() == javafx.scene.input.KeyCode.SPACE || e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    toggle();
                }
            });
        }
    }

    public void toggle() {
        open.set(!open.get());
    }

    /// Whether the content shows.
    public BooleanProperty openProperty() {
        return open;
    }

    public boolean isOpen() {
        return open.get();
    }

    public void setOpen(boolean value) {
        open.set(value);
    }
}
