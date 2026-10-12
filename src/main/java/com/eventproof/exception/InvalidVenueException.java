package com.eventproof.exception;

/** Invalid venue input. */
public final class InvalidVenueException extends IllegalArgumentException {
    public InvalidVenueException(String message) { super(message); }
}
