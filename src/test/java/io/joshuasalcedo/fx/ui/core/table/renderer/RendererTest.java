package io.joshuasalcedo.fx.ui.core.table.renderer;

import static org.assertj.core.api.Assertions.assertThat;

import io.joshuasalcedo.fx.ui.core.gridview.GridSizing;
import io.joshuasalcedo.fx.ui.core.table.ColumnAlignment;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class RendererTest {

    private final ZoneId zone = ZoneId.of("Asia/Manila");
    private final ZonedDateTime now = ZonedDateTime.of(2026, 11, 25, 14, 0, 0, 0, zone);
    private final DateCellRenderer<Object> relative = DateCellRenderer.relative(Clock.fixed(now.toInstant(), zone));

    @Test
    void relativeDates() {
        assertThat(relative.relativeText(now.minusSeconds(10), now)).isEqualTo("just now");
        assertThat(relative.relativeText(now.minusMinutes(5), now)).isEqualTo("5m ago");
        assertThat(relative.relativeText(now.minusHours(2).toInstant(), now)).isEqualTo("2h ago");
        assertThat(relative.relativeText(now.minusDays(1).minusHours(1), now)).isEqualTo("yesterday");
        assertThat(relative.relativeText(now.minusDays(3), now)).isEqualTo("3d ago");
        assertThat(relative.relativeText(now.plusHours(2), now)).isEqualTo("in 2h");
        assertThat(relative.relativeText(LocalDate.of(2026, 11, 25), now)).isEqualTo("today");
        assertThat(relative.relativeText(LocalDate.of(2026, 11, 24), now)).isEqualTo("yesterday");
        assertThat(relative.relativeText(now.minusDays(30), now)).doesNotContain("ago");
        assertThat(relative.text(Instant.from(now.minusMinutes(1)))).isEqualTo("1m ago");
    }

    @Test
    void moneyAndNumbersAreFormattedAndRightAligned() {
        var usd = new MoneyCellRenderer<Object>(Currency.getInstance("USD"), Locale.US);
        assertThat(usd.text(new BigDecimal("1240.5"))).isEqualTo("$1,240.50");
        assertThat(usd.alignment()).isEqualTo(ColumnAlignment.RIGHT);
        var yen = new MoneyCellRenderer<Object>(Currency.getInstance("JPY"), Locale.JAPAN);
        assertThat(yen.text(4990)).contains("4,990").doesNotContain(".");
        assertThat(new NumberCellRenderer<Object>(java.text.NumberFormat.getNumberInstance(Locale.US)).text(1234.5))
                .isEqualTo("1,234.5");
    }

    @Test
    void initials() {
        assertThat(AvatarCellRenderer.initials("Ana Ruiz")).isEqualTo("AR");
        assertThat(AvatarCellRenderer.initials("ana")).isEqualTo("A");
        assertThat(AvatarCellRenderer.initials("Mary Ann de la Cruz")).isEqualTo("MA");
        assertThat(AvatarCellRenderer.initials("  ")).isEmpty();
    }

    @Test
    void gridSizingFillsTheRow() {
        var s = new GridSizing(160, 1, 12);
        assertThat(s.columns(1000)).isEqualTo(5);
        assertThat(s.columns(100)).isEqualTo(1);
        assertThat(s.tileWidth(1000)).isEqualTo(190);
        assertThat(s.tileHeight(190)).isEqualTo(190);
    }
}
