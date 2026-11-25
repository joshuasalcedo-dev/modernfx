package io.joshuasalcedo.fx.ui.core.sheet;

import io.joshuasalcedo.fx.ui.core.table.ColumnAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/// How a sheet cell looks: its alignment (null: numbers right, the rest left), bold, a number format (a
/// DecimalFormat pattern such as `#,##0.00`) and a background color (a token such as `-muted`, or `#hex`).
public record CellStyle(@Nullable ColumnAlignment alignment, boolean bold, @Nullable String numberFormat,
                        @Nullable String background) {

    public static final CellStyle DEFAULT = new CellStyle(null, false, null, null);

    private static final Pattern COLOR = Pattern.compile("-[a-z][a-z0-9-]*|#[0-9a-fA-F]{3,8}");

    public CellStyle {
        if (background != null && !COLOR.matcher(background).matches()) {
            throw new IllegalArgumentException("background is a token (-muted) or #hex: " + background);
        }
    }

    public CellStyle withAlignment(@Nullable ColumnAlignment value) {
        return new CellStyle(value, bold, numberFormat, background);
    }

    public CellStyle withBold(boolean value) {
        return new CellStyle(alignment, value, numberFormat, background);
    }

    public CellStyle withNumberFormat(@Nullable String value) {
        return new CellStyle(alignment, bold, value, background);
    }

    public CellStyle withBackground(@Nullable String value) {
        return new CellStyle(alignment, bold, numberFormat, value);
    }

    /// The style classes for a cell with this style (`mfx-cell-bold`, `mfx-align-right`).
    public List<String> styleClasses() {
        List<String> out = new ArrayList<>();
        if (bold) {
            out.add("mfx-cell-bold");
        }
        if (alignment != null) {
            out.add(alignment.styleClass());
        }
        return out;
    }

    /// The inline style for the background, or "".
    public String inlineStyle() {
        return background == null ? "" : "-fx-background-color: " + background + ";";
    }
}
