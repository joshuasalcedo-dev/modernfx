package io.joshuasalcedo.fx.ui.core.table.renderer;

import io.joshuasalcedo.fx.ui.core.avatar.AvatarFallbackMfx;
import io.joshuasalcedo.fx.ui.core.avatar.AvatarImageMfx;
import io.joshuasalcedo.fx.ui.core.avatar.AvatarMfx;
import java.util.Locale;
import java.util.function.Function;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A person: a small avatar (the photo, or the initials until it loads or when there is none) and the name. The
/// column's value is the name; the photo's URL comes from the row.
///
/// ```java
/// new AvatarCellRenderer<Contact>(Contact::photoUrl);
/// ```
@NullMarked
public class AvatarCellRenderer<T> implements CellRenderer<T, String> {

    private final @Nullable Function<T, @Nullable String> imageUrl;

    /// Initials only.
    public AvatarCellRenderer() {
        this(null);
    }

    public AvatarCellRenderer(@Nullable Function<T, @Nullable String> imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String text(String name) {
        return name;
    }

    @Override
    public Node render(T row, String name) {
        Label label = new Label(name);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        label.setMinWidth(0);
        HBox box = new HBox(8, avatar(name, imageUrl == null ? null : imageUrl.apply(row)), label);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("mfx-avatar-cell");
        return box;
    }

    /// A small avatar: the image at `imageUrl` (loaded in the background) over the initials of `name`.
    public static AvatarMfx avatar(String name, @Nullable String imageUrl) {
        AvatarMfx avatar = new AvatarMfx(new AvatarFallbackMfx(initials(name))).size(AvatarMfx.Size.SM);
        if (imageUrl != null && !imageUrl.isBlank()) {
            Image image = new Image(imageUrl, 48, 48, true, true, true);
            AvatarImageMfx view = new AvatarImageMfx(image);
            view.setFitWidth(24);
            view.setFitHeight(24);
            view.visibleProperty().bind(image.progressProperty().isEqualTo(1).and(image.errorProperty().not()));
            avatar.getChildren().add(view);
        }
        return avatar;
    }

    /// Up to two initials: `Ana Ruiz` is `AR`, `ana` is `A`.
    public static String initials(String name) {
        StringBuilder out = new StringBuilder();
        for (String part : name.strip().split("\\s+")) {
            if (!part.isEmpty() && out.length() < 2) {
                out.appendCodePoint(part.codePointAt(0));
            }
        }
        return out.toString().toUpperCase(Locale.ROOT);
    }
}
