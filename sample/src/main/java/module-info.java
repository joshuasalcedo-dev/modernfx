/// The ModernFX sample: a small back office in windows inside a window.
module io.joshuasalcedo.fx.sample {
    requires io.joshuasalcedo.fx;
    requires org.kordamp.ikonli.feather;

    // JavaFX starts the app, so javafx.graphics needs to see the package.
    exports io.joshuasalcedo.fx.sample to javafx.graphics;
}
