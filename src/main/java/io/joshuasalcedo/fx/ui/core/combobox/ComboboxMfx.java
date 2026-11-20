package io.joshuasalcedo.fx.ui.core.combobox;

import java.util.Collection;
import org.controlsfx.control.SearchableComboBox;
import org.jspecify.annotations.NullMarked;

/// shadcn's Combobox: a select you can type into to filter the options (ControlsFX's SearchableComboBox).
///
/// ```java
/// var framework = new ComboboxMfx<>("Select framework...", List.of("Next.js", "SvelteKit", "Nuxt.js", "Remix"));
/// ```
@NullMarked
public class ComboboxMfx<T> extends SearchableComboBox<T> {

    /// The style class every ComboboxMfx has.
    public static final String STYLE_CLASS = "mfx-combobox";

    public ComboboxMfx() {
        getStyleClass().add(STYLE_CLASS);
    }

    public ComboboxMfx(String placeholder, Collection<T> items) {
        this();
        setPromptText(placeholder);
        getItems().setAll(items);
    }
}
