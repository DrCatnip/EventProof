package com.eventproof.strategy;

import com.eventproof.model.EventType;

public final class SportsReliabilityStrategy extends PenaltyReliabilityStrategy {
    public SportsReliabilityStrategy() { super(EventType.SPORTS, 10, 0); }
}
