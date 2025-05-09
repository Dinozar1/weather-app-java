package com.example.weatherappjava;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;


public class HelloController {
    // UI elements for location input
    @FXML
    private ToggleGroup locationMethod;

    @FXML
    private RadioButton cityRadioButton;

    @FXML
    private RadioButton coordsRadioButton;

    @FXML
    private HBox cityInputPanel;

    @FXML
    private HBox coordsInputPanel;

    @FXML
    private TextField cityInput;

    @FXML
    private TextField latitudeInput;

    @FXML
    private TextField longitudeInput;

    @FXML
    private Button searchButton;

    // UI elements for weather display
    @FXML
    private Label locationLabel;

    @FXML
    private Label temperatureLabel;

    @FXML
    private Label windSpeedLabel;

    @FXML
    private Label soilTemperatureLabel;

    @FXML
    private Label rainLabel;

    @FXML
    private Label humidityLabel;

    @FXML
    private Label pressureLabel;

    @FXML
    private Label updateTimeLabel;

    @FXML
    private GridPane forecastGrid;

    @FXML
    private Label statusLabel;

    @FXML
    private Hyperlink showRawDataLink;

    @FXML
    private ToggleGroup dataMode;

    @FXML
    private RadioButton forecastRadioButton;

    @FXML
    private RadioButton historicalRadioButton;

    @FXML
    private HBox forecastInputPanel;

    @FXML
    private HBox historicalInputPanel;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private VBox chartOptionsPanel;

    @FXML
    private CheckBox windSpeedCheckBox;

    @FXML
    private CheckBox soilTempCheckBox;

    @FXML
    private CheckBox airTempCheckBox;

    @FXML
    private CheckBox rainCheckBox;

    @FXML
    private CheckBox pressureCheckBox;

    @FXML
    private Button visualizeButton;


    // Variables to store raw data for the dialog
    private String rawGeoResponse;
    private String rawWeatherResponse;

    private List<Double> windSpeedData = new ArrayList<>();
    private List<Double> soilTempData = new ArrayList<>();
    private List<Double> airTempData = new ArrayList<>();
    private List<Double> rainData = new ArrayList<>();
    private List<Double> pressureData = new ArrayList<>();
    private List<String> timeLabels = new ArrayList<>();

    @FXML
    public void initialize() {
        // Make sure the correct input panel is visible initially
        updateInputPanelVisibility();
        updateDataModeVisibility();
    }

    @FXML
    protected void onLocationMethodChanged() {
        updateInputPanelVisibility();
    }
    @FXML
    protected void onDataModeChanged() {
        updateDataModeVisibility();
    }

    @FXML
    protected void onVisualizeButtonClick() {
        boolean anySelected = windSpeedCheckBox.isSelected() ||
                soilTempCheckBox.isSelected() ||
                airTempCheckBox.isSelected() ||
                rainCheckBox.isSelected() ||
                pressureCheckBox.isSelected();

        if (!anySelected) {
            statusLabel.setText("Zaznacz przynajmniej jeden typ danych do wizualizacji.");
            return;
        }

        // Sprawdź, czy mamy dane do wyświetlenia
        if (timeLabels.isEmpty()) {
            statusLabel.setText("Brak danych do wizualizacji. Pobierz najpierw dane pogodowe.");
            return;
        }

        // Wizualizuj każdy zaznaczony rodzaj danych w osobnym oknie
        if (windSpeedCheckBox.isSelected()) {
            openChartWindow("Prędkość wiatru", "km/h", windSpeedData, timeLabels);
        }

        if (soilTempCheckBox.isSelected()) {
            openChartWindow("Temperatura gleby", "°C", soilTempData, timeLabels);
        }

        if (airTempCheckBox.isSelected()) {
            openChartWindow("Temperatura powietrza", "°C", airTempData, timeLabels);
        }

        if (rainCheckBox.isSelected()) {
            openChartWindow("Opady", "mm", rainData, timeLabels);
        }

        if (pressureCheckBox.isSelected()) {
            openChartWindow("Ciśnienie", "hPa", pressureData, timeLabels);
        }
    }

