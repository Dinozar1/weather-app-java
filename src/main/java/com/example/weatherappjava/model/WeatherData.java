package com.example.weatherappjava.model;

import java.util.ArrayList;
import java.util.List;

public class WeatherData {
    private double temperature;
    private double windSpeed;
    private double humidity;
    private double pressure;
    private double soilTemperature;
    private double precipitation;
    private String time;

    // Dane do wykresów
    private List<Double> windSpeedData = new ArrayList<>();
    private List<Double> soilTempData = new ArrayList<>();
    private List<Double> airTempData = new ArrayList<>();
    private List<Double> rainData = new ArrayList<>();
    private List<Double> pressureData = new ArrayList<>();
    private List<String> timeLabels = new ArrayList<>();

    // Gettery i settery - pojedyncze wartości
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

    // Metody do zarządzania danymi wykresów
    public void clearChartData() {
        windSpeedData.clear();
        soilTempData.clear();
        airTempData.clear();
        rainData.clear();
        pressureData.clear();
        timeLabels.clear();
    }

    public void addChartDataPoint(double windSpeed, double soilTemp, double airTemp,
                                  double rain, double pressure, String timeLabel) {
        windSpeedData.add(windSpeed);
        soilTempData.add(soilTemp);
        airTempData.add(airTemp);
        rainData.add(rain);
        pressureData.add(pressure);
        timeLabels.add(timeLabel);
    }

    // Gettery dla list danych wykresów
    public List<Double> getWindSpeedData() {
        return windSpeedData;
    }

    public void setWindSpeedData(List<Double> windSpeedData) {
        this.windSpeedData = windSpeedData;
    }

    public List<Double> getSoilTempData() {
        return soilTempData;
    }

    public void setSoilTempData(List<Double> soilTempData) {
        this.soilTempData = soilTempData;
    }

    public List<Double> getAirTempData() {
        return airTempData;
    }

    public void setAirTempData(List<Double> airTempData) {
        this.airTempData = airTempData;
    }

    public List<Double> getRainData() {
        return rainData;
    }

    public void setRainData(List<Double> rainData) {
        this.rainData = rainData;
    }

    public List<Double> getPressureData() {
        return pressureData;
    }

    public void setPressureData(List<Double> pressureData) {
        this.pressureData = pressureData;
    }

    public List<String> getTimeLabels() {
        return timeLabels;
    }

    public void setTimeLabels(List<String> timeLabels) {
        this.timeLabels = timeLabels;
    }
}
