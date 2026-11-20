package io.joshuasalcedo.fx.ui.core.accordion;

import javafx.scene.Node;
import javafx.scene.control.Accordion;
import org.jspecify.annotations.NullMarked;

/// shadcn's Accordion: questions that open one at a time, in AtlantaFX's Accordion look.
///
/// ```java
/// new AccordionMfx()
///         .item("Is it accessible?", new Label("Yes. It follows JavaFX's keyboard handling."))
///         .item("Is it styled?", new Label("Yes. It uses the shadcn tokens."));
/// ```
@NullMarked
public class AccordionMfx extends Accordion {

    /// The style class every AccordionMfx has.
    public static final String STYLE_CLASS = "mfx-accordion";

    public AccordionMfx(AccordionItemMfx... items) {
        super(items);
        getStyleClass().add(STYLE_CLASS);
    }

    /// Adds an item with this title and content.
    public AccordionMfx item(String title, Node content) {
        getPanes().add(new AccordionItemMfx(title, content));
        return this;
    }
}
