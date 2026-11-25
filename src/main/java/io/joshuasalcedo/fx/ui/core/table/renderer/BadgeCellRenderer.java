package io.joshuasalcedo.fx.ui.core.table.renderer;

import io.joshuasalcedo.fx.ui.core.badge.BadgeMfx;
import java.util.Objects;
import java.util.function.Function;
import javafx.scene.Node;
import org.jspecify.annotations.NullMarked;

/// A status as a badge: the label and the badge's variant come from the value.
///
/// ```java
/// new BadgeCellRenderer<Invoice, Status>(Status::label,
///         s -> s == Status.OVERDUE ? BadgeMfx.Variant.DESTRUCTIVE : BadgeMfx.Variant.OUTLINE);
/// ```
@NullMarked
public class BadgeCellRenderer<T, V> implements CellRenderer<T, V> {

    private final Function<V, String> label;
    private final Function<V, BadgeMfx.Variant> variant;

    /// Outline badges with the value's `toString()`.
    public BadgeCellRenderer() {
        this(String::valueOf);
    }

    /// Outline badges.
    public BadgeCellRenderer(Function<V, String> label) {
        this(label, v -> BadgeMfx.Variant.OUTLINE);
    }

    public BadgeCellRenderer(Function<V, String> label, Function<V, BadgeMfx.Variant> variant) {
        this.label = Objects.requireNonNull(label, "label");
        this.variant = Objects.requireNonNull(variant, "variant");
    }

    @Override
    public String text(V value) {
        return label.apply(value);
    }

    @Override
    public Node render(T row, V value) {
        return new BadgeMfx(text(value)).variant(variant.apply(value));
    }
}
