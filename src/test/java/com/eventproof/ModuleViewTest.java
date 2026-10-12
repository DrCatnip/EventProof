package com.eventproof;

import com.eventproof.controller.form.EventFormController;
import com.eventproof.exception.InvalidEventException;
import com.eventproof.model.Event;
import com.eventproof.util.Views;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class ModuleViewTest {
    @BeforeAll static void toolkit() throws Exception { FxTestSupport.start(); }

    @Test void simulationViewLoadsItsSelectorsAndAssessment() throws Exception {
        FxTestSupport.run(() -> {
            try (var context = TestWorkspace.context()) {
                Parent view = Views.load(Views.loader("Simulation", context));
                var stage = new Stage();
                try {
                    stage.setScene(new Scene(view, 1050, 700)); stage.show(); view.applyCss(); view.layout();
                    assertEquals(4, ((ComboBox<?>) view.lookup("#scenario")).getItems().size());
                    assertTrue(((Button) view.lookup("#simulateButton")).isDisabled());
                } finally { stage.close(); }
            }
            return null;
        });
    }

    @Test void reliabilityViewLoadsItsIncludedAssessmentAndFactors() throws Exception {
        FxTestSupport.run(() -> {
            try (var context = TestWorkspace.context()) {
                Parent view = Views.load(Views.loader("Reliability", context));
                var stage = new Stage();
                try { stage.setScene(new Scene(view, 1050, 700)); stage.show(); view.applyCss(); view.layout(); }
                finally { stage.close(); }
                var loader = Views.loader("AssessmentPane", context);
                Parent assessment = Views.load(loader);
                ((com.eventproof.controller.AssessmentController) loader.getController())
                        .show(context.reliability().assess(context.events().getAll().getFirst()), "Test", false);
                assertEquals("90", ((Label) assessment.lookup("#score")).getText());
                assertEquals(4, ((javafx.scene.layout.VBox) assessment.lookup("#factors")).getChildren().size());
            }
            return null;
        });
    }

    @Test void eventViewAndFormLoadAndValidate() throws Exception {
        FxTestSupport.run(() -> {
            try (var context = TestWorkspace.context()) {
                Parent view = Views.load(Views.loader("Events", context));
                var stage = new Stage();
                try {
                    stage.setScene(new Scene(view, 1050, 700));
                    stage.show();
                    view.applyCss(); view.layout();
                    assertEquals(8, ((TableView<?>) view.lookup("#table")).getColumns().size());
                } finally { stage.close(); }
                var loader = Views.loader("EventForm", context);
                Parent form = Views.load(loader);
                new Scene(form);
                EventFormController controller = loader.getController();
                controller.configure(null, context.venues().getAll());
                assertThrows(InvalidEventException.class, controller::readValue);
                ((TextField) form.lookup("#name")).setText("New workshop");
                ((ComboBox<?>) form.lookup("#venue")).getSelectionModel().selectFirst();
                Event event = controller.readValue();
                assertEquals("New workshop", event.getEventName());
                assertEquals(1, event.getVenue().getVenueId());
                ((TextField) form.lookup("#attendance")).setText("-1");
                assertThrows(InvalidEventException.class, controller::readValue);
            }
            return null;
        });
    }

    @Test void venueViewAndFormLoadAndRejectInvalidCapacity() throws Exception {
        FxTestSupport.run(() -> {
            try (var context = TestWorkspace.context()) {
                Parent view = Views.load(Views.loader("Venues", context));
                var stage = new Stage();
                try { stage.setScene(new Scene(view, 1050, 700)); stage.show(); view.applyCss(); view.layout(); }
                finally { stage.close(); }
                var loader = Views.loader("VenueForm", context);
                Parent form = Views.load(loader);
                new Scene(form);
                com.eventproof.controller.form.VenueFormController controller = loader.getController();
                controller.configure(null);
                ((TextField) form.lookup("#name")).setText("New venue");
                ((TextField) form.lookup("#capacity")).setText("0");
                assertThrows(com.eventproof.exception.InvalidVenueException.class, controller::readValue);
                ((TextField) form.lookup("#capacity")).setText("500");
                assertEquals(500, controller.readValue().getCapacity());
            }
            return null;
        });
    }

    @Test void incidentViewAndFormRequireAnEventAndDescription() throws Exception {
        FxTestSupport.run(() -> {
            try (var context = TestWorkspace.context()) {
                Parent view = Views.load(Views.loader("Incidents", context));
                var stage = new Stage();
                try { stage.setScene(new Scene(view, 1050, 700)); stage.show(); view.applyCss(); view.layout(); }
                finally { stage.close(); }
                var loader = Views.loader("IncidentForm", context);
                Parent form = Views.load(loader);
                new Scene(form);
                com.eventproof.controller.form.IncidentFormController controller = loader.getController();
                controller.configure(null, context.events().getAll());
                assertThrows(com.eventproof.exception.InvalidIncidentException.class, controller::readValue);
                ((ComboBox<?>) form.lookup("#event")).getSelectionModel().selectFirst();
                ((TextArea) form.lookup("#description")).setText("Power cut");
                assertEquals(1, controller.readValue().getEventId());
            }
            return null;
        });
    }
}
