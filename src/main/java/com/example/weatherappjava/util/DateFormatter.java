package com.example.weatherappjava.util;

/**
 * Utility class for formatting dates from API responses.
 */
public class DateFormatter {
    /**
     * Converts API date (YYYY-MM-DD) to a readable format (DD.MM.YYYY).
     */
    public static String formatDate(String apiDate) {
        if (apiDate == null || apiDate.isEmpty()) return apiDate;

        try {
            // Split and reformat date
            String[] parts = apiDate.split("-");
            if (parts.length == 3) {
                return parts[2] + "." + parts[1] + "." + parts[0];
            }
        } catch (Exception e) {
            return "Parse error: " + apiDate;
        }
        return apiDate;
    }
}