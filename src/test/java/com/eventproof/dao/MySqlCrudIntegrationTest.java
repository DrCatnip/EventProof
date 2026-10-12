package com.eventproof.dao;

import com.eventproof.database.*;
import com.eventproof.exception.*;
import com.eventproof.model.*;
import com.eventproof.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in live test. Creates unique rows and removes only those rows in finally. */
@EnabledIfEnvironmentVariable(named = "EVENTPROOF_DB_TESTS", matches = "true")
class MySqlCrudIntegrationTest {
    @Test void jdbcCrudRelationshipsAndSubtypeHydration() {
        DatabaseSchemaSetup.createTables(DatabaseConnection::getConnection);
        var venues = new VenueService();
        var events = new EventService();
        var incidents = new IncidentService();
        var incidentDao = new IncidentDAO();
        var savedEvents = new ArrayList<Long>();
        long venueId = 0;
        String suffix = UUID.randomUUID().toString();
        var date = LocalDate.of(2026, 10, 15);
        try {
            var venue = venues.add(new Venue("Test Hall " + suffix, 100, true, false));
            venueId = venue.getVenueId();
            final long savedVenueId = venueId;
            assertTrue(venueId > 0);
            assertEquals(100, venues.findById(venueId).getCapacity());
            assertTrue(venues.getAll().stream().anyMatch(v -> v.getVenueId() == savedVenueId));
            venue = venues.update(new Venue(venueId, "Updated Hall " + suffix, 200, true, false));
            assertEquals(200, venues.findById(venueId).getCapacity());
            venues.update(venue); // No-op updates are valid.
            for (EventType type : EventType.values()) {
                var event = events.add(EventFactory.create(type, 0, type + " " + suffix, date, 180, venue, List.of()));
                savedEvents.add(event.getEventId());
                assertEquals(type, events.findById(event.getEventId()).getEventType());
            }
            Event technical = events.findById(savedEvents.getFirst());
            assertInstanceOf(TechnicalEvent.class, technical);
            var incident = incidents.add(new Incident(technical.getEventId(), IncidentType.EQUIPMENT_FAILURE,
                    "Apostrophe ' and unicode: café - " + suffix, date));
            assertEquals(1, incidentDao.findByEventId(technical.getEventId()).size());
            technical = events.findById(technical.getEventId());
            assertEquals(1, technical.getIncidents().size());
            assertEquals(70, new ReliabilityService().calculateScore(technical));
            var edited = new Incident(incident.getIncidentId(), technical.getEventId(),
                    IncidentType.OTHER, "Edited issue", date.plusDays(1));
            incidents.update(edited);
            incidents.update(edited);
            assertEquals("Edited issue", incidents.findById(incident.getIncidentId()).getDescription());
            assertTrue(incidents.getAll().stream().anyMatch(i -> i.getIncidentId() == incident.getIncidentId()));
            events.update(new TechnicalEvent(technical.getEventId(), "Edited event", date.plusDays(2), 100, venue, technical.getIncidents()));
            assertEquals("Edited event", events.findById(technical.getEventId()).getEventName());
            assertTrue(events.getAll().stream().anyMatch(e -> e.getEventId() == savedEvents.getFirst()));
            long protectedVenue = venueId;
            assertThrows(DatabaseException.class, () -> venues.delete(protectedVenue));
            incidents.delete(incident.getIncidentId());
            assertThrows(RecordNotFoundException.class, () -> incidents.findById(incident.getIncidentId()));
            var cascade = incidents.add(new Incident(technical.getEventId(), IncidentType.POWER_FAILURE, "Cascade test", date));
            events.delete(technical.getEventId());
            assertThrows(RecordNotFoundException.class, () -> incidents.findById(cascade.getIncidentId()));
            assertThrows(RecordNotFoundException.class, () -> events.delete(savedEvents.getFirst()));
            final Venue selectedVenue = venue;
            assertThrows(RecordNotFoundException.class, () -> events.update(new TechnicalEvent(
                    savedEvents.getFirst(), "Deleted", date, 100, selectedVenue, List.of())));
            assertEquals(venueId, venues.findById(venueId).getVenueId());
        } finally {
            for (long eventId : savedEvents) new EventDAO().delete(eventId);
            if (venueId > 0) new VenueDAO().delete(venueId);
        }
        final long deletedVenueId = venueId;
        assertThrows(RecordNotFoundException.class, () -> venues.update(
                new Venue(deletedVenueId, "Missing", 100, true, true)));
    }

}
