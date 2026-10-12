package com.eventproof.dao;

import com.eventproof.database.*;
import com.eventproof.exception.*;
import com.eventproof.model.Venue;
import java.sql.*;
import java.util.*;

public final class VenueDAO extends JdbcDAO<Venue> {
    public VenueDAO() { this(DatabaseConnection::getConnection); }
    public VenueDAO(ConnectionProvider connections) { super(connections); }

    @Override public Venue add(Venue venue) {
        Objects.requireNonNull(venue, "Venue is required.");
        if (venue.getVenueId() != 0) throw new InvalidVenueException("A new venue must have ID zero.");
        return withConnection("add venue", connection -> venue.withId(insert(connection,
                "INSERT INTO venues (venue_name, capacity, backup_power, backup_internet) VALUES (?, ?, ?, ?)",
                statement -> bind(statement, venue))));
    }

    @Override public Optional<Venue> findById(long id) {
        requireId(id);
        return withConnection("find venue", connection -> {
            try (var statement = connection.prepareStatement("SELECT * FROM venues WHERE venue_id = ?")) {
                statement.setLong(1, id);
                try (var rows = statement.executeQuery()) {
                    return rows.next() ? Optional.of(map(rows)) : Optional.empty();
                }
            }
        });
    }

    @Override public List<Venue> getAll() {
        return withConnection("list venues", connection -> {
            var venues = new ArrayList<Venue>();
            try (var statement = connection.prepareStatement("SELECT * FROM venues ORDER BY venue_name, venue_id");
                 var rows = statement.executeQuery()) {
                while (rows.next()) venues.add(map(rows));
            }
            return List.copyOf(venues);
        });
    }

    @Override public Venue update(Venue venue) {
        Objects.requireNonNull(venue, "Venue is required.");
        requireId(venue.getVenueId());
        return withConnection("update venue", connection -> {
            int changed = execute(connection, "UPDATE venues SET venue_name=?, capacity=?, backup_power=?, backup_internet=? WHERE venue_id=?",
                    statement -> { bind(statement, venue); statement.setLong(5, venue.getVenueId()); });
            // MySQL can return zero for an unchanged row when useAffectedRows=true.
            if (changed == 0 && !exists(connection, "SELECT venue_id FROM venues WHERE venue_id=?", venue.getVenueId())) {
                throw new RecordNotFoundException("Venue", venue.getVenueId());
            }
            return venue;
        });
    }

    @Override public boolean delete(long id) {
        requireId(id);
        return withConnection("delete venue", connection ->
                execute(connection, "DELETE FROM venues WHERE venue_id=?", statement -> statement.setLong(1, id)) > 0);
    }

    static Venue map(ResultSet rows) throws SQLException {
        return new Venue(rows.getLong("venue_id"), rows.getString("venue_name"),
                rows.getInt("capacity"), rows.getBoolean("backup_power"), rows.getBoolean("backup_internet"));
    }

    private static void bind(PreparedStatement statement, Venue venue) throws SQLException {
        statement.setString(1, venue.getVenueName());
        statement.setInt(2, venue.getCapacity());
        statement.setBoolean(3, venue.hasBackupPower());
        statement.setBoolean(4, venue.hasBackupInternet());
    }
}
