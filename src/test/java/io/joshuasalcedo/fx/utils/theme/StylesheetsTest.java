package io.joshuasalcedo.fx.utils.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javafx.css.CssMetaData;
import javafx.css.CssParser;
import javafx.css.Declaration;
import javafx.css.Rule;
import javafx.css.Stylesheet;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kordamp.ikonli.javafx.FontIcon;

/// Every stylesheet parses without errors and only uses properties JavaFX knows.
class StylesheetsTest {

    /// Not in any CssMetaData list, but read by JavaFX itself (transitions, looked-up colors are checked apart).
    private static final Set<String> BUILT_IN = Set.of("transition", "transition-property", "transition-duration",
            "transition-timing-function", "transition-delay");

    @BeforeAll
    static void startJavaFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException alreadyStarted) {
            // started by another test
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"tokens.css", "tailwind.css", "shadcn.css", "components.css"})
    void parsesWithoutErrorsAndUsesKnownProperties(String file) throws Exception {
        CssParser.errorsProperty().clear();
        long start = System.nanoTime();
        Stylesheet sheet = new CssParser().parse(StylesheetsTest.class.getResource("/io/joshuasalcedo/fx/" + file));
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println(file + ": " + sheet.getRules().size() + " rules, parsed in " + ms + " ms");

        assertThat(new ArrayList<>(CssParser.errorsProperty())).as("parse errors in " + file).isEmpty();

        Set<String> known = knownProperties();
        Set<String> unknown = new TreeSet<>();
        for (Rule rule : sheet.getRules()) {
            for (Declaration d : rule.getDeclarations()) {
                String p = d.getProperty();
                boolean lookedUp = p.startsWith("-") && !p.startsWith("-fx-");
                if (!lookedUp && !known.contains(p) && !BUILT_IN.contains(p)) {
                    unknown.add(p);
                }
            }
        }
        assertThat(unknown).as("properties JavaFX doesn't know in " + file).isEmpty();
    }

    @Test
    void theRulesStayFastToParse() throws Exception {
        long start = System.nanoTime();
        for (String f : List.of("tokens.css", "tailwind.css", "shadcn.css", "components.css")) {
            new CssParser().parse(StylesheetsTest.class.getResource("/io/joshuasalcedo/fx/" + f));
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        assertThat(ms).as("parse time of all stylesheets").isLessThan(3_000);
    }

    private static Set<String> knownProperties() {
        Set<String> names = new TreeSet<>();
        List<List<CssMetaData<? extends javafx.css.Styleable, ?>>> lists = List.of(
                Button.getClassCssMetaData(), Label.getClassCssMetaData(), TextField.getClassCssMetaData(),
                TextArea.getClassCssMetaData(), ComboBox.getClassCssMetaData(), ProgressBar.getClassCssMetaData(),
                HBox.getClassCssMetaData(), GridPane.getClassCssMetaData(), FlowPane.getClassCssMetaData(),
                Text.getClassCssMetaData(), Rectangle.getClassCssMetaData(), Tooltip.getClassCssMetaData(),
                new FontIcon().getCssMetaData(),
                javafx.scene.control.skin.TextInputControlSkin.getClassCssMetaData(),
                javafx.scene.control.skin.ProgressBarSkin.getClassCssMetaData(),
                javafx.scene.control.skin.PaginationSkin.getClassCssMetaData(),
                javafx.scene.control.Pagination.getClassCssMetaData(),
                javafx.scene.control.skin.CellSkinBase.getClassCssMetaData());
        for (var list : lists) {
            for (var meta : list) {
                names.add(meta.getProperty());
                for (var sub : meta.getSubProperties() == null ? List.<CssMetaData<?, ?>>of() : meta.getSubProperties()) {
                    names.add(sub.getProperty());
                }
            }
        }
        return names;
    }
}
