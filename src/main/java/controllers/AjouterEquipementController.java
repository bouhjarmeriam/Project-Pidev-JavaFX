package controllers;

import entite.Equipement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import service.EquipementService;

import java.io.IOException;

public class AjouterEquipementController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField typeField;

    @FXML
    private ComboBox<String> statutCombo;

    @FXML
    private TextField categoryField;

    // ✅ Ajouté pour gérer la zone dynamique
    private VBox contentArea;

    private final EquipementService equipementService = new EquipementService();

    private Runnable onEquipementAjoute;

    // ✅ Setter pour contentArea (appelé par le contrôleur principal)
    public void setContentArea(VBox contentArea) {
        this.contentArea = contentArea;
    }

    public void setCategorie(String categorie) {
        categoryField.setText(categorie);
        categoryField.setEditable(false);
    }

    public void setOnEquipementAjoute(Runnable callback) {
        this.onEquipementAjoute = callback;
    }

    @FXML
    private void handleEnregistrer() {
        String nom = nomField.getText().trim();
        String type = typeField.getText().trim();
        String statut = statutCombo.getValue();
        String categorie = categoryField.getText().trim();

        if (nom.isEmpty() || type.isEmpty() || statut == null || categorie.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs !");
            return;
        }

        Equipement nouvelEquipement = new Equipement();
        nouvelEquipement.setNom(nom);
        nouvelEquipement.setType(type);
        nouvelEquipement.setStatut(statut);
        nouvelEquipement.setCategory(categorie);

        equipementService.ajouterEquipement(nouvelEquipement);

        showAlert(Alert.AlertType.INFORMATION, "Succès", "L’équipement a été ajouté avec succès !");

        if (onEquipementAjoute != null) {
            onEquipementAjoute.run();
        }

        // ✅ Nouvelle logique pour revenir à la vue equipement_category.fxml
        if (contentArea != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/equipement_category.fxml"));
                Parent categoryView = loader.load();

                // Transmettre la catégorie à la nouvelle vue :
                EquipementCategoryController controller = loader.getController();
                controller.setCategorie(categorie);

                contentArea.getChildren().setAll(categoryView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void initData(Equipement equipement) {
        nomField.setText(equipement.getNom());
        typeField.setText(equipement.getType());
        statutCombo.setValue(equipement.getStatut());
        categoryField.setText(equipement.getCategory());

        categoryField.setEditable(false);
    }
}
