package io.joshuasalcedo.fx.ui.core.table.renderer;

import java.util.Objects;
import java.util.function.Function;
import org.jspecify.annotations.NullMarked;

/// Plain text, ending in an ellipsis when it doesn't fit (with the whole text in a tooltip).
///
/// ```java
/// new TextCellRenderer<Contact, Email>(Email::address);
/// ```
@NullMarked
public class TextCellRenderer<T, V> implements CellRenderer<T, V> {

    private final Function<V, String> format;

    /// The value's `toString()`.
    public TextCellRenderer() {
        this(String::valueOf);
    }

    public TextCellRenderer(Function<V, String> format) {
        this.format = Objects.requireNonNull(format, "format");
    }

    @Override
    public String text(V value) {
        return format.apply(value);
    }
}
