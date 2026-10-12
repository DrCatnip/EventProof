package com.eventproof.database;

import com.eventproof.exception.DatabaseException;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DemoDatabaseSetupTest {
    @Test void nonemptyDatabaseIsPreserved() {
        var jdbc = new SeedJdbc(true, false);
        assertFalse(DemoDatabaseSetup.seedIfEmpty(() -> jdbc.connection()));
        assertTrue(jdbc.commands.stream().noneMatch(sql -> sql.startsWith("INSERT")));
        assertFalse(jdbc.committed);
        assertTrue(jdbc.closed);
    }

    @Test void emptyDatabaseReceivesSamplesInOneTransaction() {
        var jdbc = new SeedJdbc(false, false);
        assertTrue(DemoDatabaseSetup.seedIfEmpty(() -> jdbc.connection()));
        assertTrue(jdbc.committed);
        assertFalse(jdbc.rolledBack);
        assertTrue(jdbc.commands.stream().anyMatch(sql -> sql.contains("Tech Fest")));
        assertTrue(jdbc.commands.stream().anyMatch(sql -> sql.contains("Sports Meet")));
        assertTrue(jdbc.commands.stream().anyMatch(sql -> sql.contains("Cultural Fest")));
        assertTrue(jdbc.closed);
    }

    @Test void failedSeedRollsBackAndReleasesResources() {
        var jdbc = new SeedJdbc(false, true);
        assertThrows(DatabaseException.class, () -> DemoDatabaseSetup.seedIfEmpty(() -> jdbc.connection()));
        assertTrue(jdbc.rolledBack);
        assertFalse(jdbc.committed);
        assertTrue(jdbc.commands.stream().anyMatch(sql -> sql.contains("RELEASE_LOCK")));
        assertTrue(jdbc.closed);
    }

    private static final class SeedJdbc {
        private final boolean populated;
        private final boolean failInsert;
        private final List<String> commands = new ArrayList<>();
        private boolean committed;
        private boolean rolledBack;
        private boolean closed;

        private SeedJdbc(boolean populated, boolean failInsert) {
            this.populated = populated;
            this.failInsert = failInsert;
        }

        private Connection connection() {
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{Connection.class}, (proxy, method, args) -> switch (method.getName()) {
                        case "createStatement" -> statement();
                        case "setAutoCommit" -> null;
                        case "commit" -> { committed = true; yield null; }
                        case "rollback" -> { rolledBack = true; yield null; }
                        case "close" -> { closed = true; yield null; }
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
        }

        private Statement statement() {
            return (Statement) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{Statement.class}, (proxy, method, args) -> switch (method.getName()) {
                        case "executeQuery" -> rows(((String) args[0]).contains("GET_LOCK") ? 1 : populated ? 3 : 0);
                        case "execute" -> {
                            String command = (String) args[0];
                            commands.add(command);
                            if (failInsert && command.startsWith("INSERT")) throw new SQLException("Seed failure");
                            yield false;
                        }
                        case "close" -> null;
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
        }

        private ResultSet rows(long value) {
            return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{ResultSet.class}, (proxy, method, args) -> switch (method.getName()) {
                        case "next" -> true;
                        case "getInt" -> (int) value;
                        case "getLong" -> value;
                        case "close" -> null;
                        default -> throw new UnsupportedOperationException(method.getName());
                    });
        }
    }
}
