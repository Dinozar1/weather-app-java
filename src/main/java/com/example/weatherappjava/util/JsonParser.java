package com.example.weatherappjava.util;

/**
 * Utility class for parsing and formatting JSON data.
 */
public class JsonParser {
    /**
     * Extracts a double value from JSON for a given key.
     */
    public static double extractDoubleFromJson(String json, String key) {
        String valueStr = extractStringFromJson(json, key);
        try {
            return Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Extracts a string value from JSON for a given key.
     */
    public static String extractStringFromJson(String json, String key) {
        return extractStringFromJson(json, key, null);
    }

    /**
     * Extracts a string value from JSON, optionally within a specified section.
     */
    public static String extractStringFromJson(String json, String key, String section) {
        try {
            // Narrow down to a section if specified
            if (section != null) {
                int sectionStart = json.indexOf("\"" + section + "\"");
                if (sectionStart >= 0) {
                    int braceStart = json.indexOf("{", sectionStart);
                    int braceEnd = findMatchingClosingBrace(json, braceStart);
                    if (braceStart >= 0 && braceEnd > braceStart) {
                        json = json.substring(braceStart, braceEnd + 1);
                    }
                }
            }

            // Locate key and value
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex < 0) return "";
            int colonIndex = json.indexOf(":", keyIndex);
            if (colonIndex < 0) return "";

            // Skip whitespace after colon
            int valueStart = colonIndex + 1;
            while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
                valueStart++;
            }

            // Extract value based on type
            char firstChar = json.charAt(valueStart);
            if (firstChar == '"') {
                int valueEnd = json.indexOf("\"", valueStart + 1);
                return (valueEnd > valueStart) ? json.substring(valueStart + 1, valueEnd) : "";
            } else if (firstChar == '{' || firstChar == '[') {
                int valueEnd = findMatchingClosingBrace(json, valueStart);
                return (valueEnd > valueStart) ? json.substring(valueStart, valueEnd + 1) : "";
            } else {
                int valueEnd = json.length();
                for (char endChar : new char[]{',', '}', ']'}) {
                    int index = json.indexOf(endChar, valueStart);
                    if (index > 0 && index < valueEnd) valueEnd = index;
                }
                return json.substring(valueStart, valueEnd).trim();
            }
        } catch (Exception e) {
            System.err.println("Error extracting value for key '" + key + "': " + e.getMessage());
            return "";
        }
    }

    /**
     * Finds the matching closing brace or bracket for a given opening character.
     */
    public static int findMatchingClosingBrace(String json, int openingBraceIndex) {
        char openBrace = json.charAt(openingBraceIndex);
        char closeBrace = (openBrace == '{') ? '}' : (openBrace == '[') ? ']' : openBrace;

        int count = 1;
        for (int i = openingBraceIndex + 1; i < json.length(); i++) {
            if (json.charAt(i) == openBrace) count++;
            else if (json.charAt(i) == closeBrace) count--;
            if (count == 0) return i;
        }
        return -1;
    }

    /**
     * Parses a JSON array into a string array.
     */
    public static String[] parseJsonArray(String jsonArray) {
        if (jsonArray == null || jsonArray.isEmpty()) return new String[0];

        // Remove brackets and split by commas
        jsonArray = jsonArray.trim();
        if (jsonArray.startsWith("[")) jsonArray = jsonArray.substring(1);
        if (jsonArray.endsWith("]")) jsonArray = jsonArray.substring(0, jsonArray.length() - 1);
        return jsonArray.split(",");
    }

    /**
     * Formats a JSON string for readability with indentation.
     */
    public static String formatJson(String json) {
        if (json == null || json.isEmpty()) return "";

        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        boolean inQuotes = false;

        for (char c : json.toCharArray()) {
            if (c == '\"' && !formatted.isEmpty() && formatted.charAt(formatted.length() - 1) != '\\') {
                inQuotes = !inQuotes;
                formatted.append(c);
            } else if (!inQuotes) {
                if (c == '{' || c == '[') {
                    formatted.append(c).append('\n');
                    indentLevel++;
                    addIndentation(formatted, indentLevel);
                } else if (c == '}' || c == ']') {
                    formatted.append('\n');
                    indentLevel--;
                    addIndentation(formatted, indentLevel);
                    formatted.append(c);
                } else if (c == ',') {
                    formatted.append(c).append('\n');
                    addIndentation(formatted, indentLevel);
                } else if (c == ':') {
                    formatted.append(c).append(' ');
                } else if (!Character.isWhitespace(c)) {
                    formatted.append(c);
                }
            } else {
                formatted.append(c);
            }
        }
        return formatted.toString();
    }

    /**
     * Adds indentation to a StringBuilder based on the indent level.
     */
    private static void addIndentation(StringBuilder sb, int count) {
        sb.append("  ".repeat(Math.max(0, count)));
    }
}