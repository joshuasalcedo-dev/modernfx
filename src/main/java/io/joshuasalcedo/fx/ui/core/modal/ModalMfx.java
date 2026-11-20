package io.joshuasalcedo.fx.ui.core.modal;

import atlantafx.base.controls.ModalPane;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// The layer that dialogs, drawers and toasts show in: an AtlantaFX [ModalPane] over the scene's content.
/// It's made the first time it's needed; if the scene's root isn't a StackPane, the root is wrapped in one.
///
/// ```java
/// ModalMfx.open(anyNodeInTheScene, content, Pos.CENTER, null, false);
/// ModalMfx.close(anyNodeInTheScene);
/// ```
@NullMarked
public final class ModalMfx {

    private static final String LAYER = ModalMfx.class.getName() + ".layer";

    private ModalMfx() {
    }

    /// Shows `content` over the scene that `owner` is in. `from` slides it in from that side (null: fades in);
    /// a `persistent` modal doesn't close when the user clicks outside it or presses Escape.
    public static void open(Node owner, Node content, Pos alignment, @Nullable Side from, boolean persistent) {
        ModalPane pane = layer(scene(owner));
        pane.setAlignment(alignment);
        pane.setPersistent(persistent);
        if (from != null) {
            pane.usePredefinedTransitionFactories(from);
        } else {
            pane.usePredefinedTransitionFactories(null);
        }
        pane.show(content);
    }

    /// Closes what is showing in the scene that `owner` is in.
    public static void close(Node owner) {
        Scene scene = owner.getScene();
        if (scene != null && scene.getProperties().get(LAYER) instanceof ModalPane pane) {
            pane.hide(true);
        }
    }

    /// The scene's modal layer, made if needed.
    public static ModalPane layer(Scene scene) {
        if (scene.getProperties().get(LAYER) instanceof ModalPane pane) {
            return pane;
        }
        ModalPane pane = new ModalPane();
        pane.getStyleClass().add("mfx-modal");
        overlayRoot(scene).getChildren().add(pane);
        scene.getProperties().put(LAYER, pane);
        return pane;
    }

    /// The scene's root as a StackPane, so overlays can sit on top of the content. A root that isn't a
    /// StackPane is wrapped in one (its style classes, like `dark`, move along).
    public static StackPane overlayRoot(Scene scene) {
        Parent root = scene.getRoot();
        if (root instanceof StackPane stack) {
            return stack;
        }
        StackPane stack = new StackPane();
        scene.setRoot(stack);
        stack.getChildren().add(root);
        return stack;
    }

    private static Scene scene(Node owner) {
        return Objects.requireNonNull(owner.getScene(), "the owner must be in a scene");
    }
}
