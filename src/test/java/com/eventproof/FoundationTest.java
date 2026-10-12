package com.eventproof;

import atlantafx.base.theme.PrimerDark;
import com.eventproof.controller.MainController;
import com.eventproof.database.DatabaseConnection;
import com.eventproof.util.*;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import static org.junit.jupiter.api.Assertions.*;

class FoundationTest {
    @BeforeAll static void startToolkit() throws Exception { FxTestSupport.start(); }

    @Test void venueDialogValidatesAndSavesAsynchronously() throws Exception {
        try (var context = TestWorkspace.context()) {
            var loader = Views.loader("Main", context);
            Stage stage = FxTestSupport.run(() -> {
                Parent root = Views.load(loader);
                var window = new Stage(); window.setScene(new Scene(root, 1280, 850)); window.show();
                ((MainController) loader.getController()).showPage("Venues"); return window;
            });
            try {
                Parent root = stage.getScene().getRoot();
                await(() -> !((Button) root.lookup("#addButton")).isDisabled());
                Parent dialog = FxTestSupport.run(() -> {
                    ((Button) root.lookup("#addButton")).fire();
                    return javafx.stage.Window.getWindows().stream().filter(w -> w != stage && w.isShowing())
                            .findFirst().orElseThrow().getScene().getRoot();
                });
                FxTestSupport.run(() -> {
                    ((Button) dialog.lookup("#saveButton")).fire();
                    assertFalse(((Label) dialog.lookup(".error-text")).getText().isBlank());
                    ((TextField) dialog.lookup("#name")).setText("GUI saved venue");
                    ((TextField) dialog.lookup("#capacity")).setText("500");
                    ((CheckBox) dialog.lookup("#power")).setSelected(true);
                    ((Button) dialog.lookup("#saveButton")).fire();
                    assertTrue(((Button) dialog.lookup("#saveButton")).isDisabled());
                    return null;
                });
                await(() -> ((TableView<?>) root.lookup("#table")).getItems().size() == 2);
                assertTrue(context.venues().getAll().stream().anyMatch(v -> v.getVenueName().equals("GUI saved venue") && v.getCapacity() == 500));
            } finally { FxTestSupport.run(() -> { stage.close(); return null; }); }
        }
    }

    @Test void liveNavigationFilteringSimulationAndResponsiveDashboard() throws Exception {
        try (var context = TestWorkspace.context()) {
            var loader = Views.loader("Main", context);
            Stage stage = FxTestSupport.run(() -> {
                Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                Parent root = Views.load(loader);
                var scene = new Scene(root, 1280, 850);
                for (String css : new String[]{"main", "components", "dashboard", "workspace"})
                    scene.getStylesheets().add(Resources.url("css/" + css + ".css").toExternalForm());
                var window = new Stage(); window.setScene(scene); window.show(); return window;
            });
            try {
                Parent root = stage.getScene().getRoot();
                await(() -> "1".equals(((Label) root.lookup("#eventCount")).getText()));
                FxTestSupport.run(() -> {
                    assertEquals("1", ((Label) root.lookup("#venueCount")).getText());
                    assertEquals("1", ((Label) root.lookup("#incidentCount")).getText());
                    return null;
                });
                for (String page : new String[]{"Events", "Venues", "Incidents", "Reliability", "Simulation", "Settings", "Dashboard"}) {
                    FxTestSupport.run(() -> {
                        ((MainController) loader.getController()).showPage(page);
                        var selected = (ToggleButton) root.lookupAll(".nav-button").stream()
                                .filter(node -> ((ToggleButton) node).getText().equals(page)).findFirst().orElseThrow();
                        assertTrue(selected.isSelected()); selected.fire(); assertTrue(selected.isSelected());
                        return null;
                    });
                    if (!page.equals("Settings")) await(() -> root.lookup("#status") != null &&
                            !((Label) root.lookup("#status")).getText().contains("Loading"));
                }
                await(() -> "90".equals(((Label) root.lookup("#score")).getText()));
                snapshot(stage, "dashboard-preview");
                FxTestSupport.run(() -> { stage.setWidth(1060); stage.setHeight(740); return null; });
                snapshot(stage, "dashboard-small-preview");
                FxTestSupport.run(() -> { ((MainController) loader.getController()).showPage("Events"); return null; });
                await(() -> ((TableView<?>) root.lookup("#table")).getItems().size() == 1);
                FxTestSupport.run(() -> {
                    var search = (TextField) root.lookup("#search"); search.setText("no matching event");
                    assertEquals(0, ((TableView<?>) root.lookup("#table")).getItems().size());
                    search.clear(); assertEquals(1, ((TableView<?>) root.lookup("#table")).getItems().size());
                    ((MainController) loader.getController()).showPage("Simulation"); return null;
                });
                await(() -> !((Button) root.lookup("#simulateButton")).isDisabled());
                FxTestSupport.run(() -> { ((Button) root.lookup("#simulateButton")).fire(); return null; });
                await(() -> "75".equals(((Label) root.lookup("#score")).getText()));
                FxTestSupport.run(() -> { assertTrue(((Label) root.lookup("#risk")).getText().contains("MEDIUM")); return null; });
                snapshot(stage, "simulation-preview");
                FxTestSupport.run(() -> { ((Button) root.lookup("#resetButton")).fire(); return null; });
                await(() -> "90".equals(((Label) root.lookup("#score")).getText()));
                assertEquals(90, context.reliability().assess(context.events().getAll().getFirst()).score());
            } finally { FxTestSupport.run(() -> { stage.close(); return null; }); }
        }
    }

    private static void await(Callable<Boolean> ready) throws Exception {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(10);
        while (!FxTestSupport.run(ready)) {
            if (System.nanoTime() > deadline) fail("View did not reach expected state");
            Thread.sleep(40);
        }
    }
    private static void snapshot(Stage stage, String name) throws Exception {
        Thread.sleep(250);
        FxTestSupport.run(() -> {
            var root = stage.getScene().getRoot(); root.applyCss(); root.layout();
            var pixels = root.snapshot(null, null);
            var preview = new java.awt.image.BufferedImage((int) pixels.getWidth(), (int) pixels.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < preview.getHeight(); y++) for (int x = 0; x < preview.getWidth(); x++)
                preview.setRGB(x, y, pixels.getPixelReader().getArgb(x,y));
            javax.imageio.ImageIO.write(preview, "png", new java.io.File("target/" + name + ".png")); return null;
        });
    }
    @Test void missingDatabaseConfigurationGivesActionableError() {
        if (System.getenv("DB_URL") == null || System.getenv("DB_URL").isBlank())
            assertTrue(assertThrows(SQLException.class, DatabaseConnection::getConnection).getMessage().contains("DB_URL"));
    }
}
