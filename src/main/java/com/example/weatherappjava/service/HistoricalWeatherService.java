package com.example.weatherappjava.service;

import com.example.weatherappjava.model.LocationData;
import com.example.weatherappjava.model.WeatherData;
import com.example.weatherappjava.util.DateFormatter;
import com.example.weatherappjava.util.HttpUtil;
import com.example.weatherappjava.util.JsonParser;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for fetching and processing historical weather data from the Open-Meteo archive API.
 */
public class HistoricalWeatherService {
    private static final Logger LOGGER = Logger.getLogger(HistoricalWeatherService.class.getName());
    private String rawWeatherResponse;
    private final RedisCacheService cacheService = RedisCacheService.getInstance();

    /**
     * Returns the raw JSON response from the last API request.
     */
    public String getRawWeatherResponse() {
        return rawWeatherResponse;
    }

    /**
     * Fetches historical weather data for a location and date range, using cache if available.
     */
    public WeatherData getHistoricalWeather(LocationData location, LocalDate startDate, LocalDate endDate) throws IOException {
        // Validate input dates
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Select start and end dates.");
        }

        String startDateStr = startDate.toString();
        String endDateStr = endDate.toString();
        String cacheKey = cacheService.generateHistoricalCacheKey(location.getLatitude(), location.getLongitude(), startDate, endDate);
        boolean usedCache = false;

        // Check cache first
        if (cacheService.hasCache(cacheKey)) {
            LOGGER.info("Found historical data in cache for: " + location.getName());
            rawWeatherResponse = cacheService.getFromCache(cacheKey);
            usedCache = true;
        } else {
            // Fetch from API if not in cache
            try {
                LOGGER.info("Fetching historical data from API for: " + location.getName() +
                        " from " + startDateStr + " to " + endDateStr);
                String historicalWeatherApiUrl = "https://archive-api.open-meteo.com/v1/archive?latitude=" + location.getLatitude() +
                        "&longitude=" + location.getLongitude() +
                        "&start_date=" + startDateStr +
                        "&end_date=" + endDateStr +
                        "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code," +
                        "windspeed_10m_mean,relative_humidity_2m_mean,surface_pressure_mean,soil_temperature_0_to_7cm_mean" +
                        "&timezone=auto";

                rawWeatherResponse = HttpUtil.makeHttpRequest(historicalWeatherApiUrl);
                cacheService.saveToCache(cacheKey, rawWeatherResponse, false);
            } catch (UnknownHostException e) {
                LOGGER.warning("No internet connection, checking cache...");
                usedCache = true;
            } catch (IOException e) {
                LOGGER.warning("API fetch error: " + e.getMessage() + ", checking cache...");
                usedCache = true;
            }
        }

        // Fallback to cache if API failed
        if (usedCache && rawWeatherResponse == null) {
            if (cacheService.hasCache(cacheKey)) {
                LOGGER.info("Found historical data in cache for: " + location.getName());
                rawWeatherResponse = cacheService.getFromCache(cacheKey);
            } else {
                throw new IOException("No internet connection and no cached data for: " +
                        location.getName() + " from " + startDateStr + " to " + endDateStr);
            }
        }

