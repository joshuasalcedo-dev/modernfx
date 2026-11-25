package io.joshuasalcedo.fx.ui.core;

import static org.assertj.core.api.Assertions.assertThat;

import io.joshuasalcedo.fx.ui.core.board.BoardColumnSpec;
import io.joshuasalcedo.fx.ui.core.board.BoardMfx;
import io.joshuasalcedo.fx.ui.core.board.BoardMove;
import io.joshuasalcedo.fx.ui.core.button.ButtonMfx;
import io.joshuasalcedo.fx.ui.core.buttongroup.ButtonGroupMfx;
import io.joshuasalcedo.fx.ui.core.data.KeyExtractor;
import io.joshuasalcedo.fx.ui.core.data.ListDataSource;
import io.joshuasalcedo.fx.ui.core.data.LoadState;
import io.joshuasalcedo.fx.ui.core.data.SelectionMode;
import io.joshuasalcedo.fx.ui.core.data.SelectionState;
import io.joshuasalcedo.fx.ui.core.dnd.ReorderEvent;
import io.joshuasalcedo.fx.ui.core.dnd.ReorderSupport;
import io.joshuasalcedo.fx.ui.core.iconbutton.IconButtonMfx;
import io.joshuasalcedo.fx.ui.core.list.ListMfx;
import io.joshuasalcedo.fx.ui.core.segmented.SegmentedControlMfx;
import io.joshuasalcedo.fx.ui.core.splitbutton.SplitButtonMfx;
import io.joshuasalcedo.fx.ui.core.table.ColumnPin;
import io.joshuasalcedo.fx.ui.core.table.RowActionsColumnMfx;
import io.joshuasalcedo.fx.ui.core.table.SelectionColumnMfx;
import io.joshuasalcedo.fx.ui.core.table.TableColumnMfx;
import io.joshuasalcedo.fx.ui.core.table.TableFooterMfx;
import io.joshuasalcedo.fx.ui.core.table.TableLayoutState;
import io.joshuasalcedo.fx.ui.core.table.TableMfx;
import io.joshuasalcedo.fx.ui.core.toolbar.SearchFieldMfx;
import io.joshuasalcedo.fx.ui.core.tree.TreeExpansionState;
import io.joshuasalcedo.fx.ui.core.tree.TreeMfx;
import io.joshuasalcedo.fx.ui.core.tree.TreeNode;
import io.joshuasalcedo.fx.utils.theme.ThemeMfx;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

@ExtendWith(ApplicationExtension.class)
class CoreComponentsTest {

    record Task(int id, String title, String status) {
    }

    private StackPane root;

    @Start
    void start(Stage stage) {
        root = new StackPane();
        Scene scene = new Scene(root, 640, 480);
        ThemeMfx.install(scene, ThemeMfx.Mode.LIGHT);
        stage.setScene(scene);
        stage.show();
    }

