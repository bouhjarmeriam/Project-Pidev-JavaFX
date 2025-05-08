package controllers;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class NotificationAPI {

    public static void showAdminAlert(String message) {
        Stage notificationStage = new Stage();
        notificationStage.initStyle(StageStyle.TRANSPARENT);

        Label label = new Label(message);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;");

        StackPane root = new StackPane(label);
        root.setStyle("-fx-background-color: rgba(0, 100, 200, 0.8); -fx-background-radius: 10;");
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        notificationStage.setScene(scene);

        // Position at bottom-right
        notificationStage.setX(javafx.stage.Screen.getPrimary().getVisualBounds().getWidth() - 350);
        notificationStage.setY(javafx.stage.Screen.getPrimary().getVisualBounds().getHeight() - 120);

        notificationStage.show();

        // Auto-close after 3 seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> notificationStage.close());
        delay.play();
    }
}
