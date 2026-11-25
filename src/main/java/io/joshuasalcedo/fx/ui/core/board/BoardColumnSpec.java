package io.joshuasalcedo.fx.ui.core.board;

import java.util.Objects;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/// A board column: its `key` (what the app stores, such as `todo`), its `title`, an optional color for the dot
/// beside the title (a token or palette color such as `-blue-500`, or `#0ea5e9`), and a WIP limit (0 for none:
/// over the limit the count turns red).
public record BoardColumnSpec(String key, String title, @Nullable String color, int wipLimit) {

    private static final Pattern COLOR = Pattern.compile("-[a-z][a-z0-9-]*|#[0-9a-fA-F]{3,8}");

    public BoardColumnSpec {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(title, "title");
        if (color != null && !COLOR.matcher(color).matches()) {
            throw new IllegalArgumentException("color is a token (-blue-500) or #hex: " + color);
        }
        if (wipLimit < 0) {
            throw new IllegalArgumentException("wipLimit < 0: " + wipLimit);
        }
    }

    /// A column with no color and no limit.
    public static BoardColumnSpec of(String key, String title) {
        return new BoardColumnSpec(key, title, null, 0);
    }

    public BoardColumnSpec withColor(@Nullable String value) {
        return new BoardColumnSpec(key, title, value, wipLimit);
    }

    public BoardColumnSpec withWipLimit(int value) {
        return new BoardColumnSpec(key, title, color, value);
    }
}
