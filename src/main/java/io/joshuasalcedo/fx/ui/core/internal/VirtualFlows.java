package io.joshuasalcedo.fx.ui.core.internal;

import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.skin.VirtualFlow;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Finds the VirtualFlow inside a virtualized control (ListView, TreeView, ControlsFX's GridView) and tells when it
/// scrolls, so collections can load more or show a sticky header.
@NullMarked
public final class VirtualFlows {

    private VirtualFlows() {
    }

    /// The control's flow, once its skin is made (the skin adds it as a child when it's made).
    public static @Nullable VirtualFlow<?> find(Control control) {
        Node node = control.lookup(".virtual-flow");
        return node instanceof VirtualFlow<?> flow ? flow : null;
    }

    /// Runs `scrolled` each time the control's flow scrolls, from when its skin is made.
    public static void onScroll(Control control, Runnable scrolled) {
        if (control.getSkin() != null) {
            attach(control, scrolled);
        }
        control.skinProperty().addListener((obs, old, now) -> {
            if (now != null) {
                attach(control, scrolled);
            }
        });
    }

    private static void attach(Control control, Runnable scrolled) {
        VirtualFlow<?> flow = find(control);
        if (flow != null) {
            flow.positionProperty().addListener(o -> scrolled.run());
        }
    }

    /// The index of the first row on screen, or -1.
    public static int firstVisibleIndex(Control control) {
        VirtualFlow<?> flow = find(control);
        IndexedCell<?> cell = flow == null ? null : flow.getFirstVisibleCell();
        return cell == null ? -1 : cell.getIndex();
    }

    /// The index of the last row on screen, or -1.
    public static int lastVisibleIndex(Control control) {
        VirtualFlow<?> flow = find(control);
        IndexedCell<?> cell = flow == null ? null : flow.getLastVisibleCell();
        return cell == null ? -1 : cell.getIndex();
    }
}
