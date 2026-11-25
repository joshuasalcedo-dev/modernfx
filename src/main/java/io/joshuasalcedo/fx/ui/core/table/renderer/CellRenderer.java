package io.joshuasalcedo.fx.ui.core.table.renderer;

import io.joshuasalcedo.fx.ui.core.table.ColumnAlignment;
import javafx.scene.Node;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Turns a column's value into what its cell shows. `text` is always there: the cell shows it (ending in an
/// ellipsis, with a tooltip when it doesn't fit) unless `render` gives a node (a badge, an avatar, a link). The
/// text is also what copying and searching use. A lambda is a text renderer:
///
/// ```java
/// CellRenderer<Invoice, Status> status = s -> s.label();
/// ```
@NullMarked
@FunctionalInterface
public interface CellRenderer<T, V> {

    /// The value as text.
    String text(V value);

    /// A node for the cell, or null to show the text. `row` is the row's item.
    default @Nullable Node render(T row, V value) {
        return null;
    }

    /// Where the value sits in the cell, unless the column says otherwise.
    default ColumnAlignment alignment() {
        return ColumnAlignment.LEFT;
    }
}
