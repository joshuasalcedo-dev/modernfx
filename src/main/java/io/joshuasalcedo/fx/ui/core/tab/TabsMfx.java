package io.joshuasalcedo.fx.ui.core.tab;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.jspecify.annotations.NullMarked;

/// shadcn's Tabs: a muted pill of tab names, the chosen one raised, and its content below (JavaFX's TabPane).
///
/// ```java
/// new TabsMfx().tab("Account", accountForm).tab("Password", passwordForm);
/// ```
@NullMarked
public class TabsMfx extends TabPane {

    /// The style class every TabsMfx has.
    public static final String STYLE_CLASS = "mfx-tabs";

    public TabsMfx(Tab... tabs) {
        super(tabs);
        getStyleClass().add(STYLE_CLASS);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
    }

    /// Adds a tab with this name and content.
    public TabsMfx tab(String name, Node content) {
        getTabs().add(new Tab(name, content));
        return this;
    }
}
