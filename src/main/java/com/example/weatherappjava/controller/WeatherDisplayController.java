package com.example.weatherappjava.controller;

import com.example.weatherappjava.model.WeatherData;

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

        // Display temperature with null check
        if (Double.isNaN(weatherData.getTemperature())) {
            mainController.getTemperatureLabel().setText("Brak danych");
        } else {
            mainController.getTemperatureLabel().setText(String.format("%.1f °C", weatherData.getTemperature()));
        }

        // Display wind speed with null check
        if (Double.isNaN(weatherData.getWindSpeed())) {
            mainController.getWindSpeedLabel().setText("Brak danych");
        } else {
            mainController.getWindSpeedLabel().setText(String.format("%.1f km/h", weatherData.getWindSpeed()));
        }

        // Display humidity with null check
        if (Double.isNaN(weatherData.getHumidity())) {
            mainController.getHumidityLabel().setText("Brak danych");
        } else {
            mainController.getHumidityLabel().setText(String.format("%.0f %%", weatherData.getHumidity()));
        }

        // Display pressure with null check
        if (Double.isNaN(weatherData.getPressure())) {
            mainController.getPressureLabel().setText("Brak danych");
        } else {
            mainController.getPressureLabel().setText(String.format("%.1f hPa", weatherData.getPressure()));
        }

        // Display soil temperature with null check
        if (Double.isNaN(weatherData.getSoilTemperature())) {
            mainController.getSoilTemperatureLabel().setText("Brak danych");
        } else {
            mainController.getSoilTemperatureLabel().setText(String.format("%.1f °C", weatherData.getSoilTemperature()));
        }

        // Display precipitation with null check
        if (Double.isNaN(weatherData.getPrecipitation())) {
            mainController.getRainLabel().setText("Brak danych");
        } else {
            mainController.getRainLabel().setText(String.format("%.2f mm", weatherData.getPrecipitation()));
        }

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
}