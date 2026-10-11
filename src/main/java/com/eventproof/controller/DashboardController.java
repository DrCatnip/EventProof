package com.eventproof.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Foundation view only: all displayed metrics and event rows are temporary demo data in FXML. */
public final class DashboardController {
    @FXML private VBox dashboardContent;

    @FXML private void initialize() {
        var fade = new FadeTransition(Duration.millis(360), dashboardContent);
        fade.setFromValue(0); fade.setToValue(1);
        var slide = new TranslateTransition(Duration.millis(360), dashboardContent);
        slide.setFromY(10); slide.setToY(0);
        new ParallelTransition(fade, slide).play();
    }
}
