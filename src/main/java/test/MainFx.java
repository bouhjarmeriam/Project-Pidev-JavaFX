package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainFx extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/departement.fxml"));
            AnchorPane root = loader.load(); // Make sure FXML is in the correct path

            // Set the scene and show the stage
            Scene scene = new Scene(root, 1200,800);
            primaryStage.initStyle(StageStyle.DECORATED);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Department Management");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();  // Print the error for debugging
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
