package io.joshuasalcedo.fx.utils.theme;

import static org.assertj.core.api.Assertions.assertThat;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

@ExtendWith(ApplicationExtension.class)
class ThemeMfxTest {

    private Scene scene;
    private StackPane root;

    @Start
    void start(Stage stage) {
        root = new StackPane(new Button("Upload", new FontIcon(Feather.UPLOAD)));
        scene = new Scene(root, 200, 100);
        ThemeMfx.install(scene, ThemeMfx.Mode.LIGHT);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void installsTheStylesheetsInOrder() {
        assertThat(scene.getStylesheets()).hasSize(4);
        assertThat(scene.getStylesheets().get(0)).endsWith("tokens.css");
        assertThat(scene.getStylesheets().get(3)).endsWith("tailwind.css");
    }

    @Test
    void darkModePutsTheDarkClassOnTheRootAndBack() {
        WaitForAsyncUtils.asyncFx(() -> ThemeMfx.setMode(scene, ThemeMfx.Mode.DARK));
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(root.getStyleClass()).contains(ThemeMfx.DARK);
        assertThat(ThemeMfx.isDark(scene)).isTrue();

        WaitForAsyncUtils.asyncFx(() -> ThemeMfx.toggle(scene));
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(root.getStyleClass()).doesNotContain(ThemeMfx.DARK);
    }

    @Test
    void aNewRootKeepsTheMode() {
        WaitForAsyncUtils.asyncFx(() -> {
            ThemeMfx.setMode(scene, ThemeMfx.Mode.DARK);
            scene.setRoot(new StackPane());
        });
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(scene.getRoot().getStyleClass()).contains(ThemeMfx.DARK);
    }
}
