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

public class ChartController {
    @FXML
    private LineChart<Number, Number> dataChart;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Label titleLabel;

    @FXML
    private Button exportButton;

    private List<Double> currentValues;
    private List<String> currentLabels;
    private String chartTitle;
    private String yAxisLabel;

    public void initialize() {
        // Basic chart setup
        dataChart.setAnimated(false);
        dataChart.setCreateSymbols(true);
        dataChart.setLegendVisible(false);
    }

    public void setupChart(String title, String yAxisLabel, List<Double> values, List<String> labels) {
        // Store the data for export functionality
        this.chartTitle = title;
        this.yAxisLabel = yAxisLabel;
        this.currentValues = values;
        this.currentLabels = labels;

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

    @FXML
    public void onExportButtonClick() {
        if (currentValues == null || currentLabels == null || currentValues.isEmpty()) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Zapisz dane wykresu");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Pliki tekstowe", "*.txt"));

        // Nazwa pliku bazująca na tytule wykresu z datą i czasem
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = LocalDateTime.now().format(formatter);
        String sanitizedTitle = chartTitle.replaceAll("[^a-zA-Z0-9ąćęłńóśźżĄĆĘŁŃÓŚŹŻ]", "_");
        fileChooser.setInitialFileName(sanitizedTitle + "_" + timestamp + ".txt");

        Stage stage = (Stage) dataChart.getScene().getWindow();
        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            exportDataToFile(selectedFile);
        }
    }

    private void exportDataToFile(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            // Zapisz informacje nagłówkowe
            writer.write("# Dane wykresu wygenerowane: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("# Tytuł: " + chartTitle + "\n");
            writer.write("# Jednostka: " + yAxisLabel + "\n");
            writer.write("# Liczba punktów danych: " + currentValues.size() + "\n");
            writer.write("#\n");

            // Zapisz nagłówki kolumn
            writer.write("# Indeks\tCzas\tWartość\n");
            writer.write("# ----------------------------------\n");

            // Zapisz dane
            for (int i = 0; i < currentValues.size(); i++) {
                writer.write(i + "\t" + currentLabels.get(i) + "\t" + currentValues.get(i) + "\n");
            }

            // Dodaj metadane w formacie pozwalającym na odtworzenie wykresu
            writer.write("\n# METADATA\n");
            writer.write("FORMAT=1.0\n");  // Wersja formatu danych
            writer.write("TYPE=LINE_CHART\n");
            writer.write("TITLE=" + chartTitle + "\n");
            writer.write("YAXIS=" + yAxisLabel + "\n");
            writer.write("XAXIS=Czas\n");
            writer.write("# END METADATA\n");

        } catch (IOException e) {
            System.err.println("Błąd podczas eksportu danych wykresu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}