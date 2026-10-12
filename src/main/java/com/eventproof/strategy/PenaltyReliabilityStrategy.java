package com.eventproof.strategy;

import com.eventproof.exception.InvalidEventException;
import com.eventproof.model.*;
import java.util.List;

/** Shared rules avoid duplicating attendance, incident and clamping logic. */
public abstract class PenaltyReliabilityStrategy implements ReliabilityStrategy {
    private final EventType supportedType;
    private final int missingPowerPenalty;
    private final int missingInternetPenalty;

    protected PenaltyReliabilityStrategy(EventType supportedType, int powerPenalty, int internetPenalty) {
        if (supportedType == null || powerPenalty < 0 || powerPenalty > 100
                || internetPenalty < 0 || internetPenalty > 100) {
            throw new IllegalArgumentException("Strategy weights must be between 0 and 100 and specify an event type.");
        }
        this.supportedType = supportedType;
        this.missingPowerPenalty = powerPenalty;
        this.missingInternetPenalty = internetPenalty;
    }

    @Override public final ReliabilityAssessment assess(Event event) {
        if (event == null || event.getEventType() != supportedType) {
            throw new InvalidEventException("This strategy requires a " + supportedType + " event.");
        }
        var venue = event.getVenue();
        // Long arithmetic prevents overflow for very large attendance/capacity values.
        int attendancePenalty = event.getExpectedAttendance() > venue.getCapacity() ? 25
                : (long) event.getExpectedAttendance() * 100 >= (long) venue.getCapacity() * 90 ? 5 : 0;
        int powerPenalty = venue.hasBackupPower() ? 0 : missingPowerPenalty;
        int internetPenalty = venue.hasBackupInternet() ? 0 : missingInternetPenalty;
        int incidentPenalty = Math.min(event.getIncidents().size(), 6) * 5;
        var factors = List.of(
                new ScoreFactor("Attendance Capacity", attendancePenalty,
                        event.getExpectedAttendance() + " expected / " + venue.getCapacity() + " capacity"),
                new ScoreFactor("Backup Power", powerPenalty,
                        venue.hasBackupPower() ? "Backup power available" : "No backup power"),
                new ScoreFactor("Backup Internet", internetPenalty,
                        venue.hasBackupInternet() ? "Backup internet available" : "No backup internet"),
                new ScoreFactor("Incident History", incidentPenalty,
                        event.getIncidents().size() + " recorded incidents; deduction capped at 30"));
        int score = Math.max(0, 100 - factors.stream().mapToInt(ScoreFactor::penalty).sum());
        return new ReliabilityAssessment(score, RiskLevel.fromScore(score), factors);
    }
}
