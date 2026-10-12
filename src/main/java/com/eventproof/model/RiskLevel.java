package com.eventproof.model;

public enum RiskLevel {
    LOW, MEDIUM, HIGH;

    public static RiskLevel fromScore(int score) {
        if (score < 0 || score > 100) throw new IllegalArgumentException("Score must be between 0 and 100.");
        return score >= 80 ? LOW : score >= 60 ? MEDIUM : HIGH;
    }
}
