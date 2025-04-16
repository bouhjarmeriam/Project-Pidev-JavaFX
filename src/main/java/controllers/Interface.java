package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import util.AlertUtil;

public class Interface {

    @FXML
    private VBox contentArea;  // This is where new content will appear

    @FXML
    private void handleUsers(ActionEvent event) {
        Label userLabel = new Label("User Management Section");
        userLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        contentArea.getChildren().clear();  // Clear old content
        contentArea.getChildren().add(userLabel);  // Show new content
    }

    @FXML
    private void handleDossiersMedicaux(ActionEvent event) {
        try {
            // Load the Dossier Medicale FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dossier_medicale.fxml"));
            Parent dossierForm = loader.load();

            // Replace content area with the dossier medicale form
            contentArea.getChildren().clear();
            contentArea.getChildren().add(dossierForm);
        } catch (IOException e) {
            e.printStackTrace();
            loadContent("Error loading dossiers médicaux form: " + e.getMessage());
        }
    }

    @FXML
    private void handleSejours(ActionEvent event) {
        try {
            // Load the Sejour FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sejour_form.fxml"));
            Parent sejourForm = loader.load();

            // Replace content area with the sejour form
            contentArea.getChildren().clear();
            contentArea.getChildren().add(sejourForm);
        } catch (IOException e) {
            e.printStackTrace();
            loadContent("Error loading séjours form: " + e.getMessage());
        }
    }

    @FXML
    private void handlePatientDashboard(ActionEvent event) {
        try {
            // Load the Patient Dashboard in a new window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/patient_dashboard.fxml"));
            Parent patientDashboard = loader.load();
            
            // Create a new stage for the dashboard
            Stage patientStage = new Stage();
            patientStage.setTitle("Patient Dashboard");
            patientStage.setScene(new Scene(patientDashboard));
            patientStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            loadContent("Error loading patient dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Navigate back to the front page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front.fxml"));
            Parent frontPage = loader.load();
            
            // Get current stage and set new scene
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(frontPage));
            currentStage.setTitle("Accueil");
        } catch (IOException e) {
            e.printStackTrace();
            loadContent("Error logging out: " + e.getMessage());
        }
    }

    @FXML
    private void handleMedicalServices(ActionEvent event) {
        try {
            // Load the Medical Services FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/medical_services.fxml"));
            Parent medicalForm = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(medicalForm);

        } catch (IOException e) {
            e.printStackTrace();
            loadContent("Error loading medical services form");
        }
    }

    private void loadContent(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(label);
    }

    @FXML
    private void handleAdminConsultation(ActionEvent event) {
        try {
            // Load the Medical Services FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_consultations.fxml"));
            Parent medicalForm = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(medicalForm);

        } catch (IOException e) {
            e.printStackTrace();
            loadContent("Error loading admin consulataions view");
        }
    }
}
