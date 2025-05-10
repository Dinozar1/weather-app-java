package com.example.weatherappjava.service;

import com.example.weatherappjava.model.LocationData;
import com.example.weatherappjava.model.WeatherData;
import com.example.weatherappjava.util.DateFormatter;
import com.example.weatherappjava.util.HttpUtil;
import com.example.weatherappjava.util.JsonParser;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.time.LocalDate;

public class HistoricalWeatherService {
    private String rawWeatherResponse;

    public String getRawWeatherResponse() {
        return rawWeatherResponse;
    }

    public WeatherData getHistoricalWeather(LocationData location, LocalDate startDate, LocalDate endDate) throws IOException {
        // Validate date range
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Wybierz daty początkową i końcową.");
        }

        String startDateStr = startDate.toString();
        String endDateStr = endDate.toString();

        // Historical weather API URL
        String historicalWeatherApiUrl = "https://archive-api.open-meteo.com/v1/archive?latitude=" + location.getLatitude() +
                "&longitude=" + location.getLongitude() +
                "&start_date=" + startDateStr +
                "&end_date=" + endDateStr +
                "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code" +
                "&timezone=auto";

        rawWeatherResponse = HttpUtil.makeHttpRequest(historicalWeatherApiUrl);

        // Tworzenie i wypełnianie obiektu WeatherData
        WeatherData weatherData = new WeatherData();
        weatherData.setTime(startDateStr + " do " + endDateStr);

        // Extract historical data and fill weatherData object
        processHistoricalData(weatherData);

        return weatherData;
    }

    private void processHistoricalData(WeatherData weatherData) {
        String dailyJson = JsonParser.extractStringFromJson(rawWeatherResponse, "daily");
        if (dailyJson != null && !dailyJson.isEmpty()) {
            String datesJson = JsonParser.extractStringFromJson(dailyJson, "time");
            String maxTempJson = JsonParser.extractStringFromJson(dailyJson, "temperature_2m_max");
            String minTempJson = JsonParser.extractStringFromJson(dailyJson, "temperature_2m_min");
            String precipSumJson = JsonParser.extractStringFromJson(dailyJson, "precipitation_sum");

            // Parse arrays
            String[] dates = JsonParser.parseJsonArray(datesJson);
            String[] maxTemps = JsonParser.parseJsonArray(maxTempJson);
            String[] minTemps = JsonParser.parseJsonArray(minTempJson);
            String[] precipSums = JsonParser.parseJsonArray(precipSumJson);

            // Zapisz dane historyczne do list
            for (int i = 0; i < dates.length; i++) {
                double avgTemp = 0;
                double precipVal = 0;

                try {
                    avgTemp = (Double.parseDouble(maxTemps[i]) + Double.parseDouble(minTemps[i])) / 2;
                } catch (NumberFormatException e) {
                    // Ignoruj błędy parsowania
                }

                try {
                    precipVal = Double.parseDouble(precipSums[i]);
                } catch (NumberFormatException e) {
                    // Ignoruj błędy parsowania
                }

                // Dodaj dane do list
                weatherData.addChartDataPoint(
                        0.0, // Brak danych o wietrze
                        0.0, // Brak danych o temperaturze gleby
                        avgTemp,
                        precipVal,
                        0.0, // Brak danych o ciśnieniu
                        DateFormatter.formatDate(dates[i])
                );
            }
        }
    }

    public void displayHistoricalDataInGrid(GridPane forecastGrid, WeatherData weatherData) {
        String dailyJson = JsonParser.extractStringFromJson(rawWeatherResponse, "daily");
        if (dailyJson != null && !dailyJson.isEmpty()) {
            String datesJson = JsonParser.extractStringFromJson(dailyJson, "time");
            String maxTempJson = JsonParser.extractStringFromJson(dailyJson, "temperature_2m_max");
            String minTempJson = JsonParser.extractStringFromJson(dailyJson, "temperature_2m_min");
            String precipSumJson = JsonParser.extractStringFromJson(dailyJson, "precipitation_sum");

            // Parse arrays
            String[] dates = JsonParser.parseJsonArray(datesJson);
            String[] maxTemps = JsonParser.parseJsonArray(maxTempJson);
            String[] minTemps = JsonParser.parseJsonArray(minTempJson);
            String[] precipSums = JsonParser.parseJsonArray(precipSumJson);

            // Clear grid and add forecast data
            forecastGrid.getChildren().clear();

            // Add headers
            forecastGrid.add(new Label("Data"), 0, 0);
            forecastGrid.add(new Label("Min. Temp."), 1, 0);
            forecastGrid.add(new Label("Max. Temp."), 2, 0);
            forecastGrid.add(new Label("Opady"), 3, 0);

            // Display historical data
            int days = Math.min(dates.length, 30); // Show up to 30 days
            for (int i = 0; i < days; i++) {
                forecastGrid.add(new Label(DateFormatter.formatDate(dates[i])), 0, i + 1);
                forecastGrid.add(new Label(minTemps[i] + " °C"), 1, i + 1);
                forecastGrid.add(new Label(maxTemps[i] + " °C"), 2, i + 1);
                forecastGrid.add(new Label(precipSums[i] + " mm"), 3, i + 1);
            }
        }
    }
}