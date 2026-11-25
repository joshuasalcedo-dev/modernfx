package io.joshuasalcedo.fx.ui.core.toolbar;

import atlantafx.base.controls.CustomTextField;
import java.util.Objects;
import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.jspecify.annotations.NullMarked;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// A search box: a search icon, a clear button while there is text, and `search`, the text a moment after the user
/// stops typing (300 ms by default; Enter sets it at once, Esc clears). Bind a collection's filter to `search` so it
/// doesn't reload on every key.
///
/// ```java
/// var search = new SearchFieldMfx("Search contacts…");
/// list.filterProperty().bind(search.searchProperty());
/// ```
@NullMarked
public class SearchFieldMfx extends CustomTextField {

    public static final String STYLE_CLASS = "mfx-search-field";

    private final ReadOnlyStringWrapper search = new ReadOnlyStringWrapper(this, "search", "");
    private final PauseTransition pause = new PauseTransition(Duration.millis(300));
    private final ObjectProperty<Duration> debounce = new SimpleObjectProperty<>(this, "debounce", Duration.millis(300)) {
        @Override
        protected void invalidated() {
            pause.setDuration(get());
        }
    };

    public SearchFieldMfx() {
        this("Search…");
    }

    public SearchFieldMfx(String prompt) {
        getStyleClass().addAll("mfx-input", STYLE_CLASS);
        setPromptText(prompt);
        FontIcon icon = new FontIcon(Feather.SEARCH);
        icon.getStyleClass().add("mfx-search-icon");
        setLeft(icon);
        FontIcon clear = new FontIcon(Feather.X);
        clear.getStyleClass().add("mfx-search-clear");
        clear.setCursor(Cursor.HAND);
        clear.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            clear();
            commit();
            requestFocus();
        });
        clear.visibleProperty().bind(textProperty().isNotEmpty());
        setRight(clear);
        pause.setOnFinished(e -> commit());
        textProperty().addListener(o -> pause.playFromStart());
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                commit();
            } else if (e.getCode() == KeyCode.ESCAPE && !getText().isEmpty()) {
                clear();
                commit();
                e.consume();
            }
        });
    }

    /// The search text, trimmed, once the user has stopped typing.
    public final ReadOnlyStringProperty searchProperty() { return search.getReadOnlyProperty(); }
    public final String getSearch() { return search.get(); }

    /// How long after the last key `search` changes.
    public final ObjectProperty<Duration> debounceProperty() { return debounce; }
    public final void setDebounce(Duration value) { debounce.set(Objects.requireNonNull(value)); }

    /// Sets `search` to the text now.
    public void commit() {
        pause.stop();
        String text = getText();
        search.set(text == null ? "" : text.strip());
    }
}
