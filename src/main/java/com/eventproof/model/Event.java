package com.eventproof.model;

import com.eventproof.exception.InvalidEventException;
import com.eventproof.util.ValidationUtil;
import java.time.LocalDate;
import java.util.List;

/** Immutable aggregate: venue readiness plus incident history for one event. */
public abstract class Event {
    private final long eventId;
    private final String eventName;
    private final LocalDate eventDate;
    private final int expectedAttendance;
    private final Venue venue;
    private final List<Incident> incidents;

    protected Event(long eventId, String eventName, LocalDate eventDate, int expectedAttendance,
                    Venue venue, List<Incident> incidents) {
        ValidationUtil.id(eventId, true, "Event ID", InvalidEventException::new);
        this.eventName = ValidationUtil.text(eventName, "Event name", 120, InvalidEventException::new);
        if (eventDate == null) throw new InvalidEventException("Event date is required.");
        if (expectedAttendance < 0) throw new InvalidEventException("Expected attendance cannot be negative.");
        if (venue == null) throw new InvalidEventException("A venue is required.");
        if (incidents == null || incidents.stream().anyMatch(incident ->
                incident == null || eventId == 0 || incident.getEventId() != eventId)) {
            throw new InvalidEventException("Incident history must belong to this persisted event.");
        }
        if (incidents.stream().filter(incident -> incident.getIncidentId() > 0)
                .map(Incident::getIncidentId).distinct().count()
                != incidents.stream().filter(incident -> incident.getIncidentId() > 0).count()) {
            throw new InvalidEventException("Incident history cannot contain duplicate persisted incidents.");
        }
        this.eventId = eventId;
        this.eventDate = eventDate;
        this.expectedAttendance = expectedAttendance;
        this.venue = venue;
        this.incidents = List.copyOf(incidents);
    }

    public abstract EventType getEventType();
    public abstract String getEventCategory();
    public long getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public LocalDate getEventDate() { return eventDate; }
    public int getExpectedAttendance() { return expectedAttendance; }
    public Venue getVenue() { return venue; }
    public List<Incident> getIncidents() { return incidents; }

    public Event withId(long id) {
        if (!incidents.isEmpty() && id != eventId) {
            throw new InvalidEventException("An event with incident history cannot be reassigned a different ID.");
        }
        return EventFactory.create(getEventType(), id, eventName, eventDate, expectedAttendance, venue, incidents);
    }
}
