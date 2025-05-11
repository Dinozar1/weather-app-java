package com.example.weatherappjava.controller;

import com.example.weatherappjava.model.LocationData;
import com.example.weatherappjava.model.WeatherData;
import com.example.weatherappjava.service.HistoricalWeatherService;
import com.example.weatherappjava.service.WeatherService;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for handling weather data searches.
 */
public class WeatherSearchController {
    private final MainController mainController;
    private final WeatherService weatherService;
    private final HistoricalWeatherService historicalWeatherService;
    private final WeatherDisplayController displayController;

    /**
     * Constructor initializing services and display controller.
     */
    public WeatherSearchController(MainController mainController) {
        this.mainController = mainController;
        this.weatherService = new WeatherService();
        this.historicalWeatherService = new HistoricalWeatherService();
        this.displayController = new WeatherDisplayController(mainController);
    }

    /**
     * Handles search requests based on input parameters and mode (forecast or historical).
     */
    public void handleSearch(boolean isForecastMode, boolean isCityMode, String city, String latText, String lonText, LocalDate startDate, LocalDate endDate) {
        // Validate city input
        if (isCityMode) {
            if (city.isEmpty()) {
                mainController.getStatusLabel().setText("Enter a city name.");
                return;
            }
            if (isForecastMode) {
                getWeatherByCity(city);
            } else {
                getHistoricalWeatherByCity(city, startDate, endDate);
            }
        } else {
            // Validate coordinates input
            if (latText.isEmpty() || lonText.isEmpty()) {
                mainController.getStatusLabel().setText("Enter both geographic coordinates.");
                return;
            }

            try {
                double latitude = Double.parseDouble(latText);
                double longitude = Double.parseDouble(lonText);

                if (latitude < -90 || latitude > 90) {
                    mainController.getStatusLabel().setText("Latitude must be between -90 and 90.");
                    return;
                }
                if (longitude < -180 || longitude > 180) {
                    mainController.getStatusLabel().setText("Longitude must be between -180 and 180.");
                    return;
                }

                LocationData location = new LocationData(null, latitude, longitude);
                if (isForecastMode) {
                    getWeatherByCoordinates(location);
                } else {
                    getHistoricalWeatherByCoordinates(location, startDate, endDate);
                }
            } catch (NumberFormatException e) {
                mainController.getStatusLabel().setText("Enter valid numeric coordinates.");
            }
        }
    }

    /**
     * Fetches weather data for a city using geolocation service.
     */
    private void getWeatherByCity(String city) {
        mainController.getStatusLabel().setText("Fetching weather data...");
        mainController.getSearchButton().setDisable(true);

        CompletableFuture.runAsync(() -> {
            try {
                LocationData location = mainController.getGeolocationService().getLocationByCity(city);
                getWeatherByCoordinates(location);
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mainController.getStatusLabel().setText("Error: " + e.getMessage());
                    mainController.getSearchButton().setDisable(false);
                });
            }
        });
    }

    /**
     * Fetches historical weather data for a city.
     */
    private void getHistoricalWeatherByCity(String city, LocalDate startDate, LocalDate endDate) {
        mainController.getStatusLabel().setText("Fetching historical weather data...");
        mainController.getSearchButton().setDisable(true);

        CompletableFuture.runAsync(() -> {
            try {
                LocationData location = mainController.getGeolocationService().getLocationByCity(city);
                getHistoricalWeatherByCoordinates(location, startDate, endDate);
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mainController.getStatusLabel().setText("Error: " + e.getMessage());
                    mainController.getSearchButton().setDisable(false);
                });
            }
        });
    }

    /**
     * Fetches current weather data for given coordinates and updates UI.
     */
    private void getWeatherByCoordinates(LocationData location) {
        if (!mainController.getSearchButton().isDisabled()) {
            mainController.getStatusLabel().setText("Fetching weather data...");
            mainController.getSearchButton().setDisable(true);
        }

        CompletableFuture.runAsync(() -> {
            try {
                WeatherData weatherData = weatherService.getCurrentWeather(location);
                mainController.setWeatherData(weatherData);

                javafx.application.Platform.runLater(() -> {
                    displayController.displayWeatherData(weatherData, location.toString());
                    weatherService.displayForecastInGrid(mainController.getForecastGrid(), weatherData);
                    mainController.getStatusLabel().setText("Weather data retrieved.");
                    mainController.getSearchButton().setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mainController.getStatusLabel().setText("Error: " + e.getMessage());
                    mainController.getSearchButton().setDisable(false);
                });
            }
        });
    }

    /**
     * Fetches historical weather data for given coordinates and updates UI.
     */
    private void getHistoricalWeatherByCoordinates(LocationData location, LocalDate startDate, LocalDate endDate) {
        if (!mainController.getSearchButton().isDisabled()) {
            mainController.getStatusLabel().setText("Fetching historical weather data...");
            mainController.getSearchButton().setDisable(true);
        }

        CompletableFuture.runAsync(() -> {
            try {
                WeatherData weatherData = historicalWeatherService.getHistoricalWeather(location, startDate, endDate);
                mainController.setWeatherData(weatherData);

                javafx.application.Platform.runLater(() -> {
                    displayController.displayHistoricalWeatherData(weatherData, location.toString());
                    historicalWeatherService.displayHistoricalDataInGrid(mainController.getForecastGrid(), weatherData);
                    mainController.getStatusLabel().setText("Historical weather data retrieved.");
                    mainController.getSearchButton().setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mainController.getStatusLabel().setText("Error: " + e.getMessage());
                    mainController.getSearchButton().setDisable(false);
                });
            }
        });
    }

    /**
     * Returns the raw weather response from either forecast or historical service.
     */
    public String getRawWeatherResponse() {
        String forecastResponse = weatherService.getRawWeatherResponse();
        String historicalResponse = historicalWeatherService.getRawWeatherResponse();
        return forecastResponse != null ? forecastResponse : historicalResponse;
    }
}