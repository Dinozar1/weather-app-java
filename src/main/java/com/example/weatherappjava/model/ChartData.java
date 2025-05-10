package com.example.weatherappjava.model;

import java.util.ArrayList;
import java.util.List;

public class ChartData {
    private List<Double> windSpeedData;
    private List<Double> soilTempData;
    private List<Double> airTempData;
    private List<Double> rainData;
    private List<Double> pressureData;
    private List<String> timeLabels;

    public ChartData() {
        windSpeedData = new ArrayList<>();
        soilTempData = new ArrayList<>();
        airTempData = new ArrayList<>();
        rainData = new ArrayList<>();
        pressureData = new ArrayList<>();
        timeLabels = new ArrayList<>();
    }

    public void clear() {
        windSpeedData.clear();
        soilTempData.clear();
        airTempData.clear();
        rainData.clear();
        pressureData.clear();
        timeLabels.clear();
    }

    public void addDataPoint(Double windSpeed, Double soilTemp, Double airTemp,
                             Double rain, Double pressure, String timeLabel) {
        windSpeedData.add(windSpeed);
        soilTempData.add(soilTemp);
        airTempData.add(airTemp);
        rainData.add(rain);
        pressureData.add(pressure);
        timeLabels.add(timeLabel);
    }


    public void addDataPoints(List<Double> windSpeeds, List<Double> soilTemps,
                              List<Double> airTemps, List<Double> rains,
                              List<Double> pressures, List<String> labels) {
        if (windSpeeds != null) {
            windSpeedData.addAll(windSpeeds);
        }
        if (soilTemps != null) {
            soilTempData.addAll(soilTemps);
        }
        if (airTemps != null) {
            airTempData.addAll(airTemps);
        }
        if (rains != null) {
            rainData.addAll(rains);
        }
        if (pressures != null) {
            pressureData.addAll(pressures);
        }
        if (labels != null) {
            timeLabels.addAll(labels);
        }
    }

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

    public boolean hasData() {
        return !timeLabels.isEmpty();
    }

    public int getDataSize() {
        return timeLabels.size();
    }

    public boolean hasCompleteDataAt(int index) {
        if (index < 0 || index >= timeLabels.size()) {
            return false;
        }

        return index < windSpeedData.size() &&
                index < soilTempData.size() &&
                index < airTempData.size() &&
                index < rainData.size() &&
                index < pressureData.size();
    }
}