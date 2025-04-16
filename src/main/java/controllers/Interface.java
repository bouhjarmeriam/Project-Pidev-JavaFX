package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
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

















}
