package com.example.weatherappjava.controller;

import com.example.weatherappjava.model.WeatherData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Kontroler odpowiedzialny za wizualizację danych pogodowych
 */
public class WeatherVisualizationController {
    private final MainController mainController;

    // Kontrolki z głównego formularza
    @FXML private VBox chartOptionsPanel;
    @FXML private CheckBox windSpeedCheckBox;
    @FXML private CheckBox soilTempCheckBox;
    @FXML private CheckBox airTempCheckBox;
    @FXML private CheckBox rainCheckBox;
    @FXML private CheckBox pressureCheckBox;

    public WeatherVisualizationController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Obsługuje kliknięcie przycisku wizualizacji
     */
    @FXML
    public void onVisualizeButtonClick() {
        boolean anySelected = windSpeedCheckBox.isSelected() ||
                soilTempCheckBox.isSelected() ||
                airTempCheckBox.isSelected() ||
                rainCheckBox.isSelected() ||
                pressureCheckBox.isSelected();

        if (!anySelected) {
            mainController.getStatusLabel().setText("Zaznacz przynajmniej jeden typ danych do wizualizacji.");
            return;
        }

        // Sprawdź, czy mamy dane do wyświetlenia
        if (!weatherDataHasData()) {
            mainController.getStatusLabel().setText("Brak danych do wizualizacji. Pobierz najpierw dane pogodowe.");
            return;
        }

        WeatherData weatherData = mainController.getWeatherData();

        // Wizualizuj każdy zaznaczony rodzaj danych w osobnym oknie
        if (windSpeedCheckBox.isSelected()) {
            openChartWindow("Prędkość wiatru", "km/h", weatherData.getWindSpeedData(), weatherData.getTimeLabels());
        }

        if (soilTempCheckBox.isSelected()) {
            openChartWindow("Temperatura gleby", "°C", weatherData.getSoilTempData(), weatherData.getTimeLabels());
        }

        if (airTempCheckBox.isSelected()) {
            openChartWindow("Temperatura powietrza", "°C", weatherData.getAirTempData(), weatherData.getTimeLabels());
        }

        if (rainCheckBox.isSelected()) {
            openChartWindow("Opady", "mm", weatherData.getRainData(), weatherData.getTimeLabels());
        }

        if (pressureCheckBox.isSelected()) {
            openChartWindow("Ciśnienie", "hPa", weatherData.getPressureData(), weatherData.getTimeLabels());
        }
    }

    /**
     * Otwiera okno z wykresem
     */
    private void openChartWindow(String title, String yAxisLabel, List<Double> data, List<String> labels) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/weatherappjava/chart-view.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);

            ChartController chartController = loader.getController();
            chartController.setupChart(title, yAxisLabel, data, labels);
            chartController.setWindowTitle(title);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            mainController.getStatusLabel().setText("Błąd podczas tworzenia wykresu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sprawdza, czy mamy dane do wizualizacji
     */
    private boolean weatherDataHasData() {
        return !mainController.getWeatherData().getTimeLabels().isEmpty();
    }

    // Settery dla kontrolek wizualizacji - będą ustawiane przez inicjalizację FXML
    public void setWindSpeedCheckBox(CheckBox checkBox) { this.windSpeedCheckBox = checkBox; }
    public void setSoilTempCheckBox(CheckBox checkBox) { this.soilTempCheckBox = checkBox; }
    public void setAirTempCheckBox(CheckBox checkBox) { this.airTempCheckBox = checkBox; }
    public void setRainCheckBox(CheckBox checkBox) { this.rainCheckBox = checkBox; }
    public void setPressureCheckBox(CheckBox checkBox) { this.pressureCheckBox = checkBox; }
    public void setChartOptionsPanel(VBox panel) { this.chartOptionsPanel = panel; }
}