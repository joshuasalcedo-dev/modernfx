package io.joshuasalcedo.fx.utils.tailwind;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Tailwind classes on JavaFX nodes.
///
/// ```java
/// Tw.set(box, "px-4 py-2 gap-2 items-center bg-card hover:bg-accent rounded-lg border shadow-sm");
/// Tw.set(title, "text-lg font-semibold dark:text-zinc-100");
/// ```
///
/// Most classes become style classes from tailwind.css (JavaFX CSS can't escape names, so `hover:bg-red-500` is
/// the style class `hover_bg-red-500` and `p-0.5` is `p-0_5`). Tw itself does what JavaFX CSS can't:
///
/// - per-side padding: `px-4 py-2 pt-1` (JavaFX has one -fx-padding)
/// - margins: `m-2 mx-auto mt-4 -mt-1` (layout constraints of the parent pane)
/// - `grow`, `flex-1`, `grow-0`, `shrink-0`, `w-full`, `h-full`, `w-fit`, `h-fit`
/// - `items-center`, `justify-end`... (the pane's alignment; on an HBox justify is horizontal, on a VBox vertical)
/// - `overflow-hidden` (clips to the node, with its `rounded-*` corners)
/// - `animate-spin`, `animate-pulse`, `animate-bounce`
/// - arbitrary values `w-[220px]`, `bg-[#0ea5e9]`, `text-[13px]`, palette alpha `bg-red-500/50`
///
/// Those work without variants only (`hover:px-4` doesn't). `set` replaces what Tw set before; `apply` adds to it.
@NullMarked
public final class Tw {

    private static final String STATE = Tw.class.getName();
    private static final Pattern PADDING = Pattern.compile("p([xytrblse]?)-(.+)");
    private static final Pattern MARGIN = Pattern.compile("(-?)m([xytrblse]?)-(.+)");
    private static final Pattern ARBITRARY = Pattern.compile("(-?[a-z][a-z-]*?)-\\[(.+)]");
    private static final Pattern PALETTE_ALPHA = Pattern.compile("(bg|text|border)-([a-z]+)-(\\d+)/(\\d+)");

    private Tw() {
    }

    /// Replaces the classes Tw set on the node before with these.
    public static <N extends Node> N set(N node, String classes) {
        State old = (State) node.getProperties().get(STATE);
        if (old != null) {
            old.undo(node);
        }
        State state = new State(classes);
        node.getProperties().put(STATE, state);
        state.apply(node);
        return node;
    }

    /// Adds these classes to the ones Tw set before; later classes win.
    public static <N extends Node> N apply(N node, String classes) {
        State old = (State) node.getProperties().get(STATE);
        return set(node, old == null ? classes : old.classes + " " + classes);
    }

    /// Removes what Tw set on the node.
    public static void clear(Node node) {
        State old = (State) node.getProperties().remove(STATE);
        if (old != null) {
            old.undo(node);
        }
    }

    /// The classes Tw set on the node, as written.
    public static String classes(Node node) {
        State s = (State) node.getProperties().get(STATE);
        return s == null ? "" : s.classes;
    }

    /// The JavaFX style class for a Tailwind class: `hover:bg-red-500` -> `hover_bg-red-500`.
    public static String styleClass(String twClass) {
        String cls = twClass.replace(':', '_').replace('.', '_').replace('/', '_');
        // `border`, `rounded`, `text-input`... are also JavaFX's own style classes: their utilities are `tw-border`...
        return Tailwind.RESERVED.contains(cls) ? "tw-" + cls : cls;
    }

