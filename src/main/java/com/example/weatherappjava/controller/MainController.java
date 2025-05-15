package com.example.weatherappjava.controller;

import com.example.weatherappjava.model.WeatherData;
import com.example.weatherappjava.service.GeolocationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import javafx.scene.control.DateCell;

/**
 * Main controller for the weather application, managing the UI and coordinating interactions
 * between user inputs, weather data services, and visualization.
 */

public class MainController {
    // UI elements for location input
    @FXML private RadioButton cityRadioButton;
    @FXML private HBox cityInputPanel;
    @FXML private HBox coordsInputPanel;
    @FXML private TextField cityInput;
    @FXML private TextField latitudeInput;
    @FXML private TextField longitudeInput;
    @FXML private Button searchButton;
    @FXML private ComboBox<Integer> forecastDaysComboBox;

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
    @FXML private GridPane weatherLabelsContainer;

    // Data mode elements
    @FXML private ToggleGroup dataMode;
    @FXML private RadioButton forecastRadioButton;
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

    /**
     * Constructor initializing delegated controllers.
     */
    public MainController() {
        this.searchController = new WeatherSearchController(this);
        this.displayController = new WeatherDisplayController(this);
        this.visualizationController = new WeatherVisualizationController(this);
    }


    /**
     * Initializes UI components and sets up listeners for data mode changes.
     */
    @FXML
    public void initialize() {
        updateInputPanelVisibility();
        updateDataModeVisibility();

        // Ograniczenie dat w DatePicker do wczorajszej i wcześniejszych
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // Ustawiamy maksymalną datę na wczoraj
        startDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(yesterday));
            }
        });

        endDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(yesterday));
            }
        });

        // Ustawiamy domyślne wartości dat
        endDatePicker.setValue(yesterday);
        startDatePicker.setValue(yesterday.minusDays(7)); // Domyślnie tydzień wstecz

        // Initialize the visualization controller with the UI elements
        visualizationController.setChartOptionsPanel(chartOptionsPanel);
        visualizationController.setWindSpeedCheckBox(windSpeedCheckBox);
        visualizationController.setSoilTempCheckBox(soilTempCheckBox);
        visualizationController.setAirTempCheckBox(airTempCheckBox);
        visualizationController.setRainCheckBox(rainCheckBox);
        visualizationController.setPressureCheckBox(pressureCheckBox);

        // Add a listener to update UI components when data mode changes
        dataMode.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            updateDataModeComponents();
        });

        // Initialize UI components based on initial data mode
        updateDataModeComponents();
    }

    /**
     * Updates UI components based on the current data mode
     */
    private void updateDataModeComponents() {
        boolean isForecastMode = forecastRadioButton.isSelected();
        weatherLabelsContainer.setVisible(isForecastMode);
        weatherLabelsContainer.setManaged(isForecastMode);
        soilTempCheckBox.setVisible(!isForecastMode);
        soilTempCheckBox.setManaged(!isForecastMode);
    }

    /**
     * Updates location input panel visibility based on the selected method (city or coordinates).
     */
    @FXML
    protected void onLocationMethodChanged() {
        updateInputPanelVisibility();
    }

    /**
     * Updates data mode panel visibility and components when the mode changes.
     */
    @FXML
    protected void onDataModeChanged() {
        updateDataModeVisibility();
        updateDataModeComponents();
    }

    /**
     * Handles search button click, clearing the display and delegating to the search controller.
     */
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
                endDatePicker.getValue(),
                forecastDaysComboBox.getValue()
        );
    }


    /**
     * Triggers chart visualization via visualization controller.
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

    /**
     * Toggles visibility of city or coordinates input panels.
     */
    private void updateDataModeVisibility() {
        boolean isForecastMode = forecastRadioButton.isSelected();
        forecastInputPanel.setVisible(isForecastMode);
        forecastInputPanel.setManaged(isForecastMode);
        historicalInputPanel.setVisible(!isForecastMode);
        historicalInputPanel.setManaged(!isForecastMode);
    }

    /**
     * Resets weather display elements to the default state.
     */
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

    // Getters for delegated controllers
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

    // Getter and setter for shared weather data
    public WeatherData getWeatherData() { return weatherData; }
    public void setWeatherData(WeatherData weatherData) { this.weatherData = weatherData; }
}