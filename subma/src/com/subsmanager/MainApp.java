package com.subsmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * MainApp adalah entry point JavaFX.
 * Extends Application dan meluncurkan window utama.
 */
public class MainApp extends Application {

    public static final String APP_TITLE  = "Subscription Manager";
    public static final double WIN_WIDTH  = 1100;
    public static final double WIN_HEIGHT = 700;

    @Override
    public void start(Stage primaryStage) throws Exception {
        SessionManager.setPrimaryStage(primaryStage);

        Parent root = FXMLLoader.load(
            getClass().getResource(
                "/com/subsmanager/gui/fxml/login.fxml"
            )
        );

        Scene scene = new Scene(root, WIN_WIDTH, WIN_HEIGHT);
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(550);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}