package com.example.weatherappjava.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for managing the line chart display and data export in the weather application.
 */
public class ChartController {
    @FXML private LineChart<Number, Number> dataChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label titleLabel;
    @FXML private Button exportButton;

    // Data for chart and export
    private List<Double> currentValues;
    private List<String> currentLabels;
    private String chartTitle;
    private String yAxisLabel;

    /**
     * Initializes the chart with basic settings.
     */
    public void initialize() {
        dataChart.setAnimated(false); // Disable animations for smoother updates
        dataChart.setCreateSymbols(true); // Show data points as symbols
        dataChart.setLegendVisible(false); // Hide legend as it's unnecessary
    }

    /**
     * Configures the chart with provided data, title, and axis labels.
     */
    public void setupChart(String title, String yAxisLabel, List<Double> values, List<String> labels) {
        // Store data for export
        this.chartTitle = title;
        this.yAxisLabel = yAxisLabel;
        this.currentValues = values;
        this.currentLabels = labels;

        // Update UI elements
        titleLabel.setText(title);
        yAxis.setLabel(yAxisLabel);

        // Create and populate chart series
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0; i < values.size(); i++) {
            series.getData().add(new XYChart.Data<>(i, values.get(i)));
        }

        // Update chart with new data
        dataChart.getData().clear();
        dataChart.getData().add(series);

        // Format x-axis labels if provided
        if (labels != null && !labels.isEmpty()) {
            xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis) {
                @Override
                public String toString(Number object) {
                    int index = object.intValue();
                    return (index >= 0 && index < labels.size()) ? labels.get(index) : "";
                }
            });
        }
    }

    /**
     * Sets the window title for the chart stage.
     */
    public void setWindowTitle(String title) {
        Stage stage = (Stage) dataChart.getScene().getWindow();
        if (stage != null) {
            stage.setTitle(title);
        }
    }

    /**
     * Handles export button click, opening a file chooser to save chart data as a text file.
     */
    @FXML
    public void onExportButtonClick() {
        if (currentValues == null || currentLabels == null || currentValues.isEmpty()) {
            return; // Exit if no data to export
        }

        // Configure file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Chart Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        // Generate default filename with timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = LocalDateTime.now().format(formatter);
        String sanitizedTitle = chartTitle.replaceAll("[^a-zA-Z0-9ąćęłńóśźżĄĆĘŁŃÓŚŹŻ]", "_");
        fileChooser.setInitialFileName(sanitizedTitle + "_" + timestamp + ".txt");

        // Show the save dialog and export if a file is selected
        Stage stage = (Stage) dataChart.getScene().getWindow();
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile != null) {
            exportDataToFile(selectedFile);
        }
    }

    /**
     * Exports chart data to a text file with metadata.
     */
    private void exportDataToFile(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header information
            writer.write("# Chart data generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("# Title: " + chartTitle + "\n");
            writer.write("# Unit: " + yAxisLabel + "\n");
            writer.write("# Data points: " + currentValues.size() + "\n");
            writer.write("#\n");

            // Write column headers
            writer.write("# Index\tTime\tValue\n");
            writer.write("# ----------------------------------\n");

            // Write data rows
            for (int i = 0; i < currentValues.size(); i++) {
                writer.write(i + "\t" + currentLabels.get(i) + "\t" + currentValues.get(i) + "\n");
            }

            // Write metadata for chart reconstruction
            writer.write("\n# METADATA\n");
            writer.write("FORMAT=1.0\n");
            writer.write("TYPE=LINE_CHART\n");
            writer.write("TITLE=" + chartTitle + "\n");
            writer.write("YAXIS=" + yAxisLabel + "\n");
            writer.write("XAXIS=Time\n");
            writer.write("# END METADATA\n");

        } catch (IOException e) {
            System.err.println("Error exporting chart data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}