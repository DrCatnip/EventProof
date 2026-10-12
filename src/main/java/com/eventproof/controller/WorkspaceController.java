package com.eventproof.controller;

import com.eventproof.model.WorkspaceSnapshot;
import com.eventproof.service.AppContext;
import com.eventproof.util.UiErrors;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleBooleanProperty;
import java.util.function.Supplier;

public abstract class WorkspaceController {
    protected final AppContext context;
    protected WorkspaceSnapshot snapshot;
    protected final SimpleBooleanProperty working = new SimpleBooleanProperty(false);
    @FXML protected Label status;
    @FXML protected ProgressIndicator busy;
    private long requestVersion;

    protected WorkspaceController(AppContext context) { this.context = context; }

    public void activate() { refresh(); }

    @FXML public void refresh() {
        if (working.get()) return;
        perform(context::snapshot, data -> { snapshot = data; render(data); status.setText("Up to date"); });
    }

    protected abstract void render(WorkspaceSnapshot data);

    protected <T> void perform(Supplier<T> operation, java.util.function.Consumer<T> success) {
        long version = ++requestVersion;
        working.set(true);
        busy.setVisible(true);
        busy.setManaged(true);
        status.setText("Working...");
        status.getStyleClass().remove("error-text");
        context.submit(operation, result -> {
            if (version != requestVersion) return;
            finish();
            success.accept(result);
        }, error -> {
            if (version != requestVersion) return;
            finish();
            status.setText(UiErrors.message(error));
            status.getStyleClass().add("error-text");
        });
    }

    protected void finish() {
        working.set(false);
        busy.setVisible(false);
        busy.setManaged(false);
    }
}
