package controllers;

import entite.departement;
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
import java.util.Arrays;
import java.util.List;

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

    private void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Navigation Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


    public void ConsulteDepartement(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/listedepartement.fxml"));
            Parent root = loader.load();

            Listdepartement controller = loader.getController();

            // Chargez les départements depuis votre source de données
            List<departement> departements = Arrays.asList(
                    new departement("Informatique", "Bâtiment A", "/img/blog1.jpg"),
                    new departement("Médecine", "Bâtiment B", "/img/medecine.jpg")
            );

            controller.initializeData(departements);

            Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            showErrorAlert("Erreur", "Échec du chargement: " + e.getMessage());
        }
    }
    }
