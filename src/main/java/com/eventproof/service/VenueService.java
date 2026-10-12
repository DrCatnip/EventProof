package com.eventproof.service;

import com.eventproof.dao.VenueDAO;
import com.eventproof.dao.CrudDAO;
import com.eventproof.exception.RecordNotFoundException;
import com.eventproof.model.Venue;
import java.util.List;
import java.util.Objects;

/** Application-facing CRUD boundary; model constructors enforce domain invariants. */
public final class VenueService {
    private final CrudDAO<Venue> dao;

    public VenueService() { this(new VenueDAO()); }
    public VenueService(CrudDAO<Venue> dao) { this.dao = Objects.requireNonNull(dao); }
    public Venue add(Venue entity) { return dao.add(Objects.requireNonNull(entity)); }
    public List<Venue> getAll() { return dao.getAll(); }
    public Venue findById(long id) {
        return dao.findById(id).orElseThrow(() -> new RecordNotFoundException("Venue", id));
    }
    public Venue update(Venue entity) { return dao.update(Objects.requireNonNull(entity)); }
    public void delete(long id) {
        if (!dao.delete(id)) throw new RecordNotFoundException("Venue", id);
    }
}
