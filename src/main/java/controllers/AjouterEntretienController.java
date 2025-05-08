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
import service.EquipementService;
import service.MailService;

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
    private final EquipementService equipementService = new EquipementService(); // Pour mettre à jour l'équipement

    // Callback pour notifier l'ajout de l'entretien
    private Runnable onEntretienAjoute;

    public void setEquipement(Equipement equipement) {
        this.equipement = equipement;

        if (equipement != null) {
            nomEquipementField.setText(equipement.getNom());
            nomEquipementField.setEditable(false);
            titleLabel.setText("Créer un entretien pour l’équipement " + equipement.getNom());
        }
    }

    public void setOnEntretienAjoute(Runnable onEntretienAjoute) {
        this.onEntretienAjoute = onEntretienAjoute;
    }

    @FXML
    private void handleCreateEntretien() {
        if (equipement == null) {
            System.err.println("Aucun équipement défini !");
            return;
        }

        String description = descriptionField.getText().trim();
        LocalDate selectedDate = dateEntretienPicker.getValue();

        if (description.isEmpty() || selectedDate == null) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
            return;
        }

        if (selectedDate.isBefore(LocalDate.now())) {
            showAlert("Erreur", "La date de l'entretien ne peut pas être dans le passé.");
            return;
        }

        // Création de l'entretien
        Entretien entretien = new Entretien();
        entretien.setNomEquipement(equipement.getNom());
        entretien.setDescription(description);
        entretien.setDate(selectedDate);
        entretien.setEquipementId(equipement.getId());
        entretien.setCreatedAt(LocalDateTime.now());

        // Sauvegarde de l'entretien
        entretienService.ajouterEntretien(entretien);

        // ✅ Envoi de l'email
        String emailUtilisateur = "bouhjarmariem012@gmail.com"; // à adapter dynamiquement
        String sujet = "Nouvel entretien créé";
        String message = "Bonjour,\n\nUn nouvel entretien a été enregistré pour l’équipement : " +
                equipement.getNom() + "\nDate prévue : " + selectedDate +
                "\n\nStatut : En maintenance.\n\nMerci.";

        MailService.sendEmail(emailUtilisateur, sujet, message);

        // Appel du callback pour notifier qu'un entretien a été ajouté et que le statut doit être mis à jour
        if (onEntretienAjoute != null) {
            onEntretienAjoute.run();
        }

        // ✅ Redirection vers la liste
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
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
