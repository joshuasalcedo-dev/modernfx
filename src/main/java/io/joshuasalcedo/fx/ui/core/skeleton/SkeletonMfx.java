// Made from shadcn's skeleton.tsx by tools/GenerateComponent.java. Ordinary code now: edit it.
package io.joshuasalcedo.fx.ui.core.skeleton;

import io.joshuasalcedo.fx.utils.tailwind.Tw;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NullMarked;

/// shadcn's `Skeleton` (`data-slot="skeleton"`, skeleton.tsx), as a JavaFX StackPane.
/// The look is in `components.css` (style class `mfx-skeleton`).
@NullMarked
public class SkeletonMfx extends StackPane {

    /// The style class every SkeletonMfx has.
    public static final String STYLE_CLASS = "mfx-skeleton";

    public SkeletonMfx(Node... children) {
        super(children);
        getStyleClass().add(STYLE_CLASS);
        Tw.apply(this, "animate-pulse rounded-md");
    }
}
