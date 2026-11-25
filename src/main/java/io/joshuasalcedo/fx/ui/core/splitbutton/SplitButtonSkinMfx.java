package io.joshuasalcedo.fx.ui.core.splitbutton;

import io.joshuasalcedo.fx.ui.core.button.ButtonSize;
import io.joshuasalcedo.fx.ui.core.button.ButtonVariant;
import io.joshuasalcedo.fx.ui.core.internal.StyleClasses;
import io.joshuasalcedo.fx.ui.core.menu.ContextMenuMfx;
import javafx.beans.binding.Bindings;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NullMarked;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// The look of SplitButtonMfx: two hit areas side by side, the main part and the arrow, with a divider between them.
/// Each has its own hover and pressed state, and both carry the button's variant and size classes.
@NullMarked
public class SplitButtonSkinMfx extends SkinBase<SplitButtonMfx> {

    private final StackPane mainArea = new StackPane();
    private final StackPane arrowArea = new StackPane();
    private final ContextMenu menu = new ContextMenuMfx();

    public SplitButtonSkinMfx(SplitButtonMfx control) {
        super(control);
        mainArea.getStyleClass().addAll("mfx-button", "mfx-split-main");
        arrowArea.getStyleClass().addAll("mfx-button", "mfx-split-arrow");
        Label label = new Label();
        label.textProperty().bind(control.textProperty());
        mainArea.getChildren().add(label);
        arrowArea.getChildren().add(new FontIcon(Feather.CHEVRON_DOWN));
        getChildren().addAll(mainArea, arrowArea);

        for (StackPane area : new StackPane[] {mainArea, arrowArea}) {
            StyleClasses.bind(area, control.variantProperty(), ButtonVariant::styleClass);
            StyleClasses.bind(area, control.sizeProperty(), ButtonSize::styleClass);
        }
        Bindings.bindContent(menu.getItems(), control.getItems());

        mainArea.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                control.requestFocus();
                control.fire();
            }
        });
        arrowArea.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                control.requestFocus();
                control.show();
            }
        });
        control.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.DOWN && (e.isAltDown() || !menu.isShowing())) {
                control.show();
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                control.fire();
                e.consume();
            }
        });
    }

    /// Shows the menu under the button, lined up with its left edge.
    void showMenu() {
        if (!menu.getItems().isEmpty()) {
            menu.show(getSkinnable(), Side.BOTTOM, 0, 4);
        }
    }

    @Override
    protected double computePrefWidth(double height, double top, double right, double bottom, double left) {
        return left + mainArea.prefWidth(-1) + arrowArea.prefWidth(-1) + right;
    }

    @Override
    protected double computePrefHeight(double width, double top, double right, double bottom, double left) {
        return top + Math.max(mainArea.prefHeight(-1), arrowArea.prefHeight(-1)) + bottom;
    }

    @Override
    protected double computeMaxWidth(double height, double top, double right, double bottom, double left) {
        return computePrefWidth(height, top, right, bottom, left);
    }

    @Override
    protected double computeMaxHeight(double width, double top, double right, double bottom, double left) {
        return computePrefHeight(width, top, right, bottom, left);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        double arrow = snapSizeX(arrowArea.prefWidth(h));
        mainArea.resizeRelocate(x, y, Math.max(0, w - arrow), h);
        arrowArea.resizeRelocate(x + w - arrow, y, arrow, h);
    }

    @Override
    public void dispose() {
        menu.hide();
        Bindings.unbindContent(menu.getItems(), getSkinnable().getItems());
        super.dispose();
    }
}
