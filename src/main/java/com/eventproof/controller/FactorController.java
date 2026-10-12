package com.eventproof.controller;

import com.eventproof.model.ScoreFactor;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public final class FactorController {
    @FXML private Label title, deduction, explanation;
    @FXML private ProgressBar bar;
    public void setFactor(ScoreFactor factor) {
        title.setText(factor.name());
        deduction.setText("-" + factor.penalty() + " points");
        explanation.setText(factor.explanation());
        bar.setProgress(Math.max(0, 1 - factor.penalty() / 100.0));
    }
}
