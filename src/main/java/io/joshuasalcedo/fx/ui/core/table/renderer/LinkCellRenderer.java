package io.joshuasalcedo.fx.ui.core.table.renderer;

import io.joshuasalcedo.fx.ui.core.link.LinkMfx;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.scene.Node;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A link: clicking runs `onClick` with the row (open a detail screen), or opens the row's `url` in the browser.
///
/// ```java
/// LinkCellRenderer.<Invoice, String>action(String::valueOf, this::openInvoice);
/// ```
@NullMarked
public class LinkCellRenderer<T, V> implements CellRenderer<T, V> {

    private final Function<V, String> label;
    private final @Nullable Consumer<T> onClick;
    private final @Nullable Function<T, @Nullable String> url;

    private LinkCellRenderer(Function<V, String> label, @Nullable Consumer<T> onClick, @Nullable Function<T, @Nullable String> url) {
        this.label = Objects.requireNonNull(label, "label");
        this.onClick = onClick;
        this.url = url;
    }

    /// Clicking runs `onClick` with the row.
    public static <T, V> LinkCellRenderer<T, V> action(Function<V, String> label, Consumer<T> onClick) {
        return new LinkCellRenderer<>(label, Objects.requireNonNull(onClick, "onClick"), null);
    }

    /// Clicking opens the row's url in the browser.
    public static <T, V> LinkCellRenderer<T, V> url(Function<V, String> label, Function<T, @Nullable String> url) {
        return new LinkCellRenderer<>(label, null, Objects.requireNonNull(url, "url"));
    }

    @Override
    public String text(V value) {
        return label.apply(value);
    }

    @Override
    public Node render(T row, V value) {
        LinkMfx link = new LinkMfx(text(value));
        if (url != null) {
            link.setUrl(url.apply(row));
        }
        Consumer<T> click = onClick;
        if (click != null) {
            link.setOnAction(e -> click.accept(row));
        }
        return link;
    }
}
