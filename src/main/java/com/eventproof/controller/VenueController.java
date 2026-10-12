package com.eventproof.controller;

import com.eventproof.controller.form.VenueFormController;
import com.eventproof.model.*;
import com.eventproof.service.AppContext;
import com.eventproof.util.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public final class VenueController extends WorkspaceController {
    @FXML private TableView<Venue> table;
    @FXML private TextField search;
    @FXML private Button addButton, editButton, deleteButton;
    private final ObservableList<Venue> rows = FXCollections.observableArrayList();

    public VenueController(AppContext context) { super(context); }
    @FXML private void initialize() {
        TableSupport.prepare(table, "No venues yet. Add a venue to begin planning events.");
        TableSupport.column(table, "ID", Venue::getVenueId, 65);
        TableSupport.column(table, "Venue", Venue::getVenueName, 300);
        TableSupport.column(table, "Capacity", Venue::getCapacity, 130);
        TableSupport.column(table, "Backup power", v -> v.hasBackupPower() ? "Available" : "Not available", 160);
        TableSupport.column(table, "Backup internet", v -> v.hasBackupInternet() ? "Available" : "Not available", 160);
        var filtered = new FilteredList<>(rows);
        table.setItems(filtered);
        search.textProperty().addListener((a,b,text) -> filtered.setPredicate(venue ->
                venue.getVenueName().toLowerCase(java.util.Locale.ROOT).contains(text.strip().toLowerCase(java.util.Locale.ROOT))));
        addButton.disableProperty().bind(working);
        var noSelection = table.getSelectionModel().selectedItemProperty().isNull().or(working);
        editButton.disableProperty().bind(noSelection);
        deleteButton.disableProperty().bind(noSelection);
    }
    @Override protected void render(WorkspaceSnapshot data) { rows.setAll(data.venues()); }
    @FXML private void add() { edit(null); }
    @FXML private void editSelected() { edit(table.getSelectionModel().getSelectedItem()); }
    private void edit(Venue existing) {
        CrudDialogs.<Venue, VenueFormController>edit(table.getScene().getWindow(), existing == null ? "Add venue" : "Edit venue",
                "VenueForm", context, form -> form.configure(existing),
                venue -> existing == null ? context.venues().add(venue) : context.venues().update(venue), saved -> refresh());
    }
    @FXML private void deleteSelected() {
        var venue = table.getSelectionModel().getSelectedItem();
        if (venue == null) return;
        CrudDialogs.delete(table.getScene().getWindow(), "Delete " + venue.getVenueName() + "? Assigned venues must first be removed from their events.",
                () -> perform(() -> { context.venues().delete(venue.getVenueId()); return context.snapshot(); },
                        data -> { snapshot = data; render(data); status.setText("Venue deleted"); }));
    }
}
