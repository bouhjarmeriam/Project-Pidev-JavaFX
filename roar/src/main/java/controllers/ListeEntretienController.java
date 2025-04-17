package controllers;

import entite.Entretien;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import service.EntretienService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;

public class ListeEntretienController {

    @FXML
    private TableView<Entretien> entretienTable;

    @FXML
    private TableColumn<Entretien, String> nomColumn;

    @FXML
    private TableColumn<Entretien, LocalDate> dateColumn;

    @FXML
    private TableColumn<Entretien, String> descriptionColumn;

    @FXML
    private TableColumn<Entretien, Void> actionsColumn;

    private final EntretienService entretienService = new EntretienService();

    @FXML
    public void initialize() {
        // Configurer les colonnes de la TableView
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomEquipement"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Ajouter les boutons d'action
        addActionButtonsToTable();

        // Charger tous les entretiens dans la table
        loadEntretienData();
    }

    private void loadEntretienData() {
        entretienTable.getItems().setAll(entretienService.getAllEntretiens());
    }

    private void addActionButtonsToTable() {
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox hbox = new HBox(10, btnModifier, btnSupprimer);

            {
                btnModifier.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");

                btnModifier.setOnAction(event -> {
                    Entretien entretien = getTableView().getItems().get(getIndex());
                    handleModifier(entretien);
                });

                btnSupprimer.setOnAction(event -> {
                    Entretien entretien = getTableView().getItems().get(getIndex());
                    handleSupprimer(entretien);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
    }

    private void handleModifier(Entretien entretien) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEntretien.fxml"));
            Parent root = loader.load();

            ModifierEntretienController controller = loader.getController();
            controller.initData(entretien);

            // Passer le callback pour rafraîchir la liste
            controller.setOnEntretienModifie(() -> loadEntretienData()); // Rafraîchit la liste après modification

            Stage stage = new Stage();
            stage.setTitle("Modifier Entretien");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la page de modification : " + e.getMessage());
        }
    }

    private void handleSupprimer(Entretien entretien) {
        entretienService.deleteEntretien(entretien.getId());
        entretienTable.getItems().remove(entretien);
        System.out.println("🗑 Entretien supprimé : " + entretien.getNomEquipement());
    }
}
