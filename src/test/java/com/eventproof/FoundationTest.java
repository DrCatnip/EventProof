package com.eventproof;

import atlantafx.base.theme.PrimerDark;
import com.eventproof.controller.MainController;
import com.eventproof.database.DatabaseConnection;
import com.eventproof.util.Resources;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class FoundationTest {
    @BeforeAll static void startToolkit() throws Exception {
        var ready = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); ready.countDown(); });
        assertTrue(ready.await(15, TimeUnit.SECONDS), "JavaFX toolkit must start");
    }

    @Test void windowResourcesAndNavigationWork() throws Exception {
        var task = new FutureTask<Void>(() -> {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            var loader = new FXMLLoader(Resources.url("fxml/Main.fxml"));
            Parent root = loader.load();
            var scene = new Scene(root, 1280, 850);
            for (String css : new String[]{"main", "components", "dashboard"}) {
                scene.getStylesheets().add(Resources.url("css/" + css + ".css").toExternalForm());
            }
            var stage = new Stage();
            try {
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();
                assertTrue(stage.isShowing());
                assertNotNull(root.lookup(".score"));
                for (var node : root.lookupAll(".nav-button")) {
                    var button = (ToggleButton) node;
                    button.fire();
                    root.applyCss();
                    if (!button.getText().equals("Dashboard")) {
                        assertEquals(button.getText() + " module coming soon",
                                ((Label) root.lookup("#moduleTitle")).getText());
                    }
                    assertTrue(button.isSelected());
                    button.fire();
                    assertTrue(button.isSelected(), "Clicking selected navigation keeps it selected");
                }
                ((MainController) loader.getController()).showPage("Dashboard");
                assertNotNull(root.lookup(".score"));
                root.lookup(".dashboard-scroll").setOpacity(1);
                root.lookup(".dashboard-content").setOpacity(1);
                root.lookup(".dashboard-content").setTranslateY(0);
                root.applyCss();
                root.layout();
                var snapshot = root.snapshot(null, null);
                var preview = new java.awt.image.BufferedImage((int) snapshot.getWidth(),
                        (int) snapshot.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < preview.getHeight(); y++) {
                    for (int x = 0; x < preview.getWidth(); x++) {
                        preview.setRGB(x, y, snapshot.getPixelReader().getArgb(x, y));
                    }
                }
                javax.imageio.ImageIO.write(preview, "png", new java.io.File("target/dashboard-preview.png"));
            } finally { stage.close(); }
            return null;
        });
        Platform.runLater(task);
        task.get(30, TimeUnit.SECONDS);
    }

    @Test void missingDatabaseConfigurationGivesActionableError() {
        if (System.getenv("DB_URL") == null || System.getenv("DB_URL").isBlank()) {
            var error = assertThrows(SQLException.class, DatabaseConnection::getConnection);
            assertTrue(error.getMessage().contains("DB_URL"));
        }
    }
}
