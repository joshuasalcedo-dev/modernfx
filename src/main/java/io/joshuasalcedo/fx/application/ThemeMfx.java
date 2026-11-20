package io.joshuasalcedo.fx.application;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import java.util.List;
import java.util.Objects;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// The ModernFX look for a scene: AtlantaFX as the base for every control, shadcn's tokens and components on top,
/// and the Tailwind utilities, in light or dark.
///
/// ```java
/// Scene scene = new Scene(root, 960, 640);
/// ThemeMfx.install(scene);                     // follows the system's light/dark setting
/// ThemeMfx.setMode(scene, ThemeMfx.Mode.DARK);
/// ```
///
/// Dark mode puts the style class `dark` on the scene's root, as shadcn puts it on `<html>`: the tokens switch, and
/// the `dark:` utilities apply. Stylesheets you add to the scene after `install` come later, so they win.
@NullMarked
public final class ThemeMfx {

    /// Light, dark, or whatever the operating system is set to (and following it when it changes).
    public enum Mode {
        LIGHT, DARK, SYSTEM
    }

    /// The style class dark mode puts on the root.
    public static final String DARK = "dark";

    private static final String STATE = ThemeMfx.class.getName();
    private static final List<String> STYLESHEETS = List.of(
            stylesheet("tokens.css"), stylesheet("shadcn.css"), stylesheet("tailwind.css"));
    private static final String LIGHT_BASE = new PrimerLight().getUserAgentStylesheet();
    private static final String DARK_BASE = new PrimerDark().getUserAgentStylesheet();

    private ThemeMfx() {
    }

    /// Installs the look on the scene, following the system's light/dark setting.
    public static void install(Scene scene) {
        install(scene, Mode.SYSTEM);
    }

    /// Installs the look on the scene in the given mode.
    public static void install(Scene scene, Mode mode) {
        Objects.requireNonNull(scene, "scene");
        State state = state(scene);
        if (state == null) {
            state = new State(scene);
            scene.getProperties().put(STATE, state);
            List<String> sheets = scene.getStylesheets();
            sheets.removeAll(STYLESHEETS);
            sheets.addAll(0, STYLESHEETS);
        }
        state.mode = Objects.requireNonNull(mode, "mode");
        state.apply();
    }

    /// Switches the scene to light, dark or the system's setting.
    public static void setMode(Scene scene, Mode mode) {
        install(scene, mode);
    }

    /// The scene's mode (SYSTEM before `install`).
    public static Mode mode(Scene scene) {
        State state = state(scene);
        return state == null ? Mode.SYSTEM : state.mode;
    }

    /// True while the scene shows the dark theme.
    public static boolean isDark(Scene scene) {
        return darkProperty(scene).get();
    }

    /// Whether the scene shows the dark theme, to bind to (e.g. a sun/moon icon).
    public static ReadOnlyBooleanProperty darkProperty(Scene scene) {
        State state = state(scene);
        if (state == null) {
            throw new IllegalStateException("call ThemeMfx.install(scene) first");
        }
        return state.dark.getReadOnlyProperty();
    }

    /// Switches between light and dark (from SYSTEM, to the opposite of what shows now).
    public static void toggle(Scene scene) {
        setMode(scene, isDark(scene) ? Mode.LIGHT : Mode.DARK);
    }

    /// The URL of one of the library's stylesheets: `tokens.css`, `shadcn.css` or `tailwind.css`.
    public static String stylesheet(String name) {
        var url = ThemeMfx.class.getResource("/io/joshuasalcedo/fx/" + name);
        if (url == null) {
            throw new IllegalArgumentException("no stylesheet " + name);
        }
        return url.toExternalForm();
    }

    private static @Nullable State state(Scene scene) {
        return (State) scene.getProperties().get(STATE);
    }

    private static final class State {
        private final Scene scene;
        private final ReadOnlyBooleanWrapper dark = new ReadOnlyBooleanWrapper();
        private Mode mode = Mode.SYSTEM;
        // Kept here so the weak listener lives as long as the scene.
        private final ChangeListener<ColorScheme> systemChanged = (obs, old, now) -> apply();

        State(Scene scene) {
            this.scene = scene;
            Platform.getPreferences().colorSchemeProperty().addListener(new WeakChangeListener<>(systemChanged));
            scene.rootProperty().addListener((obs, old, now) -> {
                if (old != null) {
                    old.getStyleClass().remove(DARK);
                }
                apply();
            });
        }

        void apply() {
            boolean isDark = switch (mode) {
                case LIGHT -> false;
                case DARK -> true;
                case SYSTEM -> Platform.getPreferences().getColorScheme() == ColorScheme.DARK;
            };
            scene.setUserAgentStylesheet(isDark ? DARK_BASE : LIGHT_BASE);
            Parent root = scene.getRoot();
            if (root != null) {
                if (isDark && !root.getStyleClass().contains(DARK)) {
                    root.getStyleClass().add(DARK);
                } else if (!isDark) {
                    root.getStyleClass().remove(DARK);
                }
            }
            dark.set(isDark);
        }
    }
}
