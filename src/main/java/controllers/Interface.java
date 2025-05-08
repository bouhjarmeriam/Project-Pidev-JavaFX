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
    private VBox contentArea;  // Conteneur principal dans lequel charger les vues

    // Affiche simplement un label (statique)
    @FXML
    private void handleUsers(ActionEvent event) {
        Label userLabel = new Label("User Management Section");
        userLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        contentArea.getChildren().clear();
        contentArea.getChildren().add(userLabel);
    }

    // Charge dynamiquement equipement.fxml
    @FXML
    private void handleEquipement (ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/equipement.fxml"));
            Parent equipementView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(equipementView);

        } catch (IOException e) {
            e.printStackTrace();
            loadContent("Erreur lors du chargement de la page des équipements");
        }
    }

    // Charge medical_services.fxml
    @FXML
    private void handleMedicalServices(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/medical_services.fxml"));
            Parent medicalForm = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(medicalForm);

        } catch (IOException e) {
            e.printStackTrace();
            loadContent("Erreur lors du chargement de la page des services médicaux");
        }
    }

    // Charge admin_consultations.fxml
    @FXML
    private void handleAdminConsultation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin_consultations.fxml"));
            Parent adminView = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(adminView);

        } catch (IOException e) {
            e.printStackTrace();
            loadContent("Erreur lors du chargement des consultations admin");
        }
    }

    // Méthode utilitaire pour afficher un message d'erreur dans l'UI
    private void loadContent(String message) {
        Label label = new Label(message);
        label.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: red;");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(label);
    }
}
