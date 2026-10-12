package com.eventproof.exception;

/** Safe operation context for callers, with the original JDBC cause retained for diagnostics. */
public final class DatabaseException extends RuntimeException {
    public DatabaseException(String operation, Throwable cause) {
        super("Unable to " + operation + ". Check database configuration and constraints.", cause);
    }
}
