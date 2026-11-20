package io.joshuasalcedo.fx.ui.core.menu;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import org.jspecify.annotations.NullMarked;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// shadcn's DropdownMenu: a button that opens a menu (JavaFX's MenuButton). The button looks like an outline
/// button; the menu like shadcn's. Build items with [MenuItemsMfx].
@NullMarked
public class DropdownMenuMfx extends MenuButton {

    /// The style class every DropdownMenuMfx has.
    public static final String STYLE_CLASS = "mfx-dropdown-menu";

    public DropdownMenuMfx(String text, MenuItem... items) {
        super(text, new FontIcon(Feather.CHEVRON_DOWN), items);
        getStyleClass().add(STYLE_CLASS);
        setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
    }
}
