package com.example.weatherappjava.controller;

import com.example.weatherappjava.model.WeatherData;
import com.example.weatherappjava.util.JsonParser;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Kontroler odpowiedzialny za wyświetlanie danych pogodowych
 */
public class WeatherDisplayController {
    private final MainController mainController;

    public WeatherDisplayController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Wyświetla dane pogodowe dla prognozy
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
     * Wyświetla dane pogodowe dla historii
     */
    public void displayHistoricalWeatherData(WeatherData weatherData, String locationName) {
        mainController.getLocationLabel().setText(locationName + " (Dane historyczne)");
        mainController.getTemperatureLabel().setText("N/D - tryb historyczny");
        mainController.getWindSpeedLabel().setText("N/D - tryb historyczny");
        mainController.getHumidityLabel().setText("N/D - tryb historyczny");
        mainController.getPressureLabel().setText("N/D - tryb historyczny");
        mainController.getSoilTemperatureLabel().setText("N/D - tryb historyczny");
        mainController.getRainLabel().setText("N/D - tryb historyczny");
        mainController.getUpdateTimeLabel().setText(weatherData.getTime());
    }

    /**
     * Wyświetla surowe dane JSON
     */
    public void showRawData(String rawGeoResponse, String rawWeatherResponse) {
        Stage rawDataStage = new Stage();
        rawDataStage.setTitle("Surowe dane JSON");

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(600, 500);

        StringBuilder content = new StringBuilder();

        if (rawGeoResponse != null) {
            content.append("Dane geolokalizacyjne:\n")
                    .append(JsonParser.formatJson(rawGeoResponse))
                    .append("\n\n");
        }

        if (rawWeatherResponse != null) {
            content.append("Dane pogodowe:\n")
                    .append(JsonParser.formatJson(rawWeatherResponse));
        }

        textArea.setText(content.length() > 0 ? content.toString() : "Brak danych do wyświetlenia");

        Scene scene = new Scene(new VBox(textArea));
        rawDataStage.setScene(scene);
        rawDataStage.show();
    }
}