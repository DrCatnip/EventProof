package com.eventproof.model;

public record ScoreFactor(String name, int penalty, String explanation) {
    public ScoreFactor {
        if (name == null || name.isBlank() || explanation == null || explanation.isBlank() || penalty < 0) {
            throw new IllegalArgumentException("A score factor requires a name, explanation and non-negative penalty.");
        }
    }
}
