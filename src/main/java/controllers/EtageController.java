package controllers;

import entite.etage;
import entite.departement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import service.EtageService;
import service.DepartementService;

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
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero"));
        departementColumn.setCellValueFactory(cellData -> {
            departement d = cellData.getValue().getDepartement();
            return new javafx.beans.property.SimpleStringProperty(d != null ? d.getNom() : "");
        });

        // Colonne Modifier
        modifierColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Modifier");

            {
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

        // Colonne Supprimer
        supprimerColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Supprimer");

            {
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
}