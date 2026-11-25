package io.joshuasalcedo.fx.ui.core.pagination;

import io.joshuasalcedo.fx.ui.core.select.SelectMfx;
import java.util.List;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NullMarked;

/// "Rows per page" and a select of page sizes (10, 25, 50, 100 by default). Bind `pageSize` to the collection's.
///
/// ```java
/// var size = new PageSizeSelectMfx();
/// list.pageSizeProperty().bindBidirectional(size.pageSizeProperty());
/// ```
@NullMarked
public class PageSizeSelectMfx extends HBox {

    public static final String STYLE_CLASS = "mfx-page-size-select";
    public static final List<Integer> DEFAULT_SIZES = List.of(10, 25, 50, 100);

    private final SelectMfx<Integer> select = new SelectMfx<>();
    private final IntegerProperty pageSize = new SimpleIntegerProperty(this, "pageSize", 25);

    public PageSizeSelectMfx() {
        this(DEFAULT_SIZES, 25);
    }

    public PageSizeSelectMfx(List<Integer> sizes, int initial) {
        getStyleClass().add(STYLE_CLASS);
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(8);
        Label label = new Label("Rows per page");
        label.getStyleClass().add("mfx-page-size-label");
        select.getItems().setAll(sizes);
        select.setPrefWidth(80);
        pageSize.set(initial);
        select.setValue(initial);
        select.valueProperty().addListener((obs, old, now) -> {
            if (now != null) {
                pageSize.set(now);
            }
        });
        pageSize.addListener((obs, old, now) -> {
            if (!select.getItems().contains(now.intValue())) {
                select.getItems().add(now.intValue());
                select.getItems().sort(null);
            }
            select.setValue(now.intValue());
        });
        getChildren().addAll(label, select);
    }

    /// The chosen page size.
    public final IntegerProperty pageSizeProperty() { return pageSize; }
    public final int getPageSize() { return pageSize.get(); }
    public final void setPageSize(int value) { pageSize.set(value); }

    /// The sizes to pick from.
    public final List<Integer> getSizes() {
        return select.getItems();
    }
}
