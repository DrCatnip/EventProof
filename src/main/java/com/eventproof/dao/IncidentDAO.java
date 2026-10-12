package com.eventproof.dao;

import com.eventproof.database.*;
import com.eventproof.exception.*;
import com.eventproof.model.*;
import java.sql.*;
import java.util.*;

public final class IncidentDAO extends JdbcDAO<Incident> {
    public IncidentDAO() { this(DatabaseConnection::getConnection); }
    public IncidentDAO(ConnectionProvider connections) { super(connections); }

    @Override public Incident add(Incident incident) {
        Objects.requireNonNull(incident, "Incident is required.");
        if (incident.getIncidentId() != 0) throw new InvalidIncidentException("A new incident must have ID zero.");
        return withConnection("add incident", connection -> incident.withId(insert(connection,
                "INSERT INTO incidents (event_id, incident_type, description, incident_date) VALUES (?, ?, ?, ?)",
                statement -> bind(statement, incident))));
    }

    @Override public Optional<Incident> findById(long id) {
        requireId(id);
        return withConnection("find incident", connection -> {
            try (var statement = connection.prepareStatement("SELECT * FROM incidents WHERE incident_id=?")) {
                statement.setLong(1, id);
                try (var rows = statement.executeQuery()) {
                    return rows.next() ? Optional.of(map(rows)) : Optional.empty();
                }
            }
        });
    }

    @Override public List<Incident> getAll() {
        return withConnection("list incidents", connection -> read(connection, null));
    }

    public List<Incident> findByEventId(long eventId) {
        requireId(eventId);
        return withConnection("list event incidents", connection -> read(connection, eventId));
    }

    static List<Incident> read(Connection connection, Long eventId) throws SQLException {
        String sql = "SELECT * FROM incidents" + (eventId == null ? "" : " WHERE event_id=?")
                + " ORDER BY incident_date, incident_id";
        var incidents = new ArrayList<Incident>();
        try (var statement = connection.prepareStatement(sql)) {
            if (eventId != null) statement.setLong(1, eventId);
            try (var rows = statement.executeQuery()) {
                while (rows.next()) incidents.add(map(rows));
            }
        }
        return List.copyOf(incidents);
    }

    @Override public Incident update(Incident incident) {
        Objects.requireNonNull(incident, "Incident is required.");
        requireId(incident.getIncidentId());
        return withConnection("update incident", connection -> {
            int changed = execute(connection, "UPDATE incidents SET event_id=?, incident_type=?, description=?, incident_date=? WHERE incident_id=?",
                    statement -> { bind(statement, incident); statement.setLong(5, incident.getIncidentId()); });
            if (changed == 0 && !exists(connection, "SELECT incident_id FROM incidents WHERE incident_id=?", incident.getIncidentId())) {
                throw new RecordNotFoundException("Incident", incident.getIncidentId());
            }
            return incident;
        });
    }

    @Override public boolean delete(long id) {
        requireId(id);
        return withConnection("delete incident", connection ->
                execute(connection, "DELETE FROM incidents WHERE incident_id=?", statement -> statement.setLong(1, id)) > 0);
    }

    private static Incident map(ResultSet rows) throws SQLException {
        return new Incident(rows.getLong("incident_id"), rows.getLong("event_id"),
                IncidentType.valueOf(rows.getString("incident_type")), rows.getString("description"),
                rows.getDate("incident_date").toLocalDate());
    }

    private static void bind(PreparedStatement statement, Incident incident) throws SQLException {
        statement.setLong(1, incident.getEventId());
        statement.setString(2, incident.getIncidentType().name());
        statement.setString(3, incident.getDescription());
        statement.setDate(4, java.sql.Date.valueOf(incident.getIncidentDate()));
    }
}
