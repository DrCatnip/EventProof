package com.eventproof.controller.form;

import com.eventproof.exception.InvalidEventException;
import com.eventproof.model.*;
import com.eventproof.util.DisplayNames;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;

public final class EventFormController implements FormController<Event> {
    @FXML private TextField name;
    @FXML private ComboBox<EventType> type;
    @FXML private DatePicker date;
    @FXML private TextField attendance;
    @FXML private ComboBox<Venue> venue;
    private Event existing;

    public void configure(Event existing, List<Venue> venues) {
        this.existing = existing;
        type.getItems().setAll(EventType.values());
        type.setConverter(DisplayNames.converter(DisplayNames::label));
        venue.getItems().setAll(venues);
        venue.setConverter(DisplayNames.converter(DisplayNames::venue));
        if (existing == null) {
            type.setValue(EventType.TECHNICAL);
            date.setValue(java.time.LocalDate.now());
            attendance.setText("0");
        } else {
            name.setText(existing.getEventName());
            type.setValue(existing.getEventType());
            date.setValue(existing.getEventDate());
            attendance.setText(Integer.toString(existing.getExpectedAttendance()));
            venues.stream().filter(v -> v.getVenueId() == existing.getVenue().getVenueId()).findFirst().ifPresent(venue::setValue);
        }
    }

    @Override public Event readValue() {
        int number;
        try { number = Integer.parseInt(attendance.getText().strip()); }
        catch (NumberFormatException exception) { throw new InvalidEventException("Attendance must be a whole number between 0 and 2147483647."); }
        return EventFactory.create(type.getValue(), existing == null ? 0 : existing.getEventId(),
                name.getText(), date.getValue(), number, venue.getValue(), existing == null ? List.of() : existing.getIncidents());
    }
}
