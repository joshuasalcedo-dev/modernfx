package io.joshuasalcedo.fx.ui.core.internal;

import javafx.css.PseudoClass;

/// The pseudo-classes the action controls set, for the stylesheets to match: `:loading`, `:focus-visible` and
/// `:selected`.
public final class ButtonPseudoClasses {

    public static final PseudoClass LOADING = PseudoClass.getPseudoClass("loading");
    public static final PseudoClass FOCUS_VISIBLE = PseudoClass.getPseudoClass("focus-visible");
    public static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private ButtonPseudoClasses() {
    }
}
