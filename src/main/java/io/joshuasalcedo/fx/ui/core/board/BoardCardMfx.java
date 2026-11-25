package io.joshuasalcedo.fx.ui.core.board;

import java.util.Objects;
import javafx.css.PseudoClass;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NullMarked;

/// One item on a board: a card (border, rounded corners, `-card` background) around the content the board's card
/// function made. While it is dragged it is `:dragging`.
@NullMarked
public class BoardCardMfx<T> extends StackPane {

    public static final String STYLE_CLASS = "mfx-board-card";

    static final PseudoClass DRAGGING = PseudoClass.getPseudoClass("dragging");

    private final T item;

    public BoardCardMfx(T item, Node content) {
        this.item = Objects.requireNonNull(item, "item");
        getStyleClass().add(STYLE_CLASS);
        getChildren().add(Objects.requireNonNull(content, "content"));
        setCursor(Cursor.OPEN_HAND);
        setMaxWidth(Double.MAX_VALUE);
    }

    /// The item this card shows.
    public T getItem() {
        return item;
    }
}
