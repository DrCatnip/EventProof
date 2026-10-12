package com.eventproof.model;

import com.eventproof.exception.InvalidVenueException;
import com.eventproof.util.ValidationUtil;

/** Immutable value; ID zero represents a venue not yet saved. */
public final class Venue {
    private final long venueId;
    private final String venueName;
    private final int capacity;
    private final boolean backupPower;
    private final boolean backupInternet;

    public Venue(String venueName, int capacity, boolean backupPower, boolean backupInternet) {
        this(0, venueName, capacity, backupPower, backupInternet);
    }

    public Venue(long venueId, String venueName, int capacity, boolean backupPower, boolean backupInternet) {
        ValidationUtil.id(venueId, true, "Venue ID", InvalidVenueException::new);
        this.venueName = ValidationUtil.text(venueName, "Venue name", 120, InvalidVenueException::new);
        if (capacity <= 0) throw new InvalidVenueException("Venue capacity must be greater than zero.");
        this.venueId = venueId;
        this.capacity = capacity;
        this.backupPower = backupPower;
        this.backupInternet = backupInternet;
    }

    public long getVenueId() { return venueId; }
    public String getVenueName() { return venueName; }
    public int getCapacity() { return capacity; }
    public boolean hasBackupPower() { return backupPower; }
    public boolean hasBackupInternet() { return backupInternet; }

    public Venue withId(long id) {
        return new Venue(id, venueName, capacity, backupPower, backupInternet);
    }
}
