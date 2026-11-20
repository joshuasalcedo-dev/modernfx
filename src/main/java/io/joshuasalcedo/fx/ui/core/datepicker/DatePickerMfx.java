package io.joshuasalcedo.fx.ui.core.datepicker;

import java.time.LocalDate;
import javafx.scene.control.DatePicker;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// shadcn's Date Picker: a field that opens a calendar (JavaFX's DatePicker, with the input's look and the
/// calendar's look in its popup).
@NullMarked
public class DatePickerMfx extends DatePicker {

    /// The style class every DatePickerMfx has.
    public static final String STYLE_CLASS = "mfx-date-picker";

    public DatePickerMfx() {
        this(null);
    }

    public DatePickerMfx(@Nullable LocalDate value) {
        super(value);
        getStyleClass().add(STYLE_CLASS);
        setPromptText("Pick a date");
    }
}
