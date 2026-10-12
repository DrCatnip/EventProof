package com.eventproof.util;

import com.eventproof.exception.DatabaseException;
import java.sql.SQLException;

public final class UiErrors {
    private UiErrors() { }
    public static String message(Throwable error) {
        if (error instanceof DatabaseException) {
            if (error.getCause() instanceof SQLException sql) {
                if (sql.getErrorCode() == 1451) return "This venue is assigned to events. Reassign those events before deleting it.";
                if (sql.getErrorCode() == 1452) return "The selected event or venue no longer exists. Refresh and try again.";
            }
            return "Database unavailable. Check MySQL and your DB settings, then refresh. You can start with scripts/Launch-EventProof.ps1.";
        }
        if (error instanceof IllegalArgumentException || error instanceof com.eventproof.exception.RecordNotFoundException) {
            return error.getMessage();
        }
        System.getLogger(UiErrors.class.getName()).log(System.Logger.Level.ERROR, "Unexpected application error", error);
        return "Something went wrong. Refresh and try again.";
    }
}
