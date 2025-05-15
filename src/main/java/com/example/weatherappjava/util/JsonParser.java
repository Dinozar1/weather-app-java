package com.example.weatherappjava.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for parsing and formatting JSON data.
 */
public class JsonParser {
    /**
     * Extracts a double value from JSON for a given key.
     */
    public static double extractDoubleFromJson(String json, String key) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.optDouble(key, 0.0);
        } catch (JSONException e) {
            System.err.println("Error extracting double for key '" + key + "': " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Extracts a string value from JSON for a given key.
     */
    public static String extractStringFromJson(String json, String key) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.optString(key, "");
        } catch (JSONException e) {
            System.err.println("Error extracting value for key '" + key + "': " + e.getMessage());
            return "";
        }
    }

    /**
     * Parses a JSON array into a string array.
     */
    public static String[] parseJsonArray(String jsonArray) {
        try {
            if (jsonArray == null || jsonArray.isEmpty()) return new String[0];

            JSONArray array = new JSONArray(jsonArray);
            String[] result = new String[array.length()];

            for (int i = 0; i < array.length(); i++) {
                result[i] = array.optString(i, "");
            }

            return result;
        } catch (JSONException e) {
            System.err.println("Error parsing JSON array: " + e.getMessage());
            return new String[0];
        }
    }
}