package com.example.weatherappjava.controller;

import com.example.weatherappjava.model.WeatherData;
import com.example.weatherappjava.util.JsonParser;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for displaying weather data in the UI.
 */
public class WeatherDisplayController {
    private final MainController mainController;

    /**
     * Constructor linking to the main controller.
     */
    public WeatherDisplayController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Displays current weather and forecast data in UI labels.
     */
    public void displayWeatherData(WeatherData weatherData, String locationName) {
        mainController.getLocationLabel().setText(locationName);
        mainController.getTemperatureLabel().setText(String.format("%.1f °C", weatherData.getTemperature()));
        mainController.getWindSpeedLabel().setText(String.format("%.1f km/h", weatherData.getWindSpeed()));
        mainController.getHumidityLabel().setText(String.format("%.0f %%", weatherData.getHumidity()));
        mainController.getPressureLabel().setText(String.format("%.1f hPa", weatherData.getPressure()));
        mainController.getSoilTemperatureLabel().setText(String.format("%.1f °C", weatherData.getSoilTemperature()));
        mainController.getRainLabel().setText(String.format("%.2f mm", weatherData.getPrecipitation()));
        mainController.getUpdateTimeLabel().setText(weatherData.getTime());
    }

    /**
     * Displays historical weather data, marking current weather fields as unavailable.
     */
    public void displayHistoricalWeatherData(WeatherData weatherData, String locationName) {
        mainController.getLocationLabel().setText(locationName + " (Historical Data)");
        mainController.getTemperatureLabel().setText("N/A - Historical Mode");
        mainController.getWindSpeedLabel().setText("N/A - Historical Mode");
        mainController.getHumidityLabel().setText("N/A - Historical Mode");
        mainController.getPressureLabel().setText("N/A - Historical Mode");
        mainController.getSoilTemperatureLabel().setText("N/A - Historical Mode");
        mainController.getRainLabel().setText("N/A - Historical Mode");
        mainController.getUpdateTimeLabel().setText(weatherData.getTime());
    }

    /**
     * Shows raw JSON responses in a new window.
     */
    public void showRawData(String rawGeoResponse, String rawWeatherResponse) {
        // Create a new stage for raw data
        Stage rawDataStage = new Stage();
        rawDataStage.setTitle("Raw JSON Data");

        // Set up the text area for JSON display
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(600, 500);

        // Build content with formatted JSON
        StringBuilder content = new StringBuilder();
        if (rawGeoResponse != null) {
            content.append("Geolocation Data:\n")
                    .append(JsonParser.formatJson(rawGeoResponse))
                    .append("\n\n");
        }
        if (rawWeatherResponse != null) {
            content.append("Weather Data:\n")
                    .append(JsonParser.formatJson(rawWeatherResponse));
        }
        textArea.setText(content.length() > 0 ? content.toString() : "No data available");

        // Display in a new window
        Scene scene = new Scene(new VBox(textArea));
        rawDataStage.setScene(scene);
        rawDataStage.show();
    }
}