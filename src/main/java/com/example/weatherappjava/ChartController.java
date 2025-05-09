package com.example.weatherappjava;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.List;

public class ChartController {
    @FXML
    private LineChart<Number, Number> dataChart;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Label titleLabel;

    public void initialize() {
        // Basic chart setup
        dataChart.setAnimated(false);
        dataChart.setCreateSymbols(true);
        dataChart.setLegendVisible(false);
    }

    public void setupChart(String title, String yAxisLabel, List<Double> values, List<String> labels) {
        // Set title and axis labels
        titleLabel.setText(title);
        yAxis.setLabel(yAxisLabel);

        // Create a series
        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        // Add data points
        for (int i = 0; i < values.size(); i++) {
            series.getData().add(new XYChart.Data<>(i, values.get(i)));
        }

        // Clear previous data and add the new series
        dataChart.getData().clear();
        dataChart.getData().add(series);

        // If we have labels for x-axis (like dates), format them
        if (labels != null && !labels.isEmpty()) {
            xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis) {
                @Override
                public String toString(Number object) {
                    int index = object.intValue();
                    if (index >= 0 && index < labels.size()) {
                        return labels.get(index);
                    }
                    return "";
                }
            });
        }
    }

    public void setWindowTitle(String title) {
        Stage stage = (Stage) dataChart.getScene().getWindow();
        if (stage != null) {
            stage.setTitle(title);
        }
    }
}