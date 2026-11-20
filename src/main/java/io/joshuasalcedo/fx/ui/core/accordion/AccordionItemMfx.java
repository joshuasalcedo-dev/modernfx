package io.joshuasalcedo.fx.ui.core.accordion;

import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import org.jspecify.annotations.NullMarked;

/// One item of [AccordionMfx]: a title and the content under it, in AtlantaFX's TitledPane look (its colors
/// follow the shadcn tokens).
@NullMarked
public class AccordionItemMfx extends TitledPane {

    /// The style class every AccordionItemMfx has.
    public static final String STYLE_CLASS = "mfx-accordion-item";

    public AccordionItemMfx(String title, Node content) {
        super(title, content);
        getStyleClass().add(STYLE_CLASS);
    }
}
