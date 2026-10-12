package com.eventproof.util;

import java.util.function.Function;

public final class ValidationUtil {
    private ValidationUtil() { }

    public static String text(String value, String field, int maxLength,
                              Function<String, ? extends RuntimeException> exception) {
        if (value == null || value.isBlank()) throw exception.apply(field + " is required.");
        String normalized = value.strip();
        if (normalized.codePointCount(0, normalized.length()) > maxLength) {
            throw exception.apply(field + " must contain at most " + maxLength + " characters.");
        }
        return normalized;
    }

    public static void id(long value, boolean allowUnsaved, String field,
                          Function<String, ? extends RuntimeException> exception) {
        if (value < 0 || (!allowUnsaved && value == 0)) {
            throw exception.apply(field + (allowUnsaved ? " cannot be negative." : " must be a persisted positive ID."));
        }
    }
}
