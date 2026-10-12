package com.eventproof.model;

import java.util.List;

public record ReliabilityAssessment(int score, RiskLevel riskLevel, List<ScoreFactor> factors) {
    public ReliabilityAssessment {
        if (riskLevel == null || factors == null || factors.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Assessment requires risk and score factors.");
        }
        factors = List.copyOf(factors);
        long penalties = factors.stream().mapToLong(ScoreFactor::penalty).sum();
        if (score != Math.max(0L, 100L - penalties) || riskLevel != RiskLevel.fromScore(score)) {
            throw new IllegalArgumentException("Assessment score and risk must agree with its factors.");
        }
    }
}
