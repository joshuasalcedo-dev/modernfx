package io.joshuasalcedo.fx.sample;

import io.joshuasalcedo.fx.sample.SampleData.Contact;
import io.joshuasalcedo.fx.sample.SampleData.FileEntry;
import io.joshuasalcedo.fx.sample.SampleData.Invoice;
import io.joshuasalcedo.fx.sample.SampleData.Status;
import io.joshuasalcedo.fx.sample.SampleData.Task;
import io.joshuasalcedo.fx.ui.core.badge.BadgeMfx;
import io.joshuasalcedo.fx.ui.core.board.BoardColumnSpec;
import io.joshuasalcedo.fx.ui.core.board.BoardMfx;
import io.joshuasalcedo.fx.ui.core.button.ButtonMfx;
import io.joshuasalcedo.fx.ui.core.button.ButtonSize;
import io.joshuasalcedo.fx.ui.core.button.ButtonVariant;
import io.joshuasalcedo.fx.ui.core.data.ListDataSource;
import io.joshuasalcedo.fx.ui.core.data.SelectionMode;
import io.joshuasalcedo.fx.ui.core.data.SelectionState;
import io.joshuasalcedo.fx.ui.core.data.SortSpec;
import io.joshuasalcedo.fx.ui.core.iconbutton.IconButtonMfx;
import io.joshuasalcedo.fx.ui.core.list.ListMfx;
import io.joshuasalcedo.fx.ui.core.pagination.PageSummaryMfx;
import io.joshuasalcedo.fx.ui.core.placeholder.EmptyStateSpec;
import io.joshuasalcedo.fx.ui.core.segmented.SegmentedControlMfx;
import io.joshuasalcedo.fx.ui.core.sheet.CellRange;
import io.joshuasalcedo.fx.ui.core.sheet.SheetMfx;
import io.joshuasalcedo.fx.ui.core.sheet.SheetValidation;
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
import io.joshuasalcedo.fx.ui.core.toast.ToastMfx;
import io.joshuasalcedo.fx.ui.core.toolbar.BulkActionBarMfx;
import io.joshuasalcedo.fx.ui.core.toolbar.CollectionToolbarMfx;
import io.joshuasalcedo.fx.ui.core.tree.LazyTreeLoader;
import io.joshuasalcedo.fx.ui.core.tree.TreeExpansionState;
import io.joshuasalcedo.fx.ui.core.tree.TreeMfx;
import io.joshuasalcedo.fx.ui.core.tree.TreeNode;
import io.joshuasalcedo.fx.utils.tailwind.Tw;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// The content of each window: one method per screen.
final class Screens {

    private Screens() {
    }

    private static VBox page(Node... children) {
        VBox box = new VBox(children);
        Tw.set(box, "p-4 gap-3");
        return box;
    }

    private static Label muted(String text) {
        return Tw.set(new Label(text), "text-sm text-muted-foreground");
    }

