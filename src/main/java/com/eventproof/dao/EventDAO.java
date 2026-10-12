package com.eventproof.dao;

import com.eventproof.database.*;
import com.eventproof.exception.*;
import com.eventproof.model.*;
import java.sql.*;
import java.util.*;

/** Reads the venue and complete incident history when hydrating each event. */
public final class EventDAO extends JdbcDAO<Event> {
    private static final String SELECT = """
            SELECT e.event_id, e.event_name, e.event_type, e.event_date, e.expected_attendance,
                   v.venue_id, v.venue_name, v.capacity, v.backup_power, v.backup_internet
            FROM events e JOIN venues v ON v.venue_id=e.venue_id
            """;

    public EventDAO() { this(DatabaseConnection::getConnection); }
    public EventDAO(ConnectionProvider connections) { super(connections); }

    @Override public Event add(Event event) {
        validate(event);
        if (event.getEventId() != 0) throw new InvalidEventException("A new event must have ID zero.");
        return withConnection("add event", connection -> {
            long id = insert(connection,
                    "INSERT INTO events (event_name, event_type, event_date, expected_attendance, venue_id) VALUES (?, ?, ?, ?, ?)",
                    statement -> bind(statement, event));
            return read(connection, id).getFirst();
        });
    }

    @Override public Optional<Event> findById(long id) {
        requireId(id);
        return withConnection("find event", connection -> read(connection, id).stream().findFirst());
    }

    @Override public List<Event> getAll() {
        return withConnection("list events", connection -> read(connection, null));
    }

    private static List<Event> read(Connection connection, Long id) throws SQLException {
        // Two queries per list, rather than one incident query per event.
        var histories = new HashMap<Long, List<Incident>>();
        for (var incident : IncidentDAO.read(connection, id)) {
            histories.computeIfAbsent(incident.getEventId(), ignored -> new ArrayList<>()).add(incident);
        }
        var events = new ArrayList<Event>();
        String sql = SELECT + (id == null ? "" : " WHERE e.event_id=?") + " ORDER BY e.event_date, e.event_id";
        try (var statement = connection.prepareStatement(sql)) {
            if (id != null) statement.setLong(1, id);
            try (var rows = statement.executeQuery()) {
                while (rows.next()) {
                    long eventId = rows.getLong("event_id");
                    events.add(EventFactory.create(EventType.valueOf(rows.getString("event_type")),
                            eventId, rows.getString("event_name"), rows.getDate("event_date").toLocalDate(),
                            rows.getInt("expected_attendance"), VenueDAO.map(rows),
                            histories.getOrDefault(eventId, List.of())));
                }
            }
        }
        return List.copyOf(events);
    }

    @Override public Event update(Event event) {
        validate(event);
        requireId(event.getEventId());
        return withConnection("update event", connection -> {
            execute(connection, "UPDATE events SET event_name=?, event_type=?, event_date=?, expected_attendance=?, venue_id=? WHERE event_id=?",
                    statement -> { bind(statement, event); statement.setLong(6, event.getEventId()); });
            return read(connection, event.getEventId()).stream().findFirst()
                    .orElseThrow(() -> new RecordNotFoundException("Event", event.getEventId()));
        });
    }

    @Override public boolean delete(long id) {
        requireId(id);
        // Schema intentionally cascades this event's incidents, but never deletes its venue.
        return withConnection("delete event", connection ->
                execute(connection, "DELETE FROM events WHERE event_id=?", statement -> statement.setLong(1, id)) > 0);
    }

    private static void validate(Event event) {
        Objects.requireNonNull(event, "Event is required.");
        if (event.getVenue().getVenueId() <= 0) {
            throw new InvalidEventException("Save the selected venue before saving an event.");
        }
    }

    private static void bind(PreparedStatement statement, Event event) throws SQLException {
        statement.setString(1, event.getEventName());
        statement.setString(2, event.getEventType().name());
        statement.setDate(3, java.sql.Date.valueOf(event.getEventDate()));
        statement.setInt(4, event.getExpectedAttendance());
        statement.setLong(5, event.getVenue().getVenueId());
    }
}
