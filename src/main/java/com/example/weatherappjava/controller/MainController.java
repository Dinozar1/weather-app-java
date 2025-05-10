package com.example.weatherappjava.controller;

import com.example.weatherappjava.model.LocationData;
import com.example.weatherappjava.model.WeatherData;
import com.example.weatherappjava.service.GeolocationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Główny kontroler aplikacji pogodowej
 */
public class MainController {
    // UI elements for location input
    @FXML private ToggleGroup locationMethod;
    @FXML private RadioButton cityRadioButton;
    @FXML private RadioButton coordsRadioButton;
    @FXML private HBox cityInputPanel;
    @FXML private HBox coordsInputPanel;
    @FXML private TextField cityInput;
    @FXML private TextField latitudeInput;
    @FXML private TextField longitudeInput;
    @FXML private Button searchButton;

    // UI elements for weather display
    @FXML private Label locationLabel;
    @FXML private Label temperatureLabel;
    @FXML private Label windSpeedLabel;
    @FXML private Label soilTemperatureLabel;
    @FXML private Label rainLabel;
    @FXML private Label humidityLabel;
    @FXML private Label pressureLabel;
    @FXML private Label updateTimeLabel;
    @FXML private GridPane forecastGrid;
    @FXML private Label statusLabel;
    @FXML private Hyperlink showRawDataLink;

    // Data mode elements
    @FXML private ToggleGroup dataMode;
    @FXML private RadioButton forecastRadioButton;
    @FXML private RadioButton historicalRadioButton;
    @FXML private HBox forecastInputPanel;
    @FXML private HBox historicalInputPanel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    // Visualization elements
    @FXML private VBox chartOptionsPanel;
    @FXML private CheckBox windSpeedCheckBox;
    @FXML private CheckBox soilTempCheckBox;
    @FXML private CheckBox airTempCheckBox;
    @FXML private CheckBox rainCheckBox;
    @FXML private CheckBox pressureCheckBox;

    // Service instances
    private final GeolocationService geolocationService = new GeolocationService();

    // Delegated controllers
    private final WeatherSearchController searchController;
    private final WeatherDisplayController displayController;
    private final WeatherVisualizationController visualizationController;

    // Shared weather data
    private WeatherData weatherData = new WeatherData();

    public MainController() {
        this.searchController = new WeatherSearchController(this);
        this.displayController = new WeatherDisplayController(this);
        this.visualizationController = new WeatherVisualizationController(this);
    }

    @FXML
    public void initialize() {
        updateInputPanelVisibility();
        updateDataModeVisibility();

        // Initialize visualization controller with the UI elements
        visualizationController.setChartOptionsPanel(chartOptionsPanel);
        visualizationController.setWindSpeedCheckBox(windSpeedCheckBox);
        visualizationController.setSoilTempCheckBox(soilTempCheckBox);
        visualizationController.setAirTempCheckBox(airTempCheckBox);
        visualizationController.setRainCheckBox(rainCheckBox);
        visualizationController.setPressureCheckBox(pressureCheckBox);
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
    protected void onSearchButtonClick() {
        clearWeatherDisplay();
        searchController.handleSearch(
                forecastRadioButton.isSelected(),
                cityRadioButton.isSelected(),
                cityInput.getText().trim(),
                latitudeInput.getText().trim(),
                longitudeInput.getText().trim(),
                startDatePicker.getValue(),
                endDatePicker.getValue()
        );
    }

    @FXML
    protected void onShowRawDataClick() {
        displayController.showRawData(
                geolocationService.getRawGeoResponse(),
                searchController.getRawWeatherResponse()
        );
    }

    /**
     * Handle visualize button click - delegates to visualization controller
     */
    @FXML
    protected void onVisualizeButtonClick() {
        visualizationController.onVisualizeButtonClick();
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

    public void clearWeatherDisplay() {
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
        weatherData.clearChartData();
    }

    // Gettery do elementów UI dla delegowanych kontrolerów
    public Label getStatusLabel() { return statusLabel; }
    public Button getSearchButton() { return searchButton; }
    public Label getLocationLabel() { return locationLabel; }
    public Label getTemperatureLabel() { return temperatureLabel; }
    public Label getWindSpeedLabel() { return windSpeedLabel; }
    public Label getHumidityLabel() { return humidityLabel; }
    public Label getPressureLabel() { return pressureLabel; }
    public Label getSoilTemperatureLabel() { return soilTemperatureLabel; }
    public Label getRainLabel() { return rainLabel; }
    public Label getUpdateTimeLabel() { return updateTimeLabel; }
    public GridPane getForecastGrid() { return forecastGrid; }
    public GeolocationService getGeolocationService() { return geolocationService; }

    // Getter i setter dla współdzielonych danych pogodowych
    public WeatherData getWeatherData() { return weatherData; }
    public void setWeatherData(WeatherData weatherData) { this.weatherData = weatherData; }
}