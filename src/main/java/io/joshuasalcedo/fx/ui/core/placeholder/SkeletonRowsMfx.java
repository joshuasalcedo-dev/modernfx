package io.joshuasalcedo.fx.ui.core.placeholder;

import io.joshuasalcedo.fx.ui.core.data.Density;
import io.joshuasalcedo.fx.ui.core.skeleton.SkeletonMfx;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NullMarked;

/// Pulsing placeholder rows while a list or table loads: each row a round avatar (optional) and two bars, as tall
/// as a row of the current Density.
///
/// ```java
/// new SkeletonRowsMfx(6);
/// ```
@NullMarked
public class SkeletonRowsMfx extends VBox {

    public static final String STYLE_CLASS = "mfx-skeleton-rows";

    private final IntegerProperty rows = new SimpleIntegerProperty(this, "rows", 5) {
        @Override
        protected void invalidated() {
            rebuild();
        }
    };
    private final ObjectProperty<Density> density = new SimpleObjectProperty<>(this, "density", Density.COMFORTABLE) {
        @Override
        protected void invalidated() {
            rebuild();
        }
    };
    private final BooleanProperty avatar = new SimpleBooleanProperty(this, "avatar", true) {
        @Override
        protected void invalidated() {
            rebuild();
        }
    };

    public SkeletonRowsMfx() {
        this(5);
    }

    public SkeletonRowsMfx(int rows) {
        getStyleClass().add(STYLE_CLASS);
        setRows(rows);
        rebuild();
    }

    public final IntegerProperty rowsProperty() { return rows; }
    public final int getRows() { return rows.get(); }
    public final void setRows(int value) { rows.set(Math.max(0, value)); }

    public final ObjectProperty<Density> densityProperty() { return density; }
    public final Density getDensity() { return density.get(); }
    public final void setDensity(Density value) { density.set(value); }

    /// Whether each row starts with a round avatar.
    public final BooleanProperty avatarProperty() { return avatar; }
    public final void setAvatar(boolean value) { avatar.set(value); }

    private void rebuild() {
        Density d = getDensity();
        double height = d.rowHeight();
        getChildren().clear();
        for (int i = 0; i < getRows(); i++) {
            HBox row = new HBox(12);
            row.getStyleClass().add("mfx-skeleton-row");
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(d.padding(), 12, d.padding(), 12));
            row.setMinHeight(height + 2 * d.padding());
            if (avatar.get()) {
                double size = Math.max(16, height - 8);
                SkeletonMfx circle = new SkeletonMfx();
                circle.getStyleClass().add("mfx-skeleton-circle");
                circle.setMinSize(size, size);
                circle.setMaxSize(size, size);
                row.getChildren().add(circle);
            }
            // bars of different widths, so it reads as text and not as a pattern
            double width = 0.9 - (i % 3) * 0.15;
            SkeletonMfx title = bar(12);
            SkeletonMfx subtitle = bar(10);
            title.maxWidthProperty().bind(row.widthProperty().multiply(width * 0.5));
            subtitle.maxWidthProperty().bind(row.widthProperty().multiply(width * 0.8));
            VBox lines = new VBox(6, title, subtitle);
            lines.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(lines, Priority.ALWAYS);
            row.getChildren().add(lines);
            getChildren().add(row);
        }
    }

    private static SkeletonMfx bar(double height) {
        SkeletonMfx bar = new SkeletonMfx();
        bar.setMinHeight(height);
        bar.setPrefHeight(height);
        bar.setMaxHeight(height);
        bar.setPrefWidth(10_000);
        return bar;
    }
}
