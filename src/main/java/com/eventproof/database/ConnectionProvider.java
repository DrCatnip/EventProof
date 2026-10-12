package com.eventproof.database;

import java.sql.Connection;
import java.sql.SQLException;

/** Supplies a fresh caller-owned connection; permits isolated JDBC tests. */
@FunctionalInterface
public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
}
