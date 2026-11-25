package io.joshuasalcedo.fx;

import io.joshuasalcedo.fx.ui.core.accordion.AccordionMfx;
import io.joshuasalcedo.fx.ui.core.avatar.AvatarFallbackMfx;
import io.joshuasalcedo.fx.ui.core.avatar.AvatarMfx;
import io.joshuasalcedo.fx.ui.core.badge.BadgeMfx;
import io.joshuasalcedo.fx.ui.core.breadcrumb.BreadcrumbMfx;
import io.joshuasalcedo.fx.ui.core.button.ButtonMfx;
import io.joshuasalcedo.fx.ui.core.calendar.CalendarMfx;
import io.joshuasalcedo.fx.ui.core.callout.AlertDescriptionMfx;
import io.joshuasalcedo.fx.ui.core.callout.AlertMfx;
import io.joshuasalcedo.fx.ui.core.callout.AlertTitleMfx;
import io.joshuasalcedo.fx.ui.core.card.CardContentMfx;
import io.joshuasalcedo.fx.ui.core.card.CardDescriptionMfx;
import io.joshuasalcedo.fx.ui.core.card.CardFooterMfx;
import io.joshuasalcedo.fx.ui.core.card.CardHeaderMfx;
import io.joshuasalcedo.fx.ui.core.card.CardMfx;
import io.joshuasalcedo.fx.ui.core.card.CardTitleMfx;
import io.joshuasalcedo.fx.ui.core.checkbox.CheckboxMfx;
import io.joshuasalcedo.fx.ui.core.collapsible.CollapsibleMfx;
import io.joshuasalcedo.fx.ui.core.combobox.ComboboxMfx;
import io.joshuasalcedo.fx.ui.core.datepicker.DatePickerMfx;
import io.joshuasalcedo.fx.ui.core.dialog.AlertDialogMfx;
import io.joshuasalcedo.fx.ui.core.dialog.DialogMfx;
import io.joshuasalcedo.fx.ui.core.divider.ResizableMfx;
import io.joshuasalcedo.fx.ui.core.drawer.DrawerMfx;
import io.joshuasalcedo.fx.ui.core.input.InputMfx;
import io.joshuasalcedo.fx.ui.core.kbd.KbdGroupMfx;
import io.joshuasalcedo.fx.ui.core.kbd.KbdMfx;
import io.joshuasalcedo.fx.ui.core.label.LabelMfx;
import io.joshuasalcedo.fx.ui.core.menu.ContextMenuMfx;
import io.joshuasalcedo.fx.ui.core.menu.DropdownMenuMfx;
import io.joshuasalcedo.fx.ui.core.menu.MenuItemsMfx;
import io.joshuasalcedo.fx.ui.core.menu.MenubarMfx;
import io.joshuasalcedo.fx.ui.core.modal.ModalMfx;
import io.joshuasalcedo.fx.ui.core.pagination.PaginationMfx;
import io.joshuasalcedo.fx.ui.core.popover.HoverCardMfx;
import io.joshuasalcedo.fx.ui.core.popover.PopoverMfx;
import io.joshuasalcedo.fx.ui.core.progress.ProgressMfx;
import io.joshuasalcedo.fx.ui.core.radiogroup.RadioGroupMfx;
import io.joshuasalcedo.fx.ui.core.scrollarea.ScrollAreaMfx;
import io.joshuasalcedo.fx.ui.core.select.SelectMfx;
import io.joshuasalcedo.fx.ui.core.separator.SeparatorMfx;
import io.joshuasalcedo.fx.ui.core.sheet.SheetMfx;
import io.joshuasalcedo.fx.ui.core.skeleton.SkeletonMfx;
import io.joshuasalcedo.fx.ui.core.slider.SliderMfx;
import io.joshuasalcedo.fx.ui.core.tab.TabsMfx;
import io.joshuasalcedo.fx.ui.core.table.TableMfx;
import io.joshuasalcedo.fx.ui.core.textarea.TextareaMfx;
import io.joshuasalcedo.fx.ui.core.toast.ToastMfx;
import io.joshuasalcedo.fx.ui.core.toggle.SwitchMfx;
import io.joshuasalcedo.fx.ui.core.togglegroup.ToggleGroupMfx;
import io.joshuasalcedo.fx.ui.core.tooltip.TooltipMfx;
import io.joshuasalcedo.fx.utils.state.Store;
import io.joshuasalcedo.fx.utils.tailwind.Tailwind;
import io.joshuasalcedo.fx.utils.tailwind.Tw;
import io.joshuasalcedo.fx.utils.tailwind.TwColor;
import io.joshuasalcedo.fx.utils.theme.ThemeMfx;
import java.time.LocalDate;
import java.util.stream.Stream;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.TreeMap;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// Every ModernFX package on one screen. The sidebar follows the packages: a group per package (`ui.core`,
/// `ui`, `utils`) and an item per subpackage. Packages that are only stubs so far say so and list their classes.
///
/// Run it from your IDE, or: `mvn -q compile exec:java -Dexec.mainClass=io.joshuasalcedo.fx.ModernFXShowcase`.
@NullMarked
public class ModernFXShowcase extends Application {

    /// One page: `pkg` is the package under `io.joshuasalcedo.fx` (e.g. `ui.core.button`).
    public record Page(String group, String pkg, String description, Supplier<Node> content) {
        String name() {
            return pkg.substring(pkg.lastIndexOf('.') + 1);
        }
    }

    private static final String STUB = "Not built yet.";

