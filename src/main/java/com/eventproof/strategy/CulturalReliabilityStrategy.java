package com.eventproof.strategy;

import com.eventproof.model.EventType;

public final class CulturalReliabilityStrategy extends PenaltyReliabilityStrategy {
    public CulturalReliabilityStrategy() { super(EventType.CULTURAL, 20, 5); }
}
