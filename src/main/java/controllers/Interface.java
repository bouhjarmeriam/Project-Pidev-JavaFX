package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

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
 // @FXML
  // private void handleMedicalServices(ActionEvent event) {
    //    Label serviceLabel = new Label("Medical Services Section");
      //  serviceLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

      //  contentArea.getChildren().clear();  // Remove old content
       // contentArea.getChildren().add(serviceLabel);  // Show new content
    //}

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
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleAdmininfrastructure(ActionEvent event) {
        try {
            // Chemin relatif correct (sans "src/main/resources")
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/departement.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface des étages", Alert.AlertType.ERROR);
        }
    }
}
