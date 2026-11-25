package io.joshuasalcedo.fx.ui.core.toolbar;

import io.joshuasalcedo.fx.ui.core.data.ViewMode;
import io.joshuasalcedo.fx.ui.core.segmented.SegmentedControlMfx;
import java.util.List;
import javafx.scene.Node;
import javafx.util.StringConverter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2MZ;

/// The switch between a collection's views: icons for List, Grid, Table (and Board), the name as a tooltip.
/// Its value is the ViewMode.
///
/// ```java
/// var views = new ViewModeToggleMfx(ViewMode.LIST, ViewMode.GRID);
/// views.valueProperty().subscribe(mode -> content.show(mode));
/// ```
@NullMarked
public class ViewModeToggleMfx extends SegmentedControlMfx<ViewMode> {

    /// List, Grid and Table.
    public ViewModeToggleMfx() {
        this(ViewMode.LIST, ViewMode.GRID, ViewMode.TABLE);
    }

    public ViewModeToggleMfx(ViewMode... modes) {
        super(List.of(modes));
        getStyleClass().add("mfx-view-mode-toggle");
        setConverter(new StringConverter<>() {
            @Override
            public String toString(@Nullable ViewMode mode) {
                return mode == null ? "" : mode.label();
            }

            @Override
            public ViewMode fromString(String s) {
                return ViewMode.valueOf(s.toUpperCase(java.util.Locale.ROOT));
            }
        });
        setGraphicFactory(ViewModeToggleMfx::icon);
        setShowLabels(false);
    }

    /// The icon of a view mode.
    public static Node icon(ViewMode mode) {
        Ikon ikon = switch (mode) {
            case LIST -> Feather.LIST;
            case GRID -> Feather.GRID;
            case TABLE -> Material2MZ.TABLE_ROWS;
            case BOARD -> Feather.COLUMNS;
        };
        return new FontIcon(ikon);
    }
}
