package io.joshuasalcedo.fx.ui.core.splitbutton;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;

/// The look of SplitButtonMfx: two hit areas side by side, the main part and the arrow, with a divider between them.
/// Each has its own hover and pressed state.
public class SplitButtonSkinMfx extends SkinBase<SplitButtonMfx> {

    private final StackPane mainArea = new StackPane();
    private final StackPane arrowArea = new StackPane();
    private final ContextMenu menu = new ContextMenu();

    public SplitButtonSkinMfx(SplitButtonMfx control) {
        super(control);
        mainArea.getStyleClass().add("mfx-split-main");
        arrowArea.getStyleClass().add("mfx-split-arrow");
        getChildren().addAll(mainArea, arrowArea);
        // TODO: a label bound to control.textProperty() in mainArea, a chevron icon in arrowArea
        // TODO: mainArea click -> control.fire(); arrowArea click or Alt+Down -> showMenu()
        // TODO: keep menu.getItems() in step with control.getItems(); variant and size style classes on both areas
    }

    /// Shows the menu under the button, lined up with its left edge.
    void showMenu() {
        // TODO: menu.show(getSkinnable(), Side.BOTTOM, 0, 0)
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        // TODO: arrowArea gets its preferred width on the right, mainArea takes the rest
        super.layoutChildren(x, y, w, h);
    }

    @Override
    public void dispose() {
        menu.hide();
        super.dispose();
    }
}
