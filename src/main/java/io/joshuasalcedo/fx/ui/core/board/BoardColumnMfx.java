package io.joshuasalcedo.fx.ui.core.board;

import io.joshuasalcedo.fx.ui.core.button.ButtonSize;
import io.joshuasalcedo.fx.ui.core.dnd.DropIndicatorMfx;
import io.joshuasalcedo.fx.ui.core.iconbutton.IconButtonMfx;
import java.util.Objects;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// A board column: a header (color dot, title, count, and a + button when the board has `onAdd`) over its cards,
/// which scroll. Over its WIP limit the column is `:over-limit`. Its items are [#getItems]: change them and the
/// cards follow.
@NullMarked
public class BoardColumnMfx<T> extends VBox {

    public static final String STYLE_CLASS = "mfx-board-column";

    private static final PseudoClass OVER_LIMIT = PseudoClass.getPseudoClass("over-limit");
    private static final PseudoClass DROP_TARGET = PseudoClass.getPseudoClass("drop-target");

    private final BoardColumnSpec spec;
    private final BoardMfx<T> board;
    private final ObservableList<T> items = FXCollections.observableArrayList();
    private final VBox cards = new VBox();
    private final Label count = new Label();
    private final IconButtonMfx add = new IconButtonMfx(new FontIcon(Feather.PLUS), "Add");
    private final DropIndicatorMfx indicator = new DropIndicatorMfx();

    BoardColumnMfx(BoardMfx<T> board, BoardColumnSpec spec) {
        this.board = board;
        this.spec = Objects.requireNonNull(spec, "spec");
        getStyleClass().add(STYLE_CLASS);

        HBox header = new HBox();
        header.getStyleClass().add("mfx-board-column-header");
        header.setAlignment(Pos.CENTER_LEFT);
        if (spec.color() != null) {
            Region dot = new Region();
            dot.getStyleClass().add("mfx-board-column-dot");
            dot.setStyle("-fx-background-color: " + spec.color() + ";");
            header.getChildren().add(dot);
        }
        Label title = new Label(spec.title());
        title.getStyleClass().add("mfx-board-column-title");
        count.getStyleClass().add("mfx-board-column-count");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        add.size(ButtonSize.SM);
        add.visibleProperty().bind(board.onAddProperty().isNotNull());
        add.managedProperty().bind(add.visibleProperty());
        add.setOnAction(e -> {
            var onAdd = board.onAddProperty().get();
            if (onAdd != null) {
                onAdd.accept(spec.key());
            }
        });
        header.getChildren().addAll(title, count, spacer, add);

        cards.getStyleClass().add("mfx-board-cards");
        ScrollPane scroll = new ScrollPane(cards);
        scroll.getStyleClass().add("mfx-scroll-area");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().addAll(header, scroll);

        items.addListener((ListChangeListener<T>) c -> refresh());
        refresh();

        addEventHandler(DragEvent.DRAG_OVER, e -> {
            if (board.dragged() == null) {
                return;
            }
            e.acceptTransferModes(TransferMode.MOVE);
            showIndicator(dropIndex(e.getSceneY()));
            e.consume();
        });
        addEventHandler(DragEvent.DRAG_EXITED, e -> hideIndicator());
        addEventHandler(DragEvent.DRAG_DROPPED, e -> {
            T dragged = board.dragged();
            if (dragged == null) {
                return;
            }
            int index = dropIndex(e.getSceneY());
            hideIndicator();
            board.drop(dragged, this, index);
            e.setDropCompleted(true);
            e.consume();
        });
    }

    public BoardColumnSpec spec() {
        return spec;
    }

    public String key() {
        return spec.key();
    }

    /// The column's items, top to bottom.
    public ObservableList<T> getItems() {
        return items;
    }

    /// Makes the cards again from the items.
    void refresh() {
        Function<T, Node> content = board.cardFactory();
        cards.getChildren().clear();
        for (T item : items) {
            cards.getChildren().add(card(item, content.apply(item)));
        }
        int n = items.size();
        count.setText(spec.wipLimit() > 0 ? n + " / " + spec.wipLimit() : String.valueOf(n));
        pseudoClassStateChanged(OVER_LIMIT, spec.wipLimit() > 0 && n > spec.wipLimit());
    }

    private BoardCardMfx<T> card(T item, Node content) {
        BoardCardMfx<T> card = new BoardCardMfx<>(item, content);
        card.addEventHandler(MouseEvent.DRAG_DETECTED, e -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent data = new ClipboardContent();
            data.putString(spec.key() + ":" + items.indexOf(item));
            db.setContent(data);
            db.setDragView(card.snapshot(null, null));
            card.pseudoClassStateChanged(BoardCardMfx.DRAGGING, true);
            board.dragStarted(item);
            e.consume();
        });
        card.addEventHandler(DragEvent.DRAG_DONE, e -> {
            card.pseudoClassStateChanged(BoardCardMfx.DRAGGING, false);
            board.dragEnded();
        });
        return card;
    }

    /// The index among the cards (without the indicator) a drop at this scene y lands at.
    private int dropIndex(double sceneY) {
        double y = cards.sceneToLocal(0, sceneY).getY();
        int index = 0;
        for (Node n : cards.getChildren()) {
            if (n == indicator) {
                continue;
            }
            if (y > n.getBoundsInParent().getMinY() + n.getBoundsInParent().getHeight() / 2) {
                index++;
            }
        }
        return index;
    }

    private void showIndicator(int index) {
        cards.getChildren().remove(indicator);
        cards.getChildren().add(Math.min(index, cards.getChildren().size()), indicator);
        pseudoClassStateChanged(DROP_TARGET, true);
    }

    private void hideIndicator() {
        cards.getChildren().remove(indicator);
        pseudoClassStateChanged(DROP_TARGET, false);
    }
}
