package io.joshuasalcedo.fx;

import io.joshuasalcedo.fx.ui.core.board.BoardColumnSpec;
import io.joshuasalcedo.fx.ui.core.board.BoardMfx;
import io.joshuasalcedo.fx.ui.core.button.ButtonMfx;
import io.joshuasalcedo.fx.ui.core.button.ButtonSize;
import io.joshuasalcedo.fx.ui.core.button.ButtonVariant;
import io.joshuasalcedo.fx.ui.core.buttongroup.ButtonGroupMfx;
import io.joshuasalcedo.fx.ui.core.buttongroup.GroupOrientation;
import io.joshuasalcedo.fx.ui.core.data.Density;
import io.joshuasalcedo.fx.ui.core.data.ListDataSource;
import io.joshuasalcedo.fx.ui.core.data.LoadState;
import io.joshuasalcedo.fx.ui.core.data.SelectionMode;
import io.joshuasalcedo.fx.ui.core.data.SelectionState;
import io.joshuasalcedo.fx.ui.core.data.SortSpec;
import io.joshuasalcedo.fx.ui.core.data.ViewMode;
import io.joshuasalcedo.fx.ui.core.dnd.DragHandleMfx;
import io.joshuasalcedo.fx.ui.core.dnd.ReorderSupport;
import io.joshuasalcedo.fx.ui.core.fab.FabMfx;
import io.joshuasalcedo.fx.ui.core.gridview.GridSizing;
import io.joshuasalcedo.fx.ui.core.gridview.GridViewMfx;
import io.joshuasalcedo.fx.ui.core.iconbutton.IconButtonMfx;
import io.joshuasalcedo.fx.ui.core.iconbutton.IconButtonShape;
import io.joshuasalcedo.fx.ui.core.link.LinkMfx;
import io.joshuasalcedo.fx.ui.core.list.ListMfx;
import io.joshuasalcedo.fx.ui.core.pagination.PageSizeSelectMfx;
import io.joshuasalcedo.fx.ui.core.pagination.PageSummaryMfx;
import io.joshuasalcedo.fx.ui.core.placeholder.CollectionPlaceholderMfx;
import io.joshuasalcedo.fx.ui.core.placeholder.EmptyStateSpec;
import io.joshuasalcedo.fx.ui.core.segmented.SegmentedControlMfx;
import io.joshuasalcedo.fx.ui.core.select.SelectMfx;
import io.joshuasalcedo.fx.ui.core.splitbutton.SplitButtonMfx;
import io.joshuasalcedo.fx.ui.core.table.ColumnVisibilityMenuMfx;
import io.joshuasalcedo.fx.ui.core.table.ColumnWidth;
import io.joshuasalcedo.fx.ui.core.table.RowActionsColumnMfx;
import io.joshuasalcedo.fx.ui.core.table.SelectionColumnMfx;
import io.joshuasalcedo.fx.ui.core.table.TableColumnMfx;
import io.joshuasalcedo.fx.ui.core.table.TableFooterMfx;
import io.joshuasalcedo.fx.ui.core.table.TableMfx;
import io.joshuasalcedo.fx.ui.core.table.renderer.AvatarCellRenderer;
import io.joshuasalcedo.fx.ui.core.table.renderer.BadgeCellRenderer;
import io.joshuasalcedo.fx.ui.core.table.renderer.BooleanCellRenderer;
import io.joshuasalcedo.fx.ui.core.table.renderer.DateCellRenderer;
import io.joshuasalcedo.fx.ui.core.table.renderer.MoneyCellRenderer;
import io.joshuasalcedo.fx.ui.core.togglebutton.ToggleButtonMfx;
import io.joshuasalcedo.fx.ui.core.toolbar.BulkActionBarMfx;
import io.joshuasalcedo.fx.ui.core.toolbar.CollectionToolbarMfx;
import io.joshuasalcedo.fx.ui.core.toolbar.ViewModeToggleMfx;
import io.joshuasalcedo.fx.ui.core.tree.LazyTreeLoader;
import io.joshuasalcedo.fx.ui.core.tree.TreeExpansionState;
import io.joshuasalcedo.fx.ui.core.tree.TreeMfx;
import io.joshuasalcedo.fx.ui.core.tree.TreeNode;
import io.joshuasalcedo.fx.ui.core.treetable.TreeTableColumnMfx;
import io.joshuasalcedo.fx.ui.core.treetable.TreeTableMfx;
import io.joshuasalcedo.fx.ui.core.table.renderer.NumberCellRenderer;
import io.joshuasalcedo.fx.utils.tailwind.Tw;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// The showcase's pages for the action controls and the collections (list, grid, table, tree, board).
@NullMarked
final class CollectionDemos {

