package io.joshuasalcedo.fx.ui.core.pagination;

import java.text.NumberFormat;
import java.util.Locale;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.control.Label;
import org.jspecify.annotations.NullMarked;

/// "Showing 1–25 of 1,240": where the current page is in all the items. Set `offset` (the index of the first
/// item shown), `shown` (how many are on the page) and `total` (−1 when unknown: "Showing 1–25").
@NullMarked
public class PageSummaryMfx extends Label {

    public static final String STYLE_CLASS = "mfx-page-summary";

    private final IntegerProperty offset = new SimpleIntegerProperty(this, "offset");
    private final IntegerProperty shown = new SimpleIntegerProperty(this, "shown");
    private final LongProperty total = new SimpleLongProperty(this, "total");
    private final NumberFormat numbers;

    public PageSummaryMfx() {
        this(Locale.getDefault(Locale.Category.FORMAT));
    }

    public PageSummaryMfx(Locale locale) {
        getStyleClass().add(STYLE_CLASS);
        numbers = NumberFormat.getIntegerInstance(locale);
        offset.addListener(o -> update());
        shown.addListener(o -> update());
        total.addListener(o -> update());
        update();
    }

    /// Sets all three at once.
    public void set(int offset, int shown, long total) {
        this.offset.set(offset);
        this.shown.set(shown);
        this.total.set(total);
    }

    public final IntegerProperty offsetProperty() { return offset; }
    public final IntegerProperty shownProperty() { return shown; }
    public final LongProperty totalProperty() { return total; }

    /// The text for these numbers.
    public String format(int offset, int shown, long total) {
        if (shown <= 0) {
            return total == 0 ? "No results" : "";
        }
        String range = numbers.format(offset + 1L) + "–" + numbers.format((long) offset + shown);
        return total < 0 ? "Showing " + range : "Showing " + range + " of " + numbers.format(total);
    }

    private void update() {
        setText(format(offset.get(), shown.get(), total.get()));
    }
}
