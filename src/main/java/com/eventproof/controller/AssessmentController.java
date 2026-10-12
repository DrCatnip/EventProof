package com.eventproof.controller;

import com.eventproof.model.ReliabilityAssessment;
import com.eventproof.util.*;
import javafx.animation.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import java.io.IOException;
import java.io.UncheckedIOException;

public final class AssessmentController {
    @FXML private Label score, risk, summary;
    @FXML private Circle ring;
    @FXML private VBox factors;
    private final SimpleDoubleProperty displayed = new SimpleDoubleProperty();
    private Timeline animation;

    @FXML private void initialize() {
        displayed.addListener((a,b,value) -> {
            score.setText(Integer.toString((int) Math.round(value.doubleValue())));
            double perimeter = 2 * Math.PI * ring.getRadius();
            ring.getStrokeDashArray().setAll(perimeter * value.doubleValue() / 100, perimeter);
        });
        clear();
    }

    public void clear() {
        if (animation != null) animation.stop();
        displayed.set(0);
        score.setText("--");
        ring.getStrokeDashArray().setAll(0.0, 2 * Math.PI * ring.getRadius());
        TableSupport.setRisk(risk, null);
        summary.setText("Select an event to assess its readiness.");
        factors.getChildren().clear();
    }

    public void show(ReliabilityAssessment assessment, String caption, boolean animate) {
        if (animation != null) animation.stop();
        summary.setText(caption);
        TableSupport.setRisk(risk, assessment.riskLevel());
        factors.getChildren().clear();
        for (var factor : assessment.factors()) {
            var loader = new FXMLLoader(Resources.url("fxml/FactorRow.fxml"));
            try { factors.getChildren().add(loader.load()); }
            catch (IOException exception) { throw new UncheckedIOException(exception); }
            ((FactorController) loader.getController()).setFactor(factor);
        }
        if (animate) {
            score.setText(Integer.toString((int) Math.round(displayed.get())));
            animation = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(displayed, displayed.get())),
                    new KeyFrame(Duration.millis(650), new KeyValue(displayed, assessment.score(), Interpolator.EASE_BOTH)));
            animation.setOnFinished(event -> score.setText(Integer.toString(assessment.score())));
            animation.play();
            var fade = new FadeTransition(Duration.millis(220), risk);
            fade.setFromValue(0.35); fade.setToValue(1); fade.play();
        } else {
            displayed.set(assessment.score());
            score.setText(Integer.toString(assessment.score()));
        }
    }
}
