package com.eventproof.controller.form;

import com.eventproof.exception.InvalidIncidentException;
import com.eventproof.model.*;
import com.eventproof.util.DisplayNames;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;

public final class IncidentFormController implements FormController<Incident> {
    @FXML private ComboBox<Event> event;
    @FXML private ComboBox<IncidentType> type;
    @FXML private TextArea description;
    @FXML private DatePicker date;
    private Incident existing;

    public void configure(Incident existing, List<Event> events) {
        this.existing = existing;
        event.getItems().setAll(events);
        event.setConverter(DisplayNames.converter(DisplayNames::event));
        type.getItems().setAll(IncidentType.values());
        type.setConverter(DisplayNames.converter(DisplayNames::label));
        if (existing == null) {
            type.setValue(IncidentType.OTHER);
            date.setValue(java.time.LocalDate.now());
        } else {
            events.stream().filter(e -> e.getEventId() == existing.getEventId()).findFirst().ifPresent(event::setValue);
            type.setValue(existing.getIncidentType());
            description.setText(existing.getDescription());
            date.setValue(existing.getIncidentDate());
        }
    }

    @Override public Incident readValue() {
        if (event.getValue() == null) throw new InvalidIncidentException("Select an event for this incident.");
        return new Incident(existing == null ? 0 : existing.getIncidentId(), event.getValue().getEventId(),
                type.getValue(), description.getText(), date.getValue());
    }
}
