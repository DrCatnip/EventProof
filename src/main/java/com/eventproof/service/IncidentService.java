package com.eventproof.service;

import com.eventproof.dao.IncidentDAO;
import com.eventproof.dao.CrudDAO;
import com.eventproof.exception.RecordNotFoundException;
import com.eventproof.model.Incident;
import java.util.List;
import java.util.Objects;

/** Application-facing CRUD boundary; model constructors enforce domain invariants. */
public final class IncidentService {
    private final CrudDAO<Incident> dao;

    public IncidentService() { this(new IncidentDAO()); }
    public IncidentService(CrudDAO<Incident> dao) { this.dao = Objects.requireNonNull(dao); }
    public Incident add(Incident entity) { return dao.add(Objects.requireNonNull(entity)); }
    public List<Incident> getAll() { return dao.getAll(); }
    public Incident findById(long id) {
        return dao.findById(id).orElseThrow(() -> new RecordNotFoundException("Incident", id));
    }
    public Incident update(Incident entity) { return dao.update(Objects.requireNonNull(entity)); }
    public void delete(long id) {
        if (!dao.delete(id)) throw new RecordNotFoundException("Incident", id);
    }
}
