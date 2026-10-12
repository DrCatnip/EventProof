package com.eventproof.exception;

public final class RecordNotFoundException extends RuntimeException {
    public RecordNotFoundException(String entity, long id) {
        super(entity + " with ID " + id + " was not found.");
    }
}
