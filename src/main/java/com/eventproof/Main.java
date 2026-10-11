package com.eventproof;

import atlantafx.base.theme.PrimerDark;
import com.eventproof.util.Resources;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Main extends Application {
    private static final System.Logger LOGGER = System.getLogger(Main.class.getName());
    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        var root = new FXMLLoader(Resources.url("fxml/Main.fxml")).load();
        var scene = new Scene((javafx.scene.Parent) root, 1280, 850);
        scene.getStylesheets().addAll(Resources.url("css/main.css").toExternalForm(),
                Resources.url("css/components.css").toExternalForm(),
                Resources.url("css/dashboard.css").toExternalForm());
        stage.setTitle("EventProof | Event Reliability Management");
        stage.setMinWidth(1060);
        stage.setMinHeight(740);
        stage.setScene(scene);
        stage.show();
        LOGGER.log(System.Logger.Level.INFO, "EventProof window opened successfully.");
    }

    public static void main(String[] args) { launch(args); }
}
