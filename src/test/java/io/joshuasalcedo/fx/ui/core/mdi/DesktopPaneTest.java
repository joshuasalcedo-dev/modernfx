package io.joshuasalcedo.fx.ui.core.mdi;

import static org.assertj.core.api.Assertions.assertThat;

import io.joshuasalcedo.fx.utils.theme.ThemeMfx;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

@ExtendWith(ApplicationExtension.class)
class DesktopPaneTest {

    private DesktopPane desktop;
    private InternalWindow early;

    @Start
    void start(Stage stage) {
        desktop = new DesktopPane();
        early = new InternalWindow("early", new Label("i"), "Early", new Label("added before the stage shows"));
        early.setPrefSize(300, 200);
        desktop.addInternalWindow(early, new Point2D(24, 24));
        Scene scene = new Scene(new BorderPane(desktop), 800, 600);
        ThemeMfx.install(scene);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void windowsAddedBeforeAndAfterShowingAreOnTheDesktop() {
        WaitForAsyncUtils.waitForFxEvents();
        System.out.println("early: " + early.getBoundsInParent() + " visible=" + early.isVisible()
                + " parent=" + early.getParent() + " scale=" + early.getScaleX());
        assertThat(early.getWidth()).isEqualTo(300);
        assertThat(early.getLayoutX()).isEqualTo(24);
        assertThat(early.isVisible()).isTrue();

        InternalWindow[] made = new InternalWindow[1];
        WaitForAsyncUtils.asyncFx(() -> {
            made[0] = new InternalWindow("late", new Label("i"), "Late", new Label("added after"));
            made[0].setPrefSize(320, 220);
            desktop.addInternalWindow(made[0], new Point2D(80, 80));
        });
        WaitForAsyncUtils.waitForFxEvents();
        InternalWindow late = made[0];
        System.out.println("late: " + late.getBoundsInParent() + " visible=" + late.isVisible());
        assertThat(late.getWidth()).isEqualTo(320);
        assertThat(late.getLayoutX()).isEqualTo(80);
        assertThat(desktop.getTaskBar()).isNotNull();
    }
}
