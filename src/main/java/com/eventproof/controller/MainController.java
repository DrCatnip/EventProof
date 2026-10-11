package com.eventproof.controller;

import com.eventproof.util.Resources;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class MainController {
    @FXML private StackPane contentArea;
    @FXML private ToggleGroup navigation;
    @FXML private ToggleButton dashboardButton;
    @FXML private Label breadcrumb;
    private Parent dashboard;
    private FadeTransition transition;

    @FXML private void initialize() {
        try {
            dashboard = new FXMLLoader(Resources.url("fxml/Dashboard.fxml")).load();
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not load the dashboard", exception);
        }
        dashboardButton.setSelected(true);
        showPage("Dashboard");
    }

    @FXML private void navigate(ActionEvent event) {
        var button = (ToggleButton) event.getSource();
        // A navigation group always has one selected page, including repeated clicks.
        button.setSelected(true);
        showPage(button.getText());
    }

    public void showPage(String page) {
        navigation.getToggles().stream()
                .filter(toggle -> ((ToggleButton) toggle).getText().equals(page))
                .findFirst().ifPresent(navigation::selectToggle);
        breadcrumb.setText("Workspace  /  " + page);
        Parent view;
        if ("Dashboard".equals(page)) {
            view = dashboard;
        } else {
            try {
                var loader = new FXMLLoader(Resources.url("fxml/Placeholder.fxml"));
                view = loader.load();
                ((PlaceholderController) loader.getController()).setModule(page);
            } catch (IOException exception) {
                throw new UncheckedIOException("Could not load module placeholder", exception);
            }
        }
        if (transition != null) transition.stop();
        contentArea.getChildren().setAll(view);
        transition = new FadeTransition(Duration.millis(190), view);
        transition.setFromValue(0.35);
        transition.setToValue(1);
        transition.play();
    }
}
