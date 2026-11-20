package io.joshuasalcedo.fx.ui.core.link;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Hyperlink;
import org.jspecify.annotations.Nullable;

/// A text link. With a `url` set, clicking opens it in the browser; without one it runs `onAction`, like a button.
public class LinkMfx extends Hyperlink {

    public static final String STYLE_CLASS = "mfx-link";

    private final ObjectProperty<@Nullable String> url = new SimpleObjectProperty<>(this, "url");

    public LinkMfx() {
        this("");
    }

    public LinkMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
        // TODO: on action, open the url (HostServices or Desktop.browse) when set; don't keep :visited
    }

    public LinkMfx(String text, String url) {
        this(text);
        setUrl(url);
    }

    public final ObjectProperty<@Nullable String> urlProperty() { return url; }
    public final @Nullable String getUrl() { return url.get(); }
    public final void setUrl(@Nullable String value) { url.set(value); }
}
