package io.joshuasalcedo.fx.ui;


import java.util.Map;

public record Demo(
       Component component,
       String title,
       String description,
       Map<String,Object> variables
) {
}
