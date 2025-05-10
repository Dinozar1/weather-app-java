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

        // Historical weather API URL with additional parameters:
        // - relative_humidity_2m_mean - for humidity
        // - surface_pressure_mean - for pressure
        // - windspeed_10m_mean - for wind speed
        // - soil_temperature_0_to_7cm_mean - for soil temperature
        String historicalWeatherApiUrl = "https://archive-api.open-meteo.com/v1/archive?latitude=" + location.getLatitude() +
                "&longitude=" + location.getLongitude() +
                "&start_date=" + startDateStr +
                "&end_date=" + endDateStr +
                "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code," +
                "windspeed_10m_mean,relative_humidity_2m_mean,surface_pressure_mean,soil_temperature_0_to_7cm_mean" +
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

            // Extract additional parameters
            String windSpeedJson = JsonParser.extractStringFromJson(dailyJson, "windspeed_10m_mean");
            String humidityJson = JsonParser.extractStringFromJson(dailyJson, "relative_humidity_2m_mean");
            String pressureJson = JsonParser.extractStringFromJson(dailyJson, "surface_pressure_mean");
            String soilTempJson = JsonParser.extractStringFromJson(dailyJson, "soil_temperature_0_to_7cm_mean");

            // Parse arrays
            String[] dates = JsonParser.parseJsonArray(datesJson);
            String[] maxTemps = JsonParser.parseJsonArray(maxTempJson);
            String[] minTemps = JsonParser.parseJsonArray(minTempJson);
            String[] precipSums = JsonParser.parseJsonArray(precipSumJson);

            // Parse additional parameter arrays
            String[] windSpeeds = JsonParser.parseJsonArray(windSpeedJson);
            String[] humidities = JsonParser.parseJsonArray(humidityJson);
            String[] pressures = JsonParser.parseJsonArray(pressureJson);
            String[] soilTemps = JsonParser.parseJsonArray(soilTempJson);

            // Zapisz dane historyczne do list
            for (int i = 0; i < dates.length; i++) {
                double avgTemp = 0;
                double precipVal = 0;
                double windSpeed = 0;
                double humidity = 0;
                double pressure = 0;
                double soilTemp = 0;

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

                // Parse additional parameters with error handling
                try {
                    windSpeed = Double.parseDouble(windSpeeds[i]);
                } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException e) {
                    // Ignoruj błędy parsowania
                }

                try {
                    humidity = Double.parseDouble(humidities[i]);
                } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException e) {
                    // Ignoruj błędy parsowania
                }

                try {
                    pressure = Double.parseDouble(pressures[i]);
                } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException e) {
                    // Ignoruj błędy parsowania
                }

                try {
                    soilTemp = Double.parseDouble(soilTemps[i]);
                } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException e) {
                    // Ignoruj błędy parsowania
                }

                // Dodaj dane do list
                weatherData.addChartDataPoint(
                        windSpeed,
                        soilTemp,
                        avgTemp,
                        precipVal,
                        pressure,
                        DateFormatter.formatDate(dates[i])
                );

                // If this is the first data point, set current weather properties
                if (i == 0) {
                    weatherData.setWindSpeed(windSpeed);
                    weatherData.setSoilTemperature(soilTemp);
                    weatherData.setHumidity(humidity);
                    weatherData.setPressure(pressure);
                }
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

            // Extract additional parameters
            String windSpeedJson = JsonParser.extractStringFromJson(dailyJson, "windspeed_10m_mean");
            String humidityJson = JsonParser.extractStringFromJson(dailyJson, "relative_humidity_2m_mean");
            String soilTempJson = JsonParser.extractStringFromJson(dailyJson, "soil_temperature_0_to_7cm_mean");

            // Parse arrays
            String[] dates = JsonParser.parseJsonArray(datesJson);
            String[] maxTemps = JsonParser.parseJsonArray(maxTempJson);
            String[] minTemps = JsonParser.parseJsonArray(minTempJson);
            String[] precipSums = JsonParser.parseJsonArray(precipSumJson);

            // Parse additional parameter arrays
            String[] windSpeeds = JsonParser.parseJsonArray(windSpeedJson);
            String[] humidities = JsonParser.parseJsonArray(humidityJson);
            String[] soilTemps = JsonParser.parseJsonArray(soilTempJson);

            // Clear grid and add forecast data
            forecastGrid.getChildren().clear();

            // Add headers
            forecastGrid.add(new Label("Data"), 0, 0);
            forecastGrid.add(new Label("Min. Temp."), 1, 0);
            forecastGrid.add(new Label("Max. Temp."), 2, 0);
            forecastGrid.add(new Label("Opady"), 3, 0);
            forecastGrid.add(new Label("Wiatr"), 4, 0);
            forecastGrid.add(new Label("Wilgotność"), 5, 0);
            forecastGrid.add(new Label("Temp. gleby"), 6, 0);

            // Display historical data
            int days = Math.min(dates.length, 30); // Show up to 30 days
            for (int i = 0; i < days; i++) {
                forecastGrid.add(new Label(DateFormatter.formatDate(dates[i])), 0, i + 1);
                forecastGrid.add(new Label(minTemps[i] + " °C"), 1, i + 1);
                forecastGrid.add(new Label(maxTemps[i] + " °C"), 2, i + 1);
                forecastGrid.add(new Label(precipSums[i] + " mm"), 3, i + 1);

                // Add additional data columns with error handling
                try {
                    forecastGrid.add(new Label(windSpeeds[i] + " km/h"), 4, i + 1);
                } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                    forecastGrid.add(new Label("N/A"), 4, i + 1);
                }

                try {
                    forecastGrid.add(new Label(humidities[i] + " %"), 5, i + 1);
                } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                    forecastGrid.add(new Label("N/A"), 5, i + 1);
                }

                try {
                    forecastGrid.add(new Label(soilTemps[i] + " °C"), 6, i + 1);
                } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                    forecastGrid.add(new Label("N/A"), 6, i + 1);
                }
            }
        }
    }
}