import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Makes ModernFX components from shadcn/ui components (.tsx).
 *
 * <pre>
 * java tools/GenerateComponent.java button card badge          (from shadcn's registry, style base-vega)
 * java tools/GenerateComponent.java --from W:/proj/src/components button card
 * java tools/GenerateComponent.java --style new-york-v4 button
 * java tools/GenerateComponent.java --dry-run button             (prints, writes nothing)
 * java tools/GenerateComponent.java --force button               (also replaces classes you have edited)
 * </pre>
 *
 * A starting point, used once per component: what it makes is ordinary code to edit by hand afterwards.
 * For each exported part with a data-slot (Card, CardHeader, CardTitle...) it writes a Java class
 * (ui/core/&lt;component&gt;/&lt;Part&gt;Mfx.java) and JavaFX CSS compiled from the part's Tailwind classes, as one section of
 * resources/io/joshuasalcedo/fx/components.css (which ThemeMfx loads). Running it again replaces only that
 * component's section, and only classes that are still stubs (a "// TODO" class), unless --force.
 *
 * The cva variants and the union props (size?: "sm" | "default") become enums; each value is a style class
 * (mfx-button-variant-outline, mfx-card-size-sm). States map to JavaFX pseudo-classes: hover, focus-visible,
 * active (:pressed), disabled, data-checked (:selected), aria-invalid (:invalid), aria-expanded (:showing);
 * dark: to the .dark root; data-[size=sm] and group-data-[size=sm]/card to the parts' classes.
 *
 * What JavaFX can't do (has-*, in-*, [&amp;_svg]..., not-*, peer-*, responsive md:...) is left out and listed in the
 * class's Javadoc and in the report. Layout-only classes (flex, grid, relative...) are left to the JavaFX pane the
 * part extends.
 */
public class GenerateComponent {

    static final Path RESOURCES = Path.of("src/main/resources/io/joshuasalcedo/fx");
    static final Path CSS_ALL = RESOURCES.resolve("components.css");
    static final Path JAVA_DIR = Path.of("src/main/java/io/joshuasalcedo/fx/ui/core");
    static final String PACKAGE = "io.joshuasalcedo.fx.ui.core";
    /** shadcn file -> ModernFX package, where the names differ. */
    static final Map<String, String> PACKAGE_OF = Map.of("switch", "toggle", "alert", "callout");

    static Map<String, Map<String, String>> tw;
    static Map<String, Double> radii = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        List<String> names = new ArrayList<>();
        Path from = null;
        String style = "base-vega";
        boolean dryRun = false;
        boolean force = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--from" -> from = Path.of(args[++i]);
                case "--style" -> style = args[++i];
                case "--dry-run" -> dryRun = true;
                case "--force" -> force = true;
                default -> names.add(args[i].replace(".tsx", ""));
            }
        }
        if (names.isEmpty()) {
            System.err.println("usage: java tools/GenerateComponent.java [--from dir] [--style base-vega] [--dry-run] button card ...");
            System.exit(2);
        }
        tw = readTailwind(RESOURCES.resolve("tailwind.css"));
        for (String name : names) {
            String source = from != null ? Files.readString(from.resolve(name + ".tsx")) : fetch(style, name);
            Component c = new Parser(name, source).parse();
            new Generator(c).write(dryRun, force);
        }
    }

    // ================================================================ sources

    static String fetch(String style, String name) throws IOException, InterruptedException {
        String url = "https://ui.shadcn.com/r/styles/" + style + "/" + name + ".json";
        HttpResponse<String> r = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url)).build(),
                HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) {
            throw new IOException(url + " answered " + r.statusCode());
        }
        // files[].content of the item whose path ends in /<name>.tsx
        Js.Reader js = new Js.Reader(r.body());
        @SuppressWarnings("unchecked")
        Map<String, Object> item = (Map<String, Object>) js.value();
        for (Object f : (List<?>) item.get("files")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> file = (Map<String, Object>) f;
            if (String.valueOf(file.get("path")).endsWith("/" + name + ".tsx")) {
                return (String) file.get("content");
            }
        }
        throw new IOException(url + " has no " + name + ".tsx");
    }

    /** tailwind.css: utility class (as written in tailwind) -> its declarations. */
    static Map<String, Map<String, String>> readTailwind(Path css) throws IOException {
        Map<String, Map<String, String>> m = new LinkedHashMap<>();
        Matcher rule = Pattern.compile("^\\.([^\\s,:{]+)[^{]*\\{([^}]*)}", Pattern.MULTILINE).matcher(Files.readString(css));
        while (rule.find()) {
            // tw-border and the like: utilities renamed because JavaFX uses the plain name itself
            String name = rule.group(1).startsWith("tw-") ? rule.group(1).substring(3) : rule.group(1);
            m.put(name, decls(rule.group(2)));
        }
        for (var e : m.entrySet()) {
            String r = e.getValue().get("-fx-background-radius");
            if (e.getKey().startsWith("rounded-") && e.getKey().chars().filter(ch -> ch == '-').count() == 1
                    && r != null && !r.contains(" ")) {
                radii.put(e.getKey().substring(8), Double.parseDouble(e.getValue().get("-fx-background-radius")));
            }
        }
        radii.put("", Double.parseDouble(m.get("rounded").get("-fx-background-radius")));
        return m;
    }

    static Map<String, String> decls(String body) {
        Map<String, String> d = new LinkedHashMap<>();
        for (String part : body.split(";")) {
            int c = part.indexOf(':');
            if (c > 0) {
                d.put(part.substring(0, c).trim(), part.substring(c + 1).trim());
            }
        }
        return d;
    }

    /** Puts the component's CSS in components.css between its markers: replaced if there, added if not. */
    static void writeSection(String name, String css) throws IOException {
        String begin = "/* ===== " + name + " ===== */\n";
        String end = "/* ===== end " + name + " ===== */\n";
        String all = Files.exists(CSS_ALL) ? Files.readString(CSS_ALL)
                : "/* The ModernFX components' look, one section per component. Started by tools/GenerateComponent.java\n"
                + "   from shadcn's .tsx; edit it freely (running the generator again replaces that component's section). */\n";
        int b = all.indexOf(begin);
        int e = all.indexOf(end);
        String section = begin + css + end;
        all = b >= 0 && e > b ? all.substring(0, b) + section + all.substring(e + end.length()) : all + "\n" + section;
        Files.writeString(CSS_ALL, all);
    }

    /** A class the generator may replace: missing, or still a stub. */
    static boolean replaceable(Path java, boolean force) throws IOException {
        if (force || !Files.exists(java)) {
            return true;
        }
        String s = Files.readString(java);
        return s.contains("// TODO") && s.length() < 1500;
    }

    // ================================================================ the model

    /** How a shadcn element becomes JavaFX. */
    enum Kind { LABELED, INPUT, TOGGLE, PROGRESS, ROW, COLUMN, STACK, REGION, IMAGE }

    record Base(String type, String imports, Kind kind, List<String> strip, String target, Map<String, String> parts) {
        String simpleName() {
            return type.substring(type.lastIndexOf('.') + 1);
        }
    }

    static final class Slot {
        String function;
        String name;            // data-slot
        String tag;
        List<String> classes = new ArrayList<>();
        String cva;
        Map<String, List<String>> props = new LinkedHashMap<>();   // prop -> values (first = default)
        Base base;
        Slot partOf;             // a sub-part of another slot's JavaFX control (switch-thumb -> .thumb)
        String partTarget;
    }

    record Component(String file, List<Slot> slots, Map<String, Cva> cvas) {}

    record Cva(String base, Map<String, Map<String, String>> variants, Map<String, String> defaults) {}

    // ================================================================ parsing the .tsx

    static final class Parser {
        final String file;
        final String src;

        Parser(String file, String src) {
            this.file = file;
            this.src = src;
        }

        Component parse() {
            Map<String, Cva> cvas = new LinkedHashMap<>();
            Matcher cv = Pattern.compile("const\\s+(\\w+)\\s*=\\s*cva\\(").matcher(src);
            while (cv.find()) {
                Js.Reader r = new Js.Reader(src, cv.end());
                String base = String.valueOf(r.value());
                Map<String, Map<String, String>> variants = new LinkedHashMap<>();
                Map<String, String> defaults = new LinkedHashMap<>();
                if (r.skip(',') && r.peek() == '{') {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> opts = (Map<String, Object>) r.value();
                    if (opts.get("variants") instanceof Map<?, ?> vs) {
                        vs.forEach((g, values) -> {
                            Map<String, String> m = new LinkedHashMap<>();
                            ((Map<?, ?>) values).forEach((k, v) -> m.put(String.valueOf(k), String.valueOf(v)));
                            variants.put(String.valueOf(g), m);
                        });
                    }
                    if (opts.get("defaultVariants") instanceof Map<?, ?> ds) {
                        ds.forEach((k, v) -> defaults.put(String.valueOf(k), String.valueOf(v)));
                    }
                }
                cvas.put(cv.group(1), new Cva(base, variants, defaults));
            }

            Set<String> exported = new LinkedHashSet<>();
            Matcher ex = Pattern.compile("export\\s*\\{([^}]*)}").matcher(src);
            while (ex.find()) {
                for (String n : ex.group(1).split(",")) {
                    exported.add(n.trim().split("\\s+")[0]);
                }
            }
            Matcher ef = Pattern.compile("export\\s+function\\s+(\\w+)").matcher(src);
            while (ef.find()) {
                exported.add(ef.group(1));
            }

            List<Slot> slots = new ArrayList<>();
            Matcher fn = Pattern.compile("(?m)^(?:export\\s+)?function\\s+([A-Z]\\w*)\\s*\\(").matcher(src);
            List<int[]> spans = new ArrayList<>();
            List<String> fnNames = new ArrayList<>();
            while (fn.find()) {
                spans.add(new int[] {fn.start(), 0});
                fnNames.add(fn.group(1));
            }
            for (int i = 0; i < spans.size(); i++) {
                int start = spans.get(i)[0];
                int end = i + 1 < spans.size() ? spans.get(i + 1)[0] : src.length();
                if (!exported.contains(fnNames.get(i))) {
                    continue;
                }
                slots.addAll(slotsOf(fnNames.get(i), src.substring(start, end), cvas));
            }
            return new Component(file, slots, cvas);
        }

        List<Slot> slotsOf(String function, String body, Map<String, Cva> cvas) {
            List<Slot> out = new ArrayList<>();
            String params = body.substring(body.indexOf('(') + 1, Math.max(body.indexOf('(') + 1, body.indexOf(") {")));
            Map<String, String> defaults = new LinkedHashMap<>();
            Matcher d = Pattern.compile("(\\w+)\\s*=\\s*\"([\\w-]+)\"").matcher(params);
            while (d.find()) {
                defaults.put(d.group(1), d.group(2));
            }
            Map<String, List<String>> unions = new LinkedHashMap<>();
            Matcher u = Pattern.compile("(\\w+)\\??\\s*:\\s*(\"[\\w-]+\"(?:\\s*\\|\\s*\"[\\w-]+\")+)").matcher(params);
            while (u.find()) {
                List<String> vals = new ArrayList<>();
                Matcher q = Pattern.compile("\"([\\w-]+)\"").matcher(u.group(2));
                while (q.find()) {
                    vals.add(q.group(1));
                }
                unions.put(u.group(1), vals);
            }
            if (defaults.containsKey("orientation") && !unions.containsKey("orientation")) {
                unions.put("orientation", List.of("horizontal", "vertical"));
            }

            Matcher slot = Pattern.compile("data-slot=\"([\\w-]+)\"|slot:\\s*\"([\\w-]+)\"").matcher(body);
            Matcher defaultTag = Pattern.compile("defaultTagName:\\s*\"(\\w+)\"").matcher(body);
            String renderTag = defaultTag.find() ? defaultTag.group(1) : null;
            while (slot.find()) {
                Slot s = new Slot();
                s.function = function;
                s.name = slot.group(1) != null ? slot.group(1) : slot.group(2);
                int at = slot.start();
                int tagStart = body.lastIndexOf('<', at);
                Matcher tag = Pattern.compile("<([A-Za-z][\\w.]*)").matcher(body);
                s.tag = renderTag;
                int elementStart = 0;
                if (renderTag == null && tagStart >= 0 && tag.find(tagStart)) {
                    s.tag = tag.group(1);
                    elementStart = tagStart;
                }
                Matcher comp = Pattern.compile("const\\s+" + Pattern.quote(String.valueOf(s.tag))
                        + "\\s*=\\s*[^\"\\n]*\"(\\w+)\"").matcher(body);
                if (comp.find()) {
                    s.tag = comp.group(1);
                }
                // className: the first one in this element (or anywhere, for useRender)
                int next = body.indexOf('<', at);
                int search = renderTag != null ? 0 : elementStart;
                // a className with a value (= or :), not the destructured parameter
                Matcher value = Pattern.compile("className\\s*[=:]\\s*").matcher(body);
                int cls = value.find(search) ? value.start() : -1;
                if (cls >= 0 && (renderTag != null || next < 0 || cls < next || cls < at + 600)) {
                    Js.Reader r = new Js.Reader(body, value.end());
                    r.skipAny("=: \t\r\n");
                    for (Js.Str str : r.strings()) {
                        s.classes.add(str.value());
                    }
                    Matcher call = Pattern.compile("(\\w+Variants)\\(").matcher(r.lastSpan());
                    if (call.find() && cvas.containsKey(call.group(1))) {
                        s.cva = call.group(1);
                    }
                }
                if (out.isEmpty()) {
                    // props belong to the function's root element
                    if (s.cva != null) {
                        Cva c = cvas.get(s.cva);
                        c.variants().forEach((g, vals) -> {
                            List<String> list = new ArrayList<>(vals.keySet());
                            String def = c.defaults().getOrDefault(g, defaults.get(g));
                            if (def != null && list.remove(def)) {
                                list.addFirst(def);
                            }
                            s.props.put(g, list);
                        });
                    }
                    unions.forEach((p, vals) -> {
                        if (!s.props.containsKey(p)) {
                            List<String> list = new ArrayList<>(vals);
                            String def = defaults.get(p);
                            if (def != null && list.remove(def)) {
                                list.addFirst(def);
                            }
                            s.props.put(p, list);
                        }
                    });
                }
                out.add(s);
            }
            return out;
        }
    }

    // ================================================================ JS literals

    static final class Js {
        record Str(String value) {}

        static final class Reader {
            final String s;
            int i;
            int spanStart;

            Reader(String s) {
                this(s, 0);
            }

            Reader(String s, int i) {
                this.s = s;
                this.i = i;
            }

            char peek() {
                ws();
                return i < s.length() ? s.charAt(i) : 0;
            }

            boolean skip(char c) {
                if (peek() == c) {
                    i++;
                    return true;
                }
                return false;
            }

            void skipAny(String chars) {
                while (i < s.length() && chars.indexOf(s.charAt(i)) >= 0) {
                    i++;
                }
            }

            void ws() {
                while (i < s.length()) {
                    char c = s.charAt(i);
                    if (Character.isWhitespace(c)) {
                        i++;
                    } else if (s.startsWith("//", i)) {
                        int nl = s.indexOf('\n', i);
                        i = nl < 0 ? s.length() : nl + 1;
                    } else if (s.startsWith("/*", i)) {
                        int e = s.indexOf("*/", i + 2);
                        i = e < 0 ? s.length() : e + 2;
                    } else {
                        return;
                    }
                }
            }

            Object value() {
                char c = peek();
                if (c == '"' || c == '\'' || c == '`') {
                    return string();
                }
                if (c == '{') {
                    i++;
                    Map<String, Object> m = new LinkedHashMap<>();
                    while (peek() != '}' && i < s.length()) {
                        String key = peek() == '"' || peek() == '\'' ? string() : ident();
                        if (skip(':')) {
                            m.put(key, value());
                        } else {
                            m.put(key, key);
                        }
                        skip(',');
                    }
                    i++;
                    return m;
                }
                if (c == '[') {
                    i++;
                    List<Object> l = new ArrayList<>();
                    while (peek() != ']' && i < s.length()) {
                        l.add(value());
                        skip(',');
                    }
                    i++;
                    return l;
                }
                return ident();
            }

            String string() {
                char q = s.charAt(i++);
                StringBuilder sb = new StringBuilder();
                while (i < s.length() && s.charAt(i) != q) {
                    char c = s.charAt(i++);
                    if (c == '\\' && i < s.length()) {
                        char n = s.charAt(i++);
                        sb.append(switch (n) {
                            case 'n' -> '\n';
                            case 't' -> '\t';
                            default -> n;
                        });
                    } else {
                        sb.append(c);
                    }
                }
                i++;
                return sb.toString();
            }

            String ident() {
                int st = i;
                int depth = 0;
                while (i < s.length()) {
                    char c = s.charAt(i);
                    if (c == '(' || c == '[') {
                        depth++;
                    } else if (c == ')' || c == ']') {
                        if (depth == 0) {
                            break;
                        }
                        depth--;
                    } else if (depth == 0 && (c == ',' || c == '}' || c == ':' || Character.isWhitespace(c))) {
                        break;
                    }
                    i++;
                }
                if (i == st) {
                    i++;
                }
                return s.substring(st, i);
            }

            /** The string literals of one attribute value: "..." or {cn("...", x({...}), className)}. */
            List<Str> strings() {
                List<Str> out = new ArrayList<>();
                spanStart = i;
                char c = peek();
                if (c == '"' || c == '\'' || c == '`') {
                    out.add(new Str(string()));
                    return out;
                }
                int depth = 0;
                while (i < s.length()) {
                    char ch = s.charAt(i);
                    if (ch == '"' || ch == '\'' || ch == '`') {
                        out.add(new Str(string()));
                        continue;
                    }
                    if (ch == '{' || ch == '(' || ch == '[') {
                        depth++;
                    } else if (ch == '}' || ch == ')' || ch == ']') {
                        depth--;
                        if (depth <= 0) {
                            i++;
                            if (depth < 0 || ch == '}' || ch == ')') {
                                break;
                            }
                        }
                    } else if (depth == 0 && (ch == ',' || ch == '\n')) {
                        break;
                    }
                    i++;
                }
                return out;
            }

            String lastSpan() {
                return s.substring(spanStart, Math.min(i, s.length()));
            }
        }
    }

    // ================================================================ JavaFX bases

    static Base base(Slot s) {
        String t = s.tag == null ? "div" : s.tag;
        Set<String> c = new TreeSet<>(String.join(" ", s.classes).isBlank() ? List.of()
                : Arrays.asList(String.join(" ", s.classes).trim().split("\\s+")));
        boolean flex = c.contains("flex") || c.contains("inline-flex");
        boolean col = c.contains("flex-col") || c.contains("grid") || c.contains("inline-grid");
        boolean text = c.stream().anyMatch(x -> x.matches("(text-(xs|sm|base|lg|[0-9]?xl)|font-\\w+|leading-\\w+)"));
        Base row = new Base("javafx.scene.layout.HBox", "javafx.scene.layout.HBox", Kind.ROW, List.of(), "", Map.of());
        Base column = new Base("javafx.scene.layout.VBox", "javafx.scene.layout.VBox", Kind.COLUMN, List.of(), "", Map.of());
        Base label = new Base("javafx.scene.control.Label", "javafx.scene.control.Label", Kind.LABELED, List.of(), "", Map.of());
        return switch (t) {
            case "button", "ButtonPrimitive" -> new Base("javafx.scene.control.Button", "javafx.scene.control.Button",
                    Kind.LABELED, List.of("button"), "", Map.of());
            case "a" -> new Base("javafx.scene.control.Hyperlink", "javafx.scene.control.Hyperlink", Kind.LABELED,
                    List.of("hyperlink"), "", Map.of());
            case "input", "InputPrimitive" -> new Base("javafx.scene.control.TextField", "javafx.scene.control.TextField",
                    Kind.INPUT, List.of("text-field"), "", Map.of());
            case "textarea" -> new Base("javafx.scene.control.TextArea", "javafx.scene.control.TextArea", Kind.INPUT,
                    List.of(), "", Map.of());
            case "label", "LabelPrimitive", "ProgressPrimitive.Label", "ProgressPrimitive.Value", "AvatarPrimitive.Fallback" -> label;
            case "span", "kbd", "p", "small", "code", "h1", "h2", "h3", "h4" -> flex && !text ? (col ? column : row) : label;
            case "CheckboxPrimitive.Root" -> new Base("javafx.scene.control.CheckBox", "javafx.scene.control.CheckBox",
                    Kind.TOGGLE, List.of(), " > .box", Map.of("checkbox-indicator", " > .box > .mark"));
            case "SwitchPrimitive.Root" -> new Base("atlantafx.base.controls.ToggleSwitch", "atlantafx.base.controls.ToggleSwitch",
                    Kind.TOGGLE, List.of(), " > .thumb-area", Map.of("switch-thumb", " > .thumb"));
            case "ProgressPrimitive.Root" -> new Base("javafx.scene.control.ProgressBar", "javafx.scene.control.ProgressBar",
                    Kind.PROGRESS, List.of(), "", Map.of("progress-track", " > .track", "progress-indicator", " > .bar"));
            case "SeparatorPrimitive", "hr" -> new Base("javafx.scene.layout.Region", "javafx.scene.layout.Region",
                    Kind.REGION, List.of(), "", Map.of());
            case "AvatarPrimitive.Root" -> new Base("javafx.scene.layout.StackPane", "javafx.scene.layout.StackPane",
                    Kind.STACK, List.of(), "", Map.of());
            case "AvatarPrimitive.Image", "img" -> new Base("javafx.scene.image.ImageView", "javafx.scene.image.ImageView",
                    Kind.IMAGE, List.of(), "", Map.of());
            case "div" -> col ? column : flex ? row : text ? label
                    : new Base("javafx.scene.layout.StackPane", "javafx.scene.layout.StackPane", Kind.STACK, List.of(), "", Map.of());
            default -> null;
        };
    }

    // ================================================================ Tailwind -> JavaFX

    /** Where a class applies: the state, the part's own classes (size-sm), dark, an ancestor. */
    record Chain(TreeSet<String> pseudos, TreeSet<String> own, boolean dark, String ancestor) {
        static Chain base() {
            return new Chain(new TreeSet<>(), new TreeSet<>(), false, "");
        }

        boolean within(Chain other) {
            return other.pseudos.containsAll(pseudos) && other.own.containsAll(own) && (!dark || other.dark)
                    && (ancestor.isEmpty() || ancestor.equals(other.ancestor));
        }

        String key() {
            return ancestor + "|" + dark + "|" + own + "|" + pseudos;
        }

        int weight() {
            return (ancestor.isEmpty() ? 0 : 100) + (dark ? 10 : 0) + own.size() * 3 + pseudos.size();
        }
    }

    /** What one class does. */
    static final class Effect {
        String owner;        // "variant-outline" for a cva value, null for the base
        Chain chain = Chain.base();
        Map<String, String> decls = new LinkedHashMap<>();
        double[] padding;    // t r b l, NaN = untouched
        Double borderWidth;
        String borderColor;
        Double ringWidth;
        String ringColor;
        Double radius;
        String items;
        String justify;
        Double gap;
        String textColor;
        String varName;      // defines --varName
        Double varValue;
        String usesVar;
        String source;       // the original class, to re-run with another var value
        boolean placeholder;

        boolean border() {
            return borderWidth != null || borderColor != null || ringWidth != null || ringColor != null;
        }
    }

    static final Set<String> DROP = Set.of("group", "peer", "relative", "absolute", "fixed", "sticky", "static",
            "inline-flex", "flex", "flex-col", "flex-row", "flex-wrap", "grid", "inline-grid", "block", "inline-block",
            "inline", "outline-none", "outline-hidden", "select-none", "select-text", "bg-clip-padding",
            "pointer-events-none", "pointer-events-auto", "field-sizing-content", "tabular-nums", "text-current",
            "isolate", "cursor-not-allowed", "transition", "transition-all", "transition-colors", "transition-opacity",
            "transition-shadow", "transition-transform", "transition-none", "text-balance", "text-pretty", "font-heading", "aspect-square",
            "object-cover", "contents", "transform", "shrink");
    // CSS transitions on every component made hover, page and theme changes lag in JavaFX: left out
    static final List<String> DROP_PREFIX = List.of("duration-", "ease-", "delay-", "group/", "peer/", "@container", "col-", "row-", "auto-rows-",
            "auto-cols-", "grid-cols-", "grid-rows-", "self-", "justify-self-", "place-", "object-", "aspect-",
            "underline-offset-", "leading-", "tracking-", "origin-", "will-change-", "inset-", "top-", "bottom-",
            "left-", "right-", "mix-blend-", "bg-blend-", "ring-offset-", "decoration-", "scroll-", "outline-");
    static final Set<String> RUNTIME = Set.of("animate-spin", "animate-pulse", "animate-bounce", "animate-ping",
            "overflow-hidden", "overflow-x-hidden", "overflow-clip", "grow", "flex-1", "shrink-0", "w-fit", "h-fit",
            "w-full", "h-full", "size-full");

    static final class Generator {
        final Component c;
        final Map<String, Slot> bySlot = new LinkedHashMap<>();
        final Map<Slot, List<Effect>> effects = new LinkedHashMap<>();
        final Map<Slot, Set<String>> unsupported = new LinkedHashMap<>();
        final Map<Slot, Set<String>> runtime = new LinkedHashMap<>();
        final Map<Slot, Boolean> invalid = new LinkedHashMap<>();
        final List<String> notMade = new ArrayList<>();

        Generator(Component c) {
            this.c = c;
            for (Slot s : c.slots()) {
                bySlot.putIfAbsent(s.name, s);
            }
            for (Slot s : c.slots()) {
                // the cva base classes count too when guessing the JavaFX pane (alert is a grid)
                List<String> own = s.classes;
                if (s.cva != null) {
                    s.classes = new ArrayList<>(own);
                    s.classes.add(c.cvas().get(s.cva).base());
                }
                s.base = base(s);
                s.classes = own;
            }
            for (Slot s : c.slots()) {
                for (Slot owner : c.slots()) {
                    if (owner.base != null && owner.base.parts().containsKey(s.name)) {
                        s.partOf = owner;
                        s.partTarget = owner.base.parts().get(s.name);
                    }
                }
            }
        }

        String cls(Slot s) {
            return "mfx-" + s.name;
        }

        String javaName(Slot s) {
            return s.function + "Mfx";
        }

        String pkg() {
            return PACKAGE_OF.getOrDefault(c.file(), c.file().replace("-", ""));
        }

        void write(boolean dryRun, boolean force) throws IOException {
            for (Slot s : c.slots()) {
                translate(s);
            }
            resolveVars();
            StringBuilder css = new StringBuilder("/* " + c.file() + ".tsx */\n");
            List<Slot> made = new ArrayList<>();
            for (Slot s : c.slots()) {
                if (s.base == null && s.partOf == null) {
                    notMade.add(s.function + " (<" + s.tag + ">: no JavaFX control for it yet)");
                    continue;
                }
                css.append(css(s));
                if (s.partOf == null && bySlot.get(s.name) == s) {
                    made.add(s);
                }
            }
            Path pkgDir = JAVA_DIR.resolve(pkg());
            if (dryRun) {
                System.out.println(css);
            } else {
                writeSection(c.file(), css.toString());
                Files.createDirectories(pkgDir);
            }
            for (Slot s : made) {
                String java = java(s);
                if (dryRun) {
                    System.out.println("----- " + javaName(s) + ".java\n" + java);
                } else if (!replaceable(pkgDir.resolve(javaName(s) + ".java"), force)) {
                    System.out.println("  kept " + javaName(s) + ".java: it is not a stub any more (--force replaces it)");
                } else {
                    Files.writeString(pkgDir.resolve(javaName(s) + ".java"), java);
                    if (!Files.exists(pkgDir.resolve("package-info.java"))) {
                        Files.writeString(pkgDir.resolve("package-info.java"), "/// shadcn's " + c.file()
                                + ", made by tools/GenerateComponent.java.\n@NullMarked\npackage " + PACKAGE + "." + pkg()
                                + ";\n\nimport org.jspecify.annotations.NullMarked;\n");
                    }
                }
            }
            report(made);
        }

        void report(List<Slot> made) {
            System.out.println("== " + c.file() + ".tsx -> " + PACKAGE + "." + pkg());
            for (Slot s : c.slots()) {
                if (s.base == null && s.partOf == null) {
                    continue;
                }
                long ok = effects.getOrDefault(s, List.of()).size();
                String what = s.partOf != null ? "part of " + javaName(s.partOf) + " (" + s.partTarget.trim() + ")"
                        : javaName(s) + " extends " + s.base.simpleName();
                System.out.printf("  %-24s %s: %d classes made into CSS, %d left out%n", s.name, what, ok,
                        unsupported.getOrDefault(s, Set.of()).size());
            }
            for (String n : notMade) {
                System.out.println("  not made: " + n);
            }
        }

        // ---------------------------------------------------------- classes -> effects

        void translate(Slot s) {
            List<Effect> list = effects.computeIfAbsent(s, k -> new ArrayList<>());
            Slot props = s.partOf != null ? s.partOf : s;
            for (String group : List.of(String.join(" ", s.classes))) {
                for (String cl : split(group)) {
                    add(s, props, null, cl, list);
                }
            }
            if (s.cva != null) {
                Cva cva = c.cvas().get(s.cva);
                for (String cl : split(cva.base())) {
                    add(s, props, null, cl, list);
                }
                cva.variants().forEach((g, vals) -> vals.forEach((v, classes) -> {
                    for (String cl : split(classes)) {
                        add(s, props, g + "-" + v, cl, list);
                    }
                }));
            }
        }

        static List<String> split(String classes) {
            List<String> out = new ArrayList<>();
            for (String x : classes.trim().split("\\s+")) {
                if (!x.isBlank()) {
                    out.add(x);
                }
            }
            return out;
        }

        void add(Slot s, Slot props, String owner, String cl, List<Effect> list) {
            Effect e = effect(s, props, cl);
            if (e == null) {
                return;
            }
            e.owner = owner;
            e.source = cl;
            list.add(e);
        }

        /** null when the class is left out (and recorded why). */
        Effect effect(Slot s, Slot props, String cl) {
            String raw = cl.startsWith("!") ? cl.substring(1) : cl;
            List<String> parts = splitVariants(raw);
            String u = parts.removeLast();
            Effect e = new Effect();
            TreeSet<String> pseudos = new TreeSet<>();
            TreeSet<String> own = new TreeSet<>();
            boolean dark = false;
            String ancestor = "";
            for (String v : parts) {
                String pseudo = switch (v) {
                    case "hover" -> ":hover";
                    case "focus" -> ":focused";
                    case "focus-visible" -> ":focus-visible";
                    case "focus-within" -> ":focus-within";
                    case "active" -> ":pressed";
                    case "disabled", "data-disabled", "aria-disabled" -> ":disabled";
                    case "data-checked", "aria-checked", "checked" -> ":selected";
                    case "aria-invalid" -> ":invalid";
                    case "aria-expanded", "data-open", "data-popup-open" -> ":showing";
                    case "data-unchecked" -> "";
                    default -> null;
                };
                if (pseudo != null) {
                    if (!pseudo.isEmpty()) {
                        pseudos.add(pseudo);
                    }
                    if (pseudo.equals(":invalid")) {
                        invalid.put(props, true);
                    }
                    continue;
                }
                if (v.equals("dark")) {
                    dark = true;
                    continue;
                }
                if (v.equals("placeholder")) {
                    e.placeholder = true;
                    continue;
                }
                Matcher data = Pattern.compile("data-\\[(\\w+)=([\\w-]+)]").matcher(v);
                if (data.matches() && props.props.containsKey(data.group(1))) {
                    own.add(data.group(1) + "-" + data.group(2));
                    continue;
                }
                Matcher dataValue = Pattern.compile("data-([\\w-]+)").matcher(v);
                if (dataValue.matches()) {
                    String prop = propWithValue(props, dataValue.group(1));
                    if (prop != null) {
                        own.add(prop + "-" + dataValue.group(1));
                        continue;
                    }
                }
                Matcher group = Pattern.compile("group-data-\\[(\\w+)=([\\w-]+)]/([\\w-]+)").matcher(v);
                if (group.matches()) {
                    Slot g = bySlot.get(group.group(3));
                    if (g != null && g.props.containsKey(group.group(1))) {
                        if (g == props) {
                            own.add(group.group(1) + "-" + group.group(2));
                        } else {
                            ancestor = "." + cls(g) + "-" + group.group(1) + "-" + group.group(2);
                        }
                        continue;
                    }
                }
                Matcher groupState = Pattern.compile("group-(hover|focus-visible|focus|active|disabled)/([\\w-]+)").matcher(v);
                if (groupState.matches() && bySlot.containsKey(groupState.group(2))) {
                    Slot g = bySlot.get(groupState.group(2));
                    String p = switch (groupState.group(1)) {
                        case "focus" -> ":focused";
                        case "active" -> ":pressed";
                        default -> ":" + groupState.group(1);
                    };
                    if (g == props) {
                        pseudos.add(p);
                    } else {
                        ancestor = "." + cls(g) + p;
                    }
                    continue;
                }
                return leftOut(s, cl);
            }
            e.chain = new Chain(pseudos, own, dark, ancestor);
            if (!utility(s.partOf != null ? s.partOf.base.kind() : s.base == null ? Kind.STACK : s.base.kind(), u, e)) {
                if (DROP.contains(u) || DROP_PREFIX.stream().anyMatch(u::startsWith) || u.startsWith("transition-[")) {
                    return null;
                }
                if (RUNTIME.contains(u) && parts.isEmpty()) {
                    runtime.computeIfAbsent(s, k -> new LinkedHashSet<>()).add(u);
                    return null;
                }
                return leftOut(s, cl);
            }
            if (RUNTIME.contains(u) && parts.isEmpty()) {
                runtime.computeIfAbsent(s, k -> new LinkedHashSet<>()).add(u);
            }
            return e;
        }

        String propWithValue(Slot s, String value) {
            for (var p : s.props.entrySet()) {
                if (p.getValue().contains(value)) {
                    return p.getKey();
                }
            }
            return null;
        }

        Effect leftOut(Slot s, String cl) {
            unsupported.computeIfAbsent(s, k -> new LinkedHashSet<>()).add(cl);
            return null;
        }

        static List<String> splitVariants(String cl) {
            List<String> out = new ArrayList<>();
            int depth = 0;
            int start = 0;
            for (int i = 0; i < cl.length(); i++) {
                char ch = cl.charAt(i);
                if (ch == '[' || ch == '(') {
                    depth++;
                } else if (ch == ']' || ch == ')') {
                    depth--;
                } else if (ch == ':' && depth == 0) {
                    out.add(cl.substring(start, i));
                    start = i + 1;
                }
            }
            out.add(cl.substring(start));
            return out;
        }

        /** Fills e from the utility; false when it isn't something JavaFX CSS can do. */
        boolean utility(Kind kind, String u, Effect e) {
            Matcher m;
            if ((m = Pattern.compile("\\[--([\\w-]+):(.+)]").matcher(u)).matches()) {
                Double v = length(m.group(2), null);
                if (v == null) {
                    return false;
                }
                e.varName = m.group(1);
                e.varValue = v;
                return true;
            }
            if ((m = Pattern.compile("p([xytrblse]?)-(.+)").matcher(u)).matches()) {
                Double v = spacing(m.group(2), e);
                if (v == null) {
                    return false;
                }
                e.padding = new double[] {Double.NaN, Double.NaN, Double.NaN, Double.NaN};
                switch (m.group(1)) {
                    case "" -> Arrays.fill(e.padding, v);
                    case "x" -> { e.padding[1] = v; e.padding[3] = v; }
                    case "y" -> { e.padding[0] = v; e.padding[2] = v; }
                    case "t" -> e.padding[0] = v;
                    case "r", "e" -> e.padding[1] = v;
                    case "b" -> e.padding[2] = v;
                    default -> e.padding[3] = v;
                }
                return true;
            }
            if ((m = Pattern.compile("gap(-[xy])?-(.+)").matcher(u)).matches()) {
                Double v = spacing(m.group(2), e);
                if (v == null) {
                    return false;
                }
                if (m.group(1) == null) {
                    e.gap = v;
                } else {
                    e.decls.put(m.group(1).equals("-x") ? "-fx-hgap" : "-fx-vgap", fmt(v));
                }
                return true;
            }
            if ((m = Pattern.compile("items-(start|end|center|baseline|stretch)").matcher(u)).matches()) {
                e.items = m.group(1);
                return true;
            }
            if ((m = Pattern.compile("justify-(start|end|center|between|around|evenly)").matcher(u)).matches()) {
                e.justify = m.group(1);
                return true;
            }
            if ((m = Pattern.compile("border(-[0248])?").matcher(u)).matches()) {
                e.borderWidth = m.group(1) == null ? 1.0 : Double.parseDouble(m.group(1).substring(1));
                return true;
            }
            if (u.startsWith("border-") && !u.matches("border-(solid|dashed|dotted|none|[xytrbl](-\\d+)?)")) {
                String color = color(u.substring(7));
                if (color == null) {
                    return false;
                }
                e.borderColor = color;
                return true;
            }
            if ((m = Pattern.compile("ring(?:-(\\d+)|-\\[(\\d+)px])?").matcher(u)).matches()) {
                e.ringWidth = m.group(1) != null ? Double.parseDouble(m.group(1))
                        : m.group(2) != null ? Double.parseDouble(m.group(2)) : 1.0;
                return true;
            }
            if (u.startsWith("ring-")) {
                String color = color(u.substring(5));
                if (color == null) {
                    return false;
                }
                e.ringColor = color;
                return true;
            }
            if ((m = Pattern.compile("rounded(?:-(.+))?").matcher(u)).matches()) {
                String k = m.group(1) == null ? "" : m.group(1);
                Double r = radii.get(k);
                if (r == null && k.startsWith("[")) {
                    Matcher px = Pattern.compile("([\\d.]+)px").matcher(k);
                    r = px.find() ? Double.parseDouble(px.group(1)) : null;
                }
                if (r == null && k.startsWith("(")) {
                    r = spacing(k, e);
                }
                if (r == null) {
                    return false;
                }
                e.radius = r;
                return true;
            }
            if ((m = Pattern.compile("(size|w|h|min-w|max-w|min-h|max-h)-(\\[.+]|\\(.+\\))").matcher(u)).matches()) {
                Double v = spacing(m.group(2), e);
                if (v == null) {
                    return false;
                }
                sizeDecls(m.group(1), fmt(v), e.decls);
                return true;
            }
            if ((m = Pattern.compile("text-\\[([\\d.]+)px]").matcher(u)).matches()) {
                e.decls.put("-fx-font-size", m.group(1) + "px");
                return true;
            }
            if (u.startsWith("text-") && color(u.substring(5)) != null) {
                String col = color(u.substring(5));
                if (e.placeholder) {
                    e.decls.put("-fx-prompt-text-fill", col);
                } else {
                    e.textColor = col;
                    // -mfx-text is looked up by the labels and icons inside, as CSS color is inherited on the web
                    e.decls.put("-mfx-text", col);
                    e.decls.put("-fx-text-fill", col);
                    e.decls.put("-fx-fill", col);
                }
                return true;
            }
            Map<String, String> d = tw.get(safe(u));
            if (d == null) {
                return false;
            }
            if (u.matches("(w|h|size|min-w|max-w|min-h|max-h)-.+") && kind == Kind.IMAGE) {
                return false;
            }
            e.decls.putAll(d);
            return true;
        }

        static void sizeDecls(String what, String v, Map<String, String> d) {
            switch (what) {
                case "size" -> {
                    for (String a : List.of("width", "height")) {
                        d.put("-fx-min-" + a, v);
                        d.put("-fx-pref-" + a, v);
                        d.put("-fx-max-" + a, v);
                    }
                }
                case "w", "h" -> {
                    String a = what.equals("w") ? "width" : "height";
                    d.put("-fx-min-" + a, v);
                    d.put("-fx-pref-" + a, v);
                    d.put("-fx-max-" + a, v);
                }
                default -> d.put("-fx-" + what.replace("-w", "-width").replace("-h", "-height"), v);
            }
        }

        /** A spacing key (4, 0.5, px), [13px], or (--var). */
        Double spacing(String key, Effect e) {
            if (key.startsWith("(--") && key.endsWith(")")) {
                e.usesVar = key.substring(3, key.length() - 1);
                return 0.0;
            }
            return length(key, 4.0);
        }

        static Double length(String v, Double unit) {
            v = v.trim();
            if (v.startsWith("[") && v.endsWith("]")) {
                v = v.substring(1, v.length() - 1);
            }
            Matcher fn = Pattern.compile("--spacing\\(([\\d.]+)\\)").matcher(v);
            if (fn.matches()) {
                return Double.parseDouble(fn.group(1)) * 4;
            }
            if (v.equals("px")) {
                return 1.0;
            }
            if (v.matches("[\\d.]+px")) {
                return Double.parseDouble(v.substring(0, v.length() - 2));
            }
            if (v.matches("[\\d.]+rem")) {
                return Double.parseDouble(v.substring(0, v.length() - 3)) * 16;
            }
            if (unit != null && v.matches("[\\d.]+")) {
                return Double.parseDouble(v) * unit;
            }
            return null;
        }

        /** A Tailwind color (primary, primary/50, red-500, transparent) as JavaFX, or null. */
        static String color(String name) {
            Map<String, String> d = tw.get(safe("bg-" + name));
            return d == null ? null : d.get("-fx-background-color");
        }

        static String safe(String cl) {
            return cl.replace(':', '_').replace('.', '_').replace('/', '_');
        }

        /** shadcn's CSS variables ([--card-spacing:--spacing(6)]): re-applied wherever a state changes them. */
        void resolveVars() {
            Map<String, Double> base = new LinkedHashMap<>();
            List<Object[]> scoped = new ArrayList<>();
            for (var se : effects.entrySet()) {
                for (Effect e : se.getValue()) {
                    if (e.varName != null) {
                        if (e.owner == null && e.chain.key().equals(Chain.base().key())) {
                            base.put(e.varName, e.varValue);
                        } else {
                            scoped.add(new Object[] {se.getKey(), e});
                        }
                    }
                }
            }
            for (var se : effects.entrySet()) {
                List<Effect> added = new ArrayList<>();
                for (Effect e : se.getValue()) {
                    if (e.usesVar == null) {
                        continue;
                    }
                    Double v = base.get(e.usesVar);
                    if (v == null) {
                        unsupported.computeIfAbsent(se.getKey(), k -> new LinkedHashSet<>()).add(e.source);
                        e.padding = null;
                        e.gap = null;
                        e.radius = null;
                        e.decls.clear();
                        continue;
                    }
                    applyVar(e, v);
                    for (Object[] sc : scoped) {
                        Slot definer = (Slot) sc[0];
                        Effect def = (Effect) sc[1];
                        if (!def.varName.equals(e.usesVar)) {
                            continue;
                        }
                        Effect copy = effect(se.getKey(), se.getKey().partOf != null ? se.getKey().partOf : se.getKey(), e.source);
                        if (copy == null) {
                            continue;
                        }
                        applyVar(copy, def.varValue);
                        copy.owner = e.owner;
                        TreeSet<String> own = new TreeSet<>(copy.chain.own());
                        String ancestor = copy.chain.ancestor();
                        if (definer == se.getKey() || definer == se.getKey().partOf) {
                            own.addAll(def.chain.own());
                        } else {
                            ancestor = "." + cls(definer) + (def.chain.own().isEmpty() ? "" : "-" + def.chain.own().first());
                        }
                        copy.chain = new Chain(copy.chain.pseudos(), own, copy.chain.dark(), ancestor);
                        added.add(copy);
                    }
                }
                se.getValue().addAll(added);
            }
        }

        static void applyVar(Effect e, double v) {
            if (e.padding != null) {
                for (int i = 0; i < 4; i++) {
                    if (!Double.isNaN(e.padding[i])) {
                        e.padding[i] = v;
                    }
                }
            }
            if (e.gap != null) {
                e.gap = v;
            }
            if (e.radius != null) {
                e.radius = v;
            }
            e.decls.replaceAll((k, old) -> fmt(v));
        }

        // ---------------------------------------------------------- effects -> CSS

        String css(Slot s) {
            List<Effect> list = effects.getOrDefault(s, List.of());
            Slot root = s.partOf != null ? s.partOf : s;
            Kind kind = root.base.kind();
            String target = s.partOf != null ? s.partTarget : root.base.target();
            // the rules: one per (owner, chain), base first, then each variant's, simple states before combined ones
            Map<String, List<Effect>> groups = new LinkedHashMap<>();
            List<String> owners = new ArrayList<>();
            owners.add(null);
            root.props.forEach((p, vals) -> vals.forEach(v -> owners.add(p + "-" + v)));
            List<Effect> sorted = new ArrayList<>(list);
            sorted.sort((a, b) -> {
                int oa = owners.indexOf(a.owner), ob = owners.indexOf(b.owner);
                return oa != ob ? Integer.compare(oa, ob) : Integer.compare(a.chain.weight(), b.chain.weight());
            });
            for (Effect e : sorted) {
                if (e.varName != null) {
                    continue;
                }
                groups.computeIfAbsent(e.owner + "#" + e.chain.key(), k -> new ArrayList<>()).add(e);
            }
            StringBuilder sb = new StringBuilder();
            for (List<Effect> g : groups.values()) {
                Effect first = g.getFirst();
                Map<String, String> d = new LinkedHashMap<>();
                boolean pad = false, border = false, radiusOnly = false, align = false;
                // shadcn text inherits the page's color; JavaFX doesn't, so the root's base rule sets it
                if (first.owner == null && first.chain.key().equals(Chain.base().key()) && s.partOf == null
                        && (kind == Kind.LABELED || kind == Kind.TOGGLE || kind == Kind.INPUT)) {
                    d.put("-fx-text-fill", "-mfx-text");
                }
                for (Effect e : g) {
                    d.putAll(e.decls);
                    pad |= e.padding != null;
                    border |= e.border();
                    radiusOnly |= e.radius != null;
                    align |= e.items != null || e.justify != null;
                    if (e.gap != null) {
                        switch (kind) {
                            case LABELED, TOGGLE -> d.put("-fx-graphic-text-gap", fmt(e.gap));
                            case ROW, COLUMN -> d.put("-fx-spacing", fmt(e.gap));
                            default -> { }
                        }
                    }
                    if (e.radius != null) {
                        d.put("-fx-background-radius", fmt(e.radius));
                    }
                }
                // composites, from every rule that also applies in this state
                List<Effect> within = new ArrayList<>();
                for (Effect e : sorted) {
                    if ((e.owner == null || e.owner.equals(first.owner)) && e.chain.within(first.chain) && e.varName == null) {
                        within.add(e);
                    }
                }
                if (pad) {
                    double[] p = {0, 0, 0, 0};
                    for (Effect e : within) {
                        if (e.padding != null) {
                            for (int i = 0; i < 4; i++) {
                                if (!Double.isNaN(e.padding[i])) {
                                    p[i] = e.padding[i];
                                }
                            }
                        }
                    }
                    d.put("-fx-padding", fmt(p[0]) + " " + fmt(p[1]) + " " + fmt(p[2]) + " " + fmt(p[3]));
                }
                if (radiusOnly && !border) {
                    // only the corners change (a size): leave the border's colors and widths to the other rules
                    double r = 0;
                    for (Effect e : g) {
                        if (e.radius != null) r = e.radius;
                    }
                    d.put("-fx-background-radius", fmt(r));
                    d.put("-fx-border-radius", fmt(r));
                }
                if (border) {
                    double bw = 0, rw = 0, r = 0;
                    String bc = "-border", rc = "-ring";
                    for (Effect e : within) {
                        if (e.borderWidth != null) bw = e.borderWidth;
                        if (e.borderColor != null) bc = e.borderColor;
                        if (e.ringWidth != null) rw = e.ringWidth;
                        if (e.ringColor != null) rc = e.ringColor;
                        if (e.radius != null) r = e.radius;
                    }
                    if (rw > 0) {
                        d.put("-fx-border-color", rc + ", " + bc);
                        d.put("-fx-border-width", fmt(rw) + ", " + fmt(bw));
                        d.put("-fx-border-insets", "-" + fmt(rw) + ", 0");
                        d.put("-fx-border-radius", fmt(r + rw) + ", " + fmt(r));
                    } else {
                        d.put("-fx-border-color", bc);
                        d.put("-fx-border-width", fmt(bw));
                        d.put("-fx-border-insets", "0");
                        d.put("-fx-border-radius", fmt(r));
                    }
                    d.put("-fx-background-radius", fmt(r));
                }
                if (align) {
                    String items = null, justify = null;
                    for (Effect e : within) {
                        if (e.items != null) items = e.items;
                        if (e.justify != null) justify = e.justify;
                    }
                    d.put("-fx-alignment", pos(kind, items, justify));
                }
                if (d.isEmpty()) {
                    continue;
                }
                String sel = selector(root, first, target);
                sb.append(sel).append(" {");
                d.forEach((k, v) -> sb.append(' ').append(k).append(": ").append(v).append(';'));
                sb.append(" }\n");
                String icon = d.get("-fx-text-fill");
                for (Effect e : g) {
                    if (e.textColor != null) icon = e.textColor;
                }
                if (icon != null && (kind == Kind.LABELED || kind == Kind.TOGGLE)) {
                    sb.append(sel).append(" .ikonli-font-icon { -fx-icon-color: ").append(icon).append("; }\n");
                }
            }
            return sb.toString();
        }

        String selector(Slot root, Effect e, String target) {
            StringBuilder sb = new StringBuilder();
            if (!e.chain.ancestor().isEmpty()) {
                sb.append(e.chain.ancestor()).append(' ');
            }
            if (e.chain.dark()) {
                sb.append(".dark ");
            }
            if (e.owner == null && e.chain.own().isEmpty()) {
                sb.append('.').append(cls(root));
            }
            if (e.owner != null) {
                sb.append('.').append(cls(root)).append('-').append(e.owner);
            }
            for (String o : e.chain.own()) {
                sb.append('.').append(cls(root)).append('-').append(o);
            }
            for (String p : e.chain.pseudos()) {
                sb.append(p);
            }
            return sb.append(target).toString();
        }

        static String pos(Kind kind, String items, String justify) {
            boolean column = kind == Kind.COLUMN;
            String main = edge(justify), cross = edge(items);
            String v = column ? main : cross, h = column ? cross : main;
            String vv = switch (v) {
                case "start" -> "top";
                case "end" -> "bottom";
                case "baseline" -> "baseline";
                default -> column ? "top" : "center";
            };
            if (v.equals("center")) {
                vv = "center";
            }
            String hh = switch (h) {
                case "center" -> "center";
                case "end" -> "right";
                default -> "left";
            };
            return vv.equals("center") && hh.equals("center") ? "center" : vv + "-" + hh;
        }

        static String edge(String v) {
            if (v == null) {
                return "";
            }
            return switch (v) {
                case "start" -> "start";
                case "end" -> "end";
                case "center" -> "center";
                case "baseline" -> "baseline";
                default -> "";
            };
        }

        // ---------------------------------------------------------- the Java class

        String java(Slot s) {
            Base b = s.base;
            String name = javaName(s);
            Set<String> imports = new TreeSet<>();
            imports.add(b.imports());
            imports.add("org.jspecify.annotations.NullMarked");
            StringBuilder body = new StringBuilder();
            StringBuilder init = new StringBuilder();
            init.append("        getStyleClass().add(STYLE_CLASS);\n");
            for (String strip : b.strip()) {
                init.append("        getStyleClass().remove(\"").append(strip).append("\");\n");
            }
            for (var p : s.props.entrySet()) {
                String prop = p.getKey();
                String type = cap(prop);
                imports.add("javafx.beans.property.ObjectProperty");
                imports.add("javafx.beans.property.SimpleObjectProperty");
                body.append("\n    /// shadcn's `").append(prop).append("`: ")
                        .append(String.join(", ", p.getValue().stream().map(v -> "`" + v + "`").toList())).append(".\n");
                body.append("    public enum ").append(type).append(" {\n");
                List<String> constants = new ArrayList<>();
                for (String v : p.getValue()) {
                    constants.add("        " + constant(v) + "(\"" + cls(s) + "-" + prop + "-" + v + "\")");
                }
                body.append(String.join(",\n", constants)).append(";\n\n");
                body.append("        private final String styleClass;\n\n");
                body.append("        ").append(type).append("(String styleClass) {\n            this.styleClass = styleClass;\n        }\n\n");
                body.append("        /// The style class this value puts on the ").append(s.function).append(".\n");
                body.append("        public String styleClass() {\n            return styleClass;\n        }\n    }\n");
                String field = prop.equals("size") && b.kind() != Kind.ROW ? "sizeVariant" : prop;
                String def = constant(p.getValue().getFirst());
                body.append("\n    private final ObjectProperty<").append(type).append("> ").append(field)
                        .append(" = new SimpleObjectProperty<>(this, \"").append(prop).append("\", ").append(type).append('.')
                        .append(def).append(");\n");
                body.append("\n    public ").append(name).append(' ').append(prop).append('(').append(type).append(" value) {\n")
                        .append("        ").append(field).append(".set(value);\n        return this;\n    }\n");
                body.append("\n    public ObjectProperty<").append(type).append("> ").append(prop).append("Property() {\n")
                        .append("        return ").append(field).append(";\n    }\n");
                body.append("\n    public ").append(type).append(" get").append(type).append("() {\n        return ")
                        .append(field).append(".get();\n    }\n");
                init.append("        getStyleClass().add(").append(field).append(".get().styleClass());\n");
                init.append("        ").append(field).append(".addListener((obs, old, now) -> {\n")
                        .append("            getStyleClass().remove(old.styleClass());\n")
                        .append("            getStyleClass().add(now.styleClass());\n        });\n");
            }
            if (invalid.getOrDefault(s, false)) {
                imports.add("javafx.beans.property.BooleanProperty");
                imports.add("javafx.beans.property.SimpleBooleanProperty");
                imports.add("javafx.css.PseudoClass");
                body.append("\n    private static final PseudoClass INVALID = PseudoClass.getPseudoClass(\"invalid\");\n");
                body.append("\n    private final BooleanProperty invalid = new SimpleBooleanProperty(this, \"invalid\") {\n")
                        .append("        @Override\n        protected void invalidated() {\n")
                        .append("            pseudoClassStateChanged(INVALID, get());\n        }\n    };\n");
                body.append("\n    /// shadcn's `aria-invalid`: the red border and ring of a field in error.\n")
                        .append("    public BooleanProperty invalidProperty() {\n        return invalid;\n    }\n");
                body.append("\n    public void setInvalid(boolean value) {\n        invalid.set(value);\n    }\n");
            }
            Set<String> rt = runtime.getOrDefault(s, Set.of());
            if (!rt.isEmpty()) {
                imports.add("io.joshuasalcedo.fx.utils.tailwind.Tw");
                String rounded = s.classes.stream().flatMap(x -> split(x).stream()).filter(x -> x.matches("rounded(-[a-z0-9]+)?"))
                        .findFirst().orElse("");
                init.append("        Tw.apply(this, \"").append(String.join(" ", rt)).append(rounded.isEmpty() ? "" : " " + rounded)
                        .append("\");\n");
            }
            StringBuilder ctors = new StringBuilder();
            String simple = b.simpleName();
            switch (b.kind()) {
                case LABELED -> {
                    if (simple.equals("Button") || simple.equals("Hyperlink")) {
                        imports.add("javafx.scene.Node");
                        imports.add("org.jspecify.annotations.Nullable");
                        ctors.append(ctor(name, "", "this(\"\");", null));
                        ctors.append(ctor(name, "String text", "super(text);", init));
                        ctors.append(ctor(name, "String text, @Nullable Node graphic", "super(text, graphic);", init));
                    } else {
                        ctors.append(ctor(name, "", "this(\"\");", null));
                        ctors.append(ctor(name, "String text", "super(text);", init));
                    }
                }
                case INPUT -> {
                    ctors.append(ctor(name, "", null, init));
                    ctors.append(ctor(name, "String prompt", "this();\n        setPromptText(prompt);", null));
                }
                case TOGGLE -> {
                    ctors.append(ctor(name, "", "this(\"\");", null));
                    ctors.append(ctor(name, "String text", "super(text);", init));
                }
                case PROGRESS -> {
                    ctors.append(ctor(name, "", "this(0);", null));
                    ctors.append(ctor(name, "double value", "super(value);", init));
                }
                case ROW, COLUMN, STACK -> {
                    imports.add("javafx.scene.Node");
                    ctors.append(ctor(name, "Node... children", "super(children);", init));
                }
                case IMAGE -> {
                    imports.add("javafx.scene.image.Image");
                    imports.add("org.jspecify.annotations.Nullable");
                    ctors.append(ctor(name, "", "this(null);", null));
                    ctors.append(ctor(name, "@Nullable Image image", "super(image);", init));
                }
                case REGION -> ctors.append(ctor(name, "", null, init));
            }
            StringBuilder doc = new StringBuilder();
            doc.append("/// shadcn's `").append(s.function).append("` (`data-slot=\"").append(s.name).append("\"`, ")
                    .append(c.file()).append(".tsx), as a JavaFX ").append(simple).append(".\n");
            doc.append("/// The look is in `components.css` (style class `").append(cls(s)).append("`).\n");
            List<String> parts = new ArrayList<>();
            for (Slot p : c.slots()) {
                if (p.partOf == s) {
                    parts.add("`" + p.name + "` styles `" + p.partTarget.trim() + "`");
                }
            }
            if (!parts.isEmpty()) {
                doc.append("///\n/// Its parts: ").append(String.join(", ", parts)).append(".\n");
            }
            Set<String> left = new LinkedHashSet<>(unsupported.getOrDefault(s, Set.of()));
            for (Slot p : c.slots()) {
                if (p.partOf == s) {
                    left.addAll(unsupported.getOrDefault(p, Set.of()));
                }
            }
            if (!left.isEmpty()) {
                doc.append("///\n/// Left out (JavaFX has no equivalent): ")
                        .append(String.join(" ", left.stream().map(x -> "`" + x.replace("`", "") + "`").toList())).append("\n");
            }
            StringBuilder sb = new StringBuilder();
            sb.append("// Made from shadcn's ").append(c.file())
                    .append(".tsx by tools/GenerateComponent.java. Ordinary code now: edit it.\n");
            sb.append("package ").append(PACKAGE).append('.').append(pkg()).append(";\n\n");
            for (String i : imports) {
                sb.append("import ").append(i).append(";\n");
            }
            sb.append('\n').append(doc);
            sb.append("@NullMarked\npublic class ").append(name).append(" extends ").append(simple).append(" {\n\n");
            sb.append("    /// The style class every ").append(name).append(" has.\n");
            sb.append("    public static final String STYLE_CLASS = \"").append(cls(s)).append("\";\n");
            sb.append(body);
            sb.append(ctors);
            sb.append("}\n");
            return sb.toString();
        }

        static String ctor(String name, String params, String first, StringBuilder init) {
            StringBuilder sb = new StringBuilder("\n    public ").append(name).append('(').append(params).append(") {\n");
            if (first != null) {
                sb.append("        ").append(first).append('\n');
            }
            if (init != null) {
                sb.append(init);
            }
            return sb.append("    }\n").toString();
        }

        static String cap(String s) {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }

        static String constant(String v) {
            String c = v.toUpperCase(Locale.ROOT).replace('-', '_');
            return Character.isDigit(c.charAt(0)) ? "_" + c : c;
        }
    }

    static String fmt(double d) {
        if (d == Math.rint(d)) {
            return String.valueOf((long) d);
        }
        return String.format(Locale.ROOT, "%.3f", d).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
