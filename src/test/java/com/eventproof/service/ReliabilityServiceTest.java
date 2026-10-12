package com.eventproof.service;

import com.eventproof.exception.InvalidEventException;
import com.eventproof.model.*;
import com.eventproof.strategy.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ReliabilityServiceTest {
    private final LocalDate date = LocalDate.of(2026, 10, 15);
    private final ReliabilityService service = new ReliabilityService();

    @ParameterizedTest
    @CsvSource({"TECHNICAL,60", "SPORTS,90", "CULTURAL,75"})
    void strategiesUseCategorySpecificBackupWeights(EventType type, int expectedScore) {
        Event event = EventFactory.create(type, 0, "Test", date, 50,
                new Venue("Hall", 100, false, false), List.of());
        assertEquals(expectedScore, service.calculateScore(event));
    }

    @ParameterizedTest
    @CsvSource({"0,100", "89,100", "90,95", "100,95", "101,75"})
    void attendanceThresholdsAreExplicit(int attendance, int score) {
        var event = new TechnicalEvent("Fest", date, attendance, new Venue("Hall", 100, true, true));
        assertEquals(score, service.calculateScore(event));
    }

    @ParameterizedTest
    @CsvSource({"100,LOW", "80,LOW", "79,MEDIUM", "60,MEDIUM", "59,HIGH", "0,HIGH"})
    void riskThresholdsHaveNoGaps(int score, RiskLevel level) {
        assertEquals(level, RiskLevel.fromScore(score));
    }

    @Test void incidentDeductionIsCappedAndBreakdownMatchesScore() {
        var incidents = new ArrayList<Incident>();
        for (int i = 1; i <= 20; i++) incidents.add(new Incident(i, 1, IncidentType.OTHER, "Issue " + i, date));
        Event event = new TechnicalEvent(1, "Fest", date, 101,
                new Venue("Hall", 100, false, false), incidents);
        var result = service.assess(event);
        assertEquals(5, result.score());
        assertEquals(RiskLevel.HIGH, result.riskLevel());
        assertEquals(30, result.factors().get(3).penalty());
        assertEquals(100 - result.score(), result.factors().stream().mapToInt(ScoreFactor::penalty).sum());
        assertThrows(UnsupportedOperationException.class, () -> result.factors().clear());
    }

    @Test void injectedStrategyCanReplaceRulesAndClampAtZero() {
        var custom = new PenaltyReliabilityStrategy(EventType.TECHNICAL, 100, 100) { };
        var configured = new ReliabilityService(Map.of(EventType.TECHNICAL, custom,
                EventType.SPORTS, new SportsReliabilityStrategy(), EventType.CULTURAL, new CulturalReliabilityStrategy()));
        assertEquals(0, configured.calculateScore(new TechnicalEvent("Fest", date, 50,
                new Venue("Hall", 100, false, false))));
        assertThrows(IllegalArgumentException.class, () -> new ReliabilityService(Map.of()));
    }

    @Test void invalidInputsAndMismatchedStrategiesAreRejected() {
        assertThrows(InvalidEventException.class, () -> service.assess(null));
        assertThrows(InvalidEventException.class, () -> new TechnicalReliabilityStrategy()
                .calculateScore(new SportsEvent("Meet", date, 1, new Venue("Hall", 100, true, true))));
        assertThrows(IllegalArgumentException.class, () -> RiskLevel.fromScore(-1));
        assertThrows(IllegalArgumentException.class, () -> RiskLevel.fromScore(101));
        assertThrows(IllegalArgumentException.class, () -> new ReliabilityAssessment(80, RiskLevel.LOW, List.of()));
    }

    @Test void largeAttendanceCannotOverflowArithmetic() {
        assertEquals(95, service.calculateScore(new SportsEvent("Meet", date, Integer.MAX_VALUE,
                new Venue("Ground", Integer.MAX_VALUE, true, true))));
    }
}