    private final List<Page> pages = pages();
    private final Map<String, ButtonMfx> links = new LinkedHashMap<>();
    private final VBox content = new VBox();
    private final SwitchMfx dark = new SwitchMfx("Dark mode");
    private @Nullable Scene scene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("ModernFX");
        stage.setScene(createScene());
        stage.show();
    }

    /// The showcase's scene, with the theme installed and the first page open.
    public Scene createScene() {
        BorderPane frame = new BorderPane();
        frame.setLeft(sidebar());
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        Tw.set(scroll, "bg-background");
        Tw.set(content, "p-8 gap-6 bg-background");
        frame.setCenter(scroll);
        scene = new Scene(new StackPane(frame), 1180, 760);
        ThemeMfx.install(scene);
        dark.setSelected(ThemeMfx.isDark(scene));
        ThemeMfx.darkProperty(scene).addListener((obs, old, on) -> dark.setSelected(on));
        dark.selectedProperty().addListener((obs, old, on) -> {
            if (scene != null && on != ThemeMfx.isDark(scene)) {
                ThemeMfx.setMode(scene, on ? ThemeMfx.Mode.DARK : ThemeMfx.Mode.LIGHT);
            }
        });
        show(pages.getFirst().pkg());
        return scene;
    }

    /// Opens a page by its package (`ui.core.button`) or its last part (`button`).
    public void show(String pkg) {
        Page page = pages.stream().filter(p -> p.pkg().equals(pkg)).findFirst()
                .or(() -> pages.stream().filter(p -> p.name().equals(pkg)).findFirst())
                .orElseThrow(() -> new IllegalArgumentException("no page " + pkg));
        links.forEach((k, link) -> link.variant(k.equals(page.pkg()) ? ButtonMfx.Variant.SECONDARY : ButtonMfx.Variant.GHOST));
        Label title = Tw.set(new Label(page.name()), "text-3xl font-semibold text-foreground");
        Label path = Tw.set(new Label("io.joshuasalcedo.fx." + page.pkg()), "text-sm font-mono text-muted-foreground");
        Label description = Tw.set(new Label(page.description()), "text-base text-muted-foreground");
        description.setWrapText(true);
        VBox header = new VBox(title, path, description);
        Tw.set(header, "gap-1");
        content.getChildren().setAll(header, new SeparatorMfx(), page.content().get());
    }

    /// The pages' packages, in sidebar order.
    public List<String> pageNames() {
        return pages.stream().map(Page::pkg).toList();
    }

    // ------------------------------------------------------------------ sidebar

    /// Left padding per level of the package tree, so the text of each level lines up under its parent.
    private static final int INDENT = 12;
    private static final int TEXT_START = 10;

    private Node sidebar() {
        VBox nav = new VBox();
        Tw.set(nav, "gap-0.5");
        List<String> open = new ArrayList<>();
        for (Page page : pages) {
            // a heading for each part of the package path not yet shown: "ui", then "core"
            String[] parts = page.pkg().split("\\.");
            List<String> parents = List.of(parts).subList(0, parts.length - 1);
            int same = 0;
            while (same < open.size() && same < parents.size() && open.get(same).equals(parents.get(same))) {
                same++;
            }
            for (int level = same; level < parents.size(); level++) {
                Label heading = new Label(parents.get(level));
                Tw.set(heading, level == 0 ? "text-xs font-semibold text-foreground" : "text-xs font-medium text-muted-foreground");
                Tw.apply(heading, "pr-2 pb-1 pt-" + (level == 0 ? "5" : "3") + " pl-[" + (TEXT_START + level * INDENT) + "px]");
                nav.getChildren().add(heading);
            }
            open = new ArrayList<>(parents);
            open.add(page.name());

            boolean stub = page.description().startsWith(STUB);
            ButtonMfx link = new ButtonMfx(page.name()).variant(ButtonMfx.Variant.GHOST).size(ButtonMfx.Size.SM);
            Tw.apply(link, (stub ? "w-full justify-start text-muted-foreground" : "w-full justify-start")
                    + " pr-2.5 pl-[" + (TEXT_START + parents.size() * INDENT) + "px]");
            link.setOnAction(e -> show(page.pkg()));
            links.put(page.pkg(), link);
            nav.getChildren().add(link);
        }
        Label brand = Tw.set(new Label("ModernFX"), "text-lg font-semibold text-sidebar-foreground");
        HBox header = new HBox(brand, new BadgeMfx("0.1").variant(BadgeMfx.Variant.SECONDARY));
        Tw.set(header, "gap-2 items-center px-2");
        ScrollPane navScroll = new ScrollPane(nav);
        navScroll.setFitToWidth(true);
        navScroll.getStyleClass().add("mfx-scroll-area");
        Tw.set(navScroll, "grow");
        VBox sidebar = new VBox(header, navScroll, new SeparatorMfx(), dark);
        Tw.set(sidebar, "w-60 p-3 gap-2 bg-sidebar border-r");
        return sidebar;
    }

    // ------------------------------------------------------------------ the pages, by package

    /// The sidebar's pages: one per package, read from the classes themselves, so new packages show up by
    /// themselves. Packages with a demo below show it; the others say they aren't built yet and list their classes.
    private static List<Page> pages() {
        Map<String, Page> demos = new LinkedHashMap<>();
        for (Page page : demos()) {
            demos.put(page.pkg(), page);
        }
        Map<String, List<String>> packages = packages();
        List<Page> out = new ArrayList<>();
        for (var e : packages.entrySet()) {
            String pkg = e.getKey();
            if (!(pkg.startsWith("ui.") || pkg.startsWith("utils.")) || pkg.contains("internal")) {
                continue;
            }
            // a package that only holds other packages is a heading, not a page
            boolean onlyHeading = e.getValue().isEmpty() && packages.keySet().stream().anyMatch(k -> k.startsWith(pkg + "."));
            if (demos.containsKey(pkg)) {
                out.add(demos.remove(pkg));
            } else if (!onlyHeading) {
                out.add(notBuilt(pkg, e.getValue()));
            }
        }
        out.addAll(demos.values());
        return out;
    }

    private static List<Page> demos() {
        List<Page> p = new ArrayList<>();
        String core = "ui.core";
        p.add(new Page(core, "ui.core.accordion", "Questions that open one at a time.", ModernFXShowcase::accordion));
        p.add(new Page(core, "ui.core.avatar", "A round picture of a person, with initials when there is none.",
                ModernFXShowcase::avatars));
        p.add(new Page(core, "ui.core.badge", "A small label for a status or a count.", ModernFXShowcase::badges));
        p.add(new Page(core, "ui.core.breadcrumb", "Where you are, as links back up the path.", ModernFXShowcase::breadcrumb));
        p.add(new Page(core, "ui.core.button", "A button, in shadcn's variants and sizes.", ModernFXShowcase::buttons));
        p.add(new Page(core, "ui.core.calendar", "A month to pick a day from.", ModernFXShowcase::calendar));
        p.add(new Page(core, "ui.core.callout", "shadcn's Alert: a note that calls for the user's attention.",
                ModernFXShowcase::alerts));
        p.add(new Page(core, "ui.core.card", "A box with a header, content and a footer.", ModernFXShowcase::cards));
        p.add(new Page(core, "ui.core.checkbox", "A box that is checked or not.", ModernFXShowcase::checkboxes));
        p.add(new Page(core, "ui.core.collapsible", "A trigger that shows and hides the content under it.",
                ModernFXShowcase::collapsible));
        p.add(new Page(core, "ui.core.combobox", "A select you can type into to filter the options.",
                ModernFXShowcase::combobox));
        p.add(new Page(core, "ui.core.datepicker", "A field that opens a calendar.", ModernFXShowcase::datePicker));
        p.add(new Page(core, "ui.core.dialog", "A box over the screen: Dialog, and AlertDialog to confirm.",
                ModernFXShowcase::dialogs));
        p.add(new Page(core, "ui.core.divider", "shadcn's Resizable: panels with a line between them that can be dragged.",
                ModernFXShowcase::resizable));
        p.add(new Page(core, "ui.core.drawer", "shadcn's Sheet and Drawer: a panel that slides in from an edge.",
                ModernFXShowcase::drawers));
        p.add(new Page(core, "ui.core.input", "A field for one line of text.", ModernFXShowcase::inputs));
        p.add(new Page(core, "ui.core.kbd", "A key or a keyboard shortcut.", ModernFXShowcase::kbds));
        p.add(new Page(core, "ui.core.label", "The name of a field.", ModernFXShowcase::labels));
        p.add(new Page(core, "ui.core.menu", "DropdownMenu, ContextMenu and Menubar.", ModernFXShowcase::menus));
        p.add(new Page(core, "ui.core.modal", "The layer dialogs, drawers and toasts show in.", ModernFXShowcase::modal));
        p.add(new Page(core, "ui.core.pagination", "Previous, next and the page numbers.", ModernFXShowcase::pagination));
        p.add(new Page(core, "ui.core.popover", "A floating box under a control: Popover, and HoverCard on hover.",
                ModernFXShowcase::popovers));
        p.add(new Page(core, "ui.core.progress", "How far along a task is.", ModernFXShowcase::progress));
        p.add(new Page(core, "ui.core.radiogroup", "A column of options where one is picked.", ModernFXShowcase::radioGroup));
        p.add(new Page(core, "ui.core.scrollarea", "Scrolling content with thin scrollbars.", ModernFXShowcase::scrollArea));
        p.add(new Page(core, "ui.core.select", "Pick one option from a list.", ModernFXShowcase::select));
        p.add(new Page(core, "ui.core.separator", "A thin line between items.", ModernFXShowcase::separators));
        p.add(new Page(core, "ui.core.sheet", "A spreadsheet: ControlsFX's SpreadsheetView with the shadcn look.",
                ModernFXShowcase::sheets));
        p.add(new Page(core, "ui.core.skeleton", "A placeholder while content loads.", ModernFXShowcase::skeletons));
        p.add(new Page(core, "ui.core.slider", "Pick a number by dragging.", ModernFXShowcase::slider));
        p.add(new Page(core, "ui.core.tab", "shadcn's Tabs: switch between views.", ModernFXShowcase::tabs));
        p.add(new Page(core, "ui.core.table", "Rows and columns, a line under each row.", ModernFXShowcase::table));
        p.add(new Page(core, "ui.core.textarea", "A field for several lines of text.", ModernFXShowcase::textareas));
        p.add(new Page(core, "ui.core.toast", "A short message that goes away by itself.", ModernFXShowcase::toasts));
        p.add(new Page(core, "ui.core.toggle", "shadcn's Switch: on or off.", ModernFXShowcase::switches));
        p.add(new Page(core, "ui.core.togglegroup", "Buttons joined in a row that stay pressed.", ModernFXShowcase::toggleGroup));
        p.add(new Page(core, "ui.core.tooltip", "A small label that shows while the pointer rests on a node.",
                ModernFXShowcase::tooltips));
        p.add(new Page(core, "ui.core.board", "A Kanban board: columns of cards you drag between.", CollectionDemos::board));
        p.add(new Page(core, "ui.core.buttongroup", "Buttons joined into one bar.", CollectionDemos::buttonGroup));
        p.add(new Page(core, "ui.core.data", "Where collections get their items, a page at a time, and what is selected.",
                CollectionDemos::data));
        p.add(new Page(core, "ui.core.dnd", "Drag rows to reorder them.", CollectionDemos::dnd));
        p.add(new Page(core, "ui.core.fab", "The floating action button: the screen's main action.", CollectionDemos::fab));
        p.add(new Page(core, "ui.core.gridview", "A grid of tiles, such as a gallery.", CollectionDemos::gridView));
        p.add(new Page(core, "ui.core.iconbutton", "A button with only an icon, and a tooltip.", CollectionDemos::iconButtons));
        p.add(new Page(core, "ui.core.link", "A text link.", CollectionDemos::links));
        p.add(new Page(core, "ui.core.list", "A list with rows, sections, paging, selection and search.", CollectionDemos::list));
        p.add(new Page(core, "ui.core.placeholder", "What shows while loading, when empty, or on an error.",
                CollectionDemos::placeholder));
        p.add(new Page(core, "ui.core.segmented", "A row of options where one is selected.", CollectionDemos::segmented));
        p.add(new Page(core, "ui.core.splitbutton", "A main action with a menu of others.", CollectionDemos::splitButtons));
        p.add(new Page(core, "ui.core.togglebutton", "shadcn's Toggle: a button that stays pressed.", CollectionDemos::toggleButtons));
        p.add(new Page(core, "ui.core.toolbar", "The bar above a collection: search, filters, view, bulk actions.",
                CollectionDemos::toolbar));
        p.add(new Page(core, "ui.core.tree", "A tree with lazy children and indent guides.", CollectionDemos::tree));
        p.add(new Page(core, "ui.core.treetable", "Rows that open, in columns.", CollectionDemos::treeTable));
        p.add(new Page("utils", "utils.state", "App state in one place (Store), kept in a JSON file (PersistentStore).",
                ModernFXShowcase::state));
        p.add(new Page("utils", "utils.tailwind", "Tailwind classes on JavaFX nodes (Tw), and the palette (TwColor).",
                ModernFXShowcase::tailwind));
        p.add(new Page("utils", "utils.theme", "ThemeMfx: the shadcn tokens, light and dark.", ModernFXShowcase::theme));
        return p;
    }

    private static Page notBuilt(String pkg, List<String> classes) {
        List<String> sorted = classes.stream().sorted().toList();
        String what = sorted.isEmpty() ? "Nothing in it yet." : "Classes so far: " + sorted.size() + ".";
        return new Page(pkg.substring(0, pkg.lastIndexOf('.')), pkg, STUB + " " + what, () -> {
            if (sorted.isEmpty()) {
                return text("The package is there, waiting for its components.", "text-sm text-muted-foreground");
            }
            List<Node> badges = new ArrayList<>();
            for (String c : sorted) {
                badges.add(new BadgeMfx(c).variant(BadgeMfx.Variant.OUTLINE));
            }
            return column(section("In the package (most are stubs, with a // TODO)", badges.toArray(Node[]::new)));
        });
    }

    /// io.joshuasalcedo.fx's packages (as `ui.core.button`) and their top-level classes, read from where this
    /// class was loaded: the classes folder when run from an IDE, or the jar.
    public static Map<String, List<String>> packages() {
        Map<String, List<String>> found = new TreeMap<>();
        String base = "io/joshuasalcedo/fx/";
        try {
            Path where = Path.of(ModernFXShowcase.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isDirectory(where)) {
                Path root = where.resolve(base);
                try (Stream<Path> files = Files.walk(root)) {
                    files.forEach(f -> {
                        String rel = root.relativize(f).toString().replace('\\', '/');
                        if (Files.isDirectory(f)) {
                            if (!rel.isEmpty()) {
                                found.computeIfAbsent(rel.replace('/', '.'), k -> new ArrayList<>());
                            }
                        } else {
                            addClass(found, rel);
                        }
                    });
                }
            } else {
                try (JarFile jar = new JarFile(where.toFile())) {
                    jar.stream().map(JarEntry::getName).filter(n -> n.startsWith(base) && !n.endsWith("/"))
                            .forEach(n -> addClass(found, n.substring(base.length())));
                }
            }
        } catch (Exception e) {
            // the showcase still shows the pages with demos
        }
        return found;
    }

    private static void addClass(Map<String, List<String>> found, String rel) {
        int slash = rel.lastIndexOf('/');
        if (slash < 0 || !rel.endsWith(".class") || rel.contains("$")) {
            return;
        }
        List<String> classes = found.computeIfAbsent(rel.substring(0, slash).replace('/', '.'), k -> new ArrayList<>());
        String name = rel.substring(slash + 1, rel.length() - ".class".length());
        if (!name.equals("package-info")) {
            classes.add(name);
        }
    }

    // ------------------------------------------------------------------ helpers

    private static Node section(String title, Node... examples) {
        FlowPane row = new FlowPane(examples);
        Tw.set(row, "gap-3 items-center");
        VBox box = new VBox(Tw.set(new Label(title), "text-sm font-medium text-muted-foreground"), row);
        Tw.set(box, "gap-3");
        return box;
    }

    private static Node column(Node... sections) {
        VBox box = new VBox(sections);
        Tw.set(box, "gap-8");
        return box;
    }

    private static FontIcon icon(Feather icon) {
        FontIcon i = new FontIcon(icon);
        i.setIconSize(16);
        return i;
    }

    private static String cap(String s) {
        return s.charAt(0) + s.substring(1).toLowerCase();
    }

    private static Label text(String s, String classes) {
        Label l = new Label(s);
        l.setWrapText(true);
        return Tw.set(l, classes);
    }

    // ------------------------------------------------------------------ ui.core pages

    private static Node buttons() {
        List<Node> variants = new ArrayList<>();
        for (ButtonMfx.Variant v : ButtonMfx.Variant.values()) {
            variants.add(new ButtonMfx(cap(v.name())).variant(v));
        }
        List<Node> sizes = new ArrayList<>();
        for (ButtonMfx.Size s : List.of(ButtonMfx.Size.XS, ButtonMfx.Size.SM, ButtonMfx.Size.DEFAULT, ButtonMfx.Size.LG)) {
            sizes.add(new ButtonMfx("Size " + s.name().toLowerCase()).size(s).variant(ButtonMfx.Variant.OUTLINE));
        }
        ButtonMfx disabled = new ButtonMfx("Disabled");
        disabled.setDisable(true);
        return column(
                section("Variants", variants.toArray(Node[]::new)),
                section("Sizes", sizes.toArray(Node[]::new)),
                section("With an icon",
                        new ButtonMfx("Upload", icon(Feather.UPLOAD)),
                        new ButtonMfx("Email", icon(Feather.MAIL)).variant(ButtonMfx.Variant.OUTLINE),
                        new ButtonMfx("Delete", icon(Feather.TRASH_2)).variant(ButtonMfx.Variant.DESTRUCTIVE)),
                section("Icon only",
                        iconButton(Feather.PLUS, ButtonMfx.Size.ICON_SM),
                        iconButton(Feather.SETTINGS, ButtonMfx.Size.ICON),
                        iconButton(Feather.ARROW_RIGHT, ButtonMfx.Size.ICON_LG)),
                section("Disabled", disabled));
    }

    private static ButtonMfx iconButton(Feather icon, ButtonMfx.Size size) {
        ButtonMfx b = new ButtonMfx("", icon(icon)).variant(ButtonMfx.Variant.OUTLINE).size(size);
        b.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        return b;
    }

    private static Node inputs() {
        VBox field = new VBox(new LabelMfx("Email"), new InputMfx("you@example.com"));
        Tw.set(field, "gap-2 w-[320px]");
        InputMfx invalid = new InputMfx("Name");
        invalid.setText("x");
        invalid.setInvalid(true);
        Tw.set(invalid, "w-[320px]");
        InputMfx disabled = new InputMfx("Disabled");
        disabled.setDisable(true);
        Tw.set(disabled, "w-[320px]");
        return column(section("With a label", field), section("Invalid", invalid), section("Disabled", disabled));
    }

    private static Node textareas() {
        TextareaMfx message = new TextareaMfx("Type your message here.");
        Tw.set(message, "w-[420px]");
        TextareaMfx invalid = new TextareaMfx("Bio");
        invalid.setInvalid(true);
        Tw.set(invalid, "w-[420px]");
        return column(section("Default", message), section("Invalid", invalid));
    }

    private static Node checkboxes() {
        CheckboxMfx on = new CheckboxMfx("Send me emails");
        on.setSelected(true);
        CheckboxMfx disabled = new CheckboxMfx("Disabled");
        disabled.setDisable(true);
        return column(section("Checkbox", new CheckboxMfx("Accept terms and conditions"), on, disabled));
    }

    private static Node switches() {
        SwitchMfx on = new SwitchMfx("Wi-Fi");
        on.setSelected(true);
        SwitchMfx disabled = new SwitchMfx("Disabled");
        disabled.setDisable(true);
        return column(section("Switch", new SwitchMfx("Airplane mode"), on, new SwitchMfx("Small").size(SwitchMfx.Size.SM),
                disabled));
    }

    private static Node labels() {
        HBox row = new HBox(new CheckboxMfx(), new LabelMfx("Accept terms and conditions"));
        Tw.set(row, "gap-2 items-center");
        return column(section("Next to a checkbox", row), section("Alone", new LabelMfx("Your email address")));
    }

    private static Node badges() {
        List<Node> variants = new ArrayList<>();
        for (BadgeMfx.Variant v : BadgeMfx.Variant.values()) {
            variants.add(new BadgeMfx(cap(v.name())).variant(v));
        }
        BadgeMfx verified = new BadgeMfx("Verified");
        verified.setGraphic(icon(Feather.CHECK_CIRCLE));
        return column(section("Variants", variants.toArray(Node[]::new)), section("With an icon", verified,
                new BadgeMfx("8").variant(BadgeMfx.Variant.DESTRUCTIVE), new BadgeMfx("99+").variant(BadgeMfx.Variant.OUTLINE)));
    }

    private static Node avatars() {
        return column(section("Sizes",
                new AvatarMfx(new AvatarFallbackMfx("JS")).size(AvatarMfx.Size.SM),
                new AvatarMfx(new AvatarFallbackMfx("JS")),
                new AvatarMfx(new AvatarFallbackMfx("JS")).size(AvatarMfx.Size.LG)));
    }

    private static Node progress() {
        VBox box = new VBox();
        for (double v : new double[] {0.13, 0.6, 1}) {
            box.getChildren().add(Tw.set(new ProgressMfx(v), "w-[360px]"));
        }
        Tw.set(box, "gap-4");
        return column(section("Progress", box));
    }

    private static Node skeletons() {
        VBox lines = new VBox(Tw.set(new SkeletonMfx(), "h-4 w-[250px]"), Tw.set(new SkeletonMfx(), "h-4 w-[200px]"));
        Tw.set(lines, "gap-2");
        HBox row = new HBox(Tw.set(new SkeletonMfx(), "size-12 rounded-full"), lines);
        Tw.set(row, "gap-4 items-center");
        return column(section("Loading a profile", row));
    }

    private static Node kbds() {
        return column(section("Keys", new KbdMfx("Ctrl"), new KbdMfx("Shift"), new KbdMfx("Enter")),
                section("A shortcut", new KbdGroupMfx(new KbdMfx("Ctrl"), new KbdMfx("K"))));
    }

    private static Node separators() {
        HBox links = new HBox(new Label("Blog"), vertical(), new Label("Docs"), vertical(), new Label("Source"));
        Tw.set(links, "gap-4 items-center h-5 text-sm");
        VBox box = new VBox(text("ModernFX", "text-sm font-medium"), text("shadcn components for JavaFX.",
                "text-sm text-muted-foreground"), new SeparatorMfx(), links);
        Tw.set(box, "gap-3 w-[360px]");
        return column(section("Across and down", box));
    }

    private static SeparatorMfx vertical() {
        return new SeparatorMfx().orientation(SeparatorMfx.Orientation.VERTICAL);
    }

    private static Node cards() {
        VBox email = new VBox(new LabelMfx("Email"), new InputMfx("you@example.com"));
        VBox password = new VBox(new LabelMfx("Password"), new InputMfx("Password"));
        Tw.set(email, "gap-2");
        Tw.set(password, "gap-2");
        ButtonMfx login = new ButtonMfx("Login");
        Tw.apply(login, "w-full");
        CardMfx card = new CardMfx(
                new CardHeaderMfx(new CardTitleMfx("Login to your account"),
                        new CardDescriptionMfx("Enter your email below to login to your account.")),
                new CardContentMfx(email, password),
                new CardFooterMfx(login));
        Tw.set(card, "w-[380px]");
        return column(section("Card", card));
    }

    private static Node alerts() {
        AlertMfx info = new AlertMfx(alertRow(Feather.TERMINAL, new AlertTitleMfx("Heads up!"),
                new AlertDescriptionMfx("You can add components to your app with the generator.")));
        AlertMfx error = new AlertMfx(alertRow(Feather.ALERT_CIRCLE, new AlertTitleMfx("Something went wrong"),
                new AlertDescriptionMfx("Your session has expired. Please sign in again.")))
                .variant(AlertMfx.Variant.DESTRUCTIVE);
        VBox box = new VBox(Tw.set(info, "w-[480px]"), Tw.set(error, "w-[480px]"));
        Tw.set(box, "gap-4");
        return column(section("Default and destructive", box));
    }

    /// shadcn puts the icon beside the title and description with a CSS grid; in JavaFX an HBox does it.
    private static Node alertRow(Feather icon, Node title, Node description) {
        VBox text = new VBox(title, description);
        Tw.set(text, "gap-0.5");
        FontIcon i = icon(icon);
        HBox row = new HBox(i, text);
        Tw.set(row, "gap-2.5 items-start");
        Tw.set(i, "mt-0.5");
        return row;
    }

    private static Node sheets() {
        try {
            SheetMfx.checkAccess();
        } catch (IllegalStateException e) {
            TextareaMfx options = new TextareaMfx();
            options.setText(SheetMfx.JVM_OPTIONS.replace(" --", "\n--"));
            options.setEditable(false);
            Tw.set(options, "w-[640px] h-[72px] font-mono text-sm");
            AlertMfx note = new AlertMfx(alertRow(Feather.ALERT_CIRCLE, new AlertTitleMfx("The sheet needs two JVM options"),
                    new AlertDescriptionMfx("Run as a module, ControlsFX's SpreadsheetView needs JavaFX internals opened to it. "
                            + "Add these to the run configuration's VM options and start again:")))
                    .variant(AlertMfx.Variant.DESTRUCTIVE);
            Tw.set(note, "w-[640px]");
            VBox box = new VBox(note, options);
            Tw.set(box, "gap-3");
            return box;
        }
        SheetMfx sheet = SheetMfx.of(List.of("Name", "Role", "Hours", "Rate", "Total"), List.of(
                List.of("Ana Cruz", "Designer", 38, 12.5, 475.0),
                List.of("Ben Ortiz", "Developer", 41, 13.0, 533.0),
                List.of("Carla Reyes", "Support", 35, 10.0, 350.0),
                List.of("Dan Lim", "Developer", 40, 13.0, 520.0),
                List.of("Eva Santos", "Manager", 42, 16.0, 672.0)));
        Tw.set(sheet, "h-[260px] w-[640px]");
        return column(section("Edit a cell by typing; select a range with the mouse or Shift + arrows", sheet));
    }

    // overlays -----------------------------------------------------------------

    private static DialogMfx profileDialog() {
        VBox name = new VBox(new LabelMfx("Name"), new InputMfx("Pedro Duarte"));
        VBox username = new VBox(new LabelMfx("Username"), new InputMfx("@peduarte"));
        Tw.set(name, "gap-2");
        Tw.set(username, "gap-2");
        DialogMfx d = new DialogMfx("Edit profile", "Make changes to your profile here. Click save when you're done.")
                .content(name, username);
        ButtonMfx save = new ButtonMfx("Save changes");
        save.setOnAction(e -> d.close());
        ButtonMfx cancel = new ButtonMfx("Cancel").variant(ButtonMfx.Variant.OUTLINE);
        cancel.setOnAction(e -> d.close());
        return d.footer(cancel, save);
    }

    private static Node dialogs() {
        ButtonMfx open = new ButtonMfx("Edit profile").variant(ButtonMfx.Variant.OUTLINE);
        open.setOnAction(e -> profileDialog().show(open));
        ButtonMfx confirm = new ButtonMfx("Delete account").variant(ButtonMfx.Variant.DESTRUCTIVE);
        confirm.setOnAction(e -> new AlertDialogMfx("Are you absolutely sure?",
                "This can't be undone. This will permanently delete your account and remove your data from our servers.")
                .action("Delete", ButtonMfx.Variant.DESTRUCTIVE, () -> ToastMfx.show(confirm, "Account deleted"))
                .show(confirm));
        return column(section("Open one", open, confirm), section("What it looks like", profileDialog()));
    }

    private static Node drawers() {
        List<Node> buttons = new ArrayList<>();
        for (Side side : Side.values()) {
            ButtonMfx b = new ButtonMfx(cap(side.name())).variant(ButtonMfx.Variant.OUTLINE);
            b.setOnAction(e -> drawer(side).show(b));
            buttons.add(b);
        }
        DrawerMfx preview = drawer(Side.RIGHT);
        Tw.set(preview, "h-[380px]");
        return column(section("Slide in from", buttons.toArray(Node[]::new)), section("What it looks like", preview));
    }

    private static DrawerMfx drawer(Side side) {
        VBox name = new VBox(new LabelMfx("Name"), new InputMfx("Pedro Duarte"));
        Tw.set(name, "gap-2");
        DrawerMfx d = new DrawerMfx(side, "Edit profile", "Make changes to your profile here.").content(name);
        ButtonMfx save = new ButtonMfx("Save changes");
        save.setOnAction(e -> d.close());
        return d.footer(save);
    }

    private static Node popovers() {
        VBox width = new VBox(new LabelMfx("Width"), new InputMfx("100%"));
        VBox height = new VBox(new LabelMfx("Height"), new InputMfx("25px"));
        Tw.set(width, "gap-2");
        Tw.set(height, "gap-2");
        VBox form = new VBox(text("Dimensions", "text-sm font-medium"), text("Set the dimensions for the layer.",
                "text-sm text-muted-foreground"), width, height);
        Tw.set(form, "gap-3 w-[256px]");
        PopoverMfx popover = new PopoverMfx(form);
        ButtonMfx open = new ButtonMfx("Open popover").variant(ButtonMfx.Variant.OUTLINE);
        open.setOnAction(e -> popover.toggle(open));

        Label user = Tw.set(new Label("@nextjs"), "text-sm font-medium underline cursor-pointer");
        VBox card = new VBox(text("@nextjs", "text-sm font-semibold"),
                text("The React Framework, created and maintained by @vercel.", "text-sm"),
                text("Joined December 2021", "text-xs text-muted-foreground"));
        Tw.set(card, "gap-1 w-[260px]");
        HoverCardMfx.attach(user, card);
        return column(section("Popover (click)", open), section("Hover card (rest the pointer on the name)", user));
    }

    private static Node toasts() {
        List<Node> buttons = new ArrayList<>();
        for (ToastMfx.Variant v : ToastMfx.Variant.values()) {
            ButtonMfx b = new ButtonMfx(cap(v.name())).variant(ButtonMfx.Variant.OUTLINE);
            b.setOnAction(e -> ToastMfx.show(b, new ToastMfx("Event has been created",
                    "Sunday, December 03, 2023 at 9:00 AM").variant(v)));
            buttons.add(b);
        }
        ButtonMfx undo = new ButtonMfx("With an action").variant(ButtonMfx.Variant.OUTLINE);
        undo.setOnAction(e -> ToastMfx.show(undo, new ToastMfx("Message archived").action("Undo", () -> { })));
        buttons.add(undo);
        VBox previews = new VBox(new ToastMfx("Event has been created", "Sunday, December 03, 2023 at 9:00 AM"),
                new ToastMfx("Upload failed").variant(ToastMfx.Variant.ERROR));
        Tw.set(previews, "gap-2");
        return column(section("Show one", buttons.toArray(Node[]::new)), section("What they look like", previews));
    }

    private static Node tooltips() {
        ButtonMfx hover = new ButtonMfx("Hover").variant(ButtonMfx.Variant.OUTLINE);
        TooltipMfx.install(hover, "Add to library");
        ButtonMfx save = new ButtonMfx("", icon(Feather.SAVE)).variant(ButtonMfx.Variant.OUTLINE).size(ButtonMfx.Size.ICON);
        save.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        TooltipMfx.install(save, "Save (Ctrl+S)");
        return column(section("Rest the pointer on a button", hover, save));
    }

    private static Node modal() {
        ButtonMfx open = new ButtonMfx("Open anything as a modal").variant(ButtonMfx.Variant.OUTLINE);
        open.setOnAction(e -> {
            CardMfx card = new CardMfx(new CardHeaderMfx(new CardTitleMfx("Any node"),
                    new CardDescriptionMfx("ModalMfx.open shows it over a dimmed screen; click outside to close.")));
            Tw.set(card, "w-[360px]");
            card.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            ModalMfx.open(open, card, Pos.CENTER, null, false);
        });
        return column(section("ModalMfx", open));
    }

    // menus ---------------------------------------------------------------------

    private static Node menus() {
        DropdownMenuMfx account = new DropdownMenuMfx("My account",
                MenuItemsMfx.label("My account"),
                MenuItemsMfx.separator(),
                MenuItemsMfx.item("Profile", Feather.USER, "Shortcut+Shift+P", () -> { }),
                MenuItemsMfx.item("Billing", Feather.CREDIT_CARD, "Shortcut+B", () -> { }),
                MenuItemsMfx.item("Settings", Feather.SETTINGS, "Shortcut+S", () -> { }),
                MenuItemsMfx.separator(),
                MenuItemsMfx.destructive("Log out", Feather.LOG_OUT, () -> { }));

        Label area = Tw.set(new Label("Right click here"), "text-sm text-muted-foreground");
        StackPane box = new StackPane(area);
        Tw.set(box, "h-[150px] w-[300px] rounded-md border border-dashed");
        new ContextMenuMfx(
                MenuItemsMfx.item("Back", null, "Alt+Left", () -> { }),
                MenuItemsMfx.item("Forward", null, "Alt+Right", () -> { }),
                MenuItemsMfx.item("Reload", null, "Shortcut+R", () -> { }),
                MenuItemsMfx.separator(),
                MenuItemsMfx.checkbox("Show bookmarks", true),
                MenuItemsMfx.checkbox("Show full URLs", false)).attach(box);

        Menu file = new Menu("File", null,
                MenuItemsMfx.item("New tab", null, "Shortcut+T", () -> { }),
                MenuItemsMfx.item("New window", null, "Shortcut+N", () -> { }),
                MenuItemsMfx.separator(),
                MenuItemsMfx.item("Print...", null, "Shortcut+P", () -> { }));
        Menu edit = new Menu("Edit", null,
                MenuItemsMfx.item("Undo", null, "Shortcut+Z", () -> { }),
                MenuItemsMfx.item("Redo", null, "Shortcut+Shift+Z", () -> { }));
        Menu view = new Menu("View", null, MenuItemsMfx.checkbox("Always show full URLs", true));
        return column(section("Dropdown menu", account), section("Context menu", box),
                section("Menubar", new MenubarMfx(file, edit, view)));
    }

    // forms ---------------------------------------------------------------------

    private static Node select() {
        SelectMfx<String> fruit = new SelectMfx<>("Select a fruit",
                List.of("Apple", "Banana", "Blueberry", "Grapes", "Pineapple"));
        Tw.set(fruit, "w-[220px]");
        return column(section("Select", fruit));
    }

    private static Node combobox() {
        ComboboxMfx<String> fw = new ComboboxMfx<>("Select framework...",
                List.of("Next.js", "SvelteKit", "Nuxt.js", "Remix", "Astro"));
        Tw.set(fw, "w-[220px]");
        return column(section("Type to filter", fw));
    }

    private static Node slider() {
        return column(section("Slider", Tw.set(new SliderMfx(0, 100, 50), "w-[360px]")));
    }

    private static Node radioGroup() {
        RadioGroupMfx<String> density = new RadioGroupMfx<>(List.of("Default", "Comfortable", "Compact"));
        density.setValue("Comfortable");
        return column(section("Density", density));
    }

    private static Node toggleGroup() {
        ToggleGroupMfx single = new ToggleGroupMfx(ToggleGroupMfx.Type.SINGLE, "Left", "Center", "Right");
        single.getButtons().get(0).setSelected(true);
        ToggleGroupMfx multiple = new ToggleGroupMfx(ToggleGroupMfx.Type.MULTIPLE, "Bold", "Italic", "Underline")
                .variant(ToggleGroupMfx.Variant.OUTLINE);
        multiple.getButtons().get(1).setSelected(true);
        return column(section("Single", single), section("Multiple, outline", multiple));
    }

    private static Node datePicker() {
        return column(section("Click the field", Tw.set(new DatePickerMfx(), "w-[240px]")));
    }

    private static Node calendar() {
        return column(section("Calendar", new CalendarMfx(LocalDate.now())));
    }

    // navigation and layout -----------------------------------------------------

    private static Node tabs() {
        CardMfx account = new CardMfx(new CardHeaderMfx(new CardTitleMfx("Account"),
                new CardDescriptionMfx("Make changes to your account here.")),
                new CardContentMfx(new VBox(8, new LabelMfx("Name"), new InputMfx("Pedro Duarte"))));
        CardMfx password = new CardMfx(new CardHeaderMfx(new CardTitleMfx("Password"),
                new CardDescriptionMfx("Change your password here.")),
                new CardContentMfx(new VBox(8, new LabelMfx("Current password"), new InputMfx())));
        return column(section("Tabs", Tw.set(new TabsMfx().tab("Account", account).tab("Password", password), "w-[420px]")));
    }

    private static Node breadcrumb() {
        return column(section("Breadcrumb", BreadcrumbMfx.of("Home", "Components", "Breadcrumb")));
    }

    private static Node pagination() {
        return column(section("Pagination", new PaginationMfx(10)));
    }

    private static Node accordion() {
        AccordionMfx a = new AccordionMfx()
                .item("Is it accessible?", text("Yes. It uses JavaFX's own keyboard handling.", "text-sm"))
                .item("Is it styled?", text("Yes. It comes with the shadcn look, from the tokens.", "text-sm"))
                .item("Is it animated?", text("Yes. Opening and closing slide, as JavaFX does it.", "text-sm"));
        Tw.set(a, "w-[460px]");
        a.setExpandedPane(a.getPanes().getFirst());
        return column(section("Accordion", a));
    }

    private static Node collapsible() {
        Label repo = Tw.set(new Label("@peduarte starred 3 repositories"), "text-sm font-semibold");
        ButtonMfx toggle = new ButtonMfx("", icon(Feather.CHEVRONS_DOWN)).variant(ButtonMfx.Variant.GHOST)
                .size(ButtonMfx.Size.ICON_SM);
        toggle.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        HBox trigger = new HBox(repo, Tw.set(new Region(), "grow"), toggle);
        Tw.set(trigger, "items-center gap-4");
        VBox more = new VBox(repoRow("@radix-ui/colors"), repoRow("@stitches/react"));
        Tw.set(more, "gap-2");
        CollapsibleMfx c = new CollapsibleMfx(trigger, more);
        Tw.set(c, "gap-2 w-[350px]");
        return column(section("Click the arrows", new VBox(8, c, repoRow("@radix-ui/primitives"))));
    }

    private static Node repoRow(String name) {
        Label l = Tw.set(new Label(name), "text-sm font-mono");
        StackPane row = new StackPane(l);
        StackPane.setAlignment(l, Pos.CENTER_LEFT);
        return Tw.set(row, "px-4 py-2 rounded-md border w-[350px]");
    }

    private static Node resizable() {
        ResizableMfx h = new ResizableMfx(Orientation.HORIZONTAL, panel("One"), panel("Two")).handle(true);
        Tw.set(h, "h-[200px] w-[480px]");
        ResizableMfx v = new ResizableMfx(Orientation.VERTICAL, panel("Header"), panel("Content"));
        Tw.set(v, "h-[200px] w-[480px]");
        return column(section("Side by side, with a handle", h), section("Stacked", v));
    }

    private static Node panel(String name) {
        return new StackPane(Tw.set(new Label(name), "font-semibold"));
    }

    private static Node scrollArea() {
        VBox tags = new VBox();
        tags.getChildren().add(Tw.set(new Label("Tags"), "text-sm font-medium"));
        for (int i = 50; i > 0; i--) {
            tags.getChildren().addAll(Tw.set(new Label("v1.2.0-beta." + i), "text-sm"), new SeparatorMfx());
        }
        Tw.set(tags, "gap-2 p-4");
        ScrollAreaMfx area = new ScrollAreaMfx(tags);
        Tw.set(area, "h-[240px] w-[220px] rounded-md border");
        return column(section("Scroll area", area));
    }

    record Invoice(String invoice, String status, String method, String amount) {}

    private static Node table() {
        TableMfx<Invoice> t = new TableMfx<>(List.of(
                new Invoice("INV001", "Paid", "Credit Card", "$250.00"),
                new Invoice("INV002", "Pending", "PayPal", "$150.00"),
                new Invoice("INV003", "Unpaid", "Bank Transfer", "$350.00"),
                new Invoice("INV004", "Paid", "Credit Card", "$450.00"),
                new Invoice("INV005", "Paid", "PayPal", "$550.00")))
                .column("Invoice", Invoice::invoice)
                .column("Status", Invoice::status)
                .column("Method", Invoice::method)
                .column("Amount", Invoice::amount);
        Tw.set(t, "h-[250px] w-[560px]");
        return column(section("A list of your recent invoices", t), CollectionDemos.table());
    }

    // ------------------------------------------------------------------ utils pages

    private static Node state() {
        Store<Integer, String> counter = new Store<>(0, (n, action) -> switch (action) {
            case "+" -> n + 1;
            case "-" -> n - 1;
            default -> 0;
        });
        Label value = Tw.set(new Label(), "text-3xl font-semibold w-[60px] text-center");
        value.textProperty().bind(counter.state().map(String::valueOf));
        ButtonMfx minus = new ButtonMfx("-").variant(ButtonMfx.Variant.OUTLINE).size(ButtonMfx.Size.ICON);
        ButtonMfx plus = new ButtonMfx("+").variant(ButtonMfx.Variant.OUTLINE).size(ButtonMfx.Size.ICON);
        ButtonMfx reset = new ButtonMfx("Reset").variant(ButtonMfx.Variant.GHOST);
        minus.setOnAction(e -> counter.dispatch("-"));
        plus.setOnAction(e -> counter.dispatch("+"));
        reset.setOnAction(e -> counter.dispatch("reset"));
        HBox row = new HBox(minus, value, plus, reset);
        Tw.set(row, "gap-3 items-center");
        return column(section("A Store<Integer, String>: the buttons dispatch, the number is bound to state()", row),
                section("PersistentStore", text("The same, kept in a JSON file: "
                        + "PersistentStore.builder(type, initial, reducer).app(\"Acme\", \"notes\").open().",
                        "text-sm text-muted-foreground w-[520px]")));
    }

    private static Node tailwind() {
        HBox boxes = new HBox(
                Tw.set(new StackPane(new Label("p-4 bg-muted rounded-md")), "p-4 bg-muted rounded-md"),
                Tw.set(new StackPane(new Label("bg-primary text-primary-foreground")),
                        "p-4 bg-primary text-primary-foreground rounded-lg"),
                Tw.set(new StackPane(new Label("border shadow-md hover:bg-accent")), "p-4 border rounded-xl shadow-md hover:bg-accent"));
        Tw.set(boxes, "gap-4 items-center");
        VBox palette = new VBox();
        Tw.set(palette, "gap-1");
        for (TwColor c : List.of(TwColor.SLATE, TwColor.RED, TwColor.AMBER, TwColor.GREEN, TwColor.BLUE, TwColor.VIOLET)) {
            HBox row = new HBox();
            Tw.set(row, "gap-1 items-center");
            row.getChildren().add(Tw.set(new Label(c.token()), "text-xs text-muted-foreground w-[56px]"));
            for (int shade : c.shades()) {
                Region swatch = new Region();
                Tw.set(swatch, "size-8 rounded-sm");
                swatch.setStyle("-fx-background-color: " + c.hex(shade) + ";");
                TooltipMfx.install(swatch, c.token() + "-" + shade + "  " + c.hex(shade));
                row.getChildren().add(swatch);
            }
            palette.getChildren().add(row);
        }
        return column(section("Tw.set(node, \"...\")", boxes),
                section("The palette (Tailwind " + Tailwind.VERSION + ")", palette));
    }

    private static Node theme() {
        ButtonMfx light = new ButtonMfx("Light").variant(ButtonMfx.Variant.OUTLINE);
        ButtonMfx darkMode = new ButtonMfx("Dark").variant(ButtonMfx.Variant.OUTLINE);
        ButtonMfx system = new ButtonMfx("System").variant(ButtonMfx.Variant.OUTLINE);
        light.setOnAction(e -> ThemeMfx.setMode(light.getScene(), ThemeMfx.Mode.LIGHT));
        darkMode.setOnAction(e -> ThemeMfx.setMode(darkMode.getScene(), ThemeMfx.Mode.DARK));
        system.setOnAction(e -> ThemeMfx.setMode(system.getScene(), ThemeMfx.Mode.SYSTEM));
        FlowPane tokens = new FlowPane();
        Tw.set(tokens, "gap-3");
        for (String t : Tailwind.TOKENS) {
            if (t.startsWith("chart") || t.startsWith("sidebar")) {
                continue;
            }
            Region swatch = new Region();
            Tw.set(swatch, "size-10 rounded-md border");
            swatch.setStyle("-fx-background-color: -" + t + ";");
            VBox cell = new VBox(swatch, Tw.set(new Label(t), "text-xs text-muted-foreground"));
            Tw.set(cell, "gap-1 w-[120px]");
            tokens.getChildren().add(cell);
        }
        return column(section("ThemeMfx.setMode(scene, ...)", light, darkMode, system),
                section("The shadcn tokens (they follow the mode)", tokens));
    }
}
