package io.joshuasalcedo.fx.ui.core.menu;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

/// The items of shadcn's menus (DropdownMenu, ContextMenu, Menubar), as JavaFX menu items.
///
/// ```java
/// menu.getItems().addAll(
///         MenuItemsMfx.label("My account"),
///         MenuItemsMfx.item("Profile", Feather.USER, "Shortcut+P", this::openProfile),
///         MenuItemsMfx.separator(),
///         MenuItemsMfx.item("Log out", Feather.LOG_OUT, null, this::logOut));
/// ```
@NullMarked
public final class MenuItemsMfx {

    private MenuItemsMfx() {
    }

    /// An item with an icon and a shortcut (both optional). The shortcut is a KeyCombination, e.g. "Shortcut+P".
    public static MenuItem item(String text, @Nullable Ikon icon, @Nullable String shortcut, Runnable onAction) {
        MenuItem item = new MenuItem(text, icon == null ? null : new FontIcon(icon));
        if (shortcut != null) {
            item.setAccelerator(KeyCombination.valueOf(shortcut));
        }
        item.setOnAction(e -> onAction.run());
        return item;
    }

    /// An item in red, for actions that delete or can't be undone (shadcn's variant="destructive").
    public static MenuItem destructive(String text, @Nullable Ikon icon, Runnable onAction) {
        MenuItem item = item(text, icon, null, onAction);
        item.getStyleClass().add("mfx-menu-destructive");
        return item;
    }

    /// An item with a check mark.
    public static CheckMenuItem checkbox(String text, boolean checked) {
        CheckMenuItem item = new CheckMenuItem(text);
        item.setSelected(checked);
        return item;
    }

    /// A heading for the items under it; can't be clicked.
    public static MenuItem label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("mfx-menu-label");
        CustomMenuItem item = new CustomMenuItem(label, false);
        item.setDisable(true);
        item.getStyleClass().add("mfx-menu-label-item");
        return item;
    }

    public static SeparatorMenuItem separator() {
        return new SeparatorMenuItem();
    }
}
