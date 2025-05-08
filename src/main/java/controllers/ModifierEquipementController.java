package controllers;

import entite.Equipement;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.EquipementService;

public class ModifierEquipementController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField typeField;

    @FXML
    private ComboBox<String> statutCombo;

    @FXML
    private TextField categorieField;

    private Equipement equipement; // L'équipement à modifier
    private final EquipementService equipementService = new EquipementService();

    private Runnable onEquipementModifie; // Callback pour rafraîchir la liste

    /**
     * Initialise les données de l'équipement à modifier
     */
    public void initData(Equipement equipement) {
        this.equipement = equipement;

        // Remplissage des champs avec les données existantes
        nomField.setText(equipement.getNom());
        typeField.setText(equipement.getType());

        statutCombo.getItems().addAll("Fonctionnel", "En Panne", "En Maintenance");
        statutCombo.setValue(equipement.getStatut());

        categorieField.setText(equipement.getCategory());
    }

    public void setOnEquipementModifie(Runnable onEquipementModifie) {
        this.onEquipementModifie = onEquipementModifie;
    }

    /**
     * Méthode appelée lors du clic sur le bouton "Modifier"
     */
    @FXML
    private void modifierEquipementAction() {
        // Récupération des valeurs saisies
        String nom = nomField.getText().trim();
        String type = typeField.getText().trim();
        String statut = statutCombo.getValue();
        String categorie = categorieField.getText().trim();

        // Validation de la saisie
        if (nom.isEmpty() || type.isEmpty() || statut == null || categorie.isEmpty()) {
            showAlert("Erreur de saisie", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        if (nom.length() < 3) {
            showAlert("Nom invalide", "Le nom de l'équipement doit contenir au moins 3 caractères.");
            return;
        }

        // Mise à jour de l'objet
        equipement.setNom(nom);
        equipement.setType(type);
        equipement.setStatut(statut);
        equipement.setCategory(categorie);

        // Mise à jour dans la base via le service
        equipementService.updateEquipement(equipement);

        // Callback pour rafraîchir la liste si défini
        if (onEquipementModifie != null) {
            onEquipementModifie.run();
        }

        // Fermeture de la fenêtre
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    /**
     * Méthode utilitaire pour afficher une alerte
     */
    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
