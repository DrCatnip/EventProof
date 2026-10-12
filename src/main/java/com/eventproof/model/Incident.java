package com.eventproof.model;

import com.eventproof.exception.InvalidIncidentException;
import com.eventproof.util.ValidationUtil;
import java.time.LocalDate;

public final class Incident {
    private final long incidentId;
    private final long eventId;
    private final IncidentType incidentType;
    private final String description;
    private final LocalDate incidentDate;

    public Incident(long eventId, IncidentType incidentType, String description, LocalDate incidentDate) {
        this(0, eventId, incidentType, description, incidentDate);
    }

    public Incident(long incidentId, long eventId, IncidentType incidentType,
                    String description, LocalDate incidentDate) {
        ValidationUtil.id(incidentId, true, "Incident ID", InvalidIncidentException::new);
        ValidationUtil.id(eventId, false, "Event ID", InvalidIncidentException::new);
        if (incidentType == null) throw new InvalidIncidentException("Incident type is required.");
        if (incidentDate == null) throw new InvalidIncidentException("Incident date is required.");
        this.description = ValidationUtil.text(description, "Incident description", 2000, InvalidIncidentException::new);
        this.incidentId = incidentId;
        this.eventId = eventId;
        this.incidentType = incidentType;
        this.incidentDate = incidentDate;
    }

    public long getIncidentId() { return incidentId; }
    public long getEventId() { return eventId; }
    public IncidentType getIncidentType() { return incidentType; }
    public String getDescription() { return description; }
    public LocalDate getIncidentDate() { return incidentDate; }

    public Incident withId(long id) {
        return new Incident(id, eventId, incidentType, description, incidentDate);
    }
}
