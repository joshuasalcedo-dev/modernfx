package io.joshuasalcedo.fx.ui.core.placeholder;

import io.joshuasalcedo.fx.ui.core.button.ButtonMfx;
import io.joshuasalcedo.fx.ui.core.data.LoadState;
import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// Shows a collection's content, or what stands in for it, by its LoadState: skeleton rows while the first page
/// loads, the empty state when there are no items, the error with a Retry button when loading failed, and the
/// content otherwise (IDLE, LOADED, LOADING_MORE).
///
/// ```java
/// var placeholder = new CollectionPlaceholderMfx(listView);
/// placeholder.setEmpty(EmptyStateSpec.of("No contacts", "Add one to get started."));
/// placeholder.setOnRetry(this::reload);
/// placeholder.setState(LoadState.LOADING);
/// ```
@NullMarked
public class CollectionPlaceholderMfx extends StackPane {

    public static final String STYLE_CLASS = "mfx-collection-placeholder";

    private final ObjectProperty<LoadState> state = new SimpleObjectProperty<>(this, "state", LoadState.IDLE) {
        @Override
        protected void invalidated() {
            update();
        }
    };
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content") {
        @Override
        protected void invalidated() {
            update();
        }
    };
    private final ObjectProperty<EmptyStateSpec> empty = new SimpleObjectProperty<>(this, "empty", EmptyStateSpec.DEFAULT) {
        @Override
        protected void invalidated() {
            update();
        }
    };
    private final StringProperty errorMessage = new SimpleStringProperty(this, "errorMessage", "") {
        @Override
        protected void invalidated() {
            update();
        }
    };
    private final ObjectProperty<@Nullable Runnable> onRetry = new SimpleObjectProperty<>(this, "onRetry") {
        @Override
        protected void invalidated() {
            update();
        }
    };
    private final ObjectProperty<Node> loading = new SimpleObjectProperty<>(this, "loading", new SkeletonRowsMfx()) {
        @Override
        protected void invalidated() {
            update();
        }
    };

    public CollectionPlaceholderMfx() {
        getStyleClass().add(STYLE_CLASS);
        update();
    }

    public CollectionPlaceholderMfx(Node content) {
        this();
        setContent(content);
    }

    public final ObjectProperty<LoadState> stateProperty() { return state; }
    public final LoadState getState() { return state.get(); }
    public final void setState(LoadState value) { state.set(Objects.requireNonNull(value)); }

    /// What shows when the items are there.
    public final ObjectProperty<@Nullable Node> contentProperty() { return content; }
    public final @Nullable Node getContent() { return content.get(); }
    public final void setContent(@Nullable Node value) { content.set(value); }

    /// What shows while the first page loads: skeleton rows by default.
    public final ObjectProperty<Node> loadingProperty() { return loading; }
    public final void setLoading(Node value) { loading.set(Objects.requireNonNull(value)); }

    /// What an empty collection says.
    public final ObjectProperty<EmptyStateSpec> emptyProperty() { return empty; }
    public final EmptyStateSpec getEmpty() { return empty.get(); }
    public final void setEmpty(EmptyStateSpec value) { empty.set(Objects.requireNonNull(value)); }

    /// The message shown in the ERROR state.
    public final StringProperty errorMessageProperty() { return errorMessage; }
    public final void setErrorMessage(String value) { errorMessage.set(value); }

    /// What the Retry button runs. Without it there is no Retry button.
    public final ObjectProperty<@Nullable Runnable> onRetryProperty() { return onRetry; }
    public final void setOnRetry(@Nullable Runnable value) { onRetry.set(value); }

    private void update() {
        Node shown = switch (getState()) {
            case LOADING -> loading.get();
            case EMPTY -> emptyView(getEmpty());
            case ERROR -> errorView();
            case IDLE, LOADED, LOADING_MORE -> content.get();
        };
        if (shown == null) {
            getChildren().clear();
        } else if (getChildren().size() != 1 || getChildren().getFirst() != shown) {
            getChildren().setAll(shown);
        }
    }

    private Node emptyView(EmptyStateSpec spec) {
        Runnable action = spec.action();
        return message(spec.icon() == null ? null : new FontIcon(spec.icon()), spec.title(), spec.message(),
                spec.actionText(), action);
    }

    private Node errorView() {
        String text = errorMessage.get().isBlank() ? "Something went wrong while loading." : errorMessage.get();
        Runnable retry = onRetry.get();
        Node view = message(new FontIcon(Feather.ALERT_CIRCLE), "Couldn't load", text, retry == null ? null : "Retry", retry);
        view.getStyleClass().add("mfx-collection-error");
        return view;
    }

    private static Node message(@Nullable Node icon, String title, String text,
                                @Nullable String actionText, @Nullable Runnable action) {
        VBox box = new VBox();
        box.getStyleClass().add("mfx-empty-state");
        box.setAlignment(Pos.CENTER);
        if (icon != null) {
            StackPane media = new StackPane(icon);
            media.getStyleClass().add("mfx-empty-state-icon");
            box.getChildren().add(media);
        }
        Label t = new Label(title);
        t.getStyleClass().add("mfx-empty-state-title");
        Label m = new Label(text);
        m.getStyleClass().add("mfx-empty-state-message");
        m.setWrapText(true);
        box.getChildren().addAll(t, m);
        if (actionText != null && action != null) {
            ButtonMfx b = new ButtonMfx(actionText).variant(ButtonMfx.Variant.OUTLINE).size(ButtonMfx.Size.SM);
            b.setOnAction(e -> action.run());
            box.getChildren().add(b);
        }
        return box;
    }
}