    private void openChartWindow(String title, String yAxisLabel, List<Double> data, List<String> labels) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("chart-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            ChartController chartController = loader.getController();
            chartController.setupChart(title, yAxisLabel, data, labels);
            chartController.setWindowTitle(title);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            statusLabel.setText("Błąd podczas tworzenia wykresu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateInputPanelVisibility() {
        boolean isCityMode = cityRadioButton.isSelected();
        cityInputPanel.setVisible(isCityMode);
        cityInputPanel.setManaged(isCityMode);
        coordsInputPanel.setVisible(!isCityMode);
        coordsInputPanel.setManaged(!isCityMode);
    }

    private void updateDataModeVisibility() {
        boolean isForecastMode = forecastRadioButton.isSelected();
        forecastInputPanel.setVisible(isForecastMode);
        forecastInputPanel.setManaged(isForecastMode);
        historicalInputPanel.setVisible(!isForecastMode);
        historicalInputPanel.setManaged(!isForecastMode);
    }

    @FXML
    protected void onSearchButtonClick() {
        // Clear previous data
        clearWeatherDisplay();

        boolean isForecastMode = forecastRadioButton.isSelected();

        // Check if we need to get location by city name or coordinates
        if (cityRadioButton.isSelected()) {
            String city = cityInput.getText().trim();
            if (city.isEmpty()) {
                statusLabel.setText("Wprowadź nazwę miasta.");
                return;
            }
            if (isForecastMode) {
                getWeatherByCity(city);
            } else {
                getHistoricalWeatherByCity(city);
            }
        } else {
            // Get weather by coordinates
            String latText = latitudeInput.getText().trim();
            String lonText = longitudeInput.getText().trim();

            if (latText.isEmpty() || lonText.isEmpty()) {
                statusLabel.setText("Wprowadź obie współrzędne geograficzne.");
                return;
            }

            try {
                double latitude = Double.parseDouble(latText);
                double longitude = Double.parseDouble(lonText);

                if (latitude < -90 || latitude > 90) {
                    statusLabel.setText("Szerokość geograficzna musi być w zakresie od -90 do 90.");
                    return;
                }

                if (longitude < -180 || longitude > 180) {
                    statusLabel.setText("Długość geograficzna musi być w zakresie od -180 do 180.");
                    return;
                }

                if (isForecastMode) {
                    getWeatherByCoordinates(latitude, longitude);
                } else {
                    getHistoricalWeatherByCoordinates(latitude, longitude);
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Wprowadź poprawne wartości liczbowe dla współrzędnych.");
            }
        }
    }

    private void getWeatherByCity(String city) {
        // Clear status and disable search button
        statusLabel.setText("");
        searchButton.setDisable(true);
        statusLabel.setText("Pobieranie danych pogodowych...");

        // Use CompletableFuture to handle the API call asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                // First, get coordinates for the city
                String geoApiUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
                        + URLEncoder.encode(city, StandardCharsets.UTF_8.toString())
                        + "&count=1&language=pl&format=json";

                rawGeoResponse = makeHttpRequest(geoApiUrl);

                // Check if we got results
                if (!rawGeoResponse.contains("\"results\"") || rawGeoResponse.contains("\"results\":[]")) {
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("Nie znaleziono miasta o nazwie: " + city);
                        searchButton.setDisable(false);
                    });
                    return;
                }

                // Extract coordinates from geo response
                double latitude = extractDoubleFromJson(rawGeoResponse, "latitude");
                double longitude = extractDoubleFromJson(rawGeoResponse, "longitude");
                String name = extractStringFromJson(rawGeoResponse, "name");

                // Get weather data using the coordinates
                getWeatherByCoordinates(latitude, longitude, name);
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Błąd: " + e.getMessage());
                    searchButton.setDisable(false);
                });
            }
        });
    }

    private void getHistoricalWeatherByCity(String city) {
        // Clear status and disable search button
        statusLabel.setText("");
        searchButton.setDisable(true);
        statusLabel.setText("Pobieranie historycznych danych pogodowych...");

        // Use CompletableFuture to handle the API call asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                // First, get coordinates for the city
                String geoApiUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
                        + URLEncoder.encode(city, StandardCharsets.UTF_8.toString())
                        + "&count=1&language=pl&format=json";

                rawGeoResponse = makeHttpRequest(geoApiUrl);

                // Check if we got results
                if (!rawGeoResponse.contains("\"results\"") || rawGeoResponse.contains("\"results\":[]")) {
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("Nie znaleziono miasta o nazwie: " + city);
                        searchButton.setDisable(false);
                    });
                    return;
                }

                // Extract coordinates from geo response
                double latitude = extractDoubleFromJson(rawGeoResponse, "latitude");
                double longitude = extractDoubleFromJson(rawGeoResponse, "longitude");
                String name = extractStringFromJson(rawGeoResponse, "name");

                // Get historical weather data using the coordinates
                getHistoricalWeatherByCoordinates(latitude, longitude, name);
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Błąd: " + e.getMessage());
                    searchButton.setDisable(false);
                });
            }
        });
    }

    // New method for historical weather by coordinates:
    private void getHistoricalWeatherByCoordinates(double latitude, double longitude) {
        getHistoricalWeatherByCoordinates(latitude, longitude, "Współrzędne: " + latitude + ", " + longitude);
    }

    private void getHistoricalWeatherByCoordinates(double latitude, double longitude, String locationName) {
        // Clear status and disable search button if not already done
        if (!searchButton.isDisabled()) {
            statusLabel.setText("");
            searchButton.setDisable(true);
            statusLabel.setText("Pobieranie historycznych danych pogodowych...");
        }

        // Use CompletableFuture to handle the API call asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                // Validate date range
                if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("Wybierz daty początkową i końcową.");
                        searchButton.setDisable(false);
                    });
                    return;
                }

                String startDate = startDatePicker.getValue().toString();
                String endDate = endDatePicker.getValue().toString();

                // Historical weather API URL
                String historicalWeatherApiUrl = "https://archive-api.open-meteo.com/v1/archive?latitude=" + latitude +
                        "&longitude=" + longitude +
                        "&start_date=" + startDate +
                        "&end_date=" + endDate +
                        "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code" +
                        "&timezone=auto";

                rawWeatherResponse = makeHttpRequest(historicalWeatherApiUrl);

                // Update UI with weather data
                javafx.application.Platform.runLater(() -> {
                    try {
                        displayHistoricalWeatherData(rawWeatherResponse, locationName);
                        statusLabel.setText("Historyczne dane pogodowe zostały pobrane.");
                    } catch (Exception e) {
                        statusLabel.setText("Błąd podczas przetwarzania danych: " + e.getMessage());
                    } finally {
                        searchButton.setDisable(false);
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Błąd: " + e.getMessage());
                    searchButton.setDisable(false);
                });
            }
        });
    }

    private void displayHistoricalWeatherData(String weatherData, String locationName) {
        // Set location name
        locationLabel.setText(locationName + " (Dane historyczne)");

        // Clear current weather display
        temperatureLabel.setText("N/D - tryb historyczny");
        windSpeedLabel.setText("N/D - tryb historyczny");
        humidityLabel.setText("N/D - tryb historyczny");
        pressureLabel.setText("N/D - tryb historyczny");
        soilTemperatureLabel.setText("N/D - tryb historyczny");
        rainLabel.setText("N/D - tryb historyczny");
        updateTimeLabel.setText(startDatePicker.getValue().toString() + " do " + endDatePicker.getValue().toString());

        // Extract historical forecast data
        String dailyJson = extractStringFromJson(weatherData, "daily");
        if (dailyJson != null && !dailyJson.isEmpty()) {
            String datesJson = extractStringFromJson(dailyJson, "time");
            String maxTempJson = extractStringFromJson(dailyJson, "temperature_2m_max");
            String minTempJson = extractStringFromJson(dailyJson, "temperature_2m_min");
            String precipSumJson = extractStringFromJson(dailyJson, "precipitation_sum");

            // Parse arrays
            String[] dates = parseJsonArray(datesJson);
            String[] maxTemps = parseJsonArray(maxTempJson);
            String[] minTemps = parseJsonArray(minTempJson);
            String[] precipSums = parseJsonArray(precipSumJson);

            // Zapisz dane historyczne do list
            for (int i = 0; i < dates.length; i++) {
                double avgTemp = 0;
                double precipVal = 0;

                try {
                    avgTemp = (Double.parseDouble(maxTemps[i]) + Double.parseDouble(minTemps[i])) / 2;
                } catch (NumberFormatException e) {
                    // Ignoruj błędy parsowania
                }

                try {
                    precipVal = Double.parseDouble(precipSums[i]);
                } catch (NumberFormatException e) {
                    // Ignoruj błędy parsowania
                }

                // Dodaj dane do list
                airTempData.add(avgTemp);
                rainData.add(precipVal);
                // Dla pozostałych wartości wstawiamy 0 - w rzeczywistej aplikacji
                // należałoby dodać logikę do pobierania tych danych z API
                soilTempData.add(0.0);
                windSpeedData.add(0.0);
                pressureData.add(0.0);

                timeLabels.add(formatDate(dates[i]));
            }

            // Clear grid and add forecast data
            forecastGrid.getChildren().clear();

            // Add headers
            forecastGrid.add(new Label("Data"), 0, 0);
            forecastGrid.add(new Label("Min. Temp."), 1, 0);
            forecastGrid.add(new Label("Max. Temp."), 2, 0);
            forecastGrid.add(new Label("Opady"), 3, 0);

            // Display historical data
            int days = Math.min(dates.length, 30); // Show up to 10 days
            for (int i = 0; i < days; i++) {
                forecastGrid.add(new Label(formatDate(dates[i])), 0, i + 1);
                forecastGrid.add(new Label(minTemps[i] + " °C"), 1, i + 1);
                forecastGrid.add(new Label(maxTemps[i] + " °C"), 2, i + 1);
                forecastGrid.add(new Label(precipSums[i] + " mm"), 3, i + 1);
            }
        }
    }

    private void getWeatherByCoordinates(double latitude, double longitude) {
        getWeatherByCoordinates(latitude, longitude, "Współrzędne: " + latitude + ", " + longitude);
    }

    private void getWeatherByCoordinates(double latitude, double longitude, String locationName) {
        // Clear status and disable search button if not already done
        if (!searchButton.isDisabled()) {
            statusLabel.setText("");
            searchButton.setDisable(true);
            statusLabel.setText("Pobieranie danych pogodowych...");
        }

        // Use CompletableFuture to handle the API call asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                // Rozszerzony URL API o dodatkowe parametry: soil_temperature_0cm i precipitation
                String weatherApiUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude +
                        "&longitude=" + longitude +
                        "&current=temperature_2m,relative_humidity_2m,wind_speed_10m,surface_pressure,precipitation,soil_temperature_0cm" +
                        "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code" +
                        "&timezone=auto&forecast_days=7";

                rawWeatherResponse = makeHttpRequest(weatherApiUrl);

                // Update UI with weather data
                javafx.application.Platform.runLater(() -> {
                    try {
                        displayWeatherData(rawWeatherResponse, locationName);
                        statusLabel.setText("Dane pogodowe zostały pobrane.");
                    } catch (Exception e) {
                        statusLabel.setText("Błąd podczas przetwarzania danych: " + e.getMessage());
                    } finally {
                        searchButton.setDisable(false);
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Błąd: " + e.getMessage());
                    searchButton.setDisable(false);
                });
            }
        });
    }

    private void displayWeatherData(String weatherData, String locationName) {
        // Set location name
        locationLabel.setText(locationName);

        // Extract current weather data - note the "current" section
        String currentJson = extractStringFromJson(weatherData, "current");

        double temperature = extractDoubleFromJson(currentJson, "temperature_2m");
        double windSpeed = extractDoubleFromJson(currentJson, "wind_speed_10m");
        double humidity = extractDoubleFromJson(currentJson, "relative_humidity_2m");
        double pressure = extractDoubleFromJson(currentJson, "surface_pressure");
        double soilTemperature = extractDoubleFromJson(currentJson, "soil_temperature_0cm");
        double precipitation = extractDoubleFromJson(currentJson, "precipitation");
        String time = extractStringFromJson(currentJson, "time");

        windSpeedData.add(windSpeed);
        soilTempData.add(soilTemperature);
        airTempData.add(temperature);
        rainData.add(precipitation);
        pressureData.add(pressure);
        timeLabels.add("Aktualne");

        // Display current weather
        temperatureLabel.setText(String.format("%.1f °C", temperature));
        windSpeedLabel.setText(String.format("%.1f km/h", windSpeed));
        humidityLabel.setText(String.format("%.0f %%", humidity));
        pressureLabel.setText(String.format("%.1f hPa", pressure));
        soilTemperatureLabel.setText(String.format("%.1f °C", soilTemperature));
        rainLabel.setText(String.format("%.2f mm", precipitation));
        updateTimeLabel.setText(time);


        // Extract forecast data - daily min and max temperatures
        String dailyJson = extractStringFromJson(weatherData, "daily");
        if (dailyJson != null && !dailyJson.isEmpty()) {
            String datesJson = extractStringFromJson(dailyJson, "time");
            String maxTempJson = extractStringFromJson(dailyJson, "temperature_2m_max");
            String minTempJson = extractStringFromJson(dailyJson, "temperature_2m_min");
            String precipSumJson = extractStringFromJson(dailyJson, "precipitation_sum");

            // Parse arrays
            String[] dates = parseJsonArray(datesJson);
            String[] maxTemps = parseJsonArray(maxTempJson);
            String[] minTemps = parseJsonArray(minTempJson);
            String[] precipSums = parseJsonArray(precipSumJson);

            // Zapisz dane prognozy do list dla wykresów
            for (int i = 0; i < dates.length; i++) {
                // Dla prognozy możemy użyć średniej temp max i min
                double avgTemp = 0;
                try {
                    avgTemp = (Double.parseDouble(maxTemps[i]) + Double.parseDouble(minTemps[i])) / 2;
                } catch (NumberFormatException e) {
                    avgTemp = 0;
                }

                airTempData.add(avgTemp);

                // Dla pozostałych wartości wstawiamy 0 lub wartości z API jeśli są dostępne
                // W rzeczywistej aplikacji należałoby dodać logikę do pobierania tych danych z API
                soilTempData.add(0.0); // Brak danych o temperaturze gleby w prognozie
                windSpeedData.add(0.0); // Brak danych o wietrze w prognozie
                try {
                    rainData.add(Double.parseDouble(precipSums[i]));
                } catch (NumberFormatException e) {
                    rainData.add(0.0);
                }
                pressureData.add(0.0); // Brak danych o ciśnieniu w prognozie

                timeLabels.add(formatDate(dates[i]));
            }

            // Clear grid and add forecast data
            forecastGrid.getChildren().clear();

            // Add headers
            forecastGrid.add(new Label("Data"), 0, 0);
            forecastGrid.add(new Label("Min. Temp."), 1, 0);
            forecastGrid.add(new Label("Max. Temp."), 2, 0);
            forecastGrid.add(new Label("Opady"), 3, 0);

            // Display forecast data
            int days = Math.min(dates.length, 7); // Show up to 7 days
            for (int i = 0; i < days; i++) {
                forecastGrid.add(new Label(formatDate(dates[i])), 0, i + 1);
                forecastGrid.add(new Label(minTemps[i] + " °C"), 1, i + 1);
                forecastGrid.add(new Label(maxTemps[i] + " °C"), 2, i + 1);
                forecastGrid.add(new Label(precipSums[i] + " mm"), 3, i + 1);
            }
        }
    }

    // Helper to format date from API (YYYY-MM-DD) to a more readable format
    private String formatDate(String apiDate) {
        if (apiDate == null || apiDate.isEmpty()) return apiDate;

        try {
            // Simple format conversion - in a real app use DateTimeFormatter
            String[] parts = apiDate.split("-");
            if (parts.length == 3) {
                return parts[2] + "." + parts[1] + "." + parts[0];
            }
        } catch (Exception e) {
            // Just return original on error
        }
        return apiDate;
    }

    // Helper to parse JSON array into string array
    private String[] parseJsonArray(String jsonArray) {
        if (jsonArray == null || jsonArray.isEmpty()) return new String[0];

        // Remove square brackets
        jsonArray = jsonArray.trim();
        if (jsonArray.startsWith("[")) jsonArray = jsonArray.substring(1);
        if (jsonArray.endsWith("]")) jsonArray = jsonArray.substring(0, jsonArray.length() - 1);

        // Split by commas, but respect quotes
        return jsonArray.split(",");
    }

    private void clearWeatherDisplay() {
        locationLabel.setText("---");
        temperatureLabel.setText("---");
        windSpeedLabel.setText("---");
        humidityLabel.setText("---");
        pressureLabel.setText("---");
        soilTemperatureLabel.setText("---");
        rainLabel.setText("---");
        updateTimeLabel.setText("---");
        forecastGrid.getChildren().clear();
        statusLabel.setText("");


        windSpeedData.clear();
        soilTempData.clear();
        airTempData.clear();
        rainData.clear();
        pressureData.clear();
        timeLabels.clear();
    }

    @FXML
    protected void onShowRawDataClick() {
        // Create a new window to display raw JSON data
        Stage rawDataStage = new Stage();
        rawDataStage.setTitle("Surowe dane JSON");

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(600, 500);

        // Combine both responses with formatting
        StringBuilder content = new StringBuilder();
        if (rawGeoResponse != null) {
            content.append("Dane geolokalizacyjne:\n").append(formatJson(rawGeoResponse)).append("\n\n");
        }
        if (rawWeatherResponse != null) {
            content.append("Dane pogodowe:\n").append(formatJson(rawWeatherResponse));
        }

        textArea.setText(content.length() > 0 ? content.toString() : "Brak danych do wyświetlenia");

        // Create a scene and show the stage
        javafx.scene.Scene scene = new javafx.scene.Scene(new javafx.scene.layout.VBox(textArea));
        rawDataStage.setScene(scene);
        rawDataStage.show();
    }

    // Helper method to extract a double value from JSON
    private double extractDoubleFromJson(String json, String key) {
        String valueStr = extractStringFromJson(json, key);
        try {
            return Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Helper method to extract a string value from JSON
    private String extractStringFromJson(String json, String key) {
        return extractStringFromJson(json, key, null);
    }

    private String extractStringFromJson(String json, String key, String section) {
        try {
            // If section is specified, first find the section
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

    private int findMatchingClosingBrace(String json, int openingBraceIndex) {
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

    private String makeHttpRequest(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        StringBuilder response = new StringBuilder();
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
        } else {
            // Handle HTTP errors
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            throw new IOException("Błąd HTTP: " + responseCode + ", Odpowiedź: " + response.toString());
        }

        connection.disconnect();
        return response.toString();
    }

    // Simple method to format JSON for display
    private String formatJson(String json) {
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

    private void addIndentation(StringBuilder sb, int count) {
        for (int i = 0; i < count; i++) {
            sb.append("  ");
        }
    }
}