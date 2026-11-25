package io.joshuasalcedo.fx.ui.core.toolbar;

import io.joshuasalcedo.fx.ui.core.button.ButtonMfx;
import io.joshuasalcedo.fx.ui.core.data.SelectionState;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.jspecify.annotations.NullMarked;

/// The bar that shows while items are selected: "3 selected", the actions for them, and Clear. It hides itself
/// when nothing is selected.
///
/// ```java
/// var bulk = new BulkActionBarMfx(selection);
/// bulk.getActions().add(new ButtonMfx("Archive"));
/// ```
@NullMarked
public class BulkActionBarMfx extends HBox {

    public static final String STYLE_CLASS = "mfx-bulk-action-bar";

    private final HBox actions = new HBox(8);

    public BulkActionBarMfx(SelectionState<?> selection) {
        Objects.requireNonNull(selection, "selection");
        getStyleClass().add(STYLE_CLASS);
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);
        NumberFormat numbers = NumberFormat.getIntegerInstance(Locale.getDefault(Locale.Category.FORMAT));
        Label count = new Label();
        count.getStyleClass().add("mfx-bulk-count");
        count.textProperty().bind(selection.countProperty().map(n -> numbers.format(n) + " selected"));
        actions.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        ButtonMfx clear = new ButtonMfx("Clear selection").variant(ButtonMfx.Variant.GHOST).size(ButtonMfx.Size.SM);
        clear.setOnAction(e -> selection.clear());
        getChildren().addAll(count, actions, spacer, clear);
        visibleProperty().bind(selection.emptyProperty().not());
        managedProperty().bind(visibleProperty());
    }

    /// The buttons for the selected items.
    public ObservableList<Node> getActions() {
        return actions.getChildren();
    }
}
