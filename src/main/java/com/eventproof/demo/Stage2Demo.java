package com.eventproof.demo;

import com.eventproof.exception.InvalidVenueException;
import com.eventproof.model.*;
import com.eventproof.service.ReliabilityService;
import java.time.LocalDate;
import java.util.List;

/** In-memory Review 2 demonstration; never writes to MySQL. */
public final class Stage2Demo {
    private Stage2Demo() { }

    public static void main(String[] args) {
        var venue = new Venue(1, "Main Auditorium", 1000, true, false);
        var history = List.of(new Incident(1, 1, IncidentType.EQUIPMENT_FAILURE,
                "Microphone fault during rehearsal", LocalDate.of(2026, 10, 10)));
        List<Event> events = List.of(
                new TechnicalEvent(1, "Tech Fest", LocalDate.of(2026, 10, 15), 900, venue, history),
                new SportsEvent("Sports Meet", LocalDate.of(2026, 10, 18), 700, venue),
                new CulturalEvent("Cultural Fest", LocalDate.of(2026, 10, 22), 1100, venue));
        var reliability = new ReliabilityService();
        System.out.println("EVENTPROOF - Stage 2 Advanced OOP demo (in-memory sample data)");
        for (Event event : events) {
            var assessment = reliability.assess(event);
            System.out.printf("%n%s [%s]: %d / 100 - %s RISK%n",
                    event.getEventName(), event.getEventCategory(), assessment.score(), assessment.riskLevel());
            for (var factor : assessment.factors()) {
                System.out.printf("  %-22s -%2d  %s%n", factor.name(), factor.penalty(), factor.explanation());
            }
        }
        try {
            new Venue("Invalid sample", 0, false, false);
        } catch (InvalidVenueException exception) {
            System.out.println("\nValidation demonstration: " + exception.getMessage());
        }
    }
}
