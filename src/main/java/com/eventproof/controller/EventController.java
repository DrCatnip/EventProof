package com.eventproof.controller;

import com.eventproof.controller.form.EventFormController;
import com.eventproof.model.*;
import com.eventproof.service.AppContext;
import com.eventproof.util.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Locale;

public final class EventController extends WorkspaceController {
    @FXML private TableView<Event> table;
    @FXML private TextField search;
    @FXML private ComboBox<String> filter;
    @FXML private Button addButton, editButton, deleteButton, assessButton;
    private final ObservableList<Event> rows = FXCollections.observableArrayList();

    public EventController(AppContext context) { super(context); }
    @FXML private void initialize() {
        TableSupport.prepare(table, "No events yet. Create a venue, then add your first event.");
        TableSupport.column(table, "ID", Event::getEventId, 50);
        TableSupport.column(table, "Event", Event::getEventName, 190);
        TableSupport.column(table, "Type", Event::getEventCategory, 95);
        TableSupport.column(table, "Date", Event::getEventDate, 105);
        TableSupport.column(table, "Attendance", Event::getExpectedAttendance, 90);
        TableSupport.column(table, "Venue", e -> e.getVenue().getVenueName(), 170);
        TableSupport.column(table, "Score", e -> context.reliability().calculateScore(e) + " / 100", 90);
        TableSupport.riskColumn(table, e -> context.reliability().assess(e).riskLevel());
        var filtered = new FilteredList<>(rows);
        table.setItems(filtered);
        filter.getItems().setAll("All types", "Technical", "Sports", "Cultural");
        filter.setValue("All types");
        Runnable apply = () -> filtered.setPredicate(event -> {
            String query = search.getText().strip().toLowerCase(Locale.ROOT);
            boolean match = (event.getEventName() + " " + event.getVenue().getVenueName() + " " + event.getEventId()).toLowerCase(Locale.ROOT).contains(query);
            return match && ("All types".equals(filter.getValue()) || event.getEventCategory().equals(filter.getValue()));
        });
        search.textProperty().addListener((a,b,c) -> apply.run());
        filter.valueProperty().addListener((a,b,c) -> apply.run());
        addButton.disableProperty().bind(working);
        var noSelection = table.getSelectionModel().selectedItemProperty().isNull().or(working);
        editButton.disableProperty().bind(noSelection);
        deleteButton.disableProperty().bind(noSelection);
        assessButton.disableProperty().bind(noSelection);
    }

    @Override protected void render(WorkspaceSnapshot data) {
        long id = table.getSelectionModel().getSelectedItem() == null ? 0 : table.getSelectionModel().getSelectedItem().getEventId();
        rows.setAll(data.events());
        rows.stream().filter(e -> e.getEventId() == id).findFirst().ifPresent(table.getSelectionModel()::select);
    }
    @FXML private void add() { edit(null); }
    @FXML private void editSelected() { edit(table.getSelectionModel().getSelectedItem()); }
    private void edit(Event existing) {
        if (snapshot == null || snapshot.venues().isEmpty()) {
            status.setText("Create a venue before adding an event.");
            context.navigate("Venues");
            return;
        }
        CrudDialogs.<Event, EventFormController>edit(table.getScene().getWindow(), existing == null ? "Add event" : "Edit event",
                "EventForm", context, form -> form.configure(existing, snapshot.venues()),
                event -> existing == null ? context.events().add(event) : context.events().update(event), saved -> refresh());
    }
    @FXML private void deleteSelected() {
        var event = table.getSelectionModel().getSelectedItem();
        if (event == null) return;
        CrudDialogs.delete(table.getScene().getWindow(), "Delete " + event.getEventName() + " and its " + event.getIncidents().size() + " incident(s)?",
                () -> perform(() -> { context.events().delete(event.getEventId()); return context.snapshot(); },
                        data -> { snapshot = data; render(data); status.setText("Event deleted"); }));
    }
    @FXML private void assessSelected() {
        var event = table.getSelectionModel().getSelectedItem();
        if (event == null) return;
        context.selectEvent(event.getEventId());
        context.navigate("Reliability");
    }
}
