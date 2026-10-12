package com.eventproof.model;

import com.eventproof.exception.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ModelValidationTest {
    private final LocalDate date = LocalDate.of(2026, 10, 15);
    private final Venue venue = new Venue(1, " Hall ", 100, true, false);

    @Test void invalidVenuesAreRejected() {
        assertThrows(InvalidVenueException.class, () -> new Venue("Hall", 0, true, false));
        assertThrows(InvalidVenueException.class, () -> new Venue(" ", 100, true, false));
        assertThrows(InvalidVenueException.class, () -> new Venue(-1, "Hall", 100, true, false));
        assertThrows(InvalidVenueException.class, () -> new Venue("x".repeat(121), 100, true, false));
        assertEquals("Hall", venue.getVenueName());
    }

    @Test void eventValidationAllowsPastDatesAndOvercrowdingForAssessment() {
        assertThrows(InvalidEventException.class, () -> new TechnicalEvent("", date, 1, venue));
        assertThrows(InvalidEventException.class, () -> new TechnicalEvent("Fest", null, 1, venue));
        assertThrows(InvalidEventException.class, () -> new TechnicalEvent("Fest", date, -1, venue));
        assertThrows(InvalidEventException.class, () -> new TechnicalEvent("Fest", date, 1, null));
        assertEquals(0, new SportsEvent("Meet", date.minusYears(10), 0, venue).getExpectedAttendance());
        assertEquals(200, new CulturalEvent("Fest", date, 200, venue).getExpectedAttendance());
    }

    @Test void incidentsRequireValidEventTypeDescriptionAndDate() {
        assertThrows(InvalidIncidentException.class, () -> new Incident(0, IncidentType.OTHER, "Issue", date));
        assertThrows(InvalidIncidentException.class, () -> new Incident(1, null, "Issue", date));
        assertThrows(InvalidIncidentException.class, () -> new Incident(1, IncidentType.OTHER, " ", date));
        assertThrows(InvalidIncidentException.class, () -> new Incident(1, IncidentType.OTHER, "Issue", null));
        assertThrows(InvalidIncidentException.class, () -> new Incident(1, IncidentType.OTHER, "x".repeat(2001), date));
    }

    @Test void incidentHistoryIsDefensiveAndBelongsToOneEvent() {
        var incident = new Incident(1, 1, IncidentType.OTHER, "Issue", date);
        var source = new ArrayList<>(List.of(incident));
        Event event = new TechnicalEvent(1, "Fest", date, 90, venue, source);
        source.clear();
        assertEquals(1, event.getIncidents().size());
        assertThrows(UnsupportedOperationException.class, () -> event.getIncidents().clear());
        assertThrows(InvalidEventException.class, () -> new SportsEvent(2, "Meet", date, 10, venue, List.of(incident)));
        assertThrows(InvalidEventException.class, () -> new SportsEvent(1, "Meet", date, 10, venue, List.of(incident, incident)));
        assertThrows(InvalidEventException.class, () -> event.withId(2));
    }

    @Test void factoryAndIdAssignmentPreservePolymorphism() {
        Event event = new CulturalEvent("Fest", date, 1, venue);
        assertInstanceOf(CulturalEvent.class, event.withId(42));
        assertEquals("Cultural", event.getEventCategory());
        assertEquals(42, event.withId(42).getEventId());
        assertThrows(InvalidEventException.class, () -> EventFactory.create(null, 0, "Fest", date, 1, venue, List.of()));
    }
}
