package com.eventproof.service;

import com.eventproof.dao.CrudDAO;
import com.eventproof.exception.RecordNotFoundException;
import com.eventproof.model.Venue;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class CrudServiceTest {
    @Test void missingRecordsProduceApplicationExceptions() {
        CrudDAO<Venue> emptyDao = new CrudDAO<>() {
            @Override public Venue add(Venue venue) { throw new UnsupportedOperationException(); }
            @Override public Optional<Venue> findById(long id) { return Optional.empty(); }
            @Override public List<Venue> getAll() { return List.of(); }
            @Override public Venue update(Venue venue) { throw new RecordNotFoundException("Venue", venue.getVenueId()); }
            @Override public boolean delete(long id) { return false; }
        };
        var service = new VenueService(emptyDao);
        assertTrue(service.getAll().isEmpty());
        assertThrows(RecordNotFoundException.class, () -> service.findById(42));
        assertThrows(RecordNotFoundException.class, () -> service.delete(42));
        assertThrows(RecordNotFoundException.class, () -> service.update(new Venue(42, "Missing", 100, true, true)));
    }
}
