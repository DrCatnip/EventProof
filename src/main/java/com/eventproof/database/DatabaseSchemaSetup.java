package com.eventproof.database;

import com.eventproof.exception.DatabaseException;
import com.eventproof.util.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/** Explicit setup command; never runs automatically during application startup. */
public final class DatabaseSchemaSetup {
    private DatabaseSchemaSetup() { }

    public static void main(String[] args) {
        createTables(DatabaseConnection::getConnection);
        System.out.println("EventProof's three tables are ready. Existing tables were not migrated.");
    }

    public static void createTables(ConnectionProvider connections) {
        try (var input = Resources.url("database/schema.sql").openStream()) {
            // This controlled script contains simple CREATE statements only.
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("(?m)^--.*$", "");
            try (var connection = connections.getConnection(); var statement = connection.createStatement()) {
                for (String command : sql.split(";")) {
                    if (!command.isBlank()) statement.execute(command.strip());
                }
            }
        } catch (IOException | SQLException exception) {
            throw new DatabaseException("create EventProof tables", exception);
        }
    }
}
