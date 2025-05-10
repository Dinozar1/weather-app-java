package com.example.weatherappjava.util;

public class DateFormatter {
    // Helper to format date from API (YYYY-MM-DD) to a more readable format
    public static String formatDate(String apiDate) {
        if (apiDate == null || apiDate.isEmpty()) return apiDate;

        try {
            // Simple format conversion - in a real app use DateTimeFormatter
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