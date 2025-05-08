package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class  MainFx extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        loadPage("front.fxml"); // Load login form first

        primaryStage.setTitle("Epic Login & Navigation");
        primaryStage.setMinWidth(800);  // Minimum window size
        primaryStage.setMinHeight(600);
        primaryStage.setMaxWidth(2000); // Optional: Max window size
        primaryStage.setMaxHeight(900);
        primaryStage.show();
    }

    public static void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(MainFx.class.getResource("/" + fxmlFile));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));  // Let the content scale automatically
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
