package com.eventproof.database;

import com.eventproof.dao.EventDAO;
import com.eventproof.exception.DatabaseException;
import com.eventproof.service.ReliabilityService;
import com.eventproof.util.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/** Opt-in creation and sample-data setup. No passwords are stored or printed. */
public final class DemoDatabaseSetup {
    private DemoDatabaseSetup() { }

    public static void main(String[] args) {
        try {
            runSetup();
        } finally {
            // This standalone CLI owns its driver lifecycle; stop the daemon before Maven exits.
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        }
    }

    private static void runSetup() {
        try (var connection = DatabaseConnection.getServerConnection();
             var statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE IF NOT EXISTS eventproof CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (SQLException exception) {
            throw new DatabaseException("create the eventproof database", exception);
        }
        DatabaseSchemaSetup.createTables(DatabaseConnection::getConnection);
        boolean inserted = seedIfEmpty(DatabaseConnection::getConnection);
        System.out.println(inserted ? "Sample data inserted." : "Existing records preserved; sample insertion skipped.");
        var reliability = new ReliabilityService();
        for (var event : new EventDAO().getAll()) {
            var result = reliability.assess(event);
            System.out.printf("%s | %s | %s | attendance %d | incidents %d | %d/100 %s%n",
                    event.getEventName(), event.getEventCategory(), event.getVenue().getVenueName(),
                    event.getExpectedAttendance(), event.getIncidents().size(), result.score(), result.riskLevel());
        }
        try (var connection = DatabaseConnection.getConnection()) {
            System.out.printf("Verified database: %s | venues=%d | events=%d | incidents=%d%n",
                    connection.getCatalog(), count(connection, "venues"), count(connection, "events"),
                    count(connection, "incidents"));
        } catch (SQLException exception) {
            throw new DatabaseException("verify sample database", exception);
        }
    }

    public static boolean seedIfEmpty(ConnectionProvider connections) {
        try (var input = Resources.url("database/sample-data.sql").openStream()) {
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8).replaceAll("(?m)^--.*$", "");
            try (var connection = connections.getConnection()) {
                acquireSeedLock(connection);
                try {
                    connection.setAutoCommit(false);
                    try {
                        if (count(connection, "venues") + count(connection, "events") + count(connection, "incidents") > 0) {
                            connection.rollback();
                            return false;
                        }
                        try (var statement = connection.createStatement()) {
                            for (String command : sql.split(";")) {
                                if (!command.isBlank()) statement.execute(command.strip());
                            }
                        }
                        connection.commit();
                        return true;
                    } catch (SQLException | RuntimeException exception) {
                        try { connection.rollback(); }
                        catch (SQLException rollbackFailure) { exception.addSuppressed(rollbackFailure); }
                        throw exception;
                    }
                } finally {
                    // Release is best-effort: closing this session also releases the advisory lock.
                    try (var statement = connection.createStatement()) {
                        statement.execute("SELECT RELEASE_LOCK('eventproof-demo-seed')");
                    } catch (SQLException ignored) { }
                }
            }
        } catch (IOException | SQLException exception) {
            throw new DatabaseException("insert sample database records", exception);
        }
    }

    private static void acquireSeedLock(Connection connection) throws SQLException {
        try (var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT GET_LOCK('eventproof-demo-seed', 10)")) {
            if (!result.next() || result.getInt(1) != 1) throw new SQLException("Sample setup is already running.");
        }
    }

    private static long count(Connection connection, String table) throws SQLException {
        // Table names come exclusively from the fixed internal schema, never from user input.
        try (var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            if (!result.next()) throw new SQLException("Could not read table count.");
            return result.getLong(1);
        }
    }
}
