package io.joshuasalcedo.fx.ui.core.table.renderer;

import io.joshuasalcedo.fx.ui.core.table.ColumnAlignment;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

/// An amount of money in one currency, in the locale's format (`$1,240.00`, `1.240,00 €`), right-aligned.
/// Pass amounts as BigDecimal to keep the cents exact.
///
/// ```java
/// new MoneyCellRenderer<Invoice>(Currency.getInstance("USD"));
/// ```
@NullMarked
public class MoneyCellRenderer<T> implements CellRenderer<T, Number> {

    private final NumberFormat format;

    /// The currency in the default locale's format.
    public MoneyCellRenderer(Currency currency) {
        this(currency, Locale.getDefault(Locale.Category.FORMAT));
    }

    public MoneyCellRenderer(Currency currency, Locale locale) {
        format = NumberFormat.getCurrencyInstance(Objects.requireNonNull(locale, "locale"));
        format.setCurrency(Objects.requireNonNull(currency, "currency"));
        format.setMinimumFractionDigits(currency.getDefaultFractionDigits());
        format.setMaximumFractionDigits(currency.getDefaultFractionDigits());
    }

    @Override
    public String text(Number value) {
        synchronized (format) {
            return format.format(value);
        }
    }

    @Override
    public ColumnAlignment alignment() {
        return ColumnAlignment.RIGHT;
    }
}
