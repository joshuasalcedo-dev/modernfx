package io.joshuasalcedo.fx.ui;


import java.util.List;

public interface Component {
    Mode mode();
    void toggleMode();
    String name();
    List<Demo> demos();
    Size size();
    enum Mode{
        DARK,
        LIGHT
    }
    enum Size{
        EXTRA_SMALL,
        SMALL,
        MEDIUM,
        NORMAL,
        LARGE,
        EXTRA_LARGE
    }
}
