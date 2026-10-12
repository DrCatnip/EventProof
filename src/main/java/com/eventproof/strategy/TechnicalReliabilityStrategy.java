package com.eventproof.strategy;

import com.eventproof.model.EventType;

public final class TechnicalReliabilityStrategy extends PenaltyReliabilityStrategy {
    public TechnicalReliabilityStrategy() { super(EventType.TECHNICAL, 20, 20); }
}
