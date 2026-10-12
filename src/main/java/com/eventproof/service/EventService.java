package com.eventproof.service;

import com.eventproof.dao.EventDAO;
import com.eventproof.dao.CrudDAO;
import com.eventproof.exception.RecordNotFoundException;
import com.eventproof.model.Event;
import java.util.List;
import java.util.Objects;

/** Application-facing CRUD boundary; model constructors enforce domain invariants. */
public final class EventService {
    private final CrudDAO<Event> dao;

    public EventService() { this(new EventDAO()); }
    public EventService(CrudDAO<Event> dao) { this.dao = Objects.requireNonNull(dao); }
    public Event add(Event entity) { return dao.add(Objects.requireNonNull(entity)); }
    public List<Event> getAll() { return dao.getAll(); }
    public Event findById(long id) {
        return dao.findById(id).orElseThrow(() -> new RecordNotFoundException("Event", id));
    }
    public Event update(Event entity) { return dao.update(Objects.requireNonNull(entity)); }
    public void delete(long id) {
        if (!dao.delete(id)) throw new RecordNotFoundException("Event", id);
    }
}
