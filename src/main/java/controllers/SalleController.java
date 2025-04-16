package controllers;

import entite.salle;
import entite.etage;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import service.EtageService;
import service.SalleService;

import java.util.List;

public class SalleController {

    @FXML private TextField nomField;
    @FXML private TextField capaciteField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Spinner<Integer> prioriteSpinner;
    @FXML private ComboBox<etage> etageCombo;

    @FXML private TableView<salle> salleTable;
    @FXML private TableColumn<salle, String> colNom;
    @FXML private TableColumn<salle, Integer> colCapacite;
    @FXML private TableColumn<salle, String> colType;
    @FXML private TableColumn<salle, String> colStatus;
    @FXML private TableColumn<salle, Integer> colPriorite;
    @FXML private TableColumn<salle, Integer> colEtage;

    private final SalleService salleService = new SalleService();
    private final EtageService etageService = new EtageService();

    private final ObservableList<salle> salleData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList("Consultation", "Bloc opératoire", "Réanimation", "Chambre"));
        statusCombo.setItems(FXCollections.observableArrayList("Disponible", "Occupée", "En maintenance"));

        prioriteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));

        List<etage> etages = EtageService.getAll();
        etageCombo.setItems(FXCollections.observableArrayList(etages));

        // Cell value factories (sans JavaFX Properties)
        colNom.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getNom()));
        colCapacite.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getCapacite()));
        colType.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getType()));
        colStatus.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatus()));
        colPriorite.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getPriorite()));
        colEtage.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getEtage().getNumero()));

        loadSalles();
    }

    private void loadSalles() {
        salleData.setAll(salleService.getAll());
        salleTable.setItems(salleData);
    }

    @FXML
    private void handleAjouter() {
        try {
            String nom = nomField.getText();
            int capacite = Integer.parseInt(capaciteField.getText());
            String type = typeCombo.getValue();
            String status = statusCombo.getValue();
            int priorite = prioriteSpinner.getValue();
            etage etageSelected = etageCombo.getValue();

            if (nom.isEmpty() || type == null || status == null || etageSelected == null) {
                showAlert("Champs manquants", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            salle s = new salle(nom, capacite, type, status, priorite);
            salleService.addSalle(s);

            loadSalles();
            clearForm();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Capacité invalide !");
        }
    }

    private void clearForm() {
        nomField.clear();
        capaciteField.clear();
        typeCombo.getSelectionModel().clearSelection();
        statusCombo.getSelectionModel().clearSelection();
        prioriteSpinner.getValueFactory().setValue(1);
        etageCombo.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
