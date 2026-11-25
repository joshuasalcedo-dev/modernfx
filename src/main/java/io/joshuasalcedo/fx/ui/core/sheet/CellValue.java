package io.joshuasalcedo.fx.ui.core.sheet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A typed sheet value, with the text it was typed (or pasted) as. [#parse] reads what a user types the way a
/// spreadsheet does: `12.5` is a NUMBER, `$1,200` MONEY, `15%` PERCENT (0.15), `2026-11-25` a DATE, `true` a
/// BOOLEAN, nothing EMPTY, and anything else TEXT.
///
/// The value is a BigDecimal for NUMBER, MONEY and PERCENT, a LocalDate for DATE, a Boolean for BOOLEAN, a String
/// for TEXT, and null for EMPTY.
@NullMarked
public final class CellValue {

    private static final CellValue EMPTY = new CellValue(CellType.EMPTY, null, "");
    private static final Pattern NUMBER = Pattern.compile("[-+]?(\\d{1,3}(,\\d{3})+|\\d+)?(\\.\\d+)?([eE][-+]?\\d+)?");
    private static final String CURRENCY_SYMBOLS = "$€£¥₱₹";

    private final CellType type;
    private final @Nullable Object value;
    private final String raw;

    private CellValue(CellType type, @Nullable Object value, String raw) {
        this.type = type;
        this.value = value;
        this.raw = raw;
    }

    public static CellValue empty() {
        return EMPTY;
    }

    public static CellValue text(String text) {
        return text.isEmpty() ? EMPTY : new CellValue(CellType.TEXT, text, text);
    }

    public static CellValue number(BigDecimal number) {
        return new CellValue(CellType.NUMBER, number, number.toPlainString());
    }

    public static CellValue number(double number) {
        return number(BigDecimal.valueOf(number));
    }

    public static CellValue money(BigDecimal amount) {
        return new CellValue(CellType.MONEY, amount, amount.toPlainString());
    }

    /// A percentage: 0.15 is 15%.
    public static CellValue percent(BigDecimal fraction) {
        return new CellValue(CellType.PERCENT, fraction, fraction.movePointRight(2).stripTrailingZeros().toPlainString() + "%");
    }

    public static CellValue date(LocalDate date) {
        return new CellValue(CellType.DATE, date, date.toString());
    }

    public static CellValue bool(boolean value) {
        return new CellValue(CellType.BOOLEAN, value, value ? "TRUE" : "FALSE");
    }

    /// The value of something typed or pasted.
    public static CellValue parse(@Nullable String input) {
        if (input == null) {
            return EMPTY;
        }
        String s = input.strip();
        if (s.isEmpty()) {
            return EMPTY;
        }
        String lower = s.toLowerCase(Locale.ROOT);
        if (lower.equals("true") || lower.equals("false")) {
            return new CellValue(CellType.BOOLEAN, lower.equals("true"), s);
        }
        if (s.endsWith("%")) {
            BigDecimal n = number(s.substring(0, s.length() - 1).strip());
            if (n != null) {
                return new CellValue(CellType.PERCENT, n.movePointLeft(2), s);
            }
        }
        String unsigned = s.startsWith("-") || s.startsWith("+") ? s.substring(1) : s;
        if (!unsigned.isEmpty() && CURRENCY_SYMBOLS.indexOf(unsigned.charAt(0)) >= 0) {
            BigDecimal n = number(unsigned.substring(1).strip());
            if (n != null) {
                return new CellValue(CellType.MONEY, s.startsWith("-") ? n.negate() : n, s);
            }
        }
        BigDecimal n = number(s);
        if (n != null) {
            return new CellValue(CellType.NUMBER, n, s);
        }
        if (s.length() == 10 && s.charAt(4) == '-' && s.charAt(7) == '-') {
            try {
                return new CellValue(CellType.DATE, LocalDate.parse(s), s);
            } catch (DateTimeParseException ignored) {
                // not a date: text
            }
        }
        return new CellValue(CellType.TEXT, s, s);
    }

    private static @Nullable BigDecimal number(String s) {
        if (s.isEmpty() || !NUMBER.matcher(s).matches() || s.equals("-") || s.equals("+") || s.equals(".")) {
            return null;
        }
        try {
            return new BigDecimal(s.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public CellType type() {
        return type;
    }

    /// The typed value (see the class description), or null when EMPTY.
    public @Nullable Object value() {
        return value;
    }

    /// The text it was typed as.
    public String raw() {
        return raw;
    }

    public boolean isEmpty() {
        return type == CellType.EMPTY;
    }

    /// The value as a number, or null if it isn't one.
    public @Nullable BigDecimal asNumber() {
        return value instanceof BigDecimal d ? d : null;
    }

    /// The value as text: what copying puts on the clipboard.
    public String asText() {
        return switch (type) {
            case EMPTY -> "";
            case BOOLEAN -> Boolean.TRUE.equals(value) ? "TRUE" : "FALSE";
            case NUMBER -> Objects.requireNonNull(asNumber()).stripTrailingZeros().toPlainString();
            default -> raw;
        };
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CellValue other && type == other.type && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return type + "(" + raw + ")";
    }
}
