package com.eventproof.service;

import com.eventproof.model.WorkspaceSnapshot;
import javafx.concurrent.Task;
import java.util.concurrent.*;
import java.util.function.*;

/** Shared services and one worker queue; JDBC never blocks the JavaFX thread. */
public final class AppContext implements AutoCloseable {
    private final EventService events;
    private final VenueService venues;
    private final IncidentService incidents;
    private final ReliabilityService reliability = new ReliabilityService();
    private final ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
        var thread = new Thread(runnable, "eventproof-database");
        thread.setDaemon(true);
        return thread;
    });
    private Consumer<String> navigator = page -> { };
    private long selectedEventId;

    public AppContext() { this(new EventService(), new VenueService(), new IncidentService()); }
    public AppContext(EventService events, VenueService venues, IncidentService incidents) {
        this.events = java.util.Objects.requireNonNull(events);
        this.venues = java.util.Objects.requireNonNull(venues);
        this.incidents = java.util.Objects.requireNonNull(incidents);
    }

    public WorkspaceSnapshot snapshot() { return new WorkspaceSnapshot(events.getAll(), venues.getAll()); }
    public EventService events() { return events; }
    public VenueService venues() { return venues; }
    public IncidentService incidents() { return incidents; }
    public ReliabilityService reliability() { return reliability; }
    public void setNavigator(Consumer<String> navigator) { this.navigator = navigator; }
    public void navigate(String page) { navigator.accept(page); }
    public long selectedEventId() { return selectedEventId; }
    public void selectEvent(long id) { selectedEventId = id; }

    public <T> void submit(Supplier<T> operation, Consumer<T> success, Consumer<Throwable> failure) {
        var task = new Task<T>() { @Override protected T call() { return operation.get(); } };
        task.setOnSucceeded(event -> success.accept(task.getValue()));
        task.setOnFailed(event -> failure.accept(task.getException()));
        worker.execute(task);
    }

    @Override public void close() { worker.shutdownNow(); }
}
