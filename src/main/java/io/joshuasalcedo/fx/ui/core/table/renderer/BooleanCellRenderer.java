package io.joshuasalcedo.fx.ui.core.table.renderer;

import io.joshuasalcedo.fx.ui.core.table.ColumnAlignment;
import io.joshuasalcedo.fx.ui.core.toggle.SwitchMfx;
import java.util.function.BiConsumer;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// True or false: a check for true and nothing for false, or a switch. A switch with `onChange` can be flipped in
/// the table (the app saves the new value); without it, it only shows the value.
///
/// ```java
/// BooleanCellRenderer.<User>toggle((user, on) -> api.setActive(user.id(), on));
/// ```
@NullMarked
public class BooleanCellRenderer<T> implements CellRenderer<T, Boolean> {

    private final boolean asSwitch;
    private final @Nullable BiConsumer<T, Boolean> onChange;

    private BooleanCellRenderer(boolean asSwitch, @Nullable BiConsumer<T, Boolean> onChange) {
        this.asSwitch = asSwitch;
        this.onChange = onChange;
    }

    /// A check icon when true.
    public static <T> BooleanCellRenderer<T> check() {
        return new BooleanCellRenderer<>(false, null);
    }

    /// A switch that only shows the value.
    public static <T> BooleanCellRenderer<T> readOnlySwitch() {
        return new BooleanCellRenderer<>(true, null);
    }

    /// A switch the user can flip; `onChange` gets the row and the new value.
    public static <T> BooleanCellRenderer<T> toggle(BiConsumer<T, Boolean> onChange) {
        return new BooleanCellRenderer<>(true, onChange);
    }

    @Override
    public String text(Boolean value) {
        return value ? "Yes" : "No";
    }

    @Override
    public Node render(T row, Boolean value) {
        if (!asSwitch) {
            if (!value) {
                return new Region();
            }
            FontIcon check = new FontIcon(Feather.CHECK);
            check.getStyleClass().add("mfx-cell-check");
            return check;
        }
        SwitchMfx toggle = new SwitchMfx();
        toggle.size(SwitchMfx.Size.SM);
        toggle.setSelected(value);
        toggle.setAccessibleText(text(value));
        BiConsumer<T, Boolean> change = onChange;
        if (change == null) {
            toggle.setMouseTransparent(true);
            toggle.setFocusTraversable(false);
        } else {
            toggle.selectedProperty().addListener((obs, old, on) -> change.accept(row, on));
        }
        return toggle;
    }

    @Override
    public ColumnAlignment alignment() {
        return ColumnAlignment.CENTER;
    }
}
