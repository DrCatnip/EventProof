package com.eventproof.exception;

/** Invalid event input. */
public final class InvalidEventException extends IllegalArgumentException {
    public InvalidEventException(String message) { super(message); }
}
