package com.eventproof.controller;

import com.eventproof.model.*;
import com.eventproof.service.AppContext;
import com.eventproof.util.DisplayNames;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public final class ReliabilityController extends WorkspaceController {
    @FXML private ComboBox<Event> event;
    @FXML private AssessmentController assessmentController;

    public ReliabilityController(AppContext context) { super(context); }
    @FXML private void initialize() {
        event.setConverter(DisplayNames.converter(DisplayNames::event));
        event.disableProperty().bind(working);
        event.valueProperty().addListener((a,b,value) -> showSelected());
    }
    @Override protected void render(WorkspaceSnapshot data) {
        long selectedId = context.selectedEventId();
        event.getItems().setAll(data.events());
        event.setValue(data.events().stream().filter(e -> e.getEventId() == selectedId).findFirst()
                .orElse(data.events().isEmpty() ? null : data.events().getFirst()));
        showSelected();
    }
    private void showSelected() {
        var selected = event.getValue();
        if (selected == null) { assessmentController.clear(); return; }
        context.selectEvent(selected.getEventId());
        assessmentController.show(context.reliability().assess(selected),
                selected.getEventName() + " | " + selected.getVenue().getVenueName(), true);
    }
}
