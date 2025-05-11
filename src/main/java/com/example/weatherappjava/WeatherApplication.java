package com.example.weatherappjava;

import com.example.weatherappjava.controller.MainControllerFactory;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main class for the weather application.
 */
public class WeatherApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        MainControllerFactory.createMainController(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}