    /// Joins class strings, skipping nulls and blanks: `Tw.cx("p-4", active ? "bg-accent" : null)`.
    public static String cx(@Nullable String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                if (!sb.isEmpty()) {
                    sb.append(' ');
                }
                sb.append(p.trim());
            }
        }
        return sb.toString();
    }

    /// Pixels for a spacing key: `"4"` -> 16, `"0.5"` -> 2, `"px"` -> 1, `"[13px]"` -> 13.
    public static Optional<Double> spacing(String key) {
        if (key.equals("px")) {
            return Optional.of(1.0);
        }
        if (key.startsWith("[") && key.endsWith("]")) {
            return number(key.substring(1, key.length() - 1));
        }
        try {
            double steps = Double.parseDouble(key);
            return steps < 0 ? Optional.empty() : Optional.of(steps * Tailwind.SPACING);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static Optional<Double> number(String v) {
        try {
            return Optional.of(Double.parseDouble(v.endsWith("px") ? v.substring(0, v.length() - 2) : v));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static String fmt(double d) {
        return d == Math.rint(d) ? String.valueOf((long) d) : String.valueOf(d);
    }

    // ------------------------------------------------------------------------------------------------------

    private static final class State {
        final String classes;
        final List<String> styleClasses = new ArrayList<>();
        final Map<String, String> inline = new LinkedHashMap<>();
        @Nullable String inlineWritten;
        final List<Runnable> undo = new ArrayList<>();

        // Collected while reading the classes
        final double[] padding = {Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        boolean sidePadding;
        final double[] margin = {Double.NaN, Double.NaN, Double.NaN, Double.NaN};
        boolean hasMargin;
        @Nullable String items;
        @Nullable String justify;
        @Nullable String rounded;
        boolean clip;
        @Nullable String animation;

        State(String classes) {
            this.classes = classes.trim().replaceAll("\\s+", " ");
        }

        void apply(Node node) {
            for (String c : classes.isEmpty() ? new String[0] : classes.split(" ")) {
                read(node, c);
            }
            node.getStyleClass().addAll(styleClasses);
            if (sidePadding) {
                inline.put("-fx-padding", "%s %s %s %s".formatted(fmt(z(padding[0])), fmt(z(padding[1])),
                        fmt(z(padding[2])), fmt(z(padding[3]))));
            }
            if (items != null || justify != null) {
                inline.put("-fx-alignment", alignment(node).name().toLowerCase(Locale.ROOT).replace('_', '-'));
            }
            if (hasMargin) {
                setMargin(node, new Insets(z(margin[0]), z(margin[1]), z(margin[2]), z(margin[3])));
                undo.add(() -> setMargin(node, null));
            }
            if (clip && node instanceof Region region) {
                clip(region);
            }
            if (animation != null) {
                animate(node, animation);
            }
            writeInline(node);
        }

        void undo(Node node) {
            node.getStyleClass().removeAll(styleClasses);
            inline.clear();
            writeInline(node);
            for (int i = undo.size() - 1; i >= 0; i--) {
                undo.get(i).run();
            }
        }

        private void read(Node node, String c) {
            boolean base = !c.contains(":");
            if (base && side(c)) {
                return;
            }
            if (base) {
                Matcher pa = PALETTE_ALPHA.matcher(c);
                if (pa.matches()) {
                    Optional<TwColor> color = TwColor.of(pa.group(2));
                    int shade = Integer.parseInt(pa.group(3));
                    if (color.isPresent() && color.get().hasShade(shade)) {
                        colorInline(pa.group(1), rgba(color.get().color(shade), Integer.parseInt(pa.group(4)) / 100.0));
                        return;
                    }
                }
                Matcher a = ARBITRARY.matcher(c);
                if (a.matches() && arbitrary(a.group(1), a.group(2).replace('_', ' '))) {
                    return;
                }
                if (layout(node, c)) {
                    return;
                }
                if (c.startsWith("rounded")) {
                    rounded = c;
                }
            }
            styleClasses.add(styleClass(c));
        }

        /// Per-side padding and margins.
        private boolean side(String c) {
            Matcher p = PADDING.matcher(c);
            if (p.matches()) {
                Optional<Double> v = spacing(p.group(2));
                if (v.isEmpty()) {
                    return false;
                }
                if (p.group(1).isEmpty() && !p.group(2).startsWith("[")) {
                    // p-4 alone is a style class; with per-side classes it's part of the inline padding
                    for (int i = 0; i < 4; i++) {
                        padding[i] = v.get();
                    }
                    styleClasses.add(styleClass(c));
                    return true;
                }
                sides(padding, p.group(1), v.get());
                sidePadding = true;
                return true;
            }
            Matcher m = MARGIN.matcher(c);
            if (m.matches()) {
                String key = m.group(3);
                double v;
                if (key.equals("auto")) {
                    v = 0;
                } else {
                    Optional<Double> s = spacing(key);
                    if (s.isEmpty()) {
                        return false;
                    }
                    v = m.group(1).isEmpty() ? s.get() : -s.get();
                }
                sides(margin, m.group(2), v);
                hasMargin = true;
                return true;
            }
            return false;
        }

        private static void sides(double[] t, String side, double v) {
            switch (side) {
                case "" -> { t[0] = v; t[1] = v; t[2] = v; t[3] = v; }
                case "x" -> { t[1] = v; t[3] = v; }
                case "y" -> { t[0] = v; t[2] = v; }
                case "t" -> t[0] = v;
                case "r", "e" -> t[1] = v;
                case "b" -> t[2] = v;
                case "l", "s" -> t[3] = v;
                default -> { }
            }
        }

        private boolean arbitrary(String prefix, String value) {
            boolean color = value.startsWith("#") || value.startsWith("rgb") || value.startsWith("hsl")
                    || value.startsWith("-");
            String size = value.matches("-?[\\d.]+(px)?") ? fmt(number(value).orElse(0.0)) : null;
            switch (prefix) {
                case "bg" -> inline.put("-fx-background-color", value);
                case "text" -> {
                    if (color) {
                        colorInline("text", value);
                    } else if (size != null) {
                        inline.put("-fx-font-size", size + "px");
                    } else {
                        return false;
                    }
                }
                case "border" -> {
                    if (color) {
                        inline.put("-fx-border-color", value);
                    } else if (size != null) {
                        inline.put("-fx-border-width", size);
                    } else {
                        return false;
                    }
                }
                case "w", "h", "size", "min-w", "max-w", "min-h", "max-h" -> {
                    if (size == null) {
                        return false;
                    }
                    for (String axis : prefix.equals("size") ? List.of("width", "height")
                            : List.of(prefix.endsWith("w") ? "width" : "height")) {
                        if (prefix.startsWith("min")) {
                            inline.put("-fx-min-" + axis, size);
                        } else if (prefix.startsWith("max")) {
                            inline.put("-fx-max-" + axis, size);
                        } else {
                            inline.put("-fx-min-" + axis, size);
                            inline.put("-fx-pref-" + axis, size);
                            inline.put("-fx-max-" + axis, size);
                        }
                    }
                }
                case "gap" -> {
                    if (size == null) {
                        return false;
                    }
                    inline.put("-fx-spacing", size);
                    inline.put("-fx-hgap", size);
                    inline.put("-fx-vgap", size);
                }
                case "rounded" -> {
                    if (size == null) {
                        return false;
                    }
                    inline.put("-fx-background-radius", size);
                    inline.put("-fx-border-radius", size);
                    rounded = "rounded-[" + size + "]";
                }
                case "opacity" -> inline.put("-fx-opacity", value);
                case "font" -> inline.put("-fx-font-weight", value);
                default -> {
                    return false;
                }
            }
            return true;
        }

        private void colorInline(String what, String color) {
            switch (what) {
                case "bg" -> inline.put("-fx-background-color", color);
                case "border" -> inline.put("-fx-border-color", color);
                default -> {
                    inline.put("-fx-text-fill", color);
                    inline.put("-fx-fill", color);
                    inline.put("-fx-icon-color", color);
                }
            }
        }

        /// Classes that set layout constraints or properties instead of CSS.
        private boolean layout(Node node, String c) {
            switch (c) {
                case "grow", "flex-1", "flex-grow", "flex-auto" -> grow(node, Priority.ALWAYS, Priority.ALWAYS);
                case "grow-0", "flex-none" -> grow(node, Priority.NEVER, Priority.NEVER);
                case "shrink-0" -> {
                    if (node instanceof Region r) {
                        double w = r.getMinWidth();
                        double h = r.getMinHeight();
                        r.setMinWidth(Region.USE_PREF_SIZE);
                        r.setMinHeight(Region.USE_PREF_SIZE);
                        undo.add(() -> {
                            r.setMinWidth(w);
                            r.setMinHeight(h);
                        });
                    }
                }
                case "w-full" -> {
                    styleClasses.add("w-full");
                    grow(node, Priority.ALWAYS, null);
                }
                case "h-full" -> {
                    styleClasses.add("h-full");
                    grow(node, null, Priority.ALWAYS);
                }
                case "size-full" -> {
                    styleClasses.add("size-full");
                    grow(node, Priority.ALWAYS, Priority.ALWAYS);
                }
                case "w-fit", "h-fit" -> {
                    if (node instanceof Region r) {
                        boolean w = c.startsWith("w");
                        double old = w ? r.getMaxWidth() : r.getMaxHeight();
                        if (w) {
                            r.setMaxWidth(Region.USE_PREF_SIZE);
                        } else {
                            r.setMaxHeight(Region.USE_PREF_SIZE);
                        }
                        undo.add(() -> {
                            if (w) {
                                r.setMaxWidth(old);
                            } else {
                                r.setMaxHeight(old);
                            }
                        });
                    }
                }
                case "overflow-hidden", "overflow-clip" -> clip = true;
                case "animate-spin", "animate-pulse", "animate-bounce", "animate-ping" -> animation = c;
                case "animate-none" -> animation = null;
                default -> {
                    if (c.startsWith("items-")) {
                        items = c.substring(6);
                        return true;
                    }
                    if (c.startsWith("justify-")) {
                        justify = c.substring(8);
                        return true;
                    }
                    return false;
                }
            }
            return true;
        }

        private void grow(Node node, @Nullable Priority h, @Nullable Priority v) {
            Priority oldH = HBox.getHgrow(node);
            Priority oldV = VBox.getVgrow(node);
            if (h != null) {
                HBox.setHgrow(node, h);
                GridPane.setHgrow(node, h);
            }
            if (v != null) {
                VBox.setVgrow(node, v);
                GridPane.setVgrow(node, v);
            }
            undo.add(() -> {
                HBox.setHgrow(node, oldH);
                GridPane.setHgrow(node, oldH);
                VBox.setVgrow(node, oldV);
                GridPane.setVgrow(node, oldV);
            });
        }

        /// items-* is the cross axis, justify-* the main axis (vertical in a VBox, horizontal elsewhere).
        private Pos alignment(Node node) {
            boolean column = node instanceof VBox;
            String main = edge(justify);
            String cross = edge(items);
            String vertical = column ? main : cross;
            String horizontal = column ? cross : main;
            String v = switch (vertical) {
                case "start" -> "TOP";
                case "end" -> "BOTTOM";
                case "baseline" -> "BASELINE";
                case "center" -> "CENTER";
                default -> column ? "TOP" : "CENTER";
            };
            String h = switch (horizontal) {
                case "center" -> "CENTER";
                case "end" -> "RIGHT";
                default -> "LEFT";
            };
            if (v.equals("CENTER") && h.equals("CENTER")) {
                return Pos.CENTER;
            }
            return Pos.valueOf(v + "_" + h);
        }

        private static String edge(@Nullable String v) {
            if (v == null) {
                return "";
            }
            return switch (v) {
                case "start", "flex-start", "left", "top" -> "start";
                case "end", "flex-end", "right", "bottom" -> "end";
                case "center" -> "center";
                case "baseline" -> "baseline";
                default -> "";
            };
        }

        private void clip(Region region) {
            double r = 0;
            if (rounded != null) {
                Matcher m = Pattern.compile("rounded(?:-([a-z0-9]+|\\[([\\d.]+)]))?").matcher(rounded);
                if (m.matches()) {
                    r = m.group(2) != null ? Double.parseDouble(m.group(2))
                            : Tailwind.RADII.getOrDefault(m.group(1) == null ? "" : m.group(1), 0.0);
                }
            }
            Rectangle shape = new Rectangle();
            shape.widthProperty().bind(region.widthProperty());
            shape.heightProperty().bind(region.heightProperty());
            double arc = Math.min(r * 2, 100000);
            shape.setArcWidth(arc);
            shape.setArcHeight(arc);
            var old = region.getClip();
            region.setClip(shape);
            undo.add(() -> region.setClip(old));
        }

        private void animate(Node node, String name) {
            Timeline t = switch (name) {
                case "animate-spin" -> new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(node.rotateProperty(), 0)),
                        new KeyFrame(Duration.seconds(1), new KeyValue(node.rotateProperty(), 360, Interpolator.LINEAR)));
                case "animate-bounce" -> {
                    Timeline b = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(node.translateYProperty(), -6, Interpolator.EASE_OUT)),
                            new KeyFrame(Duration.millis(500), new KeyValue(node.translateYProperty(), 0, Interpolator.EASE_IN)));
                    b.setAutoReverse(true);
                    yield b;
                }
                case "animate-ping" -> new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(node.scaleXProperty(), 1), new KeyValue(node.scaleYProperty(), 1),
                                new KeyValue(node.opacityProperty(), 1)),
                        new KeyFrame(Duration.seconds(1), new KeyValue(node.scaleXProperty(), 2), new KeyValue(node.scaleYProperty(), 2),
                                new KeyValue(node.opacityProperty(), 0)));
                default -> {
                    Timeline p = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 1, Interpolator.EASE_BOTH)),
                            new KeyFrame(Duration.seconds(1), new KeyValue(node.opacityProperty(), 0.5, Interpolator.EASE_BOTH)));
                    p.setAutoReverse(true);
                    yield p;
                }
            };
            t.setCycleCount(Animation.INDEFINITE);
            // Runs only while the node is in a scene, so a removed node doesn't keep animating.
            javafx.beans.value.ChangeListener<javafx.scene.Scene> inScene = (obs, old, now) -> {
                if (now == null) {
                    t.stop();
                } else {
                    t.play();
                }
            };
            node.sceneProperty().addListener(inScene);
            if (node.getScene() != null) {
                t.play();
            }
            undo.add(() -> {
                node.sceneProperty().removeListener(inScene);
                t.stop();
                node.setRotate(0);
                node.setTranslateY(0);
                node.setScaleX(1);
                node.setScaleY(1);
                node.setOpacity(1);
            });
        }

        private void writeInline(Node node) {
            String current = node.getStyle() == null ? "" : node.getStyle();
            if (inlineWritten != null && current.endsWith(inlineWritten)) {
                current = current.substring(0, current.length() - inlineWritten.length());
            }
            StringBuilder sb = new StringBuilder();
            inline.forEach((k, v) -> sb.append(k).append(": ").append(v).append("; "));
            String mine = sb.toString().trim();
            inlineWritten = mine.isEmpty() ? null : (current.isBlank() || current.endsWith(";") ? "" : "; ") + mine;
            node.setStyle(current + (inlineWritten == null ? "" : inlineWritten));
        }

        private static double z(double v) {
            return Double.isNaN(v) ? 0 : v;
        }

        private static String rgba(Color c, double alpha) {
            return "rgba(%d, %d, %d, %s)".formatted(Math.round(c.getRed() * 255), Math.round(c.getGreen() * 255),
                    Math.round(c.getBlue() * 255), fmt(alpha));
        }

        private static void setMargin(Node node, @Nullable Insets m) {
            HBox.setMargin(node, m);
            VBox.setMargin(node, m);
            StackPane.setMargin(node, m);
            BorderPane.setMargin(node, m);
            FlowPane.setMargin(node, m);
            GridPane.setMargin(node, m);
            TilePane.setMargin(node, m);
            if (node.getParent() != null) {
                node.getParent().requestLayout();
            }
        }
    }
}
