package com.example.weatherappjava.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Fabryka odpowiedzialna za inicjalizację kontrolerów i powiązanie ich ze sobą
 */
public class MainControllerFactory {

    /**
     * Tworzy i inicjalizuje główny kontroler wraz z jego podkontrolerami
     */
    public static MainController createMainController(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainControllerFactory.class.getResource("/com/example/weatherappjava/main-view.fxml"));
        Parent root = loader.load();

        MainController mainController = loader.getController();

        // Pobierz referencje do kontrolek wizualizacji z FXML
        WeatherVisualizationController visualizationController = new WeatherVisualizationController(mainController);
        visualizationController.setWindSpeedCheckBox((CheckBox) root.lookup("#windSpeedCheckBox"));
        visualizationController.setSoilTempCheckBox((CheckBox) root.lookup("#soilTempCheckBox"));
        visualizationController.setAirTempCheckBox((CheckBox) root.lookup("#airTempCheckBox"));
        visualizationController.setRainCheckBox((CheckBox) root.lookup("#rainCheckBox"));
        visualizationController.setPressureCheckBox((CheckBox) root.lookup("#pressureCheckBox"));
        visualizationController.setChartOptionsPanel((VBox) root.lookup("#chartOptionsPanel"));

        // Możesz dodać podobne inicjalizacje dla innych kontrolerów

        // Ustaw scenę i wyświetl
        Scene scene = new Scene(root, 900, 700);
        primaryStage.setTitle("Weather App");
        primaryStage.setScene(scene);

        return mainController;
    }
}