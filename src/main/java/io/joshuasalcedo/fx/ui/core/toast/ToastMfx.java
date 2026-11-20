package io.joshuasalcedo.fx.ui.core.toast;

import atlantafx.base.controls.Notification;
import io.joshuasalcedo.fx.ui.core.modal.ModalMfx;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// shadcn's Toast (Sonner): a short message in the bottom-right corner that goes away by itself
/// (AtlantaFX's Notification). Toasts stack, newest at the bottom; the rest of the window stays usable.
///
/// ```java
/// ToastMfx.show(anyNode, "Event created", "Sunday, December 03 at 9:00 AM");
/// ToastMfx.show(anyNode, new ToastMfx("Upload failed").variant(ToastMfx.Variant.ERROR));
/// ```
@NullMarked
public class ToastMfx extends Notification {

    /// The style class every ToastMfx has.
    public static final String STYLE_CLASS = "mfx-toast";

    /// How long a toast stays.
    public static final Duration DURATION = Duration.seconds(4);

    public enum Variant {
        DEFAULT(null), SUCCESS(Feather.CHECK_CIRCLE), INFO(Feather.INFO), WARNING(Feather.ALERT_TRIANGLE),
        ERROR(Feather.ALERT_CIRCLE);

        private final @Nullable Feather icon;

        Variant(@Nullable Feather icon) {
            this.icon = icon;
        }

        public String styleClass() {
            return "mfx-toast-" + name().toLowerCase();
        }
    }

    private static final String TOASTER = ToastMfx.class.getName() + ".toaster";
    private Variant variant = Variant.DEFAULT;

    public ToastMfx(String message) {
        super(message);
        getStyleClass().add(STYLE_CLASS);
        setMaxWidth(356);
        setPrefWidth(356);
        setMaxHeight(USE_PREF_SIZE);
        setOnClose(e -> dismiss());
    }

    /// A message with a second, quieter line.
    public ToastMfx(String message, String description) {
        this(message + "\n" + description);
    }

    public ToastMfx variant(Variant v) {
        getStyleClass().remove(variant.styleClass());
        variant = v;
        getStyleClass().add(v.styleClass());
        setGraphic(v.icon == null ? null : new FontIcon(v.icon));
        return this;
    }

    /// A button on the toast (Sonner's action), e.g. Undo.
    public ToastMfx action(String text, Runnable onAction) {
        Button b = new Button(text);
        b.getStyleClass().add("mfx-toast-action");
        b.setOnAction(e -> {
            onAction.run();
            dismiss();
        });
        setPrimaryActions(b);
        return this;
    }

    /// Fades it out and removes it.
    public void dismiss() {
        FadeTransition out = new FadeTransition(Duration.millis(150), this);
        out.setToValue(0);
        out.setOnFinished(e -> {
            if (getParent() instanceof VBox box) {
                box.getChildren().remove(this);
            }
        });
        out.play();
    }

    /// Shows a toast in the window that `owner` is in.
    public static ToastMfx show(Node owner, String message) {
        return show(owner, new ToastMfx(message));
    }

    public static ToastMfx show(Node owner, String message, String description) {
        return show(owner, new ToastMfx(message, description));
    }

    public static ToastMfx show(Node owner, ToastMfx toast) {
        Scene scene = owner.getScene();
        if (scene == null) {
            throw new IllegalStateException("the owner must be in a scene");
        }
        toaster(scene).getChildren().add(toast);
        toast.setOpacity(0);
        FadeTransition in = new FadeTransition(Duration.millis(150), toast);
        in.setToValue(1);
        in.play();
        PauseTransition stay = new PauseTransition(DURATION);
        stay.setOnFinished(e -> toast.dismiss());
        toast.setOnMouseEntered(e -> stay.pause());
        toast.setOnMouseExited(e -> stay.play());
        stay.play();
        return toast;
    }

    private static VBox toaster(Scene scene) {
        if (scene.getProperties().get(TOASTER) instanceof VBox box) {
            return box;
        }
        VBox box = new VBox();
        box.getStyleClass().add("mfx-toaster");
        box.setPickOnBounds(false);
        box.setAlignment(Pos.BOTTOM_RIGHT);
        box.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        StackPane root = ModalMfx.overlayRoot(scene);
        StackPane.setAlignment(box, Pos.BOTTOM_RIGHT);
        root.getChildren().add(box);
        box.setViewOrder(-1000);
        scene.getProperties().put(TOASTER, box);
        return box;
    }
}
