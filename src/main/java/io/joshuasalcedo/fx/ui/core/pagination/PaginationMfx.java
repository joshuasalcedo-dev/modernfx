package io.joshuasalcedo.fx.ui.core.pagination;

import java.util.function.IntFunction;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import org.jspecify.annotations.NullMarked;

/// shadcn's Pagination: previous and next buttons and the page numbers, the current one outlined (JavaFX's
/// Pagination). The page content comes from the factory; leave it out to show only the buttons.
///
/// ```java
/// var pages = new PaginationMfx(12, page -> loadPage(page));
/// ```
@NullMarked
public class PaginationMfx extends Pagination {

    /// The style class every PaginationMfx has.
    public static final String STYLE_CLASS = "mfx-pagination";

    /// Only the page buttons (no content).
    public PaginationMfx(int pages) {
        this(pages, page -> new javafx.scene.layout.Region());
    }

    public PaginationMfx(int pages, IntFunction<Node> page) {
        super(pages, 0);
        getStyleClass().add(STYLE_CLASS);
        setMaxPageIndicatorCount(5);
        setPageFactory(page::apply);
    }

    /// NUMBERED shows up to five page numbers; SIMPLE only the current one between previous and next.
    /// LOAD_MORE isn't for pages: it's ListMfx's "Load more" button.
    public PaginationMfx style(PaginationStyle style) {
        switch (style) {
            case NUMBERED -> setMaxPageIndicatorCount(5);
            case SIMPLE -> setMaxPageIndicatorCount(1);
            case LOAD_MORE -> throw new IllegalArgumentException("LOAD_MORE is ListMfx's; PaginationMfx is NUMBERED or SIMPLE");
        }
        return this;
    }
}
