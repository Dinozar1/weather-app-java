package com.example.weatherappjava.controller;

import com.example.weatherappjava.model.LocationData;
import com.example.weatherappjava.model.WeatherData;
import com.example.weatherappjava.service.HistoricalWeatherService;
import com.example.weatherappjava.service.WeatherService;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

/**
 * Kontroler odpowiedzialny za wyszukiwanie danych pogodowych
 */
public class WeatherSearchController {
    private final MainController mainController;
    private final WeatherService weatherService;
    private final HistoricalWeatherService historicalWeatherService;
    private final WeatherDisplayController displayController;

    public WeatherSearchController(MainController mainController) {
        this.mainController = mainController;
        this.weatherService = new WeatherService();
        this.historicalWeatherService = new HistoricalWeatherService();
        this.displayController = new WeatherDisplayController(mainController);
    }

    public void handleSearch(
            boolean isForecastMode,
            boolean isCityMode,
            String city,
            String latText,
            String lonText,
            LocalDate startDate,
            LocalDate endDate) {

        if (isCityMode) {
            if (city.isEmpty()) {
                mainController.getStatusLabel().setText("Wprowadź nazwę miasta.");
                return;
            }
            if (isForecastMode) {
                getWeatherByCity(city);
            } else {
                getHistoricalWeatherByCity(city, startDate, endDate);
            }
        } else {
            if (latText.isEmpty() || lonText.isEmpty()) {
                mainController.getStatusLabel().setText("Wprowadź obie współrzędne geograficzne.");
                return;
            }

            try {
                double latitude = Double.parseDouble(latText);
                double longitude = Double.parseDouble(lonText);

                if (latitude < -90 || latitude > 90) {
                    mainController.getStatusLabel().setText("Szerokość geograficzna musi być w zakresie od -90 do 90.");
                    return;
                }

                if (longitude < -180 || longitude > 180) {
                    mainController.getStatusLabel().setText("Długość geograficzna musi być w zakresie od -180 do 180.");
                    return;
                }

                LocationData location = new LocationData(null, latitude, longitude);
                if (isForecastMode) {
                    getWeatherByCoordinates(location);
                } else {
                    getHistoricalWeatherByCoordinates(location, startDate, endDate);
                }
            } catch (NumberFormatException e) {
                mainController.getStatusLabel().setText("Wprowadź poprawne wartości liczbowe dla współrzędnych.");
            }
        }
    }

    private void getWeatherByCity(String city) {
        mainController.getStatusLabel().setText("");
        mainController.getSearchButton().setDisable(true);
        mainController.getStatusLabel().setText("Pobieranie danych pogodowych...");

        CompletableFuture.runAsync(() -> {
            try {
                LocationData location = mainController.getGeolocationService().getLocationByCity(city);
                getWeatherByCoordinates(location);
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mainController.getStatusLabel().setText("Błąd: " + e.getMessage());
                    mainController.getSearchButton().setDisable(false);
                });
            }
        });
    }

    private void getHistoricalWeatherByCity(String city, LocalDate startDate, LocalDate endDate) {
        mainController.getStatusLabel().setText("");
        mainController.getSearchButton().setDisable(true);
        mainController.getStatusLabel().setText("Pobieranie historycznych danych pogodowych...");

        CompletableFuture.runAsync(() -> {
            try {
                LocationData location = mainController.getGeolocationService().getLocationByCity(city);
                getHistoricalWeatherByCoordinates(location, startDate, endDate);
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mainController.getStatusLabel().setText("Błąd: " + e.getMessage());
                    mainController.getSearchButton().setDisable(false);
                });
            }
        });
    }

    private void getWeatherByCoordinates(LocationData location) {
        if (!mainController.getSearchButton().isDisabled()) {
            mainController.getStatusLabel().setText("");
            mainController.getSearchButton().setDisable(true);
            mainController.getStatusLabel().setText("Pobieranie danych pogodowych...");
        }

        CompletableFuture.runAsync(() -> {
            try {
                WeatherData weatherData = weatherService.getCurrentWeather(location);
                mainController.setWeatherData(weatherData);

                javafx.application.Platform.runLater(() -> {
                    displayController.displayWeatherData(weatherData, location.toString());
                    weatherService.displayForecastInGrid(mainController.getForecastGrid(), weatherData);
                    mainController.getStatusLabel().setText("Dane pogodowe zostały pobrane.");
                    mainController.getSearchButton().setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mainController.getStatusLabel().setText("Błąd: " + e.getMessage());
                    mainController.getSearchButton().setDisable(false);
                });
            }
        });
    }

    private void getHistoricalWeatherByCoordinates(LocationData location, LocalDate startDate, LocalDate endDate) {
        if (!mainController.getSearchButton().isDisabled()) {
            mainController.getStatusLabel().setText("");
            mainController.getSearchButton().setDisable(true);
            mainController.getStatusLabel().setText("Pobieranie historycznych danych pogodowych...");
        }

        CompletableFuture.runAsync(() -> {
            try {
                WeatherData weatherData = historicalWeatherService.getHistoricalWeather(location, startDate, endDate);
                mainController.setWeatherData(weatherData);

                javafx.application.Platform.runLater(() -> {
                    displayController.displayHistoricalWeatherData(weatherData, location.toString());
                    historicalWeatherService.displayHistoricalDataInGrid(mainController.getForecastGrid(), weatherData);
                    mainController.getStatusLabel().setText("Historyczne dane pogodowe zostały pobrane.");
                    mainController.getSearchButton().setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    mainController.getStatusLabel().setText("Błąd: " + e.getMessage());
                    mainController.getSearchButton().setDisable(false);
                });
            }
        });
    }

    public String getRawWeatherResponse() {
        String forecastResponse = weatherService.getRawWeatherResponse();
        String historicalResponse = historicalWeatherService.getRawWeatherResponse();

        return forecastResponse != null ? forecastResponse : historicalResponse;
    }
}