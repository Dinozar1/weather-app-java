package com.example.weatherappjava.model;

/**
 * Model class representing location data with a name and geographic coordinates.
 */
public class LocationData {
    private final String name;
    private final double latitude;
    private final double longitude;

    /**
     * Constructor initializing location data.
     */
    public LocationData(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    /**
     * Returns a string representation of the location, using name if available, otherwise coordinates.
     */
    @Override
    public String toString() {
        if (name != null && !name.isEmpty()) {
            return name;
        } else {
            return "Coordinates: " + latitude + ", " + longitude;
        }
    }
}