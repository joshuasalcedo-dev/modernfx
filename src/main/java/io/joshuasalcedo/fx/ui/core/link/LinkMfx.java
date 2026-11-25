package io.joshuasalcedo.fx.ui.core.link;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javafx.application.HostServices;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Hyperlink;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A text link. With a `url` set, clicking opens it in the browser; without one it runs `onAction`, like a button.
/// It doesn't keep the `:visited` look.
///
/// Links open with the app's HostServices when given ([#useHostServices], from your Application), otherwise with
/// the system's opener (`open`, `xdg-open`, or the Windows shell). Only `http`, `https` and `mailto` links open.
///
/// ```java
/// new LinkMfx("Terms of service", "https://example.com/terms");
/// ```
@NullMarked
public class LinkMfx extends Hyperlink {

    public static final String STYLE_CLASS = "mfx-link";

    private static final System.Logger LOG = System.getLogger(LinkMfx.class.getName());
    private static final Set<String> SCHEMES = Set.of("http", "https", "mailto");
    private static volatile @Nullable HostServices hostServices;

    private final ObjectProperty<@Nullable String> url = new SimpleObjectProperty<>(this, "url");

    public LinkMfx() {
        this("");
    }

    public LinkMfx(String text) {
        super(text);
        getStyleClass().add(STYLE_CLASS);
        addEventHandler(ActionEvent.ACTION, e -> {
            setVisited(false);
            String target = getUrl();
            if (target != null && !target.isBlank()) {
                open(target);
            }
        });
    }

    public LinkMfx(String text, String url) {
        this(text);
        setUrl(url);
    }

    public final ObjectProperty<@Nullable String> urlProperty() { return url; }
    public final @Nullable String getUrl() { return url.get(); }
    public final void setUrl(@Nullable String value) { url.set(value); }

    /// Opens links with these HostServices (`getHostServices()` in your Application).
    public static void useHostServices(@Nullable HostServices services) {
        hostServices = services;
    }

    /// Opens a link in the browser (or mail app). Returns false if it isn't an http, https or mailto link.
    public static boolean open(String link) {
        URI uri;
        try {
            uri = URI.create(link.strip());
        } catch (IllegalArgumentException e) {
            LOG.log(System.Logger.Level.WARNING, "not a link: " + link, e);
            return false;
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!SCHEMES.contains(scheme)) {
            LOG.log(System.Logger.Level.WARNING, "won't open " + link + ": only http, https and mailto links open");
            return false;
        }
        HostServices services = hostServices;
        if (services != null) {
            services.showDocument(uri.toString());
            return true;
        }
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        List<String> command = os.contains("win") ? List.of("rundll32", "url.dll,FileProtocolHandler", uri.toString())
                : os.contains("mac") ? List.of("open", uri.toString())
                : List.of("xdg-open", uri.toString());
        Thread.ofVirtual().start(() -> {
            try {
                new ProcessBuilder(command).redirectErrorStream(true).start();
            } catch (IOException e) {
                LOG.log(System.Logger.Level.WARNING, "couldn't open " + link, e);
            }
        });
        return true;
    }
}
