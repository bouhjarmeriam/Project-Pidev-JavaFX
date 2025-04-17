package controllers;

import entite.Entretien;
import entite.Equipement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.EntretienService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AjouterEntretienController {

    @FXML
    private Label titleLabel;

    @FXML
    private TextField nomEquipementField;

    @FXML
    private DatePicker dateEntretienPicker;

    @FXML
    private TextArea descriptionField;

    private Equipement equipement;

    private final EntretienService entretienService = new EntretienService();

    // Méthode pour définir l'équipement sélectionné
    public void setEquipement(Equipement equipement) {
        this.equipement = equipement;

        if (equipement != null) {
            nomEquipementField.setText(equipement.getNom());
            nomEquipementField.setEditable(false); // Rendre non modifiable

            titleLabel.setText("Créer un entretien pour l’équipement " + equipement.getNom());
        }
    }

    // Méthode pour gérer la création d'un entretien
    @FXML
    private void handleCreateEntretien() {
        if (equipement == null) {
            System.err.println("Aucun équipement défini !");
            return;
        }

        String nomEquipement = nomEquipementField.getText().trim();
        if (nomEquipement.isEmpty()) {
            nomEquipement = equipement.getNom(); // Valeur de secours
        }

        String description = descriptionField.getText().trim();
        LocalDate selectedDate = dateEntretienPicker.getValue();

        if (nomEquipement.isEmpty() || description.isEmpty() || selectedDate == null) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }

        if (selectedDate.isBefore(LocalDate.now())) {
            showAlert("Erreur", "La date de l'entretien ne peut pas être dans le passé.");
            return;
        }

        Entretien entretien = new Entretien();
        entretien.setNomEquipement(nomEquipement);
        entretien.setDescription(description);
        entretien.setDate(selectedDate);
        entretien.setEquipementId(equipement.getId());
        entretien.setCreatedAt(LocalDateTime.now());

        entretienService.ajouterEntretien(entretien);
        System.out.println("✅ Entretien créé pour : " + equipement.getNom());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/liste_entretien.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Liste des entretiens");
            stage.setScene(new Scene(root));
            stage.show();

            // Fermer la fenêtre actuelle
            nomEquipementField.getScene().getWindow().hide();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la liste : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour afficher des alertes
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
