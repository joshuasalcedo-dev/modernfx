package io.joshuasalcedo.fx.ui.core.popover;

import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.util.Duration;
import org.jspecify.annotations.NullMarked;

/// shadcn's HoverCard: a popover that opens while the pointer rests on a node (a name, an avatar, a link).
///
/// ```java
/// HoverCardMfx.attach(userLink, new UserSummary(user));
/// ```
@NullMarked
public final class HoverCardMfx {

    /// How long the pointer rests before it opens, and stays away before it closes.
    public static final Duration OPEN_DELAY = Duration.millis(600);
    public static final Duration CLOSE_DELAY = Duration.millis(300);

    private HoverCardMfx() {
    }

    /// Opens `content` under `owner` when the pointer rests on it. Returns the popover, to change it.
    public static PopoverMfx attach(Node owner, Node content) {
        PopoverMfx card = new PopoverMfx(content);
        card.getStyleClass().add("mfx-hover-card");
        PauseTransition open = new PauseTransition(OPEN_DELAY);
        PauseTransition close = new PauseTransition(CLOSE_DELAY);
        open.setOnFinished(e -> card.showBelow(owner));
        close.setOnFinished(e -> card.hide());
        owner.setOnMouseEntered(e -> {
            close.stop();
            if (!card.isShowing()) {
                open.playFromStart();
            }
        });
        owner.setOnMouseExited(e -> {
            open.stop();
            close.playFromStart();
        });
        content.setOnMouseEntered(e -> close.stop());
        content.setOnMouseExited(e -> close.playFromStart());
        return card;
    }
}
