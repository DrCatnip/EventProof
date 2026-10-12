package com.eventproof.controller.form;

import com.eventproof.exception.InvalidVenueException;
import com.eventproof.model.Venue;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public final class VenueFormController implements FormController<Venue> {
    @FXML private TextField name, capacity;
    @FXML private CheckBox power, internet;
    private Venue existing;

    public void configure(Venue existing) {
        this.existing = existing;
        if (existing != null) {
            name.setText(existing.getVenueName());
            capacity.setText(Integer.toString(existing.getCapacity()));
            power.setSelected(existing.hasBackupPower());
            internet.setSelected(existing.hasBackupInternet());
        }
    }

    @Override public Venue readValue() {
        int number;
        try { number = Integer.parseInt(capacity.getText().strip()); }
        catch (NumberFormatException exception) { throw new InvalidVenueException("Capacity must be a positive whole number."); }
        return new Venue(existing == null ? 0 : existing.getVenueId(), name.getText(), number,
                power.isSelected(), internet.isSelected());
    }
}
