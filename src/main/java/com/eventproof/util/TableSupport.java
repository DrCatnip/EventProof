package com.eventproof.util;

import com.eventproof.model.RiskLevel;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.*;
import java.util.function.Function;

public final class TableSupport {
    private TableSupport() { }
    public static <T> void column(TableView<T> table, String title, Function<T, ?> value, double width) {
        var column = new TableColumn<T, Object>(title);
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(value.apply(cell.getValue())));
        column.setPrefWidth(width);
        table.getColumns().add(column);
    }

    public static <T> void riskColumn(TableView<T> table, Function<T, RiskLevel> value) {
        var column = new TableColumn<T, RiskLevel>("Risk");
        column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(value.apply(cell.getValue())));
        column.setCellFactory(ignored -> new TableCell<>() {
            private final Label badge = new Label();
            @Override protected void updateItem(RiskLevel risk, boolean empty) {
                super.updateItem(risk, empty);
                if (empty || risk == null) { setGraphic(null); return; }
                setRisk(badge, risk);
                setGraphic(badge);
            }
        });
        column.setPrefWidth(95);
        table.getColumns().add(column);
    }

    public static void setRisk(Label badge, RiskLevel risk) {
        badge.setText(risk == null ? "NO ASSESSMENT" : risk.name() + " RISK");
        badge.getStyleClass().removeAll("success", "warning", "danger");
        if (!badge.getStyleClass().contains("badge")) badge.getStyleClass().add("badge");
        if (risk != null) badge.getStyleClass().add(switch (risk) {
            case LOW -> "success"; case MEDIUM -> "warning"; case HIGH -> "danger";
        });
    }

    public static <T> void prepare(TableView<T> table, String emptyText) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label(emptyText));
    }
}
