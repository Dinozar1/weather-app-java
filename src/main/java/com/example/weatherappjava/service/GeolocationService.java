package com.example.weatherappjava.service;

import com.example.weatherappjava.model.LocationData;
import com.example.weatherappjava.util.HttpUtil;
import com.example.weatherappjava.util.JsonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GeolocationService {
    private String rawGeoResponse;

    public LocationData getLocationByCity(String city) throws IOException {
        String geoApiUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
                + URLEncoder.encode(city, StandardCharsets.UTF_8.toString())
                + "&count=1&language=pl&format=json";

        rawGeoResponse = HttpUtil.makeHttpRequest(geoApiUrl);

        // Check if we got results
        if (!rawGeoResponse.contains("\"results\"") || rawGeoResponse.contains("\"results\":[]")) {
            throw new IOException("Nie znaleziono miasta o nazwie: " + city);
        }

        // Extract coordinates from geo response
        double latitude = JsonParser.extractDoubleFromJson(rawGeoResponse, "latitude");
        double longitude = JsonParser.extractDoubleFromJson(rawGeoResponse, "longitude");
        String name = JsonParser.extractStringFromJson(rawGeoResponse, "name");

        return new LocationData(name, latitude, longitude);
    }

    public String getRawGeoResponse() {
        return rawGeoResponse;
    }
}