package com.eventproof.database;

import java.sql.SQLException;

/** Opt-in diagnostic; never invoked by a JavaFX controller. */
public final class DatabaseConnectionCheck {
    private DatabaseConnectionCheck() { }
    public static void main(String[] args) throws SQLException {
        try (var connection = DatabaseConnection.getConnection()) {
            if (!connection.isValid(5)) throw new SQLException("MySQL connection validation failed.");
            System.out.println("MySQL connection verified. Database: " + connection.getCatalog());
        }
    }
}
