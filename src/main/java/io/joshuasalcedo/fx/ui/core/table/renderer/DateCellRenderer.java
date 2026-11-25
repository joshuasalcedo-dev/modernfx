package io.joshuasalcedo.fx.ui.core.table.renderer;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Objects;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A date or time: absolute in the locale's format (`Nov 25, 2026, 2:33 PM`), or relative (`2h ago`, `yesterday`,
/// `in 3d`) with the absolute one as a tooltip. Relative dates older than a week show the date. Takes Instant,
/// LocalDate, LocalDateTime, ZonedDateTime and OffsetDateTime.
///
/// ```java
/// DateCellRenderer.<Message>relative();
/// ```
@NullMarked
public class DateCellRenderer<T> implements CellRenderer<T, Temporal> {

    private final boolean relative;
    private final DateTimeFormatter dateTime;
    private final DateTimeFormatter date;
    private final Clock clock;

    private DateCellRenderer(boolean relative, DateTimeFormatter dateTime, DateTimeFormatter date, Clock clock) {
        this.relative = relative;
        this.dateTime = dateTime;
        this.date = date;
        this.clock = clock;
    }

    /// Dates and times in the locale's medium format.
    public static <T> DateCellRenderer<T> absolute() {
        return absolute(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT));
    }

    /// Dates and times in this format (plain dates in the locale's medium date format).
    public static <T> DateCellRenderer<T> absolute(DateTimeFormatter format) {
        return new DateCellRenderer<>(false, format, DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM),
                Clock.systemDefaultZone());
    }

    /// `2h ago` from now, on the computer's clock.
    public static <T> DateCellRenderer<T> relative() {
        return relative(Clock.systemDefaultZone());
    }

    /// `2h ago` from this clock's now (a fixed clock in tests).
    public static <T> DateCellRenderer<T> relative(Clock clock) {
        return new DateCellRenderer<>(true, DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT),
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM), Objects.requireNonNull(clock, "clock"));
    }

    @Override
    public String text(Temporal value) {
        return relative ? relativeText(value, ZonedDateTime.now(clock)) : absoluteText(value);
    }

    @Override
    public @Nullable Node render(T row, Temporal value) {
        if (!relative) {
            return null;
        }
        Label label = new Label(text(value));
        label.setTooltip(new Tooltip(absoluteText(value)));
        return label;
    }

    /// The value in the absolute format.
    public String absoluteText(Temporal value) {
        return value instanceof LocalDate d ? date.format(d) : dateTime.format(zoned(value, clock.getZone()));
    }

    /// `just now`, `5m ago`, `2h ago`, `yesterday`, `3d ago`, `in 2h`... from `now`; the date past a week.
    public String relativeText(Temporal value, ZonedDateTime now) {
        if (value instanceof LocalDate d) {
            long days = ChronoUnit.DAYS.between(now.toLocalDate(), d);
            if (days == 0) {
                return "today";
            }
            if (days == -1) {
                return "yesterday";
            }
            if (days == 1) {
                return "tomorrow";
            }
            return Math.abs(days) < 7 ? (days < 0 ? -days + "d ago" : "in " + days + "d") : date.format(d);
        }
        ZonedDateTime then = zoned(value, now.getZone());
        Duration between = Duration.between(then, now);
        boolean past = !between.isNegative();
        Duration d = between.abs();
        String amount;
        if (d.toSeconds() < 45) {
            return "just now";
        } else if (d.toMinutes() < 60) {
            amount = Math.max(1, d.toMinutes()) + "m";
        } else if (d.toHours() < 24) {
            amount = d.toHours() + "h";
        } else if (past && ChronoUnit.DAYS.between(then.toLocalDate(), now.toLocalDate()) == 1) {
            return "yesterday";
        } else if (d.toDays() < 7) {
            amount = d.toDays() + "d";
        } else {
            return date.format(then.toLocalDate());
        }
        return past ? amount + " ago" : "in " + amount;
    }

    private static ZonedDateTime zoned(Temporal value, ZoneId zone) {
        return switch (value) {
            case ZonedDateTime z -> z;
            case OffsetDateTime o -> o.atZoneSameInstant(zone);
            case Instant i -> i.atZone(zone);
            case LocalDateTime l -> l.atZone(zone);
            case LocalDate d -> d.atStartOfDay(zone);
            default -> throw new IllegalArgumentException("not a date or time: " + value.getClass().getName());
        };
    }
}
