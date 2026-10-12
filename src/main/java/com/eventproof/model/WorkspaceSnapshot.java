package com.eventproof.model;

import java.time.LocalDate;
import java.util.List;

public record WorkspaceSnapshot(List<Event> events, List<Venue> venues) {
    public WorkspaceSnapshot {
        events = List.copyOf(events);
        venues = List.copyOf(venues);
    }
    public List<Incident> incidents() { return events.stream().flatMap(event -> event.getIncidents().stream()).toList(); }
    public List<Event> upcoming(LocalDate today) {
        return events.stream().filter(event -> !event.getEventDate().isBefore(today))
                .sorted(java.util.Comparator.comparing(Event::getEventDate).thenComparingLong(Event::getEventId)).toList();
    }
}
