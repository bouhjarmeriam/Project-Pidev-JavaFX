package controllers;

import entite.etage;
import entite.departement;
import javafx.beans.property.SimpleIntegerProperty;
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
import service.DepartemntService;
import service.EtageService;
import service.SalleService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EtageController {
    @FXML private TextField numeroField;
    @FXML private ComboBox<departement> departementCombo;
    @FXML private Label numeroError;
    @FXML private Label departementError;
    @FXML private TableView<etage> etageTable;
    @FXML private TableColumn<etage, Integer> idColumn;
    @FXML private TableColumn<etage, Integer> numeroColumn;
    @FXML private TableColumn<etage, String> departementColumn;
    @FXML private TableColumn<etage, Integer> nbrSalleColumn;
    @FXML private TableColumn<etage, Void> modifierColumn;
    @FXML private TableColumn<etage, Void> supprimerColumn;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private TextField searchField; // Added for dynamic search

    private final EtageService etageService = new EtageService();
    private final DepartemntService departementService = new DepartemntService();
    private final SalleService salleService = new SalleService();
    private final ObservableList<etage> etageList = FXCollections.observableArrayList();
    private final ObservableList<etage> filteredEtageList = FXCollections.observableArrayList(); // For filtered data
    private final ObservableList<departement> departementList = FXCollections.observableArrayList();
    private etage etageEnCoursDeModification = null;

    @FXML
    public void initialize() {
        setupTable();
        loadDepartements();
        configureDepartementCombo();
        loadEtages();
        setupSearch(); // Added to set up dynamic search
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
        // Configuration des colonnes
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero"));

        departementColumn.setCellValueFactory(cellData -> {
            departement d = cellData.getValue().getDepartement();
            return new javafx.beans.property.SimpleStringProperty(d != null ? d.getNom() : "");
        });

        nbrSalleColumn.setCellValueFactory(cellData -> {
            etage e = cellData.getValue();
            return new SimpleIntegerProperty(e.getNbrSalle()).asObject();
        });

        // Configuration des boutons d'action
        modifierColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("✏️");
            {
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5px;");
                btn.setOnAction(event -> {
                    etage etage = getTableView().getItems().get(getIndex());
                    handleModify(etage);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        supprimerColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("🗑️");
            {
                btn.setStyle("-fx-background-color: #EF5350; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5px;");
                btn.setOnAction(event -> {
                    etage etage = getTableView().getItems().get(getIndex());
                    handleDelete(etage);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
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

        // Calcul du nombre de salles pour chaque étage
        for (etage e : etages) {
            int count = salleService.countSallesByEtage(e.getId());
            e.setNbrSalle(count);
        }

        etageList.setAll(etages);
        filteredEtageList.setAll(etages); // Initialize filtered list
        etageTable.setItems(filteredEtageList);
    }

    private void setupSearch() {
        // Add listener to searchField for dynamic filtering
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEtages(newValue);
        });
    }

    private void filterEtages(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredEtageList.setAll(etageList);
        } else {
            String lowerCaseFilter = searchText.toLowerCase();
            List<etage> filteredList = etageList.stream()
                    .filter(etage -> {
                        boolean matchesNumero = String.valueOf(etage.getNumero()).contains(lowerCaseFilter);
                        boolean matchesDepartement = etage.getDepartement() != null &&
                                etage.getDepartement().getNom().toLowerCase().contains(lowerCaseFilter);
                        return matchesNumero || matchesDepartement;
                    })
                    .collect(Collectors.toList());
            filteredEtageList.setAll(filteredList);
        }
    }

    public void refreshEtage(etage etage) {
        int count = salleService.countSallesByEtage(etage.getId());
        etage.setNbrSalle(count);
        etageTable.refresh();
    }

    @FXML
    private void handleSave() {
        if (validateForm()) {
            if (etageEnCoursDeModification == null) {
                // Ajout d'un nouvel étage
                etage newEtage = new etage();
                newEtage.setNumero(Integer.parseInt(numeroField.getText()));
                newEtage.setDepartement(departementCombo.getValue());
                etageService.addEtage(newEtage);
            } else {
                // Modification d'un étage existant
                etageEnCoursDeModification.setNumero(Integer.parseInt(numeroField.getText()));
                etageEnCoursDeModification.setDepartement(departementCombo.getValue());
                etageService.updateEtage(etageEnCoursDeModification);
                etageEnCoursDeModification = null;
                saveButton.setText("Ajouter");
            }
            clearForm();
            loadEtages();
        }
    }

    private void handleModify(etage etage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifierEtage.fxml"));
            Parent root = loader.load();

            ModifierEtageController controller = loader.getController();
            controller.setEtageData(etage);
            controller.setDepartements(departementList);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Après fermeture de la fenêtre de modification
            loadEtages();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", Alert.AlertType.ERROR);
        }
    }

    private void handleDelete(etage etage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
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
            numeroError.setText("Numéro invalide");
            isValid = false;
        }

        if (departementCombo.getValue() == null) {
            departementError.setText("Département requis");
            isValid = false;
        } else {
            departementError.setText("");
        }

        return isValid;
    }

    @FXML
    private void clearForm() {
        numeroField.clear();
        departementCombo.setValue(null);
        numeroError.setText("");
        departementError.setText("");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthodes de navigation
    @FXML
    private void showDepartements(ActionEvent event) {
        loadView("/departement.fxml", event);
    }

    @FXML
    private void showEtages(ActionEvent event) {
        loadView("/etage.fxml", event);
    }

    @FXML
    private void showSalles(ActionEvent event) {
        loadView("/salle.fxml", event);
    }

    @FXML
    private void Acceuil(ActionEvent event) {
        loadView("/interface.fxml", event);
    }

    private void loadView(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la vue", Alert.AlertType.ERROR);
        }
    }
}