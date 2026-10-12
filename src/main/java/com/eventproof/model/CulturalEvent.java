package com.eventproof.model;

import java.time.LocalDate;
import java.util.List;

public final class CulturalEvent extends Event {
    public CulturalEvent(String name, LocalDate date, int attendance, Venue venue) {
        this(0, name, date, attendance, venue, List.of());
    }

    public CulturalEvent(long id, String name, LocalDate date, int attendance,
                      Venue venue, List<Incident> incidents) {
        super(id, name, date, attendance, venue, incidents);
    }

    @Override public EventType getEventType() { return EventType.CULTURAL; }
    @Override public String getEventCategory() { return "Cultural"; }
}
