module com.example.weatherappjava {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.logging;
    requires redis.clients.jedis;

    opens com.example.weatherappjava to javafx.fxml;
    exports com.example.weatherappjava;
    exports com.example.weatherappjava.controller;
    opens com.example.weatherappjava.controller to javafx.fxml;
}