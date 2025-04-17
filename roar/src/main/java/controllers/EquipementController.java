package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage; // Ajoutez Stage pour gérer les fenêtres
import java.io.IOException;

public class EquipementController {

    @FXML
    private VBox categoryList; // Le conteneur où les catégories seront ajoutées

    @FXML
    private HBox contentArea;  // Définissez un HBox pour la zone de contenu, à ajuster selon votre layout

    // Méthode pour ajouter une nouvelle catégorie à la liste
    public void addCategory(String categoryName) {
        HBox categoryItem = new HBox(10); // Crée un HBox pour chaque catégorie
        Label categoryLabel = new Label(categoryName);
        Button detailsButton = new Button("Voir les détails");
        detailsButton.setOnAction(event->ouvrirFenetreCategorie(categoryName)); // L'action pour voir les détails

        categoryItem.getChildren().addAll(categoryLabel, detailsButton);
        categoryList.getChildren().add(categoryItem); // Ajoute cette HBox au VBox
    }

    // Méthode appelée lorsqu'on clique sur "Voir les détails"
    //@FXML
    /*private void handleEquipementCategory(ActionEvent event) {
        try {
            // Charger la vue de la catégorie d'équipement
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/equipement_category.fxml"));
            Parent equipementCategoryView = loader.load();

            // Récupérer le contrôleur pour lui passer l’objet
            EquipementCategoryController controller = loader.getController();
            controller.setCategoryTitle(selectedCategorie);

            // Créer une nouvelle scène pour afficher la catégorie d'équipement dans une nouvelle fenêtre
            Stage newStage = new Stage();
            newStage.setTitle("Détails de la catégorie");
            newStage.setScene(new Scene(equipementCategoryView));
            newStage.show(); // Ouvre la nouvelle fenêtre

        } catch (IOException e) {
            e.printStackTrace();
            loadContent("Erreur lors du chargement de la page de la catégorie d'équipement");
        }
    }*/

    @FXML
    private void ouvrirFenetreCategorie(String categorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/equipement_category.fxml"));
            Parent view = loader.load();

            EquipementCategoryController controller = loader.getController();
            controller.setCategorie(categorie);

            Stage stage = new Stage();
            stage.setTitle("Détails de la catégorie");
            stage.setScene(new Scene(view));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour afficher un message d'erreur ou autre contenu
    private void loadContent(String message) {
        Label errorLabel = new Label(message);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(errorLabel);
    }
    // Exemple pour ajouter des catégories
    public void initialize() {
        addCategory("Imagerie médicale");
        addCategory("Équipements de laboratoire");
        addCategory("Équipements de soins");
        addCategory("Équipements d'urgence");
        addCategory("Équipements de chirurgie");
        addCategory("Équipements de surveillance");
        addCategory("Équipements de stérilisation");
    }

}
