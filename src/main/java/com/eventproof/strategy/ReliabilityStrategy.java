package com.eventproof.strategy;

import com.eventproof.model.Event;
import com.eventproof.model.ReliabilityAssessment;

public interface ReliabilityStrategy {
    ReliabilityAssessment assess(Event event);

    default int calculateScore(Event event) { return assess(event).score(); }
}
