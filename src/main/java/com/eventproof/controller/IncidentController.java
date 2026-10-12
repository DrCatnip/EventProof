package com.eventproof.controller;

import com.eventproof.controller.form.IncidentFormController;
import com.eventproof.model.*;
import com.eventproof.service.AppContext;
import com.eventproof.util.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Map;
import java.util.stream.Collectors;

public final class IncidentController extends WorkspaceController {
    @FXML private TableView<Incident> table;
    @FXML private TextField search;
    @FXML private Label details;
    @FXML private Button addButton, editButton, deleteButton;
    private final ObservableList<Incident> rows = FXCollections.observableArrayList();
    private Map<Long, String> names = Map.of();

    public IncidentController(AppContext context) { super(context); }
    @FXML private void initialize() {
        TableSupport.prepare(table, "No incidents recorded. Add an incident when an event encounters a problem.");
        TableSupport.column(table, "ID", Incident::getIncidentId, 60);
        TableSupport.column(table, "Event", i -> names.getOrDefault(i.getEventId(), "Event #" + i.getEventId()), 190);
        TableSupport.column(table, "Type", i -> DisplayNames.label(i.getIncidentType()), 180);
        TableSupport.column(table, "Description", Incident::getDescription, 310);
        TableSupport.column(table, "Date", Incident::getIncidentDate, 110);
        var filtered = new FilteredList<>(rows);
        table.setItems(filtered);
        search.textProperty().addListener((a,b,text) -> filtered.setPredicate(incident ->
                (names.getOrDefault(incident.getEventId(), "") + " " + incident.getDescription() + " "
                        + DisplayNames.label(incident.getIncidentType())).toLowerCase(java.util.Locale.ROOT)
                        .contains(text.strip().toLowerCase(java.util.Locale.ROOT))));
        table.getSelectionModel().selectedItemProperty().addListener((a,b,incident) ->
                details.setText(incident == null ? "Select an incident to read its full description." : incident.getDescription()));
        addButton.disableProperty().bind(working);
        var noSelection = table.getSelectionModel().selectedItemProperty().isNull().or(working);
        editButton.disableProperty().bind(noSelection);
        deleteButton.disableProperty().bind(noSelection);
    }
    @Override protected void render(WorkspaceSnapshot data) {
        names = data.events().stream().collect(Collectors.toMap(Event::getEventId, Event::getEventName));
        rows.setAll(data.incidents());
        table.refresh();
    }
    @FXML private void add() { edit(null); }
    @FXML private void editSelected() { edit(table.getSelectionModel().getSelectedItem()); }
    private void edit(Incident existing) {
        if (snapshot == null || snapshot.events().isEmpty()) {
            status.setText("Create an event before recording an incident.");
            context.navigate("Events");
            return;
        }
        CrudDialogs.<Incident, IncidentFormController>edit(table.getScene().getWindow(), existing == null ? "Record incident" : "Edit incident",
                "IncidentForm", context, form -> form.configure(existing, snapshot.events()),
                incident -> existing == null ? context.incidents().add(incident) : context.incidents().update(incident), saved -> refresh());
    }
    @FXML private void deleteSelected() {
        var incident = table.getSelectionModel().getSelectedItem();
        if (incident == null) return;
        CrudDialogs.delete(table.getScene().getWindow(), "Delete this " + DisplayNames.label(incident.getIncidentType()) + " record?",
                () -> perform(() -> { context.incidents().delete(incident.getIncidentId()); return context.snapshot(); },
                        data -> { snapshot = data; render(data); status.setText("Incident deleted"); }));
    }
}