    private void show(javafx.scene.Node node) {
        WaitForAsyncUtils.asyncFx(() -> root.getChildren().setAll(node));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private static void fx(Runnable run) {
        WaitForAsyncUtils.asyncFx(run);
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void buttonGroupKeepsThePositionClassesRight() {
        var a = new ButtonMfx("Day");
        var b = new ButtonMfx("Week");
        var c = new ButtonMfx("Month");
        var group = new ButtonGroupMfx(a, b, c);
        show(group);
        assertThat(a.getStyleClass()).contains(ButtonGroupMfx.FIRST);
        assertThat(b.getStyleClass()).contains(ButtonGroupMfx.MIDDLE);
        assertThat(c.getStyleClass()).contains(ButtonGroupMfx.LAST);

        fx(() -> c.setVisible(false));
        assertThat(b.getStyleClass()).contains(ButtonGroupMfx.LAST).doesNotContain(ButtonGroupMfx.MIDDLE);

        fx(() -> group.getButtons().remove(a));
        assertThat(b.getStyleClass()).contains(ButtonGroupMfx.ONLY);
        assertThat(a.getStyleClass()).doesNotContain(ButtonGroupMfx.FIRST);
        assertThat(group.getWidth()).isGreaterThan(0);
    }

    @Test
    void iconButtonIsSquareAndLoadingBlocksClicks() {
        var button = new IconButtonMfx(new FontIcon(Feather.TRASH), "Delete");
        AtomicInteger clicks = new AtomicInteger();
        button.setOnAction(e -> clicks.incrementAndGet());
        show(button);
        assertThat(button.getWidth()).isEqualTo(button.getHeight());
        assertThat(button.getTooltip().getText()).isEqualTo("Delete");

        fx(() -> {
            button.setLoading(true);
            button.fire();
        });
        assertThat(clicks).hasValue(0);
        fx(() -> {
            button.setLoading(false);
            button.fire();
        });
        assertThat(clicks).hasValue(1);
        assertThat(button.getGraphic()).isInstanceOf(FontIcon.class);
    }

    @Test
    void segmentedControlCantBeClearedAndArrowsMove() {
        var filter = new SegmentedControlMfx<>(List.of("All", "Unread", "VIP"));
        show(filter);
        assertThat(filter.getValue()).isEqualTo("All");
        var first = (javafx.scene.control.ToggleButton) filter.lookup(".mfx-segmented-item");
        fx(first::fire);
        assertThat(filter.getValue()).as("clicking the selected one keeps it").isEqualTo("All");
        assertThat(first.isSelected()).isTrue();
        fx(() -> filter.fireEvent(new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED, "", "",
                javafx.scene.input.KeyCode.LEFT, false, false, false, false)));
        assertThat(filter.getValue()).isEqualTo("VIP");
        fx(() -> filter.getItems().remove("VIP"));
        assertThat(filter.getValue()).isEqualTo("All");
    }

    @Test
    void splitButtonRunsItsActionAndHasItsMenu() {
        var send = new SplitButtonMfx("Send");
        AtomicInteger sent = new AtomicInteger();
        send.setOnAction(e -> sent.incrementAndGet());
        send.getItems().add(new javafx.scene.control.MenuItem("Send later"));
        show(send);
        fx(send::fire);
        assertThat(sent).hasValue(1);
        fx(() -> send.setDisable(true));
        fx(send::fire);
        assertThat(sent).hasValue(1);
        assertThat(send.getWidth()).isGreaterThan(send.getHeight());
    }

    @Test
    void listLoadsPagesKeepsSelectionAndFilters() {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            tasks.add(new Task(i, "Task " + i, i % 2 == 0 ? "todo" : "done"));
        }
        var list = new ListMfx<Task>();
        list.setSelection(new SelectionState<>(Task::id, SelectionMode.MULTIPLE));
        list.setCellFactory((row, t) -> row.title(t.title()).subtitle(t.status()));
        list.setPageSize(10);
        fx(() -> list.setDataSource(new ListDataSource<>(tasks).filterBy((t, q) -> t.title().endsWith(q))));
        show(list);
        assertThat(list.getItems()).hasSize(10);
        assertThat(list.getLoadState()).isEqualTo(LoadState.LOADED);
        assertThat(list.hasMoreProperty().get()).isTrue();
        assertThat(list.totalProperty().get()).isEqualTo(30);

        fx(() -> list.listView().getSelectionModel().select(3));
        assertThat(list.getSelection().keys()).containsExactly(3);

        fx(list::loadMore);
        assertThat(list.getItems()).hasSize(20);
        fx(list::reload);
        assertThat(list.listView().getSelectionModel().getSelectedIndices()).as("selected again after a reload")
                .containsExactly(3);

        fx(() -> list.setFilter("7"));
        assertThat(list.getItems()).extracting(Task::id).containsExactly(7, 17, 27);
        fx(() -> list.setFilter("nothing"));
        assertThat(list.getLoadState()).isEqualTo(LoadState.EMPTY);
    }

    @Test
    void listShowsTheErrorAndRetries() {
        AtomicInteger calls = new AtomicInteger();
        var list = new ListMfx<String>();
        fx(() -> list.setDataSource(r -> calls.incrementAndGet() == 1
                ? CompletableFuture.failedFuture(new IllegalStateException("offline"))
                : CompletableFuture.completedFuture(io.joshuasalcedo.fx.ui.core.data.Page.of(List.of("a"), 1))));
        show(list);
        assertThat(list.getLoadState()).isEqualTo(LoadState.ERROR);
        assertThat(list.placeholder().errorMessageProperty().get()).isEqualTo("offline");
        fx(list::reload);
        assertThat(list.getLoadState()).isEqualTo(LoadState.LOADED);
        assertThat(list.getItems()).containsExactly("a");
    }

