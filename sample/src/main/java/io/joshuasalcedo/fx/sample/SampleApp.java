package io.joshuasalcedo.fx.sample;

import io.joshuasalcedo.fx.ui.core.button.ButtonMfx;
import io.joshuasalcedo.fx.ui.core.button.ButtonSize;
import io.joshuasalcedo.fx.ui.core.button.ButtonVariant;
import io.joshuasalcedo.fx.ui.core.buttongroup.ButtonGroupMfx;
import io.joshuasalcedo.fx.ui.core.iconbutton.IconButtonMfx;
import io.joshuasalcedo.fx.ui.core.link.LinkMfx;
import io.joshuasalcedo.fx.ui.core.mdi.DesktopPane;
import io.joshuasalcedo.fx.ui.core.mdi.InternalWindow;
import io.joshuasalcedo.fx.ui.core.splitbutton.SplitButtonMfx;
import io.joshuasalcedo.fx.utils.tailwind.Tw;
import io.joshuasalcedo.fx.utils.theme.ThemeMfx;
import java.util.function.Supplier;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// A small back office built with ModernFX: each screen is a window inside the main window (ui.core.mdi), opened
/// from the bar at the top. Contacts is a list, Invoices a data table, Tasks a board, Files a tree, Timesheet a
/// spreadsheet.
public class SampleApp extends Application {

    private final DesktopPane desktop = new DesktopPane();
    private int opened;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        LinkMfx.useHostServices(getHostServices());

        Label title = Tw.set(new Label("Back office"), "text-base font-semibold text-foreground");
        ButtonGroupMfx screens = new ButtonGroupMfx(
                screen("Contacts", Feather.USERS, () -> open("contacts", Feather.USERS, "Contacts", Screens::contacts, 600, 560)),
                screen("Invoices", Feather.FILE_TEXT, () -> open("invoices", Feather.FILE_TEXT, "Invoices", Screens::invoices, 860, 520)),
                screen("Tasks", Feather.COLUMNS, () -> open("tasks", Feather.COLUMNS, "Tasks", Screens::tasks, 900, 460)),
                screen("Files", Feather.FOLDER, () -> open("files", Feather.FOLDER, "Files", Screens::files, 360, 440)),
                screen("Timesheet", Feather.GRID, () -> open("timesheet", Feather.GRID, "Timesheet", Screens::timesheet, 620, 460)));

        SplitButtonMfx arrange = new SplitButtonMfx("Tile");
        arrange.setVariant(ButtonVariant.OUTLINE);
        arrange.setSize(ButtonSize.SM);
        arrange.setOnAction(e -> desktop.tileVisibleWindows());
        arrange.getItems().addAll(
                item("Side by side", desktop::tileVertically),
                item("Stacked", desktop::tileHorizontally),
                item("Minimize all", desktop::minimizeAllWindows),
                item("Restore all", desktop::restoreMinimizedWindows),
                item("Close all", desktop::closeAllWindows));

        IconButtonMfx theme = new IconButtonMfx(new FontIcon(Feather.MOON), "Light or dark").variant(ButtonVariant.GHOST);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(12, title, screens, spacer, arrange, theme);
        bar.setAlignment(Pos.CENTER_LEFT);
        Tw.set(bar, "px-4 py-2 bg-background border-b");

        BorderPane root = new BorderPane(desktop);
        root.setTop(bar);
        Tw.set(desktop, "bg-muted");

        Scene scene = new Scene(root, 1360, 860);
        ThemeMfx.install(scene);
        theme.setOnAction(e -> ThemeMfx.toggle(scene));

        stage.setTitle("ModernFX Sample");
        stage.setScene(scene);
        stage.show();

        open("invoices", Feather.FILE_TEXT, "Invoices", Screens::invoices, 860, 520);
        open("contacts", Feather.USERS, "Contacts", Screens::contacts, 600, 560);
    }

    private static ButtonMfx screen(String text, Ikon icon, Runnable open) {
        ButtonMfx b = new ButtonMfx(text, new FontIcon(icon)).variant(ButtonMfx.Variant.OUTLINE).size(ButtonMfx.Size.SM);
        b.setOnAction(e -> open.run());
        return b;
    }

    private static MenuItem item(String text, Runnable run) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(e -> run.run());
        return item;
    }

    /// Brings a screen's window to the front, or opens it, a little below and right of the last one.
    private void open(String id, Ikon icon, String title, Supplier<Node> content, double width, double height) {
        desktop.findInternalWindow(id).ifPresentOrElse(desktop::toFront, () -> {
            InternalWindow window = new InternalWindow(id, new FontIcon(icon), title, content.get());
            window.setPrefSize(width, height);
            double offset = 24 + (opened++ % 8) * 32;
            desktop.addInternalWindow(window, new Point2D(offset, offset));
        });
    }
}
