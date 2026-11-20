package io.joshuasalcedo.fx.utils.state;

import java.nio.file.Path;
import java.util.Locale;
import org.jspecify.annotations.NullMarked;

/// Where a desktop app keeps its own files on this computer.
@NullMarked
public final class AppDirs {

    private AppDirs() {
    }

    /// The app's data folder (not created here), e.g. `AppDirs.data("Acme", "notes")`:
    /// - Windows: `%APPDATA%\<vendor>\<app>`
    /// - macOS: `~/Library/Application Support/<vendor>/<app>`
    /// - Linux: `$XDG_DATA_HOME/<vendor>/<app>`, or `~/.local/share/<vendor>/<app>` (vendor in lower case)
    public static Path data(String vendor, String app) {
        check(vendor, "vendor");
        check(app, "app");
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        Path home = Path.of(System.getProperty("user.home"));
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            Path base = appData != null && !appData.isBlank() ? Path.of(appData) : home.resolve("AppData/Roaming");
            return base.resolve(vendor).resolve(app);
        }
        if (os.contains("mac")) {
            return home.resolve("Library/Application Support").resolve(vendor).resolve(app);
        }
        String xdg = System.getenv("XDG_DATA_HOME");
        Path base = xdg != null && !xdg.isBlank() ? Path.of(xdg) : home.resolve(".local/share");
        return base.resolve(vendor.toLowerCase(Locale.ROOT)).resolve(app);
    }

    private static void check(String name, String what) {
        if (name.isBlank() || name.contains("/") || name.contains("\\") || name.contains("..")) {
            throw new IllegalArgumentException("not a usable " + what + " name: '" + name + "'");
        }
    }
}
