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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import service.EtageService;
import service.DepartementService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final EtageService etageService = new EtageService();
    private final DepartementService departementService = new DepartementService();
    private final ObservableList<etage> etageList = FXCollections.observableArrayList();
    private final ObservableList<departement> departementList = FXCollections.observableArrayList();
    private etage etageEnCoursDeModification = null;

    @FXML
    public void initialize() {
        setupTable();
        loadDepartements();
        configureDepartementCombo();
        loadEtages();
    }

    private void configureDepartementCombo() {
        departementCombo.setConverter(new StringConverter<departement>() {
            @Override
            public String toString(departement departement) {
                return departement != null ? departement.getNom() : "";
            }

            @Override
            public departement fromString(String string) {
                return departementCombo.getItems().stream()
                        .filter(d -> d.getNom().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        departementCombo.setCellFactory(param -> new ListCell<departement>() {
            @Override
            protected void updateItem(departement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());
                }
            }
        });
    }

    private void setupTable() {
        idColumn.setPrefWidth(80);
        numeroColumn.setPrefWidth(100);
        departementColumn.setPrefWidth(200);
        modifierColumn.setPrefWidth(100);
        supprimerColumn.setPrefWidth(100);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero"));
        departementColumn.setCellValueFactory(cellData -> {
            departement d = cellData.getValue().getDepartement();
            return new javafx.beans.property.SimpleStringProperty(d != null ? d.getNom() : "");
        });

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
            if (etageEnCoursDeModification == null) {
                etage newEtage = new etage();
                newEtage.setNumero(Integer.parseInt(numeroField.getText()));
                newEtage.setDepartement(departementCombo.getValue());
                etageService.addEtage(newEtage);
            } else {
                etageEnCoursDeModification.setNumero(Integer.parseInt(numeroField.getText()));
                etageEnCoursDeModification.setDepartement(departementCombo.getValue());
                etageService.updateEtage(etageEnCoursDeModification);
                etageEnCoursDeModification = null;
                saveButton.setText("Ajouter");
            }
            clearForm();
            loadEtages();
            cancelButton.setDisable(true);
        }
    }

    private void handleModify(etage etage) {
        try {
            // Créer la boîte de dialogue
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier l'étage");
            dialog.setHeaderText("Modification de l'étage #" + etage.getId());

            // Ajouter les boutons OK et Annuler
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Charger le contenu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifierEtage.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur
            ModifierEtageController controller = loader.getController();
            controller.setEtageData(etage);
            controller.setDepartements(departementList);

            // Ajouter le contenu à la boîte de dialogue
            dialog.getDialogPane().setContent(root);

            // Personnaliser le style
            dialog.getDialogPane().setStyle("-fx-font-size: 14px;");

            // Afficher la boîte de dialogue et attendre la réponse
            Optional<ButtonType> result = dialog.showAndWait();

            // Traiter le résultat
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Sauvegarder les modifications
                etage updatedEtage = controller.getUpdatedEtage();
                if (updatedEtage != null) {
                    etageService.updateEtage(updatedEtage);
                    loadEtages(); // Rafraîchir la table
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la boîte de dialogue de modification", Alert.AlertType.ERROR);
        }
    }

    private void handleDelete(etage etage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet étage ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            etageService.deleteEtage(etage.getId());
            loadEtages();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        try {
            Integer.parseInt(numeroField.getText());
            numeroError.setText("");
        } catch (NumberFormatException e) {
            numeroError.setText("Veuillez entrer un numéro valide");
            isValid = false;
        }

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/departement.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger l'interface des départements", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showEtages(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/etage.fxml"));
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
    private void showSalles(ActionEvent event) {
        try {
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

    @FXML
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