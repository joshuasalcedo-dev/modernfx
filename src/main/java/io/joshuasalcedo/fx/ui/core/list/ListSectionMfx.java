package io.joshuasalcedo.fx.ui.core.list;

import javafx.scene.control.Label;
import org.jspecify.annotations.NullMarked;

/// A section header in a list: a letter, a date, a group's name. ListMfx puts one above the first item of each
/// section and keeps the current section's header stuck to the top while scrolling.
@NullMarked
public class ListSectionMfx extends Label {

    public static final String STYLE_CLASS = "mfx-list-section";

    public ListSectionMfx() {
        this("");
    }

    public ListSectionMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
        setMaxWidth(Double.MAX_VALUE);
    }
}
