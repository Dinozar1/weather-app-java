package com.example.weatherappjava.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Factory class for initializing and wiring controllers in the weather application.
 */
public class MainControllerFactory {

    /**
     * Creates and initializes the main controller, linking it with sub-controllers and setting up the UI.
     */
    public static MainController createMainController(Stage primaryStage) throws IOException {
        // Load the main FXML file
        FXMLLoader loader = new FXMLLoader(MainControllerFactory.class.getResource("/com/example/weatherappjava/main-view.fxml"));
        Parent root = loader.load();

        // Get the MainController instance from the loader
        MainController mainController = loader.getController();

        // Initialize WeatherVisualizationController and link UI elements
        WeatherVisualizationController visualizationController = new WeatherVisualizationController(mainController);
        visualizationController.setWindSpeedCheckBox((CheckBox) root.lookup("#windSpeedCheckBox"));
        visualizationController.setSoilTempCheckBox((CheckBox) root.lookup("#soilTempCheckBox"));
        visualizationController.setAirTempCheckBox((CheckBox) root.lookup("#airTempCheckBox"));
        visualizationController.setRainCheckBox((CheckBox) root.lookup("#rainCheckBox"));
        visualizationController.setPressureCheckBox((CheckBox) root.lookup("#pressureCheckBox"));
        visualizationController.setChartOptionsPanel((VBox) root.lookup("#chartOptionsPanel"));

        // Set up the scene and display the stage
        Scene scene = new Scene(root, 900, 700);
        primaryStage.setTitle("Weather App");
        primaryStage.setScene(scene);

        return mainController;
    }
}