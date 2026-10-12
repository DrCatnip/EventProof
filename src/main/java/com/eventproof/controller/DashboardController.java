package com.eventproof.controller;

import com.eventproof.model.*;
import com.eventproof.service.AppContext;
import com.eventproof.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;

public final class DashboardController extends WorkspaceController {
    @FXML private Label eventCount, venueCount, incidentCount, average;
    @FXML private TableView<Event> upcoming;
    @FXML private Button assessButton;
    @FXML private AssessmentController assessmentController;

    public DashboardController(AppContext context) { super(context); }
    @FXML private void initialize() {
        TableSupport.prepare(upcoming, "No upcoming events. Add an event to start planning.");
        TableSupport.column(upcoming, "Upcoming event", Event::getEventName, 220);
        TableSupport.column(upcoming, "Date", Event::getEventDate, 105);
        TableSupport.column(upcoming, "Venue", e -> e.getVenue().getVenueName(), 220);
        TableSupport.column(upcoming, "Score", e -> context.reliability().calculateScore(e) + " / 100", 100);
        TableSupport.riskColumn(upcoming, e -> context.reliability().assess(e).riskLevel());
        assessButton.disableProperty().bind(upcoming.getSelectionModel().selectedItemProperty().isNull().or(working));
        upcoming.getSelectionModel().selectedItemProperty().addListener((a,b,selected) -> {
            if (selected == null) assessmentController.clear();
            else assessmentController.show(context.reliability().assess(selected),
                    selected.getEventName() + " | " + selected.getVenue().getVenueName(), false);
        });
    }
    @Override protected void render(WorkspaceSnapshot data) {
        eventCount.setText(Integer.toString(data.events().size()));
        venueCount.setText(Integer.toString(data.venues().size()));
        incidentCount.setText(Integer.toString(data.incidents().size()));
        if (data.events().isEmpty()) average.setText("No events assessed yet");
        else {
            int score = (int) Math.round(data.events().stream().mapToInt(context.reliability()::calculateScore).average().orElseThrow());
            average.setText("Portfolio average: " + score + " / 100 | " + RiskLevel.fromScore(score) + " RISK");
        }
        upcoming.getItems().setAll(data.upcoming(LocalDate.now()));
        upcoming.getSelectionModel().selectFirst();
        if (upcoming.getItems().isEmpty()) assessmentController.clear();
    }
    @FXML private void viewSelected() {
        var selected = upcoming.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        context.selectEvent(selected.getEventId()); context.navigate("Reliability");
    }
}
