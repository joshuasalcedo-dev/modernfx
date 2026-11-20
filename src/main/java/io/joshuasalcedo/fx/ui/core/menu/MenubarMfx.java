package io.joshuasalcedo.fx.ui.core.menu;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import org.jspecify.annotations.NullMarked;

/// shadcn's Menubar: a row of menus (File, Edit, View) in a bordered box (JavaFX's MenuBar). Build items with
/// [MenuItemsMfx].
@NullMarked
public class MenubarMfx extends MenuBar {

    /// The style class every MenubarMfx has.
    public static final String STYLE_CLASS = "mfx-menubar";

    public MenubarMfx(Menu... menus) {
        super(menus);
        getStyleClass().add(STYLE_CLASS);
        setMaxWidth(USE_PREF_SIZE);
    }
}