    /// Contacts: search, sections by letter, 25 at a time.
    static Node contacts() {
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
        list.setEmpty(EmptyStateSpec.of("No one found", "Try another name or email."));

        SegmentedControlMfx<String> team = new SegmentedControlMfx<>(List.of("All", "Sales", "Support", "Design"));
        ListDataSource<Contact> source = new ListDataSource<>(SampleData.contacts())
                .sortBy("name", Contact::name)
                .filterBy((c, q) -> (c.name() + " " + c.email()).toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT)));
        list.setDataSource(source);
        // the team filter changes the source's items; reloading shows them
        List<Contact> everyone = List.copyOf(source.getItems());
        team.valueProperty().subscribe(t -> {
            source.getItems().setAll(t == null || t.equals("All") ? everyone
                    : everyone.stream().filter(c -> c.team().equals(t)).toList());
            list.reload();
        });

        CollectionToolbarMfx toolbar = new CollectionToolbarMfx();
        toolbar.search().setPromptText("Search contacts…");
        list.filterProperty().bind(toolbar.search().searchProperty());
        toolbar.getFilters().add(team);

        PageSummaryMfx summary = new PageSummaryMfx();
        Runnable count = () -> summary.set(0, list.getItems().size(), list.totalProperty().get());
        list.getItems().addListener((ListChangeListener<Contact>) c -> count.run());
        list.totalProperty().subscribe(t -> count.run());

        VBox.setVgrow(list, Priority.ALWAYS);
        return page(toolbar, list, summary);
    }

    /// Invoices: shadcn's data table.
    static Node invoices() {
        SelectionState<Invoice> selection = new SelectionState<>(Invoice::id, SelectionMode.MULTIPLE);
        TableMfx<Invoice> table = new TableMfx<>(SampleData.invoices());
        table.columns(
                new SelectionColumnMfx<>(selection),
                TableColumnMfx.<Invoice, String>of("id", "Invoice", i -> "INV" + i.id()).width(ColumnWidth.fixed(90)),
                TableColumnMfx.of("customer", "Customer", Invoice::customer)
                        .renderer(new AvatarCellRenderer<>()).width(ColumnWidth.flex(2)),
                TableColumnMfx.of("status", "Status", Invoice::status)
                        .renderer(new BadgeCellRenderer<>(Status::label,
                                s -> s == Status.OVERDUE ? BadgeMfx.Variant.DESTRUCTIVE : BadgeMfx.Variant.OUTLINE)),
                TableColumnMfx.<Invoice, Temporal>of("issued", "Issued", Invoice::issued)
                        .renderer(DateCellRenderer.relative()),
                TableColumnMfx.<Invoice, Boolean>of("paid", "Paid", Invoice::paid)
                        .renderer(BooleanCellRenderer.check()).width(ColumnWidth.fixed(64)),
                TableColumnMfx.<Invoice, Number>of("amount", "Amount", Invoice::amount)
                        .renderer(new MoneyCellRenderer<>(Currency.getInstance("USD"), Locale.US)),
                new RowActionsColumnMfx<Invoice>()
                        .inline(Feather.CHECK, "Mark paid", inv -> replace(table, inv.withStatus(Status.PAID)))
                        .menu("Mark overdue", inv -> replace(table, inv.withStatus(Status.OVERDUE)))
                        .dangerMenu("Delete", inv -> table.getItems().remove(inv)));

        ButtonMfx markPaid = new ButtonMfx("Mark paid").variant(ButtonMfx.Variant.OUTLINE).size(ButtonMfx.Size.SM);
        markPaid.setOnAction(e -> {
            table.getItems().replaceAll(inv -> selection.isSelected(inv) ? inv.withStatus(Status.PAID) : inv);
            selection.clear();
        });
        ButtonMfx delete = new ButtonMfx("Delete").variant(ButtonMfx.Variant.DESTRUCTIVE).size(ButtonMfx.Size.SM);
        delete.setOnAction(e -> {
            table.getItems().removeIf(selection::isSelected);
            selection.clear();
        });
        BulkActionBarMfx bulk = new BulkActionBarMfx(selection);
        bulk.getActions().addAll(markPaid, delete);

        CollectionToolbarMfx toolbar = new CollectionToolbarMfx();
        toolbar.search().setPromptText("Search customers…");
        List<Invoice> all = new ArrayList<>(table.getItems());
        toolbar.search().searchProperty().subscribe(q -> {
            String text = q.toLowerCase(Locale.ROOT);
            table.getItems().setAll(all.stream().filter(i -> i.customer().toLowerCase(Locale.ROOT).contains(text)).toList());
        });
        toolbar.getTrailing().add(new ColumnVisibilityMenuMfx(table));
        toolbar.setBulkActions(bulk);

        TableFooterMfx<Invoice> footer = new TableFooterMfx<>(table, selection)
                .sum("Total", i -> i.amount().doubleValue(), NumberFormat.getCurrencyInstance(Locale.US))
                .summary("Unpaid", rows -> NumberFormat.getCurrencyInstance(Locale.US).format(rows.stream()
                        .filter(i -> !i.paid()).map(Invoice::amount).reduce(BigDecimal.ZERO, BigDecimal::add)));

        VBox.setVgrow(table, Priority.ALWAYS);
        return page(toolbar, table, footer);
    }

    private static void replace(TableMfx<Invoice> table, Invoice changed) {
        table.getItems().replaceAll(i -> i.id() == changed.id() ? changed : i);
    }

    /// Tasks: a Kanban board.
    static Node tasks() {
        BoardMfx<Task> board = new BoardMfx<>(t -> Tw.set(new Label(t.title()), "text-sm"));
        board.addColumn(BoardColumnSpec.of("todo", "To do"));
        board.addColumn(BoardColumnSpec.of("doing", "In progress").withWipLimit(2));
        board.addColumn(BoardColumnSpec.of("done", "Done"));
        board.setItems(SampleData.tasks(), Task::column);
        int[] next = {100};
        board.setOnAdd(column -> board.add(column, new Task(next[0]++, "New task", column)));
        board.setOnMove(m -> {
            if (m.changedColumn()) {
                ToastMfx.show(board, "\"" + m.item().title() + "\" moved", "Now in " + board.column(m.toColumn()).spec().title());
            }
        });
        VBox.setVgrow(board, Priority.ALWAYS);
        return page(muted("Drag cards between columns. In progress takes 2 at most; + adds a card."), board);
    }

    /// Files: a tree whose folders load when first opened.
    static Node files() {
        LazyTreeLoader<FileEntry> loader = LazyTreeLoader.blocking(folder -> {
            Thread.sleep(600); // a slow disk or server
            List<TreeNode<FileEntry>> children = new ArrayList<>();
            children.add(TreeNode.lazy(new FileEntry(folder.name() + "-old", 0, true)));
            for (int i = 1; i <= 3; i++) {
                children.add(TreeNode.leaf(new FileEntry(folder.name() + "-" + i + ".pdf", i * 240_000L, false)));
            }
            return children;
        });
        TreeMfx<FileEntry> tree = new TreeMfx<>();
        tree.setText(FileEntry::name);
        tree.setIcon(f -> new FontIcon(f.folder() ? Feather.FOLDER : Feather.FILE_TEXT));
        tree.setLoader(loader);
        tree.setKeyExtractor(FileEntry::name);
        tree.setExpansionState(new TreeExpansionState());
        tree.setRootNode(TreeNode.of(new FileEntry("root", 0, true),
                TreeNode.of(new FileEntry("contracts", 0, true),
                        TreeNode.leaf(new FileEntry("acme.pdf", 182_000, false)),
                        TreeNode.leaf(new FileEntry("globex.pdf", 240_500, false))),
                TreeNode.lazy(new FileEntry("receipts", 0, true)),
                TreeNode.lazy(new FileEntry("archive", 0, true)),
                TreeNode.leaf(new FileEntry("notes.txt", 2_300, false))));

        Label detail = muted("Pick a file.");
        NumberFormat bytes = NumberFormat.getIntegerInstance();
        tree.getSelectionModel().selectedItemProperty().subscribe(item -> {
            FileEntry f = item == null ? null : item.getValue();
            detail.setText(f == null ? "Pick a file." : f.folder() ? f.name() + " (folder)" : f.name() + " · " + bytes.format(f.size()) + " bytes");
        });
        HBox.setHgrow(tree, Priority.ALWAYS);
        VBox.setVgrow(tree, Priority.ALWAYS);
        return page(muted("Receipts and archive load when opened."), tree, detail);
    }

    /// Timesheet: a spreadsheet with checks, undo and Excel copy and paste.
    static Node timesheet() {
        List<String> days = List.of("Name", "Mon", "Tue", "Wed", "Thu", "Fri");
        List<List<?>> rows = new ArrayList<>();
        for (Contact c : SampleData.contacts().subList(0, 12)) {
            rows.add(List.of(c.name(), 8, 8, 7.5, 8, 6));
        }
        SheetMfx sheet = SheetMfx.of(days, rows);
        sheet.setValidation(SheetValidation.numberBetween(CellRange.of(0, 1, rows.size() - 1, 5), BigDecimal.ZERO, BigDecimal.valueOf(24))
                .and(SheetValidation.required(CellRange.of(0, 0, rows.size() - 1, 0))));

        IconButtonMfx undo = new IconButtonMfx(new FontIcon(Feather.ROTATE_CCW), "Undo (Ctrl+Z)").variant(ButtonVariant.OUTLINE).size(ButtonSize.SM);
        IconButtonMfx redo = new IconButtonMfx(new FontIcon(Feather.ROTATE_CW), "Redo (Ctrl+Y)").variant(ButtonVariant.OUTLINE).size(ButtonSize.SM);
        undo.disableProperty().bind(sheet.undoStack().canUndoProperty().not());
        redo.disableProperty().bind(sheet.undoStack().canRedoProperty().not());
        undo.setOnAction(e -> sheet.undoStack().undo());
        redo.setOnAction(e -> sheet.undoStack().redo());

        Label where = muted("");
        sheet.selection().addListener(() -> {
            var range = sheet.selection().selectedRange();
            where.setText(range == null ? "" : range.toString());
        });
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(8, undo, redo, spacer, where);
        bar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label hint = muted("Hours are 0 to 24. Copy and paste works with Excel and Google Sheets.");
        VBox.setVgrow(sheet, Priority.ALWAYS);
        return page(bar, sheet, hint);
    }
}
