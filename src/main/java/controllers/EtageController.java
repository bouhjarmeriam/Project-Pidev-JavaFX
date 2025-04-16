package controllers;

import entite.etage;
import entite.departement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import service.EtageService;
import service.DepartementService;

import java.io.IOException;
import java.util.List;

public class EtageController {
    @FXML private TextField numeroField;
    @FXML private ComboBox<departement> departementCombo;
    @FXML private Label numeroError;
    @FXML private Label departementError;
    @FXML private TableView<etage> etageTable;
    @FXML private TableColumn<etage, Integer> idColumn;
    @FXML private TableColumn<etage, Integer> numeroColumn;
    @FXML private TableColumn<etage, String> departementColumn;
    @FXML private TableColumn<etage, Void> modifierColumn;
    @FXML private TableColumn<etage, Void> supprimerColumn;

    private final EtageService etageService = new EtageService();
    private final DepartementService departementService = new DepartementService();
    private final ObservableList<etage> etageList = FXCollections.observableArrayList();
    private final ObservableList<departement> departementList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadDepartements();
        loadEtages();
    }

    private void setupTable() {
        // Configuration des largeurs de colonnes
        idColumn.setPrefWidth(80);
        numeroColumn.setPrefWidth(100);
        departementColumn.setPrefWidth(200);
        modifierColumn.setPrefWidth(100);
        supprimerColumn.setPrefWidth(100);

        // Configuration des colonnes de données
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero"));
        departementColumn.setCellValueFactory(cellData -> {
            departement d = cellData.getValue().getDepartement();
            return new javafx.beans.property.SimpleStringProperty(d != null ? d.getNom() : "");
        });

        // Colonne Modifier améliorée
        modifierColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("✏️");

            {
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btn.setTooltip(new Tooltip("Modifier"));

                btn.setOnAction(event -> {
                    etage etage = getTableView().getItems().get(getIndex());
                    handleModify(etage);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        // Colonne Supprimer améliorée
        supprimerColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("🗑️");

            {
                btn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                btn.setTooltip(new Tooltip("Supprimer"));

                btn.setOnAction(event -> {
                    etage etage = getTableView().getItems().get(getIndex());
                    handleDelete(etage);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        // Style général du tableau
        etageTable.setStyle("-fx-font-size: 14px;");
        etageTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadDepartements() {
        List<departement> departements = departementService.getAllDepartements();
        departementList.setAll(departements);
        departementCombo.setItems(departementList);
    }

    private void loadEtages() {
        List<etage> etages = etageService.getAllEtages();
        etageList.setAll(etages);
        etageTable.setItems(etageList);
    }

    @FXML
    private void handleSave() {
        if (validateForm()) {
            etage etage = new etage();
            etage.setNumero(Integer.parseInt(numeroField.getText()));
            etage.setDepartement(departementCombo.getValue());

            etageService.addEtage(etage);
            clearForm();
            loadEtages();
        }
    }

    private void handleModify(etage etage) {
        numeroField.setText(String.valueOf(etage.getNumero()));
        departementCombo.setValue(etage.getDepartement());

        // Vous pouvez ajouter ici la logique pour mettre à jour
        // Par exemple, enregistrer l'ID de l'étage à modifier
    }

    private void handleDelete(etage etage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer cet étage ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet étage ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                etageService.deleteEtage(etage.getId());
                loadEtages();
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation numéro
        try {
            Integer.parseInt(numeroField.getText());
            numeroError.setText("");
        } catch (NumberFormatException e) {
            numeroError.setText("Veuillez entrer un numéro valide");
            isValid = false;
        }

        // Validation département
        if (departementCombo.getValue() == null) {
            departementError.setText("Veuillez sélectionner un département");
            isValid = false;
        } else {
            departementError.setText("");
        }

        return isValid;
    }

    private void clearForm() {
        numeroField.clear();
        departementCombo.setValue(null);
        numeroError.setText("");
        departementError.setText("");
    }
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void showDepartements(ActionEvent event) {
        try {
            // Chemin relatif correct (sans "src/main/resources")
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/departement.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface des étages", Alert.AlertType.ERROR);
        }

    }

    @FXML
    private void showEtages(ActionEvent event) {

    }

    @FXML
    private void showSalles(ActionEvent event) {
        try {
            // Chemin relatif correct (sans "src/main/resources")
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/salle.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface des salles", Alert.AlertType.ERROR);
        }
    }

    public void Acceuil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interface.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le dashboard", Alert.AlertType.ERROR);
        }
    }
}