    @Test
    void tableKeepsPinsSelectsAllAndSavesItsLayout() {
        var selection = new SelectionState<Task>(Task::id, SelectionMode.MULTIPLE);
        var table = new TableMfx<Task>();
        var title = TableColumnMfx.of("title", "Title", Task::title);
        var status = TableColumnMfx.of("status", "Status", Task::status);
        var id = TableColumnMfx.of("id", "Id", Task::id).pin(ColumnPin.LEFT);
        var actions = new RowActionsColumnMfx<Task>().inline(Feather.EDIT_2, "Edit", t -> { });
        var select = new SelectionColumnMfx<>(selection);
        fx(() -> {
            table.getColumns().addAll(actions, title, status, id, select);
            table.getItems().setAll(new Task(1, "a", "todo"), new Task(2, "b", "done"));
        });
        var footer = new TableFooterMfx<>(table, selection);
        show(new javafx.scene.layout.VBox(table, footer));
        assertThat(table.getColumns()).containsExactly(id, select, title, status, actions);

        fx(() -> ((io.joshuasalcedo.fx.ui.core.checkbox.CheckboxMfx) select.getGraphic()).fire());
        assertThat(selection.count()).isEqualTo(2);
        assertThat(((Label) footer.getChildren().getFirst()).getText()).isEqualTo("2 of 2 selected");

        fx(() -> {
            status.setVisible(false);
            table.getSortOrder().setAll(List.of(title));
        });
        TableLayoutState saved = TableLayoutState.capture(table);
        assertThat(saved.hidden()).containsExactly("status");
        fx(() -> {
            status.setVisible(true);
            table.getSortOrder().clear();
            saved.apply(table);
        });
        assertThat(status.isVisible()).isFalse();
        assertThat(table.getSortOrder()).containsExactly(title);
    }

    @Test
    void reorderSupportMovesItems() {
        var view = new ListView<>(FXCollections.observableArrayList("a", "b", "c", "d"));
        AtomicReference<ReorderEvent<String>> moved = new AtomicReference<>();
        ReorderSupport<String> support = ReorderSupport.install(view, moved::set);
        show(view);
        fx(() -> support.move(0, 3));
        assertThat(view.getItems()).containsExactly("b", "c", "a", "d");
        assertThat(moved.get()).isEqualTo(new ReorderEvent<>(0, 2, "a"));
        fx(() -> view.setCellFactory(null));
        assertThat(view.getCellFactory()).as("re-wrapped after a new factory").isNotNull();
    }

    @Test
    void boardMovesCardsBetweenColumns() {
        var board = new BoardMfx<Task>(t -> new Label(t.title()));
        AtomicReference<BoardMove<Task>> moved = new AtomicReference<>();
        Task a = new Task(1, "a", "todo");
        Task b = new Task(2, "b", "todo");
        fx(() -> {
            board.addColumn(BoardColumnSpec.of("todo", "To do"));
            board.addColumn(BoardColumnSpec.of("done", "Done").withWipLimit(1));
            board.setItems(List.of(a, b), Task::status);
            board.setOnMove(moved::set);
        });
        show(board);
        fx(() -> board.move(b, "done", 0));
        assertThat(board.column("todo").getItems()).containsExactly(a);
        assertThat(board.column("done").getItems()).containsExactly(b);
        assertThat(moved.get()).isEqualTo(new BoardMove<>(b, "todo", 1, "done", 0));
        assertThat(moved.get().changedColumn()).isTrue();
        fx(() -> board.move(a, "done", 5));
        assertThat(board.column("done").getPseudoClassStates().toString()).contains("over-limit");
    }

    @Test
    void treeLoadsLazyChildrenAndRemembersWhatIsOpen() {
        var archive = TreeNode.lazy("Archive");
        var root = TreeNode.of("root", TreeNode.of("Projects", TreeNode.leaf("ModernFX")), archive);
        var tree = new TreeMfx<String>();
        var open = new TreeExpansionState();
        tree.setLoader(parent -> CompletableFuture.completedFuture(List.of(TreeNode.leaf(parent + " 2025"))));
        tree.setExpansionState(open);
        fx(() -> tree.setRootNode(root));
        show(tree);
        TreeItem<String> archiveItem = tree.getRoot().getChildren().get(1);
        assertThat(archiveItem.isLeaf()).isFalse();
        fx(() -> archiveItem.setExpanded(true));
        assertThat(archiveItem.getChildren()).extracting(TreeItem::getValue).containsExactly("Archive 2025");
        assertThat(open.keys()).contains("Archive");

        var again = new TreeMfx<String>();
        again.setKeyExtractor(KeyExtractor.identity());
        again.setExpansionState(open);
        fx(() -> again.setRootNode(TreeNode.of("root", TreeNode.of("Projects", TreeNode.leaf("x")), TreeNode.of("Archive", TreeNode.leaf("y")))));
        assertThat(again.getRoot().getChildren().get(1).isExpanded()).isTrue();
        assertThat(again.getRoot().getChildren().get(0).isExpanded()).isFalse();
    }

    @Test
    void searchFieldCommitsOnEnterAndClearsOnEscape() {
        var search = new SearchFieldMfx();
        show(search);
        fx(() -> search.setText("  ana "));
        assertThat(search.getSearch()).as("not before the pause").isEmpty();
        fx(search::commit);
        assertThat(search.getSearch()).isEqualTo("ana");
        fx(() -> search.fireEvent(new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED, "", "",
                javafx.scene.input.KeyCode.ESCAPE, false, false, false, false)));
        assertThat(search.getText()).isEmpty();
        assertThat(search.getSearch()).isEmpty();
    }
}