    private CollectionDemos() {
    }

    record Contact(int id, String name, String email, String team) {
    }

    record Invoice(int id, String customer, String status, BigDecimal amount, Instant issued, boolean paid) {
    }

    record Task(int id, String title, String status) {
    }

    record FileEntry(String name, long size, boolean folder) {
        @Override
        public String toString() {
            return name;
        }
    }

    private static final String[] FIRST = {"Ana", "Ben", "Cara", "Dan", "Eli", "Faye", "Gus", "Hana", "Ian", "Jo",
            "Kai", "Lea", "Max", "Nia", "Oto", "Pia", "Quin", "Rae", "Sam", "Tia"};
    private static final String[] LAST = {"Ruiz", "Cruz", "Lim", "Tan", "Reyes", "Santos", "Garcia", "Mendoza"};

    static List<Contact> contacts(int n) {
        List<Contact> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String name = FIRST[i % FIRST.length] + " " + LAST[(i / FIRST.length + i) % LAST.length];
            out.add(new Contact(i, name, name.toLowerCase(Locale.ROOT).replace(' ', '.') + "@example.com",
                    i % 3 == 0 ? "Sales" : i % 3 == 1 ? "Support" : "Design"));
        }
        out.sort(java.util.Comparator.comparing(Contact::name));
        return out;
    }

    // ------------------------------------------------------------------ helpers (as the showcase's)

    private static Node section(String title, Node... examples) {
        FlowPane row = new FlowPane(examples);
        Tw.set(row, "gap-3 items-center");
        VBox box = new VBox(Tw.set(new Label(title), "text-sm font-medium text-muted-foreground"), row);
        Tw.set(box, "gap-3");
        return box;
    }

    private static Node wide(String title, Node content) {
        VBox box = new VBox(Tw.set(new Label(title), "text-sm font-medium text-muted-foreground"), content);
        Tw.set(box, "gap-3");
        return box;
    }

    private static Node column(Node... sections) {
        VBox box = new VBox(sections);
        Tw.set(box, "gap-8");
        return box;
    }

    private static FontIcon icon(Feather icon) {
        return new FontIcon(icon);
    }

    // ------------------------------------------------------------------ actions

    static Node buttonGroup() {
        ButtonGroupMfx range = new ButtonGroupMfx(
                new ButtonMfx("Day").variant(ButtonMfx.Variant.OUTLINE),
                new ButtonMfx("Week").variant(ButtonMfx.Variant.OUTLINE),
                new ButtonMfx("Month").variant(ButtonMfx.Variant.OUTLINE));
        ButtonGroupMfx pager = new ButtonGroupMfx(
                new ButtonMfx("", icon(Feather.CHEVRON_LEFT)).variant(ButtonMfx.Variant.OUTLINE).size(ButtonMfx.Size.ICON),
                new ButtonMfx("", icon(Feather.CHEVRON_RIGHT)).variant(ButtonMfx.Variant.OUTLINE).size(ButtonMfx.Size.ICON));
        ButtonGroupMfx vertical = new ButtonGroupMfx(
                new ButtonMfx("Top").variant(ButtonMfx.Variant.OUTLINE),
                new ButtonMfx("Middle").variant(ButtonMfx.Variant.OUTLINE),
                new ButtonMfx("Bottom").variant(ButtonMfx.Variant.OUTLINE));
        vertical.setOrientation(GroupOrientation.VERTICAL);
        return column(section("Horizontal", range, pager), section("Vertical", vertical));
    }

    static Node iconButtons() {
        List<Node> variants = new ArrayList<>();
        for (ButtonVariant v : List.of(ButtonVariant.GHOST, ButtonVariant.OUTLINE, ButtonVariant.SECONDARY, ButtonVariant.PRIMARY)) {
            variants.add(new IconButtonMfx(icon(Feather.SETTINGS), "Settings").variant(v));
        }
        IconButtonMfx save = new IconButtonMfx(icon(Feather.SAVE), "Save").variant(ButtonVariant.OUTLINE);
        save.setOnAction(e -> {
            save.setLoading(true);
            javafx.animation.PauseTransition wait = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            wait.setOnFinished(f -> save.setLoading(false));
            wait.play();
        });
        return column(
                section("Variants", variants.toArray(Node[]::new)),
                section("Sizes and shapes",
                        new IconButtonMfx(icon(Feather.PLUS), "Add").variant(ButtonVariant.OUTLINE).size(ButtonSize.SM),
                        new IconButtonMfx(icon(Feather.PLUS), "Add").variant(ButtonVariant.OUTLINE),
                        new IconButtonMfx(icon(Feather.PLUS), "Add").variant(ButtonVariant.OUTLINE).size(ButtonSize.LG),
                        new IconButtonMfx(icon(Feather.PLUS), "Add").variant(ButtonVariant.OUTLINE).shape(IconButtonShape.CIRCLE)),
                section("Loading (click it)", save));
    }

    static Node toggleButtons() {
        return column(section("Pressed until clicked again",
                new ToggleButtonMfx("Bold", icon(Feather.BOLD)),
                new ToggleButtonMfx("Italic", icon(Feather.ITALIC)),
                new ToggleButtonMfx("Underline", icon(Feather.UNDERLINE)).variant(ButtonVariant.OUTLINE)));
    }

    static Node splitButtons() {
        Label said = Tw.set(new Label("Nothing sent yet."), "text-sm text-muted-foreground");
        SplitButtonMfx send = new SplitButtonMfx("Send");
        send.setOnAction(e -> said.setText("Sent."));
        MenuItem later = new MenuItem("Send later");
        later.setOnAction(e -> said.setText("Scheduled."));
        MenuItem all = new MenuItem("Send to all");
        all.setOnAction(e -> said.setText("Sent to everyone."));
        send.getItems().addAll(later, all);
        SplitButtonMfx save = new SplitButtonMfx("Save");
        save.setVariant(ButtonVariant.OUTLINE);
        save.getItems().addAll(new MenuItem("Save as…"), new MenuItem("Save a copy"));
        return column(section("Click the text, or the arrow for more", send, save, said));
    }

    static Node segmented() {
        SegmentedControlMfx<String> filter = new SegmentedControlMfx<>(List.of("All", "Unread", "VIP"));
        Label shown = Tw.set(new Label(), "text-sm text-muted-foreground");
        shown.textProperty().bind(filter.valueProperty().map(v -> "Showing: " + v));
        return column(section("One is always selected; the arrow keys move it", filter, shown),
                section("Icons only", new ViewModeToggleMfx()));
    }

    static Node fab() {
        Label content = Tw.set(new Label("Your chats show here."), "text-sm text-muted-foreground");
        FabMfx fab = new FabMfx(icon(Feather.PLUS), "New chat");
        StackPane screen = new StackPane(content, fab);
        Tw.set(screen, "w-[420px] h-[220px] bg-card border rounded-xl");
        ToggleButtonMfx extended = new ToggleButtonMfx("Extended").variant(ButtonVariant.OUTLINE);
        fab.extendedProperty().bind(extended.selectedProperty());
        return column(section("Floats in a corner over the content", screen, extended));
    }

    static Node links() {
        Label clicked = Tw.set(new Label(), "text-sm text-muted-foreground");
        LinkMfx action = new LinkMfx("Show details");
        action.setOnAction(e -> clicked.setText("Details opened."));
        return column(section("Opens in the browser", new LinkMfx("modernfx on GitHub", "https://github.com/joshuasalcedo-dev/modernfx")),
                section("Runs an action", action, clicked));
    }

    // ------------------------------------------------------------------ collections

    static Node data() {
        return column(
                Tw.set(new Label("The list, grid, table and board take their items from a DataSource: ListDataSource "
                        + "for items in memory, PagedDataSource for a server. A PageRequest asks for one page (offset, "
                        + "limit, sort, filter); SelectionState keeps what is selected by key, so it survives reloads."),
                        "text-sm text-muted-foreground max-w-[640px]"),
                section("Density", densityRows(Density.COMPACT), densityRows(Density.COMFORTABLE), densityRows(Density.SPACIOUS)));
    }

    private static Node densityRows(Density d) {
        ListMfx<Contact> list = new ListMfx<>();
        list.setDensity(d);
        list.setCellFactory((row, c) -> row.title(c.name()));
        list.getItems().setAll(contacts(4));
        VBox box = new VBox(Tw.set(new Label(d.name().toLowerCase(Locale.ROOT)), "text-xs text-muted-foreground"), list);
        Tw.set(box, "gap-1 w-[200px] h-[240px]");
        VBox.setVgrow(list, Priority.ALWAYS);
        return box;
    }

    static Node placeholder() {
        ListView<String> content = new ListView<>(FXCollections.observableArrayList("Ana Ruiz", "Ben Cruz", "Cara Lim"));
        content.getStyleClass().add("mfx-list");
        CollectionPlaceholderMfx placeholder = new CollectionPlaceholderMfx(content);
        placeholder.setEmpty(EmptyStateSpec.of("No contacts yet", "Contacts you add show up here.")
                .withAction("New contact", () -> { }));
        placeholder.setErrorMessage("The server didn't answer.");
        placeholder.setOnRetry(() -> placeholder.setState(LoadState.LOADED));
        placeholder.setState(LoadState.LOADED);
        SegmentedControlMfx<LoadState> state = new SegmentedControlMfx<>(
                List.of(LoadState.LOADED, LoadState.LOADING, LoadState.EMPTY, LoadState.ERROR));
        state.setValue(LoadState.LOADED);
        state.valueProperty().subscribe(s -> {
            if (s != null) {
                placeholder.setState(s);
            }
        });
        placeholder.stateProperty().subscribe(s -> state.setValue(s));
        Tw.set(placeholder, "w-[420px] h-[300px] border rounded-xl");
        return column(section("Pick a state", state), placeholder);
    }

    static Node list() {
        ListMfx<Contact> list = new ListMfx<>();
        list.setSelection(new SelectionState<>(Contact::id, SelectionMode.MULTIPLE));
        list.setCellFactory((row, c) -> row
                .leading(AvatarCellRenderer.avatar(c.name(), null))
                .title(c.name())
                .subtitle(c.email())
                .meta(c.team()));
        list.setSectionBy(c -> c.name().substring(0, 1));
        list.setPageSize(25);
        list.setSort(List.of(SortSpec.ascending("name")));
        list.setDataSource(new ListDataSource<>(contacts(200)).sortBy("name", Contact::name)
                .filterBy((c, q) -> (c.name() + " " + c.email()).toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT))));
        list.setEmpty(EmptyStateSpec.of("No one found", "Try another name."));
        CollectionToolbarMfx toolbar = new CollectionToolbarMfx();
        list.filterProperty().bind(toolbar.search().searchProperty());
        PageSummaryMfx summary = new PageSummaryMfx();
        list.getItems().addListener((javafx.collections.ListChangeListener<Contact>) c ->
                summary.set(0, list.getItems().size(), list.totalProperty().get()));
        list.totalProperty().subscribe(t -> summary.set(0, list.getItems().size(), t.longValue()));
        toolbar.getTrailing().add(summary);
        VBox box = new VBox(toolbar, list);
        Tw.set(box, "gap-3 w-[560px] h-[480px]");
        VBox.setVgrow(list, Priority.ALWAYS);
        return column(wide("200 contacts, 25 at a time; sections by letter; Ctrl or Shift to select several", box));
    }

    static Node gridView() {
        List<Integer> photos = new ArrayList<>();
        for (int i = 1; i <= 60; i++) {
            photos.add(i);
        }
        GridViewMfx<Integer> grid = new GridViewMfx<>(n -> {
            Label l = Tw.set(new Label("Photo " + n), "text-sm text-muted-foreground");
            StackPane tile = new StackPane(l);
            Tw.set(tile, "bg-muted rounded-md");
            return tile;
        }, photos);
        grid.setSizing(new GridSizing(140, 4 / 3.0, 12));
        Label selected = Tw.set(new Label("0 selected"), "text-sm text-muted-foreground");
        selected.textProperty().bind(grid.getSelection().countProperty().map(n -> n + " selected"));
        Tw.set(grid, "w-[640px] h-[420px]");
        return column(wide("Columns follow the width; click tiles to select them", new VBox(8, selected, grid)));
    }

    static Node table() {
        List<Invoice> invoices = new ArrayList<>();
        String[] statuses = {"Paid", "Pending", "Overdue"};
        List<Contact> people = contacts(12);
        for (int i = 0; i < 12; i++) {
            invoices.add(new Invoice(1000 + i, people.get(i).name(), statuses[i % 3],
                    BigDecimal.valueOf(150 + i * 37.5), Instant.now().minus(i * 7L + 1, ChronoUnit.HOURS), i % 3 == 0));
        }
        var selection = new SelectionState<Invoice>(Invoice::id, SelectionMode.MULTIPLE);
        var usd = Currency.getInstance("USD");
        TableMfx<Invoice> table = new TableMfx<>(invoices);
        table.columns(
                new SelectionColumnMfx<>(selection),
                TableColumnMfx.of("id", "Invoice", (Invoice inv) -> "INV" + inv.id()).width(ColumnWidth.fixed(96)),
                TableColumnMfx.of("customer", "Customer", Invoice::customer).renderer(new AvatarCellRenderer<>()).width(ColumnWidth.flex(2)),
                TableColumnMfx.of("status", "Status", Invoice::status).renderer(new BadgeCellRenderer<>()),
                TableColumnMfx.of("issued", "Issued", (Invoice inv) -> (java.time.temporal.Temporal) inv.issued()).renderer(DateCellRenderer.relative()),
                TableColumnMfx.of("paid", "Paid", Invoice::paid).renderer(BooleanCellRenderer.check()).width(ColumnWidth.fixed(64)),
                TableColumnMfx.of("amount", "Amount", (Invoice inv) -> (Number) inv.amount()).renderer(new MoneyCellRenderer<>(usd, Locale.US)),
                new RowActionsColumnMfx<Invoice>().inline(Feather.EDIT_2, "Edit", inv -> { })
                        .menu("Duplicate", inv -> { }).dangerMenu("Delete", inv -> table.getItems().remove(inv)));
        TableFooterMfx<Invoice> footer = new TableFooterMfx<>(table, selection)
                .sum("Total", inv -> inv.amount().doubleValue(), NumberFormat.getCurrencyInstance(Locale.US));
        BulkActionBarMfx bulk = new BulkActionBarMfx(selection);
        ButtonMfx markPaid = new ButtonMfx("Mark paid").variant(ButtonMfx.Variant.OUTLINE).size(ButtonMfx.Size.SM);
        bulk.getActions().add(markPaid);
        CollectionToolbarMfx toolbar = new CollectionToolbarMfx();
        toolbar.getTrailing().add(new ColumnVisibilityMenuMfx(table));
        toolbar.setBulkActions(bulk);
        ReorderSupport.install(table, null).handleOnly(false);
        VBox box = new VBox(toolbar, table, footer);
        Tw.set(box, "gap-2 w-[860px] h-[520px]");
        VBox.setVgrow(table, Priority.ALWAYS);
        return wide("shadcn's data table: select rows, hide columns, drag rows, row actions, totals", box);
    }

    static Node tree() {
        LazyTreeLoader<FileEntry> slow = LazyTreeLoader.blocking(parent -> {
            Thread.sleep(700);
            return List.of(TreeNode.leaf(new FileEntry(parent.name() + "-2024.zip", 1_200_000, false)),
                    TreeNode.leaf(new FileEntry(parent.name() + "-2025.zip", 2_400_000, false)));
        });
        TreeMfx<FileEntry> tree = new TreeMfx<>();
        tree.setIcon(f -> new FontIcon(f.folder() ? Feather.FOLDER : Feather.FILE));
        tree.setText(FileEntry::name);
        tree.setLoader(slow);
        tree.setExpansionState(new TreeExpansionState());
        tree.setRootNode(files());
        Tw.set(tree, "w-[320px] h-[360px] border rounded-xl p-1");
        return column(wide("Archive loads its children when first opened", tree));
    }

    static Node treeTable() {
        TreeTableMfx<FileEntry> table = new TreeTableMfx<>();
        table.columns(
                TreeTableColumnMfx.of("name", "Name", FileEntry::name).width(ColumnWidth.flex(3)),
                TreeTableColumnMfx.of("size", "Size", (FileEntry f) -> f.folder() ? null : (Number) f.size())
                        .renderer(new NumberCellRenderer<>()));
        table.setLoader(parent -> java.util.concurrent.CompletableFuture.completedFuture(
                List.of(TreeNode.leaf(new FileEntry("old.zip", 9_000_000, false)))));
        table.setRootNode(files());
        Tw.set(table, "w-[560px] h-[360px]");
        return column(wide("Rows that open, in columns", table));
    }

    private static TreeNode<FileEntry> files() {
        return TreeNode.of(new FileEntry("home", 0, true),
                TreeNode.of(new FileEntry("projects", 0, true),
                        TreeNode.of(new FileEntry("modernfx", 0, true),
                                TreeNode.leaf(new FileEntry("pom.xml", 11_796, false)),
                                TreeNode.leaf(new FileEntry("README.md", 6_069, false))),
                        TreeNode.leaf(new FileEntry("notes.txt", 820, false))),
                TreeNode.lazy(new FileEntry("archive", 0, true)),
                TreeNode.leaf(new FileEntry("todo.md", 312, false)));
    }

    static Node board() {
        BoardMfx<Task> board = new BoardMfx<>(t -> Tw.set(new Label(t.title()), "text-sm"));
        board.addColumn(BoardColumnSpec.of("todo", "To do"));
        board.addColumn(BoardColumnSpec.of("doing", "In progress").withWipLimit(2));
        board.addColumn(BoardColumnSpec.of("done", "Done"));
        board.setItems(List.of(
                new Task(1, "Write the changelog", "todo"),
                new Task(2, "Release 0.1.0", "todo"),
                new Task(3, "Use ModernFX in the desktop", "todo"),
                new Task(4, "Build the core components", "doing"),
                new Task(5, "Set up Maven Central", "done")), Task::status);
        Label moved = Tw.set(new Label("Drag a card."), "text-sm text-muted-foreground");
        board.setOnMove(m -> moved.setText(m.item().title() + " → " + m.toColumn()));
        int[] next = {6};
        board.setOnAdd(column -> board.add(column, new Task(next[0]++, "New task", column)));
        Tw.set(board, "w-[880px] h-[400px]");
        return column(wide("Drag cards within and between columns; + adds one", new VBox(8, moved, board)));
    }

    static Node dnd() {
        ListView<String> steps = new ListView<>(FXCollections.observableArrayList(
                "Write the changelog", "Commit the pom", "Push the tag", "Press Publish on Central"));
        steps.getStyleClass().add("mfx-list");
        steps.setCellFactory(v -> new ListCell<>() {
            private final DragHandleMfx handle = new DragHandleMfx();
            private final Label text = new Label();
            private final HBox row = new HBox(8, handle, text);

            {
                row.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(@Nullable String item, boolean empty) {
                super.updateItem(item, empty);
                text.setText(item);
                setText(null);
                setGraphic(empty ? null : row);
            }
        });
        Label moved = Tw.set(new Label("Drag a row by its grip."), "text-sm text-muted-foreground");
        ReorderSupport.install(steps, e -> moved.setText("Moved \"" + e.item() + "\" from " + (e.from() + 1) + " to " + (e.to() + 1)))
                .handleOnly(true);
        Tw.set(steps, "w-[360px] h-[220px]");
        return column(wide("Drag to reorder", new VBox(8, moved, steps)));
    }

    static Node toolbar() {
        var selection = new SelectionState<Integer>();
        CollectionToolbarMfx toolbar = new CollectionToolbarMfx();
        SelectMfx<String> team = new SelectMfx<>("Team", List.of("All teams", "Sales", "Support", "Design"));
        toolbar.getFilters().add(team);
        toolbar.setViewModeToggle(new ViewModeToggleMfx(ViewMode.LIST, ViewMode.GRID, ViewMode.TABLE, ViewMode.BOARD));
        BulkActionBarMfx bulk = new BulkActionBarMfx(selection);
        bulk.getActions().addAll(new ButtonMfx("Archive").variant(ButtonMfx.Variant.OUTLINE).size(ButtonMfx.Size.SM),
                new ButtonMfx("Delete").variant(ButtonMfx.Variant.DESTRUCTIVE).size(ButtonMfx.Size.SM));
        toolbar.setBulkActions(bulk);
        Label state = Tw.set(new Label(), "text-sm text-muted-foreground");
        state.textProperty().bind(javafx.beans.binding.Bindings.createStringBinding(
                () -> "Search: \"" + toolbar.search().getSearch() + "\" · view: "
                        + (toolbar.viewModeToggleProperty().get() == null ? "" : toolbar.viewModeToggleProperty().get().getValue()),
                toolbar.search().searchProperty(), toolbar.viewModeToggleProperty().get().valueProperty()));
        ButtonMfx select = new ButtonMfx("Select 3 items").variant(ButtonMfx.Variant.OUTLINE).size(ButtonMfx.Size.SM);
        select.setOnAction(e -> selection.selectAll(List.of(1, 2, 3)));
        VBox box = new VBox(toolbar, state, select);
        Tw.set(box, "gap-3 w-[760px]");
        return column(wide("Search, filters and the view switch; the bulk bar takes over while items are selected", box),
                section("Pages", new PageSizeSelectMfx(), pageSummary()));
    }

    private static Node pageSummary() {
        PageSummaryMfx summary = new PageSummaryMfx();
        summary.set(0, 25, 1240);
        return summary;
    }
}
