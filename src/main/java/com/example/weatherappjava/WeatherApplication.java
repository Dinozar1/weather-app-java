package com.example.weatherappjava;

import com.example.weatherappjava.controller.MainController;
import com.example.weatherappjava.controller.MainControllerFactory;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Główna klasa aplikacji
 */
public class WeatherApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        MainController mainController = MainControllerFactory.createMainController(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}