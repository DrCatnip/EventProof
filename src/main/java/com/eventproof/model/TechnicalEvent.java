package com.eventproof.model;

import java.time.LocalDate;
import java.util.List;

public final class TechnicalEvent extends Event {
    public TechnicalEvent(String name, LocalDate date, int attendance, Venue venue) {
        this(0, name, date, attendance, venue, List.of());
    }

    public TechnicalEvent(long id, String name, LocalDate date, int attendance,
                      Venue venue, List<Incident> incidents) {
        super(id, name, date, attendance, venue, incidents);
    }

    @Override public EventType getEventType() { return EventType.TECHNICAL; }
    @Override public String getEventCategory() { return "Technical"; }
}
