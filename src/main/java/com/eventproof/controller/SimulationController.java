package com.eventproof.controller;

import com.eventproof.model.*;
import com.eventproof.service.*;
import com.eventproof.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public final class SimulationController extends WorkspaceController {
    @FXML private ComboBox<Event> event;
    @FXML private ComboBox<FailureScenario> scenario;
    @FXML private Button simulateButton, resetButton;
    @FXML private Label baseline, change;
    @FXML private AssessmentController assessmentController;
    private final SimulationService simulation;

    public SimulationController(AppContext context) {
        super(context);
        simulation = new SimulationService(context.reliability());
    }
    @FXML private void initialize() {
        event.setConverter(DisplayNames.converter(DisplayNames::event));
        scenario.setConverter(DisplayNames.converter(DisplayNames::label));
        scenario.getItems().setAll(FailureScenario.values());
        scenario.setValue(FailureScenario.POWER_FAILURE);
        event.disableProperty().bind(working);
        scenario.disableProperty().bind(working);
        simulateButton.disableProperty().bind(event.valueProperty().isNull().or(scenario.valueProperty().isNull()).or(working));
        resetButton.disableProperty().bind(event.valueProperty().isNull().or(working));
        event.valueProperty().addListener((a,b,c) -> reset());
        scenario.valueProperty().addListener((a,b,c) -> reset());
    }
    @Override protected void render(WorkspaceSnapshot data) {
        long selectedId = context.selectedEventId();
        event.getItems().setAll(data.events());
        event.setValue(data.events().stream().filter(e -> e.getEventId() == selectedId).findFirst()
                .orElse(data.events().isEmpty() ? null : data.events().getFirst()));
        reset();
    }
    @FXML private void reset() {
        var selected = event.getValue();
        if (selected == null) {
            baseline.setText("-- / 100"); change.setText("Select an event to start.");
            assessmentController.clear();
            return;
        }
        context.selectEvent(selected.getEventId());
        var result = context.reliability().assess(selected);
        baseline.setText(result.score() + " / 100");
        change.setText("Baseline restored. No simulation is applied.");
        assessmentController.show(result, selected.getEventName() + " | stored readiness", false);
    }
    @FXML private void simulate() {
        try {
            var result = simulation.simulate(event.getValue(), scenario.getValue());
            change.setText(DisplayNames.label(result.scenario()) + ": " + result.baseline().score()
                    + " to " + result.simulated().score() + " (" + (result.simulated().score() - result.baseline().score()) + " points)");
            assessmentController.show(result.simulated(), event.getValue().getEventName() + " | temporary simulation", true);
            status.setText("Simulation complete. Database records are unchanged.");
        } catch (RuntimeException exception) {
            status.setText(UiErrors.message(exception));
        }
    }
}
