package com.eventproof.service;

import com.eventproof.exception.InvalidEventException;
import com.eventproof.model.*;
import java.util.ArrayList;
import java.util.Objects;

/** Temporary operational deductions layered onto Stage 2 readiness. No persistence dependencies. */
public final class SimulationService {
    private final ReliabilityService reliability;
    public SimulationService(ReliabilityService reliability) { this.reliability = Objects.requireNonNull(reliability); }

    public SimulationResult simulate(Event event, FailureScenario scenario) {
        if (event == null || scenario == null) throw new InvalidEventException("Select an event and a failure scenario.");
        var baseline = reliability.assess(event);
        int penalty = switch (scenario) {
            case POWER_FAILURE -> event.getVenue().hasBackupPower() ? 15 : 30;
            case INTERNET_FAILURE -> event.getVenue().hasBackupInternet() ? 8
                    : switch (event.getEventType()) { case TECHNICAL -> 20; case SPORTS -> 5; case CULTURAL -> 12; };
            case EQUIPMENT_FAILURE -> 20;
            case OVERCROWDING -> 25;
        };
        String explanation = switch (scenario) {
            case POWER_FAILURE -> event.getVenue().hasBackupPower()
                    ? "Backup supply mitigates disruption, but recovery still costs readiness."
                    : "Power loss without a backup supply has a larger operational impact.";
            case INTERNET_FAILURE -> event.getVenue().hasBackupInternet()
                    ? "Backup connectivity mitigates the simulated outage."
                    : "No backup connection; impact depends on the event category.";
            case EQUIPMENT_FAILURE -> "Critical equipment is assumed unavailable during event operations.";
            case OVERCROWDING -> "A temporary crowd surge is assumed; staffing and access controls are under pressure.";
        };
        var factors = new ArrayList<>(baseline.factors());
        factors.add(new ScoreFactor("Simulated " + scenario.name().toLowerCase(java.util.Locale.ROOT).replace('_', ' '),
                penalty, explanation));
        int score = Math.max(0, baseline.score() - penalty);
        var simulated = new ReliabilityAssessment(score, RiskLevel.fromScore(score), factors);
        return new SimulationResult(scenario, baseline, simulated);
    }
}
