package com.eventproof.service;

import com.eventproof.exception.InvalidEventException;
import com.eventproof.model.*;
import com.eventproof.strategy.*;
import java.util.Map;

/** Strategy selection is independent of JavaFX and persistence. */
public final class ReliabilityService {
    private final Map<EventType, ReliabilityStrategy> strategies;

    public ReliabilityService() {
        this(Map.of(EventType.TECHNICAL, new TechnicalReliabilityStrategy(),
                EventType.SPORTS, new SportsReliabilityStrategy(),
                EventType.CULTURAL, new CulturalReliabilityStrategy()));
    }

    public ReliabilityService(Map<EventType, ReliabilityStrategy> strategies) {
        if (strategies == null || java.util.Arrays.stream(EventType.values())
                .anyMatch(type -> strategies.get(type) == null)) {
            throw new IllegalArgumentException("Provide a reliability strategy for every event type.");
        }
        this.strategies = Map.copyOf(strategies);
    }

    public ReliabilityAssessment assess(Event event) {
        if (event == null) throw new InvalidEventException("Select an event to assess.");
        return strategies.get(event.getEventType()).assess(event);
    }

    public int calculateScore(Event event) { return assess(event).score(); }
}
