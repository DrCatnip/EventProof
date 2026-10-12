package com.eventproof.dao;

import com.eventproof.database.ConnectionProvider;
import com.eventproof.exception.DatabaseException;
import java.sql.*;
import java.util.Objects;

abstract class JdbcDAO<T> implements CrudDAO<T> {
    private final ConnectionProvider connections;

    protected JdbcDAO(ConnectionProvider connections) {
        this.connections = Objects.requireNonNull(connections);
    }

    @FunctionalInterface protected interface SqlWork<R> { R run(Connection connection) throws SQLException; }
    @FunctionalInterface protected interface Binder { void bind(PreparedStatement statement) throws SQLException; }

    protected final <R> R withConnection(String operation, SqlWork<R> work) {
        try (var connection = connections.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            connection.setAutoCommit(false);
            try {
                R result = work.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException exception) {
                try { connection.rollback(); }
                catch (SQLException rollbackFailure) { exception.addSuppressed(rollbackFailure); }
                throw exception;
            }
        } catch (SQLException | IllegalArgumentException exception) {
            throw new DatabaseException(operation, exception);
        }
    }

    protected final long insert(Connection connection, String sql, Binder binder) throws SQLException {
        try (var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            binder.bind(statement);
            if (statement.executeUpdate() != 1) throw new SQLException("Insert did not affect one row.");
            try (var keys = statement.getGeneratedKeys()) {
                if (!keys.next() || keys.getLong(1) <= 0) throw new SQLException("Missing generated primary key.");
                return keys.getLong(1);
            }
        }
    }

    protected final int execute(Connection connection, String sql, Binder binder) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            return statement.executeUpdate();
        }
    }

    protected final boolean exists(Connection connection, String sql, long id) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (var rows = statement.executeQuery()) { return rows.next(); }
        }
    }

    protected final void requireId(long id) {
        if (id <= 0) throw new IllegalArgumentException("A persisted positive ID is required.");
    }
}
