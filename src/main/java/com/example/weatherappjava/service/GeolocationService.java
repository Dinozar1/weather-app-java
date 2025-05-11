package com.example.weatherappjava.service;

import com.example.weatherappjava.model.LocationData;
import com.example.weatherappjava.util.HttpUtil;
import com.example.weatherappjava.util.JsonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service for fetching geolocation data from the Open-Meteo geocoding API.
 */
public class GeolocationService {
    private String rawGeoResponse;

    /**
     * Retrieves location data (name, coordinates) for a given city.
     */
    public LocationData getLocationByCity(String city) throws IOException {
        // Build geocoding API URL with encoded city name
        String geoApiUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
                + URLEncoder.encode(city, StandardCharsets.UTF_8)
                + "&count=1&language=pl&format=json";

        // Fetch raw JSON response
        rawGeoResponse = HttpUtil.makeHttpRequest(geoApiUrl);

        // Validate response
        if (!rawGeoResponse.contains("\"results\"") || rawGeoResponse.contains("\"results\":[]")) {
            throw new IOException("City not found: " + city);
        }

        // Parse coordinates and name from JSON
        double latitude = JsonParser.extractDoubleFromJson(rawGeoResponse, "latitude");
        double longitude = JsonParser.extractDoubleFromJson(rawGeoResponse, "longitude");
        String name = JsonParser.extractStringFromJson(rawGeoResponse, "name");

        return new LocationData(name, latitude, longitude);
    }

    /**
     * Returns the raw JSON response from the last geocoding request.
     */
    public String getRawGeoResponse() {
        return rawGeoResponse;
    }
}