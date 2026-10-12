package com.eventproof.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/** Opens a fresh caller-owned connection. The GUI does not require a running database. */
public final class DatabaseConnection {
    private DatabaseConnection() { }

    public static Connection getConnection() throws SQLException {
        String url = requiredEnvironment("DB_URL");
        return openConnection(url);
    }

    /** Explicit demo setup only: connect to the server before the database exists. */
    static Connection getServerConnection() throws SQLException {
        String url = requiredEnvironment("DB_URL");
        try {
            if (!url.startsWith("jdbc:mysql://")) throw new SQLException("DB_URL must be a jdbc:mysql:// URL.");
            var parsed = new URI(url.substring("jdbc:".length()));
            if (!"/eventproof".equals(parsed.getRawPath()) || parsed.getRawAuthority() == null
                    || parsed.getRawUserInfo() != null || parsed.getRawFragment() != null) {
                throw new SQLException("Demo setup requires DB_URL pointing to the eventproof database.");
            }
            String serverUrl = "jdbc:mysql://" + parsed.getRawAuthority() + "/"
                    + (parsed.getRawQuery() == null ? "" : "?" + parsed.getRawQuery());
            return openConnection(serverUrl);
        } catch (URISyntaxException exception) {
            throw new SQLException("DB_URL is not a valid MySQL URL.", exception);
        }
    }

    private static Connection openConnection(String url) throws SQLException {
        String user = requiredEnvironment("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        if (password == null) throw new SQLException("Set the DB_PASSWORD environment variable (empty is allowed for local development).");
        if (!url.startsWith("jdbc:mysql://")) throw new SQLException("DB_URL must be a jdbc:mysql:// URL.");
        var properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        properties.setProperty("connectTimeout", "5000");
        properties.setProperty("socketTimeout", "5000");
        return DriverManager.getConnection(url, properties);
    }

    private static String requiredEnvironment(String name) throws SQLException {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new SQLException("Set the " + name + " environment variable. See README.md.");
        return value;
    }
}
