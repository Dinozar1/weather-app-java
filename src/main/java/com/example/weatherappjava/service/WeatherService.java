package com.example.weatherappjava.service;

import com.example.weatherappjava.model.LocationData;
import com.example.weatherappjava.model.WeatherData;
import com.example.weatherappjava.util.DateFormatter;
import com.example.weatherappjava.util.HttpUtil;
import com.example.weatherappjava.util.JsonParser;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.io.IOException;

public class WeatherService {
    private String rawWeatherResponse;

    public String getRawWeatherResponse() {
        return rawWeatherResponse;
    }

    public WeatherData getCurrentWeather(LocationData location) throws IOException {
        // Enhanced API URL to include all necessary parameters for both current and forecast data
        String weatherApiUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + location.getLatitude() +
                "&longitude=" + location.getLongitude() +
                "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,surface_pressure,precipitation,soil_temperature_0cm" +
                "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code," +
                "windspeed_10m_mean,relative_humidity_2m_mean,surface_pressure_mean" +
                "&timezone=auto&forecast_days=7";

        rawWeatherResponse = HttpUtil.makeHttpRequest(weatherApiUrl);

        // Tworzenie i wypełnianie obiektu WeatherData
        WeatherData weatherData = new WeatherData();

        // Extract current weather data from JSON
        String currentJson = JsonParser.extractStringFromJson(rawWeatherResponse, "current");

        weatherData.setTemperature(JsonParser.extractDoubleFromJson(currentJson, "temperature_2m"));
        weatherData.setWindSpeed(JsonParser.extractDoubleFromJson(currentJson, "wind_speed_10m"));
        weatherData.setHumidity(JsonParser.extractDoubleFromJson(currentJson, "relative_humidity_2m"));
        weatherData.setPressure(JsonParser.extractDoubleFromJson(currentJson, "surface_pressure"));
        weatherData.setSoilTemperature(JsonParser.extractDoubleFromJson(currentJson, "soil_temperature_0cm"));
        weatherData.setPrecipitation(JsonParser.extractDoubleFromJson(currentJson, "precipitation"));
        weatherData.setTime(JsonParser.extractStringFromJson(currentJson, "time"));

        // Dodanie danych dla bieżącej pogody do list do wykresów
        weatherData.addChartDataPoint(
                weatherData.getWindSpeed(),
                weatherData.getSoilTemperature(),
                weatherData.getTemperature(),
                weatherData.getPrecipitation(),
                weatherData.getPressure(),
                "Aktualne"
        );

        // Extract forecast data - forecasts will be saved to chart data lists
        processForecastData(weatherData);

        return weatherData;
    }

    private void processForecastData(WeatherData weatherData) {
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

            // Parse arrays
            String[] dates = JsonParser.parseJsonArray(datesJson);
            String[] maxTemps = JsonParser.parseJsonArray(maxTempJson);
            String[] minTemps = JsonParser.parseJsonArray(minTempJson);
            String[] precipSums = JsonParser.parseJsonArray(precipSumJson);

            // Parse additional parameter arrays
            String[] windSpeeds = JsonParser.parseJsonArray(windSpeedJson);
            String[] humidities = JsonParser.parseJsonArray(humidityJson);
            String[] pressures = JsonParser.parseJsonArray(pressureJson);

            // Zapisz dane prognozy do list dla wykresów
            for (int i = 0; i < dates.length; i++) {
                // Dla prognozy możemy użyć średniej temp max i min
                double avgTemp = 0;
                double rainValue = 0;
                double windSpeed = 0;
                double humidity = 0;
                double pressure = 0;
                double soilTemp = 0; // W trybie prognozy nie mamy temperatury gleby, ale utrzymujemy zmienną dla spójności

                try {
                    avgTemp = (Double.parseDouble(maxTemps[i]) + Double.parseDouble(minTemps[i])) / 2;
                } catch (NumberFormatException e) {
                    avgTemp = 0;
                }

                try {
                    rainValue = Double.parseDouble(precipSums[i]);
                } catch (NumberFormatException e) {
                    rainValue = 0.0;
                }

                // Parse additional parameters with error handling
                try {
                    windSpeed = Double.parseDouble(windSpeeds[i]);
                } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException e) {
                    windSpeed = 0.0;
                }

                try {
                    humidity = Double.parseDouble(humidities[i]);
                } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException e) {
                    humidity = 0.0;
                }

                try {
                    pressure = Double.parseDouble(pressures[i]);
                } catch (NumberFormatException | NullPointerException | ArrayIndexOutOfBoundsException e) {
                    pressure = 0.0;
                }

                // Dodaj dane do list wykresów
                weatherData.addChartDataPoint(
                        windSpeed,
                        soilTemp, // Przekazujemy 0 jako temperatura gleby w prognozie
                        avgTemp,
                        rainValue,
                        pressure,
                        DateFormatter.formatDate(dates[i])
                );
            }
        }
    }

    public void displayForecastInGrid(GridPane forecastGrid, WeatherData weatherData) {
        String dailyJson = JsonParser.extractStringFromJson(rawWeatherResponse, "daily");
        if (dailyJson != null && !dailyJson.isEmpty()) {
            String datesJson = JsonParser.extractStringFromJson(dailyJson, "time");
            String maxTempJson = JsonParser.extractStringFromJson(dailyJson, "temperature_2m_max");
            String minTempJson = JsonParser.extractStringFromJson(dailyJson, "temperature_2m_min");
            String precipSumJson = JsonParser.extractStringFromJson(dailyJson, "precipitation_sum");

            // Extract additional parameters
            String windSpeedJson = JsonParser.extractStringFromJson(dailyJson, "windspeed_10m_mean");
            String humidityJson = JsonParser.extractStringFromJson(dailyJson, "relative_humidity_2m_mean");

            // Parse arrays
            String[] dates = JsonParser.parseJsonArray(datesJson);
            String[] maxTemps = JsonParser.parseJsonArray(maxTempJson);
            String[] minTemps = JsonParser.parseJsonArray(minTempJson);
            String[] precipSums = JsonParser.parseJsonArray(precipSumJson);

            // Parse additional parameter arrays
            String[] windSpeeds = JsonParser.parseJsonArray(windSpeedJson);
            String[] humidities = JsonParser.parseJsonArray(humidityJson);

            // Clear grid and add forecast data
            forecastGrid.getChildren().clear();

            // Add headers
            forecastGrid.add(new Label("Data"), 0, 0);
            forecastGrid.add(new Label("Min. Temp."), 1, 0);
            forecastGrid.add(new Label("Max. Temp."), 2, 0);
            forecastGrid.add(new Label("Opady"), 3, 0);
            forecastGrid.add(new Label("Wiatr"), 4, 0);
            forecastGrid.add(new Label("Wilgotność"), 5, 0);

            // Display forecast data
            int days = Math.min(dates.length, 7); // Show up to 7 days
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
            }
        }
    }
}