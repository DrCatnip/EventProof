package com.eventproof;

import com.eventproof.dao.CrudDAO;
import com.eventproof.model.*;
import com.eventproof.service.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.*;

public final class TestWorkspace {
    private TestWorkspace() { }
    public static AppContext context() {
        var venue = new Venue(1, "Fixture Auditorium", 1000, true, true);
        var incident = new Incident(1, 1, IncidentType.EQUIPMENT_FAILURE, "Fixture issue", LocalDate.now());
        Event event = new TechnicalEvent(1, "Fixture Tech Fest", LocalDate.now().plusDays(3), 900, venue, List.of(incident));
        return new AppContext(new EventService(new MemoryDAO<>(List.of(event), Event::getEventId, Event::withId)),
                new VenueService(new MemoryDAO<>(List.of(venue), Venue::getVenueId, Venue::withId)),
                new IncidentService(new MemoryDAO<>(List.of(incident), Incident::getIncidentId, Incident::withId)));
    }

    private static final class MemoryDAO<T> implements CrudDAO<T> {
        private final Map<Long, T> rows = new LinkedHashMap<>();
        private final ToLongFunction<T> id;
        private final BiFunction<T, Long, T> assignId;
        private long nextId = 100;
        private MemoryDAO(List<T> initial, ToLongFunction<T> id, BiFunction<T, Long, T> assignId) {
            this.id = id; this.assignId = assignId;
            initial.forEach(item -> rows.put(id.applyAsLong(item), item));
        }
        @Override public T add(T item) { T saved = assignId.apply(item, nextId++); rows.put(id.applyAsLong(saved), saved); return saved; }
        @Override public Optional<T> findById(long key) { return Optional.ofNullable(rows.get(key)); }
        @Override public List<T> getAll() { return List.copyOf(rows.values()); }
        @Override public T update(T item) { rows.put(id.applyAsLong(item), item); return item; }
        @Override public boolean delete(long key) { return rows.remove(key) != null; }
    }
}
