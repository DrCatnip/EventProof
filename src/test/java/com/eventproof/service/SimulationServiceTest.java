package com.eventproof.service;

import com.eventproof.model.*;
import com.eventproof.exception.InvalidEventException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SimulationServiceTest {
    private final ReliabilityService reliability = new ReliabilityService();
    private final SimulationService simulation = new SimulationService(reliability);
    private Event event(boolean power, boolean internet) {
        return new TechnicalEvent(1, "Tech Fest", LocalDate.now(), 900,
                new Venue(1, "Hall", 1000, power, internet),
                List.of(new Incident(1, 1, IncidentType.OTHER, "History", LocalDate.now())));
    }

    @ParameterizedTest
    @CsvSource({"POWER_FAILURE,75", "INTERNET_FAILURE,82", "EQUIPMENT_FAILURE,70", "OVERCROWDING,65"})
    void scenariosHaveDocumentedEffects(FailureScenario scenario, int score) {
        var original = event(true, true);
        var result = simulation.simulate(original, scenario);
        assertEquals(90, result.baseline().score());
        assertEquals(score, result.simulated().score());
        assertEquals(5, result.simulated().factors().size());
        assertEquals(90, reliability.calculateScore(original));
        assertEquals(1, original.getIncidents().size());
        assertTrue(original.getVenue().hasBackupPower());
    }

    @Test void failuresDoNotAccumulateAndScoresClampAtZero() {
        var original = event(false, false);
        assertEquals(simulation.simulate(original, FailureScenario.EQUIPMENT_FAILURE),
                simulation.simulate(original, FailureScenario.EQUIPMENT_FAILURE));
        var crowded = new TechnicalEvent(1, "Tech", LocalDate.now(), 1001,
                new Venue(1, "Hall", 1000, false, false),
                java.util.stream.LongStream.rangeClosed(1, 6).mapToObj(id ->
                        new Incident(id, 1, IncidentType.OTHER, "Issue", LocalDate.now())).toList());
        assertEquals(0, simulation.simulate(crowded, FailureScenario.POWER_FAILURE).simulated().score());
        assertThrows(InvalidEventException.class, () -> simulation.simulate(original, null));
    }
}
