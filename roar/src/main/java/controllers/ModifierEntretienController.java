package controllers;

import entite.Entretien;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.EntretienService;
import javafx.stage.Stage;

import java.time.LocalDate;

public class ModifierEntretienController {

    @FXML
    private TextField nomEquipementField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextArea descriptionArea;

    private Entretien entretien;

    private final EntretienService entretienService = new EntretienService();

    private Runnable onEntretienModifie; // ✅ Callback pour rafraîchir la liste

    // Méthode pour initialiser les données de l'entretien à modifier
    public void initData(Entretien entretien) {
        this.entretien = entretien;
        nomEquipementField.setText(entretien.getNomEquipement());
        datePicker.setValue(entretien.getDate());
        descriptionArea.setText(entretien.getDescription());
    }

    // Setter pour le callback
    public void setOnEntretienModifie(Runnable onEntretienModifie) {
        this.onEntretienModifie = onEntretienModifie;
    }

    // Méthode pour mettre à jour l'entretien dans la base de données
    @FXML
    private void handleUpdate() {
        String nom = nomEquipementField.getText().trim();
        String description = descriptionArea.getText().trim();
        LocalDate selectedDate = datePicker.getValue();

        // Validation des champs
        if (nom.isEmpty() || description.isEmpty() || selectedDate == null) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }

        if (selectedDate.isBefore(LocalDate.now())) {
            showAlert("Erreur", "La date de l'entretien ne peut pas être dans le passé.");
            return;
        }

        // Mise à jour de l'entretien
        entretien.setNomEquipement(nom);
        entretien.setDescription(description);
        entretien.setDate(selectedDate);

        try {
            entretienService.updateEntretien(entretien);

            // Rafraîchissement de la liste après mise à jour
            if (onEntretienModifie != null) {
                onEntretienModifie.run();
            }

            showAlert("Succès", "L'entretien a été modifié avec succès.");
            ((Stage) nomEquipementField.getScene().getWindow()).close();
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de la mise à jour : " + e.getMessage());
        }
    }

    // Méthode utilitaire pour afficher des alertes
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
