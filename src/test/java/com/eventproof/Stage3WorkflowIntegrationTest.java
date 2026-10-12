package com.eventproof;

import com.eventproof.controller.form.*;
import com.eventproof.model.*;
import com.eventproof.service.*;
import com.eventproof.util.*;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Real FXML form values through services/JDBC, reloads, score changes and simulation isolation. */
@EnabledIfEnvironmentVariable(named = "EVENTPROOF_DB_TESTS", matches = "true")
class Stage3WorkflowIntegrationTest {
    @BeforeAll static void toolkit() throws Exception { FxTestSupport.start(); }
    @Test void formToDatabaseWorkflowAndTransientSimulation() throws Exception {
        try (var context = new AppContext()) {
            String name = "Stage3 verification " + UUID.randomUUID();
            long venueId = 0, eventId = 0;
            try {
                Venue input = FxTestSupport.run(() -> {
                    var loader = Views.loader("VenueForm", context); Parent form = Views.load(loader);
                    ((VenueFormController) loader.getController()).configure(null);
                    ((TextField) form.lookup("#name")).setText(name);
                    ((TextField) form.lookup("#capacity")).setText("100");
                    ((CheckBox) form.lookup("#power")).setSelected(true);
                    ((CheckBox) form.lookup("#internet")).setSelected(true);
                    return ((VenueFormController) loader.getController()).readValue();
                });
                var venue = context.venues().add(input); venueId = venue.getVenueId();
                Event eventInput = FxTestSupport.run(() -> {
                    var loader = Views.loader("EventForm", context); Parent form = Views.load(loader);
                    var controller = (EventFormController) loader.getController(); controller.configure(null, List.of(venue));
                    ((TextField) form.lookup("#name")).setText(name);
                    ((TextField) form.lookup("#attendance")).setText("50");
                    ((DatePicker) form.lookup("#date")).setValue(LocalDate.now().plusDays(2));
                    combo(form, "#venue").setValue(venue);
                    return controller.readValue();
                });
                var event = context.events().add(eventInput); eventId = event.getEventId();
                assertEquals(100, context.reliability().calculateScore(context.events().findById(eventId)));
                Incident incidentInput = FxTestSupport.run(() -> {
                    var loader = Views.loader("IncidentForm", context); Parent form = Views.load(loader);
                    var controller = (IncidentFormController) loader.getController(); controller.configure(null, List.of(event));
                    combo(form, "#event").setValue(event);
                    ((TextArea) form.lookup("#description")).setText("Temporary verification incident");
                    combo(form, "#type").setValue(IncidentType.EQUIPMENT_FAILURE);
                    return controller.readValue();
                });
                var incident = context.incidents().add(incidentInput);
                var reloaded = context.events().findById(eventId);
                assertEquals(95, context.reliability().calculateScore(reloaded));
                var simulation = new SimulationService(context.reliability()).simulate(reloaded, FailureScenario.POWER_FAILURE);
                assertEquals(80, simulation.simulated().score());
                assertEquals(95, context.reliability().calculateScore(context.events().findById(eventId)));
                assertEquals(1, context.events().findById(eventId).getIncidents().size());
                Event edited = FxTestSupport.run(() -> {
                    var loader = Views.loader("EventForm", context); Parent form = Views.load(loader);
                    var controller = (EventFormController) loader.getController(); controller.configure(reloaded, List.of(venue));
                    ((TextField) form.lookup("#attendance")).setText("120");
                    return controller.readValue();
                });
                context.events().update(edited);
                assertEquals(70, context.reliability().calculateScore(context.events().findById(eventId)));
                context.incidents().delete(incident.getIncidentId());
                assertEquals(75, context.reliability().calculateScore(context.events().findById(eventId)));
                final long savedId = eventId;
                assertTrue(context.snapshot().events().stream().anyMatch(e -> e.getEventId() == savedId));
            } finally {
                if (eventId > 0) context.events().delete(eventId);
                if (venueId > 0) context.venues().delete(venueId);
            }
        }
    }
    @SuppressWarnings("unchecked")
    private static ComboBox<Object> combo(Parent form, String selector) { return (ComboBox<Object>) form.lookup(selector); }
}
