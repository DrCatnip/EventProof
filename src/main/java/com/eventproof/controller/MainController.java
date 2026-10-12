package com.eventproof.controller;

import com.eventproof.service.AppContext;
import com.eventproof.util.Views;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;

public final class MainController {
    @FXML private StackPane contentArea;
    @FXML private ToggleGroup navigation;
    @FXML private Label breadcrumb;
    private final AppContext context;
    private final Map<String, Page> pages = new HashMap<>();
    private FadeTransition transition;
    private record Page(Parent root, WorkspaceController controller) { }

    public MainController() { this(new AppContext()); }
    public MainController(AppContext context) { this.context = context; }

    @FXML private void initialize() {
        context.setNavigator(this::showPage);
        showPage("Dashboard");
    }
    @FXML private void navigate(ActionEvent event) {
        var button = (ToggleButton) event.getSource();
        button.setSelected(true);
        showPage(button.getText());
    }
    public void showPage(String name) {
        navigation.getToggles().stream().filter(toggle -> ((ToggleButton) toggle).getText().equals(name))
                .findFirst().ifPresent(navigation::selectToggle);
        breadcrumb.setText("Workspace  /  " + name);
        Page page = pages.computeIfAbsent(name, selected -> {
            if ("Settings".equals(selected)) {
                var loader = Views.loader("Placeholder", context);
                Parent root = Views.load(loader);
                ((PlaceholderController) loader.getController()).setModule(selected);
                return new Page(root, null);
            }
            if (!java.util.Set.of("Dashboard", "Events", "Venues", "Incidents", "Reliability", "Simulation").contains(selected)) {
                throw new IllegalArgumentException("Unknown page: " + selected);
            }
            var loader = Views.loader(selected, context);
            Parent root = Views.load(loader);
            return new Page(root, loader.getController());
        });
        if (transition != null) transition.stop();
        contentArea.getChildren().setAll(page.root());
        transition = new FadeTransition(Duration.millis(180), page.root());
        transition.setFromValue(0.4); transition.setToValue(1); transition.play();
        if (page.controller() != null) page.controller().activate();
    }
}
