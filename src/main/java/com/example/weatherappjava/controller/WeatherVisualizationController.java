package com.example.weatherappjava.controller;

import com.example.weatherappjava.model.WeatherData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for visualizing weather data in charts.
 */
public class WeatherVisualizationController {
    private final MainController mainController;

    @FXML private CheckBox windSpeedCheckBox;
    @FXML private CheckBox soilTempCheckBox;
    @FXML private CheckBox airTempCheckBox;
    @FXML private CheckBox rainCheckBox;
    @FXML private CheckBox pressureCheckBox;

    /**
     * Constructor linking to the main controller.
     */
    public WeatherVisualizationController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Handles visualization button click, opening chart windows for selected data types.
     */
    @FXML
    public void onVisualizeButtonClick() {
        // Check if any data type is selected
        boolean anySelected = windSpeedCheckBox.isSelected() ||
                soilTempCheckBox.isSelected() ||
                airTempCheckBox.isSelected() ||
                rainCheckBox.isSelected() ||
                pressureCheckBox.isSelected();

        if (!anySelected) {
            mainController.getStatusLabel().setText("Select at least one data type for visualization.");
            return;
        }

        // Verify weather data availability
        if (!weatherDataHasData()) {
            mainController.getStatusLabel().setText("No data to visualize. Fetch weather data first.");
            return;
        }

        WeatherData weatherData = mainController.getWeatherData();

        // Open chart windows for selected data types
        if (windSpeedCheckBox.isSelected()) {
            openChartWindow("Wind Speed", "km/h", weatherData.getWindSpeedData(), weatherData.getTimeLabels());
        }
        if (soilTempCheckBox.isSelected()) {
            openChartWindow("Soil Temperature", "°C", weatherData.getSoilTempData(), weatherData.getTimeLabels());
        }
        if (airTempCheckBox.isSelected()) {
            openChartWindow("Air Temperature", "°C", weatherData.getAirTempData(), weatherData.getTimeLabels());
        }
        if (rainCheckBox.isSelected()) {
            openChartWindow("Precipitation", "mm", weatherData.getRainData(), weatherData.getTimeLabels());
        }
        if (pressureCheckBox.isSelected()) {
            openChartWindow("Pressure", "hPa", weatherData.getPressureData(), weatherData.getTimeLabels());
        }
    }

    /**
     * Opens a new window with a chart for the specified data.
     */
    private void openChartWindow(String title, String yAxisLabel, List<Double> data, List<String> labels) {
        try {
            // Load chart FXML and set up controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/weatherappjava/chart-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            ChartController chartController = loader.getController();
            chartController.setupChart(title, yAxisLabel, data, labels);
            chartController.setWindowTitle(title);

            // Display the chart in a new window
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            mainController.getStatusLabel().setText("Error creating chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if weather data is available for visualization.
     */
    private boolean weatherDataHasData() {
        return !mainController.getWeatherData().getTimeLabels().isEmpty();
    }

    // Setters for UI elements, configured during FXML initialization
    public void setWindSpeedCheckBox(CheckBox checkBox) { this.windSpeedCheckBox = checkBox; }
    public void setSoilTempCheckBox(CheckBox checkBox) { this.soilTempCheckBox = checkBox; }
    public void setAirTempCheckBox(CheckBox checkBox) { this.airTempCheckBox = checkBox; }
    public void setRainCheckBox(CheckBox checkBox) { this.rainCheckBox = checkBox; }
    public void setPressureCheckBox(CheckBox checkBox) { this.pressureCheckBox = checkBox; }
    public void setChartOptionsPanel(VBox panel) { // UI elements for chart options
    }
}