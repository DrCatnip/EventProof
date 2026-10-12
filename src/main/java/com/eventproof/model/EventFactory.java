package com.eventproof.model;

import com.eventproof.exception.InvalidEventException;
import java.time.LocalDate;
import java.util.List;

/** Restores the concrete subtype from the database discriminator. */
public final class EventFactory {
    private EventFactory() { }
    public static Event create(EventType type, long id, String name, LocalDate date,
                               int attendance, Venue venue, List<Incident> incidents) {
        if (type == null) throw new InvalidEventException("Event type is required.");
        return switch (type) {
            case TECHNICAL -> new TechnicalEvent(id, name, date, attendance, venue, incidents);
            case SPORTS -> new SportsEvent(id, name, date, attendance, venue, incidents);
            case CULTURAL -> new CulturalEvent(id, name, date, attendance, venue, incidents);
        };
    }
}
