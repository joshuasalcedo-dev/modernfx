package io.joshuasalcedo.fx.ui.core.calendar;

import atlantafx.base.controls.Calendar;
import java.time.LocalDate;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// shadcn's Calendar: a month to pick a day from, always open (AtlantaFX's Calendar). The day picked is
/// [#valueProperty()].
@NullMarked
public class CalendarMfx extends Calendar {

    /// The style class every CalendarMfx has.
    public static final String STYLE_CLASS = "mfx-calendar";

    public CalendarMfx() {
        this(null);
    }

    public CalendarMfx(@Nullable LocalDate value) {
        super(value);
        getStyleClass().add(STYLE_CLASS);
    }
}
