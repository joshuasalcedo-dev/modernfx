package io.joshuasalcedo.fx.ui.core.list;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// The slots of a list row: `leading` (an avatar or icon), `title`, `subtitle`, `meta` (a time, on the title's
/// line) and `trailing` (a badge or an action). Empty slots take no space; long text ends in an ellipsis.
///
/// ```java
/// new ListItemLayout().leading(avatar).title("Ana Ruiz").subtitle("Can we move the call?").meta("2m");
/// ```
@NullMarked
public class ListItemLayout extends HBox {

    public static final String STYLE_CLASS = "mfx-list-item";

    private final StackPane leadingBox = slot("mfx-list-item-leading");
    private final StackPane trailingBox = slot("mfx-list-item-trailing");
    private final Label titleLabel = text("mfx-list-item-title");
    private final Label subtitleLabel = text("mfx-list-item-subtitle");
    private final Label metaLabel = text("mfx-list-item-meta");
    private final ObjectProperty<@Nullable Node> leading = new SimpleObjectProperty<>(this, "leading") {
        @Override
        protected void invalidated() {
            fill(leadingBox, get());
        }
    };
    private final ObjectProperty<@Nullable Node> trailing = new SimpleObjectProperty<>(this, "trailing") {
        @Override
        protected void invalidated() {
            fill(trailingBox, get());
        }
    };

    public ListItemLayout() {
        getStyleClass().add(STYLE_CLASS);
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);
        metaLabel.setMinWidth(USE_PREF_SIZE);
        HBox titleRow = new HBox(8, titleLabel, metaLabel);
        titleRow.setAlignment(Pos.BASELINE_LEFT);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        VBox text = new VBox(2, titleRow, subtitleLabel);
        text.setAlignment(Pos.CENTER_LEFT);
        text.setMinWidth(0);
        HBox.setHgrow(text, Priority.ALWAYS);
        getChildren().addAll(leadingBox, text, trailingBox);
        fill(leadingBox, null);
        fill(trailingBox, null);
    }

    public ListItemLayout leading(@Nullable Node value) {
        leading.set(value);
        return this;
    }

    public ListItemLayout title(@Nullable String value) {
        titleLabel.setText(value);
        return this;
    }

    public ListItemLayout subtitle(@Nullable String value) {
        subtitleLabel.setText(value);
        return this;
    }

    public ListItemLayout meta(@Nullable String value) {
        metaLabel.setText(value);
        return this;
    }

    public ListItemLayout trailing(@Nullable Node value) {
        trailing.set(value);
        return this;
    }

    /// Empties every slot, to fill it again for another item.
    public ListItemLayout clear() {
        return leading(null).title(null).subtitle(null).meta(null).trailing(null);
    }

    public final ObjectProperty<@Nullable Node> leadingProperty() { return leading; }
    public final ObjectProperty<@Nullable Node> trailingProperty() { return trailing; }
    public final StringProperty titleProperty() { return titleLabel.textProperty(); }
    public final StringProperty subtitleProperty() { return subtitleLabel.textProperty(); }
    public final StringProperty metaProperty() { return metaLabel.textProperty(); }

    private static StackPane slot(String styleClass) {
        StackPane box = new StackPane();
        box.getStyleClass().add(styleClass);
        box.setMinWidth(USE_PREF_SIZE);
        return box;
    }

    private static void fill(StackPane box, @Nullable Node node) {
        if (node == null) {
            box.getChildren().clear();
        } else {
            box.getChildren().setAll(node);
        }
        box.setVisible(node != null);
        box.setManaged(node != null);
    }

    private static Label text(String styleClass) {
        Label label = new Label();
        label.getStyleClass().add(styleClass);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        label.setMinWidth(0);
        label.managedProperty().bind(label.textProperty().isNotEmpty());
        label.visibleProperty().bind(label.managedProperty());
        return label;
    }
}
