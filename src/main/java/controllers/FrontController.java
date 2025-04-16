package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;

public class FrontController {

    @FXML
    private Button servicesButton;

    @FXML
    private void loadInterfaceScene(ActionEvent event) {
        try {
            // Load interface.fxml from resources
            Parent root = FXMLLoader.load(getClass().getResource("/interface.fxml"));

            // Get current window
            Stage currentStage = (Stage) servicesButton.getScene().getWindow();

            // Set new scene
            currentStage.setScene(new Scene(root));

            // Maintain window state
            boolean wasMaximized = currentStage.isMaximized();
            currentStage.setMaximized(wasMaximized);

            // Update title
            currentStage.setTitle("Medical Services");

        } catch (IOException e) {
            showErrorAlert("Navigation Error",
                    "Could not load services interface",
                    e.getMessage());
        }
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Overloaded method with two parameters
    private void showErrorAlert(String header, String content) {
        showErrorAlert("Navigation Error", header, content);
    }

    @FXML
    private void handleAppointmentButton(ActionEvent event) {
        try {
            // Load the consultation.fxml file
            Parent consultationRoot = FXMLLoader.load(getClass().getResource("/consultation.fxml"));

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(consultationRoot);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Failed to load consultation page", "Please try again later.");
        }
    }

    @FXML
    private void handlePatientDashboardButton(ActionEvent event) {
        try {
            // Load patient_dashboard.fxml from resources
            Parent root = FXMLLoader.load(getClass().getResource("/patient_dashboard.fxml"));

            // Get current window
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set new scene
            currentStage.setScene(new Scene(root));

            // Maintain window state
            boolean wasMaximized = currentStage.isMaximized();
            currentStage.setMaximized(wasMaximized);

            // Update title
            currentStage.setTitle("Patient Dashboard");

        } catch (IOException e) {
            showErrorAlert("Navigation Error",
                    "Could not load patient dashboard",
                    e.getMessage());
        }
    }
    
    @FXML
    private void handleSejourDetailButton(ActionEvent event) {
        try {
            // Load sejour_detail_patient.fxml from resources
            Parent root = FXMLLoader.load(getClass().getResource("/sejour_detail_patient.fxml"));

            // Get current window
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set new scene
            currentStage.setScene(new Scene(root));

            // Maintain window state
            boolean wasMaximized = currentStage.isMaximized();
            currentStage.setMaximized(wasMaximized);

            // Update title
            currentStage.setTitle("Détails du Séjour");

        } catch (IOException e) {
            showErrorAlert("Navigation Error",
                    "Could not load séjour details",
                    e.getMessage());
        }
    }
}