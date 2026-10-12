package com.eventproof.dao;

import com.eventproof.exception.*;
import com.eventproof.model.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class JdbcFailureTest {
    @Test void connectionFailuresAreWrappedWithOriginalCause() {
        var sqlError = new SQLException("Access denied", "28000", 1045);
        var dao = new VenueDAO(() -> { throw sqlError; });
        var error = assertThrows(DatabaseException.class, dao::getAll);
        assertSame(sqlError, error.getCause());
        assertTrue(error.getMessage().contains("list venues"));
        assertFalse(error.getMessage().contains("Access denied"));
    }

    @Test void failedSqlRollsBackAndClosesConnection() {
        var rolledBack = new AtomicBoolean();
        var closed = new AtomicBoolean();
        var failure = new SQLException("Statement failure");
        Connection connection = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "setAutoCommit", "setTransactionIsolation" -> null;
                    case "prepareStatement" -> throw failure;
                    case "rollback" -> { rolledBack.set(true); yield null; }
                    case "close" -> { closed.set(true); yield null; }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        var error = assertThrows(DatabaseException.class, () -> new VenueDAO(() -> connection).getAll());
        assertSame(failure, error.getCause());
        assertTrue(rolledBack.get());
        assertTrue(closed.get());
    }

    @Test void invalidPersistenceInputsNeverOpenConnection() {
        var opened = new AtomicBoolean();
        var dao = new EventDAO(() -> { opened.set(true); throw new SQLException(); });
        var event = new TechnicalEvent("Fest", LocalDate.of(2026, 10, 15), 50,
                new Venue("Unsaved Hall", 100, true, true));
        assertThrows(InvalidEventException.class, () -> dao.add(event));
        assertThrows(IllegalArgumentException.class, () -> dao.delete(0));
        assertFalse(opened.get());
    }

    @Test void insertBindsUserTextAndCommitsGeneratedIdWhileClosingResources() {
        var boundValues = new java.util.HashMap<Integer, Object>();
        var sql = new java.util.concurrent.atomic.AtomicReference<String>();
        var committed = new AtomicBoolean();
        var connectionClosed = new AtomicBoolean();
        var statementClosed = new AtomicBoolean();
        var keysClosed = new AtomicBoolean();
        var firstKey = new AtomicBoolean(true);
        ResultSet keys = (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{ResultSet.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> firstKey.getAndSet(false);
                    case "getLong" -> 17L;
                    case "close" -> { keysClosed.set(true); yield null; }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{PreparedStatement.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "setString", "setInt", "setBoolean" -> { boundValues.put((Integer) args[0], args[1]); yield null; }
                    case "executeUpdate" -> 1;
                    case "getGeneratedKeys" -> keys;
                    case "close" -> { statementClosed.set(true); yield null; }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        Connection connection = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "setAutoCommit", "setTransactionIsolation" -> null;
                    case "prepareStatement" -> { sql.set((String) args[0]); yield statement; }
                    case "commit" -> { committed.set(true); yield null; }
                    case "close" -> { connectionClosed.set(true); yield null; }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        String userText = "Hall'); DROP TABLE venues; --";
        var saved = new VenueDAO(() -> connection).add(new Venue(userText, 100, true, false));
        assertEquals(17, saved.getVenueId());
        assertFalse(sql.get().contains(userText));
        assertEquals(java.util.Map.of(1, userText, 2, 100, 3, true, 4, false), boundValues);
        assertTrue(committed.get());
        assertTrue(connectionClosed.get());
        assertTrue(statementClosed.get());
        assertTrue(keysClosed.get());
    }
}
