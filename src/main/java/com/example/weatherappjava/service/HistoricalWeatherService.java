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

            // Track whether we have valid data for initial weather properties
            boolean hasInitialData = false;
            double initialWindSpeed = 0;
            double initialSoilTemp = 0;
            double initialHumidity = 0;
            double initialPressure = 0;

            // Process daily data
            for (int i = 0; i < dates.length; i++) {
                // Initialize with special values to indicate missing data
                Double avgTemp = null;
                Double precipVal = null;
                Double windSpeed = null;
                Double humidity = null;
                Double pressure = null;
                Double soilTemp = null;

                // Parse values with error handling - using null to represent missing data
                try {
                    if (maxTemps != null && minTemps != null &&
                            i < maxTemps.length && i < minTemps.length &&
                            maxTemps[i] != null && !maxTemps[i].equals("null") &&
                            minTemps[i] != null && !minTemps[i].equals("null")) {

                        double maxTemp = Double.parseDouble(maxTemps[i]);
                        double minTemp = Double.parseDouble(minTemps[i]);
                        avgTemp = (maxTemp + minTemp) / 2;
                    }
                } catch (NumberFormatException e) {
                    avgTemp = null;
                }

                try {
                    if (precipSums != null && i < precipSums.length &&
                            precipSums[i] != null && !precipSums[i].equals("null")) {
                        precipVal = Double.parseDouble(precipSums[i]);
                    }
                } catch (NumberFormatException e) {
                    precipVal = null;
                }

                try {
                    if (windSpeeds != null && i < windSpeeds.length &&
                            windSpeeds[i] != null && !windSpeeds[i].equals("null")) {
                        windSpeed = Double.parseDouble(windSpeeds[i]);
                        if (i == 0 || !hasInitialData) {
                            initialWindSpeed = windSpeed;
                        }
                    }
                } catch (NumberFormatException e) {
                    windSpeed = null;
                }

                try {
                    if (humidities != null && i < humidities.length &&
                            humidities[i] != null && !humidities[i].equals("null")) {
                        humidity = Double.parseDouble(humidities[i]);
                        if (i == 0 || !hasInitialData) {
                            initialHumidity = humidity;
                        }
                    }
                } catch (NumberFormatException e) {
                    humidity = null;
                }

                try {
                    if (pressures != null && i < pressures.length &&
                            pressures[i] != null && !pressures[i].equals("null")) {
                        pressure = Double.parseDouble(pressures[i]);
                        if (i == 0 || !hasInitialData) {
                            initialPressure = pressure;
                        }
                    }
                } catch (NumberFormatException e) {
                    pressure = null;
                }

                try {
                    if (soilTemps != null && i < soilTemps.length &&
                            soilTemps[i] != null && !soilTemps[i].equals("null")) {
                        soilTemp = Double.parseDouble(soilTemps[i]);
                        if (i == 0 || !hasInitialData) {
                            initialSoilTemp = soilTemp;
                        }
                    }
                } catch (NumberFormatException e) {
                    soilTemp = null;
                }

                // Add data point to WeatherData only if we have valid data
                // Skip this date point on the chart if data is missing
                if (avgTemp != null && precipVal != null && windSpeed != null && pressure != null && soilTemp != null) {
                    weatherData.addChartDataPoint(
                            windSpeed,
                            soilTemp,
                            avgTemp,
                            precipVal,
                            pressure,
                            DateFormatter.formatDate(dates[i])
                    );

                    // Set initial weather properties from the first valid data point
                    if (!hasInitialData) {
                        hasInitialData = true;
                        weatherData.setWindSpeed(windSpeed);
                        weatherData.setSoilTemperature(soilTemp);
                        weatherData.setHumidity(humidity != null ? humidity : 0);
                        weatherData.setPressure(pressure);
                    }
                }
            }
        }
    }

    /**
     * Displays historical weather data in a grid layout
     */
    public void displayHistoricalDataInGrid(GridPane forecastGrid, WeatherData weatherData) {
        // Clear existing items
        forecastGrid.getChildren().clear();

        // Add headers
        forecastGrid.add(new Label("Date"), 0, 0);
        forecastGrid.add(new Label("Min Temp"), 1, 0);
        forecastGrid.add(new Label("Max Temp"), 2, 0);
        forecastGrid.add(new Label("Precipitation"), 3, 0);
        forecastGrid.add(new Label("Wind Speed"), 4, 0);
        forecastGrid.add(new Label("Humidity"), 5, 0);
        forecastGrid.add(new Label("Soil Temp"), 6, 0);

        // Extract data from JSON if available
        String dailyJson = JsonParser.extractStringFromJson(rawWeatherResponse, "daily");
        if (dailyJson == null || dailyJson.isEmpty()) {
            // If no data, just show a message in the first row
            forecastGrid.add(new Label("No weather data available"), 0, 1, 7, 1);
            return;
        }

        // Extract and parse date array first (required for iteration)
        String datesJson = JsonParser.extractStringFromJson(dailyJson, "time");
        String[] dates = JsonParser.parseJsonArray(datesJson);
        if (dates == null || dates.length == 0) {
            forecastGrid.add(new Label("No date data available"), 0, 1, 7, 1);
            return;
        }

        // Extract all data arrays
        String[] maxTemps = safeParseJsonArray(dailyJson, "temperature_2m_max");
        String[] minTemps = safeParseJsonArray(dailyJson, "temperature_2m_min");
        String[] precipSums = safeParseJsonArray(dailyJson, "precipitation_sum");
        String[] windSpeeds = safeParseJsonArray(dailyJson, "windspeed_10m_mean");
        String[] humidities = safeParseJsonArray(dailyJson, "relative_humidity_2m_mean");
        String[] soilTemps = safeParseJsonArray(dailyJson, "soil_temperature_0_to_7cm_mean");

        // Populate the grid with data
        for (int i = 0; i < dates.length; i++) {
            int row = i + 1; // Start from row 1 (after header)

            // Add date
            forecastGrid.add(new Label(DateFormatter.formatDate(dates[i])), 0, row);

            // Add all values with safe access
            forecastGrid.add(new Label(safeGetValueWithUnit(minTemps, i, "°C")), 1, row);
            forecastGrid.add(new Label(safeGetValueWithUnit(maxTemps, i, "°C")), 2, row);
            forecastGrid.add(new Label(safeGetValueWithUnit(precipSums, i, "mm")), 3, row);
            forecastGrid.add(new Label(safeGetValueWithUnit(windSpeeds, i, "km/h")), 4, row);
            forecastGrid.add(new Label(safeGetValueWithUnit(humidities, i, "%")), 5, row);
            forecastGrid.add(new Label(safeGetValueWithUnit(soilTemps, i, "°C")), 6, row);
        }
    }

    /**
     * Safely parses a JSON array from a JSON object
     */
    private String[] safeParseJsonArray(String jsonObject, String key) {
        try {
            String jsonArray = JsonParser.extractStringFromJson(jsonObject, key);
            return JsonParser.parseJsonArray(jsonArray);
        } catch (Exception e) {
            LOGGER.warning("Error parsing JSON array for key: " + key + " - " + e.getMessage());
            return new String[0];
        }
    }

    /**
     * Safely gets a value from an array with proper formatting or returns "N/A"
     */
    private String safeGetValueWithUnit(String[] array, int index, String unit) {
        if (array == null || index >= array.length || array[index] == null || array[index].equals("null")) {
            return "N/A";
        }

        try {
            // Try to parse and format as number for validation
            Double.parseDouble(array[index]);
            return array[index] + " " + unit;
        } catch (NumberFormatException e) {
            return "N/A";
        }
    }
}