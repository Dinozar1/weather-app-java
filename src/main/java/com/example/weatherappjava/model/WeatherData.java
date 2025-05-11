package com.example.weatherappjava.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing weather data for display and visualization.
 */
public class WeatherData {
    // Current weather metrics
    private double temperature;
    private double windSpeed;
    private double humidity;
    private double pressure;
    private double soilTemperature;
    private double precipitation;
    private String time;

    // Chart data lists (immutable references)
    private final List<Double> windSpeedData = new ArrayList<>();
    private final List<Double> soilTempData = new ArrayList<>();
    private final List<Double> airTempData = new ArrayList<>();
    private final List<Double> rainData = new ArrayList<>();
    private final List<Double> pressureData = new ArrayList<>();
    private final List<String> timeLabels = new ArrayList<>();

    // Getters and setters for current weather metrics
    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getSoilTemperature() {
        return soilTemperature;
    }

    public void setSoilTemperature(double soilTemperature) {
        this.soilTemperature = soilTemperature;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Clears all chart data lists.
     */
    public void clearChartData() {
        windSpeedData.clear();
        soilTempData.clear();
        airTempData.clear();
        rainData.clear();
        pressureData.clear();
        timeLabels.clear();
    }

    /**
     * Adds a data point for all chart metrics with a time label.
     */
    public void addChartDataPoint(double windSpeed, double soilTemp, double airTemp,
                                  double rain, double pressure, String timeLabel) {
        windSpeedData.add(windSpeed);
        soilTempData.add(soilTemp);
        airTempData.add(airTemp);
        rainData.add(rain);
        pressureData.add(pressure);
        timeLabels.add(timeLabel);
    }

    // Getters for chart data lists
    public List<Double> getWindSpeedData() {
        return windSpeedData;
    }

    public List<Double> getSoilTempData() {
        return soilTempData;
    }

    public List<Double> getAirTempData() {
        return airTempData;
    }

    public List<Double> getRainData() {
        return rainData;
    }

    public List<Double> getPressureData() {
        return pressureData;
    }

    public List<String> getTimeLabels() {
        return timeLabels;
    }
}