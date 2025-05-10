package com.example.weatherappjava.util;

public class JsonParser {
    // Metody przeniesione z HelloController
    public static double extractDoubleFromJson(String json, String key) {
        String valueStr = extractStringFromJson(json, key);
        try {
            return Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static String extractStringFromJson(String json, String key) {
        return extractStringFromJson(json, key, null);
    }

    public static String extractStringFromJson(String json, String key, String section) {
        try {
            // Cała implementacja przeniesiona z oryginalnego HelloController
            // Jeśli section is specified, first find the section
            if (section != null) {
                int sectionStart = json.indexOf("\"" + section + "\"");
                if (sectionStart >= 0) {
                    // Find the opening brace after the section name
                    int braceStart = json.indexOf("{", sectionStart);
                    // Find the closing brace
                    int braceEnd = findMatchingClosingBrace(json, braceStart);
                    // Extract the section JSON
                    if (braceStart >= 0 && braceEnd > braceStart) {
                        json = json.substring(braceStart, braceEnd + 1);
                    }
                }
            }

            // Find the key
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex < 0) return "";

            // Find the colon after the key
            int colonIndex = json.indexOf(":", keyIndex);
            if (colonIndex < 0) return "";

            // Skip whitespace after the colon
            int valueStart = colonIndex + 1;
            while (valueStart < json.length() &&
                    (json.charAt(valueStart) == ' ' || json.charAt(valueStart) == '\t' ||
                            json.charAt(valueStart) == '\n' || json.charAt(valueStart) == '\r')) {
                valueStart++;
            }

            // Check what kind of value we have
            char firstChar = json.charAt(valueStart);
            if (firstChar == '"') {
                // String value, find the closing quote
                int valueEnd = json.indexOf("\"", valueStart + 1);
                if (valueEnd > valueStart) {
                    return json.substring(valueStart + 1, valueEnd);
                }
            } else if (firstChar == '{' || firstChar == '[') {
                // Object or array, find the matching closing brace/bracket
                int valueEnd = findMatchingClosingBrace(json, valueStart);
                if (valueEnd > valueStart) {
                    return json.substring(valueStart, valueEnd + 1);
                }
            } else {
                // Numeric value or boolean, find the end (comma, brace, bracket, or end of string)
                int commaIndex = json.indexOf(",", valueStart);
                int braceIndex = json.indexOf("}", valueStart);
                int bracketIndex = json.indexOf("]", valueStart);

                int valueEnd = json.length();
                if (commaIndex > 0 && commaIndex < valueEnd) valueEnd = commaIndex;
                if (braceIndex > 0 && braceIndex < valueEnd) valueEnd = braceIndex;
                if (bracketIndex > 0 && bracketIndex < valueEnd) valueEnd = bracketIndex;

                return json.substring(valueStart, valueEnd).trim();
            }
        } catch (Exception e) {
            System.err.println("Error extracting value for key '" + key + "': " + e.getMessage());
        }

        return "";
    }

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

    public static String[] parseJsonArray(String jsonArray) {
        if (jsonArray == null || jsonArray.isEmpty()) return new String[0];

        // Remove square brackets
        jsonArray = jsonArray.trim();
        if (jsonArray.startsWith("[")) jsonArray = jsonArray.substring(1);
        if (jsonArray.endsWith("]")) jsonArray = jsonArray.substring(0, jsonArray.length() - 1);

        // Split by commas, but respect quotes
        return jsonArray.split(",");
    }

    public static String formatJson(String json) {
        if (json == null || json.isEmpty()) return "";

        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        boolean inQuotes = false;

        for (char c : json.toCharArray()) {
            if (c == '\"' && formatted.length() > 0 && formatted.charAt(formatted.length() - 1) != '\\') {
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

    private static void addIndentation(StringBuilder sb, int count) {
        for (int i = 0; i < count; i++) {
            sb.append("  ");
        }
    }
}