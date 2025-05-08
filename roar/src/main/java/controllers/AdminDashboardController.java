package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.EventObject;

public class AdminDashboardController {

    @FXML
    private VBox contentArea;

    @FXML
    private void handleUsers() {
        try {
            // Charger UserList.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeUtilisateurs.fxml"));
            VBox userListView = loader.load();

            // Vider le contenu existant et ajouter la liste des utilisateurs
            contentArea.getChildren().clear();
            contentArea.getChildren().add(userListView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMedicalServices() {
        // À implémenter pour charger une autre page si nécessaire
    }

    @FXML
    private void handleAdminConsultation() {
        // À implémenter pour charger une autre page si nécessaire
    }

    @FXML
    private void handleEquipement() {
        // À implémenter pour charger une autre page si nécessaire
    }

    @FXML
    private void handleLinkAction() {
        // À implémenter pour les autres liens (Infrastructure, Medication Stock, etc.)
    }

    @FXML
    private void handleLogoutAction(ActionEvent event) {
        try {
            // Load the front.fxml file
            Parent frontPage = FXMLLoader.load(getClass().getResource("/front.fxml"));
            Scene frontScene = new Scene(frontPage);

            // Get the stage from the event source
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new scene
            stage.setScene(frontScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading front.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void initialize() {
        // Initialization logic if needed, e.g., setting up dynamic data or bindings
    }
}