        // Process and return weather data
        WeatherData weatherData = new WeatherData();
        weatherData.setTime(startDateStr + " to " + endDateStr + (usedCache ? " (cached)" : ""));
        processHistoricalData(weatherData);
        return weatherData;
    }

    /**
     * Extracts and processes historical weather data from JSON response into a WeatherData object.
     */
    private void processHistoricalData(WeatherData weatherData) {
        String dailyJson = JsonParser.extractStringFromJson(rawWeatherResponse, "daily");
        if (dailyJson != null && !dailyJson.isEmpty()) {
            // Extract JSON arrays
            String datesJson = JsonParser.extractStringFromJson(dailyJson, "time");
            String maxTempJson = JsonParser.extractStringFromJson(dailyJson, "temperature_2m_max");
            String minTempJson = JsonParser.extractStringFromJson(dailyJson, "temperature_2m_min");
            String precipSumJson = JsonParser.extractStringFromJson(dailyJson, "precipitation_sum");
            String windSpeedJson = JsonParser.extractStringFromJson(dailyJson, "windspeed_10m_mean");
            String humidityJson = JsonParser.extractStringFromJson(dailyJson, "relative_humidity_2m_mean");
            String pressureJson = JsonParser.extractStringFromJson(dailyJson, "surface_pressure_mean");
            String soilTempJson = JsonParser.extractStringFromJson(dailyJson, "soil_temperature_0_to_7cm_mean");

            // Parse arrays
            String[] dates = JsonParser.parseJsonArray(datesJson);
            String[] maxTemps = JsonParser.parseJsonArray(maxTempJson);
            String[] minTemps = JsonParser.parseJsonArray(minTempJson);
            String[] precipSums = JsonParser.parseJsonArray(precipSumJson);
            String[] windSpeeds = JsonParser.parseJsonArray(windSpeedJson);
            String[] humidities = JsonParser.parseJsonArray(humidityJson);
            String[] pressures = JsonParser.parseJsonArray(pressureJson);
            String[] soilTemps = JsonParser.parseJsonArray(soilTempJson);

            // Process daily data
            for (int i = 0; i < dates.length; i++) {
                double avgTemp = 0, precipVal = 0, windSpeed = 0, humidity = 0, pressure = 0, soilTemp = 0;

                // Parse values with error handling
                try {
                    avgTemp = (Double.parseDouble(maxTemps[i]) + Double.parseDouble(minTemps[i])) / 2;
                } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException ignored) {}
                try {
                    precipVal = Double.parseDouble(precipSums[i]);
                } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException ignored) {}
                try {
                    windSpeed = Double.parseDouble(windSpeeds[i]);
                } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException ignored) {}
                try {
                    humidity = Double.parseDouble(humidities[i]);
                } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException ignored) {}
                try {
                    pressure = Double.parseDouble(pressures[i]);
                } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException ignored) {}
                try {
                    soilTemp = Double.parseDouble(soilTemps[i]);
                } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException ignored) {}

                // Add data point to WeatherData
                weatherData.addChartDataPoint(windSpeed, soilTemp, avgTemp, precipVal, pressure, DateFormatter.formatDate(dates[i]));

                // Set initial weather properties for the first data point
                if (i == 0) {
                    weatherData.setWindSpeed(windSpeed);
                    weatherData.setSoilTemperature(soilTemp);
                    weatherData.setHumidity(humidity);
                    weatherData.setPressure(pressure);
                }
            }
        }
    }

    /**
     * Displays historical weather data in a grid layout
     */
    public void displayHistoricalDataInGrid(GridPane forecastGrid, WeatherData weatherData) {
        String dailyJson = JsonParser.extractStringFromJson(rawWeatherResponse, "daily");
        if (dailyJson != null && !dailyJson.isEmpty()) {
            // Extract JSON arrays
            String datesJson = JsonParser.extractStringFromJson(dailyJson, "time");
            String maxTempJson = JsonParser.extractStringFromJson(dailyJson, "temperature_2m_max");
            String minTempJson = JsonParser.extractStringFromJson(dailyJson, "temperature_2m_min");
            String precipSumJson = JsonParser.extractStringFromJson(dailyJson, "precipitation_sum");
            String windSpeedJson = JsonParser.extractStringFromJson(dailyJson, "windspeed_10m_mean");
            String humidityJson = JsonParser.extractStringFromJson(dailyJson, "relative_humidity_2m_mean");
            String soilTempJson = JsonParser.extractStringFromJson(dailyJson, "soil_temperature_0_to_7cm_mean");

            // Parse arrays
            String[] dates = JsonParser.parseJsonArray(datesJson);
            String[] maxTemps = JsonParser.parseJsonArray(maxTempJson);
            String[] minTemps = JsonParser.parseJsonArray(minTempJson);
            String[] precipSums = JsonParser.parseJsonArray(precipSumJson);
            String[] windSpeeds = JsonParser.parseJsonArray(windSpeedJson);
            String[] humidities = JsonParser.parseJsonArray(humidityJson);
            String[] soilTemps = JsonParser.parseJsonArray(soilTempJson);

            // Clear and populate the grid
            forecastGrid.getChildren().clear();
            forecastGrid.add(new Label("Date"), 0, 0);
            forecastGrid.add(new Label("Min Temp"), 1, 0);
            forecastGrid.add(new Label("Max Temp"), 2, 0);
            forecastGrid.add(new Label("Precipitation"), 3, 0);
            forecastGrid.add(new Label("Wind Speed"), 4, 0);
            forecastGrid.add(new Label("Humidity"), 5, 0);
            forecastGrid.add(new Label("Soil Temp"), 6, 0);

            // Display up to 30 days of data
            int days = Math.min(dates.length, 30);
            for (int i = 0; i < days; i++) {
                forecastGrid.add(new Label(DateFormatter.formatDate(dates[i])), 0, i + 1);
                forecastGrid.add(new Label(minTemps[i] + " °C"), 1, i + 1);
                forecastGrid.add(new Label(maxTemps[i] + " °C"), 2, i + 1);
                forecastGrid.add(new Label(precipSums[i] + " mm"), 3, i + 1);

                // Add additional data with error handling
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