package com.eventproof;

import atlantafx.base.theme.PrimerDark;
import com.eventproof.util.Resources;
import com.eventproof.util.Views;
import com.eventproof.service.AppContext;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Main extends Application {
    private static final System.Logger LOGGER = System.getLogger(Main.class.getName());
    private AppContext context;
    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        context = new AppContext();
        javafx.scene.Parent root = Views.load(Views.loader("Main", context));
        var scene = new Scene(root, 1280, 850);
        scene.getStylesheets().addAll(Resources.url("css/main.css").toExternalForm(),
                Resources.url("css/components.css").toExternalForm(),
                Resources.url("css/dashboard.css").toExternalForm());
        scene.getStylesheets().add(Resources.url("css/workspace.css").toExternalForm());
        stage.setTitle("EventProof | Event Reliability Management");
        stage.setMinWidth(1060);
        stage.setMinHeight(740);
        stage.setScene(scene);
        stage.show();
        LOGGER.log(System.Logger.Level.INFO, "EventProof window opened successfully.");
    }

    @Override public void stop() {
        if (context != null) context.close();
        com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
    }

    public static void main(String[] args) { launch(args); }
}
