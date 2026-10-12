package com.eventproof.model;

public record SimulationResult(FailureScenario scenario, ReliabilityAssessment baseline,
                               ReliabilityAssessment simulated) {
    public SimulationResult {
        if (scenario == null || baseline == null || simulated == null || simulated.score() > baseline.score()) {
            throw new IllegalArgumentException("A failure simulation must have a scenario and cannot improve readiness.");
        }
    }
}
