package com.eventproof.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public final class PlaceholderController {
    @FXML private Label moduleTitle;
    public void setModule(String module) { moduleTitle.setText(module + " module coming soon"); }
}
