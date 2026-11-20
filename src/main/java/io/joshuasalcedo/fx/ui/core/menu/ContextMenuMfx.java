package io.joshuasalcedo.fx.ui.core.menu;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.jspecify.annotations.NullMarked;

/// shadcn's ContextMenu: the menu a right-click opens (JavaFX's ContextMenu). Build items with [MenuItemsMfx].
///
/// ```java
/// new ContextMenuMfx(MenuItemsMfx.item("Back", null, "Alt+Left", this::back)).attach(area);
/// ```
@NullMarked
public class ContextMenuMfx extends ContextMenu {

    /// The style class every ContextMenuMfx has.
    public static final String STYLE_CLASS = "mfx-context-menu";

    public ContextMenuMfx(MenuItem... items) {
        super(items);
        getStyleClass().add(STYLE_CLASS);
    }

    /// Opens this menu when `node` is right-clicked.
    public ContextMenuMfx attach(Node node) {
        node.setOnContextMenuRequested(e -> show(node, e.getScreenX(), e.getScreenY()));
        return this;
    }
}
