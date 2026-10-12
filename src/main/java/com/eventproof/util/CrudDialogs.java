package com.eventproof.util;

import com.eventproof.controller.form.FormController;
import com.eventproof.service.AppContext;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import java.util.function.*;

/** FXML editors remain open on validation/database errors and save asynchronously. */
public final class CrudDialogs {
    private CrudDialogs() { }

    public static <T, C extends FormController<T>> void edit(Window owner, String title, String form,
            AppContext context, Consumer<C> configure, Function<T, T> save, Consumer<T> saved) {
        var loader = Views.loader(form, context);
        VBox fields = Views.load(loader);
        C controller = loader.getController();
        configure.accept(controller);
        var dialog = new Dialog<Void>();
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setResizable(true);
        var pane = dialog.getDialogPane();
        pane.getStyleClass().add("app-root");
        pane.getStylesheets().setAll(owner.getScene().getStylesheets());
        var error = new Label();
        error.setWrapText(true);
        error.getStyleClass().add("error-text");
        var content = new VBox(14, fields, error);
        content.getStyleClass().add("editor-content");
        pane.setContent(content);
        var saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().setAll(saveType, ButtonType.CANCEL);
        var saveButton = (Button) pane.lookupButton(saveType);
        saveButton.setId("saveButton");
        saveButton.getStyleClass().add("primary-button");
        var saving = new javafx.beans.property.SimpleBooleanProperty();
        pane.lookupButton(ButtonType.CANCEL).disableProperty().bind(saving);
        dialog.setOnCloseRequest(event -> { if (saving.get()) event.consume(); });
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            T value;
            try { value = controller.readValue(); }
            catch (RuntimeException exception) { error.setText(UiErrors.message(exception)); return; }
            saving.set(true);
            saveButton.setDisable(true);
            error.setText("Saving...");
            context.submit(() -> save.apply(value), result -> {
                saving.set(false);
                dialog.close();
                saved.accept(result);
            }, exception -> {
                saving.set(false);
                saveButton.setDisable(false);
                error.setText(UiErrors.message(exception));
            });
        });
        dialog.show();
    }

    public static void delete(Window owner, String message, Runnable action) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.CANCEL, ButtonType.OK);
        alert.initOwner(owner);
        alert.setTitle("Confirm deletion");
        alert.setHeaderText("Delete this record?");
        alert.getDialogPane().getStyleClass().add("app-root");
        alert.getDialogPane().getStylesheets().setAll(owner.getScene().getStylesheets());
        alert.showAndWait().filter(ButtonType.OK::equals).ifPresent(ignored -> action.run());
    }
}
