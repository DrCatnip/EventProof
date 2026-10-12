package com.eventproof.exception;

/** Invalid incident input. */
public final class InvalidIncidentException extends IllegalArgumentException {
    public InvalidIncidentException(String message) { super(message); }
}
