package io.joshuasalcedo.fx.ui.core.breadcrumb;

import atlantafx.base.controls.Breadcrumbs;
import javafx.scene.control.Hyperlink;
import org.jspecify.annotations.NullMarked;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/// shadcn's Breadcrumb: where you are, as links back up the path (AtlantaFX's Breadcrumbs). The last one is
/// the current page.
///
/// ```java
/// var crumbs = BreadcrumbMfx.of("Home", "Components", "Breadcrumb");
/// crumbs.setOnCrumbAction(e -> navigate(e.getSelectedCrumb().getValue()));
/// ```
@NullMarked
public class BreadcrumbMfx<T> extends Breadcrumbs<T> {

    /// The style class every BreadcrumbMfx has.
    public static final String STYLE_CLASS = "mfx-breadcrumb";

    public BreadcrumbMfx() {
        getStyleClass().add(STYLE_CLASS);
        setCrumbFactory(item -> {
            Hyperlink link = new Hyperlink(String.valueOf(item.getValue()));
            link.getStyleClass().add(item.isLast() ? "mfx-breadcrumb-page" : "mfx-breadcrumb-link");
            return link;
        });
        setDividerFactory(item -> {
            if (item == null || item.isLast()) {
                return null;
            }
            FontIcon chevron = new FontIcon(Feather.CHEVRON_RIGHT);
            chevron.getStyleClass().add("mfx-breadcrumb-separator");
            return chevron;
        });
    }

    /// A breadcrumb for this path, the last one selected.
    @SafeVarargs
    public static <T> BreadcrumbMfx<T> of(T... path) {
        BreadcrumbMfx<T> b = new BreadcrumbMfx<>();
        BreadCrumbItem<T> last = Breadcrumbs.buildTreeModel(path);
        b.setSelectedCrumb(last);
        return b;
    }
}
