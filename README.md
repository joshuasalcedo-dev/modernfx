# ModernFX

shadcn-style components, Tailwind classes and app state for JavaFX.

- **The shadcn look**: shadcn/ui's tokens (`-background`, `-primary`, `-border`, `-ring`...) as JavaFX looked-up colors,
  light and dark, with AtlantaFX underneath so every standard control follows them.
- **Tailwind classes on JavaFX nodes**: `Tw.set(box, "px-4 py-2 gap-2 items-center bg-card hover:bg-accent rounded-lg")`.
  The Tailwind 4 palette and the utilities JavaFX can do are generated from Tailwind's own theme.
- **Components** in `io.joshuasalcedo.fx.ui.core`, one package per component, like shadcn's `components/ui`.
- **App state**: a Redux-style `Store`, and a `PersistentStore` that keeps it in a JSON file on the computer.

Java 25, JavaFX 27. Work in progress: many components are still stubs.

## Add it

```xml
<dependency>
    <groupId>io.joshuasalcedo</groupId>
    <artifactId>modernfx</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Build it yourself with `mvn install` until a release is published.

## Theme

```java
Scene scene = new Scene(root, 960, 640);
ThemeMfx.install(scene);                      // follows the system's light/dark setting
ThemeMfx.setMode(scene, ThemeMfx.Mode.DARK);  // or LIGHT, SYSTEM
ThemeMfx.toggle(scene);
```

Dark mode puts the style class `dark` on the scene's root, as shadcn puts it on `<html>`: the tokens switch and the
`dark:` classes apply. Your own stylesheets added after `install` win over the library's.

In your own CSS, use the tokens: `-fx-background-color: -card; -fx-border-color: -border;`. Each token also has alpha
steps (`-primary-90`, `-ring-50`), and the palette is there too (`-red-500`).

## Tailwind classes

```java
Tw.set(card, "p-6 gap-4 bg-card rounded-xl border shadow-sm");
Tw.set(title, "text-lg font-semibold dark:text-zinc-100");
Tw.apply(button, "hover:scale-105 transition duration-200");   // adds to what's there
```

JavaFX CSS can't escape class names, so `hover:bg-red-500` is the style class `hover_bg-red-500` and `p-0.5` is
`p-0_5`. `Tw` writes them for you. You can also use the style classes directly in FXML or CSS.

`Tw` also does what JavaFX CSS can't:

- per-side padding (`px-4 py-2`) and margins (`mt-2 -mx-1`)
- `grow`, `shrink-0`, `w-full`, `w-fit`
- `items-*` / `justify-*` (the pane's alignment)
- `overflow-hidden` (clips to the rounded corners)
- `animate-spin` / `animate-pulse`
- arbitrary values (`w-[220px]`, `bg-[#0ea5e9]`)
- palette alpha (`bg-red-500/50`)

Variants available:

| Tailwind | JavaFX |
|---|---|
| `hover:` | `:hover` |
| `focus:` | `:focused` |
| `focus-visible:` | `:focus-visible` |
| `focus-within:` | `:focus-within` |
| `active:` | `:pressed` |
| `disabled:` | `:disabled` |
| `checked:` | `:selected` |
| `group-hover:` | `.group:hover` |
| `dark:` | `.dark` ancestor |
| `dark:hover:` | both |

There's no flex or grid in JavaFX CSS: layout comes from the pane (`HBox`, `VBox`, `GridPane`...), and
responsive classes (`md:`) don't exist.

## App state

```java
record Settings(String theme, double width) {}
sealed interface Action { record Theme(String theme) implements Action {} }

try (var store = PersistentStore.builder(Settings.class, new Settings("dark", 1200), Settings::reduce)
        .app("Acme", "notes")                 // %APPDATA%\Acme\notes\state.json on Windows
        .open()) {
    label.textProperty().bind(store.state().map(Settings::theme));
    store.dispatch(new Action.Theme("light"));
}
```

The store saves 300 ms after the last change and on `close()`. It writes to a temporary file first, then replaces
the real one, so a crash mid-save doesn't break it. A file it can't read is renamed to `state.json.broken`. When the
state's shape changes, raise `version(n)` and add a `migrate(n - 1, json -> ...)` step. Never keep secrets in it.

## Running on the module path

`SheetMfx` (ControlsFX's SpreadsheetView) needs two JavaFX internals opened to ControlsFX when your app runs as a
module (in an IDE too). Add these JVM options; on the classpath nothing is needed:

```
--add-exports=javafx.base/com.sun.javafx.event=org.controlsfx.controls
--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=org.controlsfx.controls
```

Without them, `SheetMfx` stops with a message listing these options.

## Packages

| Package | What |
|---|---|
| `io.joshuasalcedo.fx.utils.theme` | `ThemeMfx`: the look, light/dark |
| `io.joshuasalcedo.fx.utils.tailwind` | `Tw`, `TwColor` (the Tailwind palette) |
| `io.joshuasalcedo.fx.utils.state` | `Store`, `PersistentStore`, `AppDirs` |
| `io.joshuasalcedo.fx.ui.core.*` | The components: `button`, `input`, `card`, `dialog`, `table`... |
| `io.joshuasalcedo.fx.ui.valueobjects.*` | Fields and views for common values: users, links, locale, auth |
| `io.joshuasalcedo.fx.ui.application.*` | Whole screens: chat, auth, media, contacts, settings... |

Classes end in `Mfx` (`ButtonMfx`), style classes start with `mfx-`.

## Regenerating the styles

`tokens.css`, `tailwind.css`, `TwColor` and `Tailwind` are generated from `tools/input/tailwind-theme.css` (Tailwind
CSS 4.3.3) and `tools/input/shadcn-globals.css` (shadcn's tokens):

```
java tools/GenerateStyles.java
```

To use another shadcn theme, put its `globals.css` in `tools/input/shadcn-globals.css` and run it again.

## Releasing

Releases go to Maven Central through the Central Portal (`mvn deploy -Pcentral`: sources, Javadoc, GPG signatures).
Push a tag `vX.Y.Z`, or run the Release workflow by hand with a version. The upload then waits under Deployments on
[central.sonatype.com](https://central.sonatype.com) until someone presses Publish; nothing is public before that.

From your own computer: `mvn -Pcentral deploy` (the Central Portal token is server id `central` in
`~/.m2/settings.xml`; gpg signs with your default key).

## License

Apache License 2.0. Tailwind CSS's theme file in `tools/input` is MIT (Tailwind Labs).
