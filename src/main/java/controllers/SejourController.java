package controllers;

import entite.DossierMedicale;
import entite.Sejour;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import service.DossierMedicaleService;
import service.SejourService;
import util.AlertUtil;
import util.FormValidationUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class SejourController implements Initializable {

    @FXML
    private TableView<Sejour> sejourTable;

    @FXML
    private TableColumn<Sejour, Integer> colId;

    @FXML
    private TableColumn<Sejour, LocalDate> colDateEntree;

    @FXML
    private TableColumn<Sejour, LocalDate> colDateSortie;

    @FXML
    private TableColumn<Sejour, String> colTypeSejour;

    @FXML
    private TableColumn<Sejour, Double> colFraisSejour;

    @FXML
    private TableColumn<Sejour, String> colMoyenPaiement;

    @FXML
    private TableColumn<Sejour, String> colStatutPaiement;

    @FXML
    private TableColumn<Sejour, Double> colPrixExtras;

    @FXML
    private TableColumn<Sejour, String> colDossierMedicale;

    @FXML
    private DatePicker dateEntreePicker;

    @FXML
    private DatePicker dateSortiePicker;

    @FXML
    private ComboBox<String> comboTypeSejour;

    @FXML
    private TextField txtFraisSejour;

    @FXML
    private ComboBox<String> comboMoyenPaiement;

    @FXML
    private ComboBox<String> comboStatutPaiement;

    @FXML
    private TextField txtPrixExtras;

    @FXML
    private ComboBox<DossierMedicale> comboDossierMedicale;

    @FXML
    private Button btnAjouter;

    @FXML
    private Button btnModifier;

    @FXML
    private Button btnSupprimer;

    @FXML
    private Button btnVoir;

    @FXML
    private Label lblValidationError;

    @FXML
    private HBox boxDateEntreeError;

    @FXML
    private HBox boxDateSortieError;

    @FXML
    private HBox boxTypeSejourError;

    @FXML
    private HBox boxFraisSejourError;

    @FXML
    private HBox boxMoyenPaiementError;

    @FXML
    private HBox boxStatutPaiementError;

    @FXML
    private HBox boxPrixExtrasError;

    @FXML
    private HBox boxDossierMedicaleError;

    private final SejourService sejourService = new SejourService();
    private final DossierMedicaleService dossierMedicaleService = new DossierMedicaleService();
    private ObservableList<Sejour> sejourList = FXCollections.observableArrayList();
    private Sejour currentSejour;
    private FormValidationUtil validator = new FormValidationUtil();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] TYPE_SEJOUR_OPTIONS = {"Hospitalisation", "Consultation", "Urgence", "Chirurgie", "Observation"};
    private static final String[] MOYEN_PAIEMENT_OPTIONS = {"Carte Bancaire", "Espèces", "Chèque", "Assurance", "Virement"};
    private static final String[] STATUT_PAIEMENT_OPTIONS = {"Payé", "En attente", "Partiel", "Annulé", "Remboursé"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up circular dependencies between services but break the circular reference during load operations
        sejourService.setDossierService(dossierMedicaleService);
        dossierMedicaleService.setSejourService(sejourService);

        setupTableColumns();
        loadSejours();
        setupComboBoxes();
        setupEventHandlers();
        setupValidation();
        resetFormFields();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        colDateEntree.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getDateEntree();
            return new SimpleObjectProperty<>(dateTime != null ? dateTime.toLocalDate() : null);
        });
        colDateSortie.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getDateSortie();
            return new SimpleObjectProperty<>(dateTime != null ? dateTime.toLocalDate() : null);
        });
        colTypeSejour.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTypeSejour()));
        colFraisSejour.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getFraisSejour()));
        colMoyenPaiement.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMoyenPaiement()));
        colStatutPaiement.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatutPaiement()));
        colPrixExtras.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrixExtras()));
        colDossierMedicale.setCellValueFactory(cellData -> {
            DossierMedicale dossier = cellData.getValue().getDossierMedicale();
            if (dossier != null && dossier.getPatient() != null) {
                return new SimpleStringProperty(dossier.getPatient().getNom() + " " + dossier.getPatient().getPrenom());
            } else {
                return new SimpleStringProperty("N/A");
            }
        });
    }

    private void loadSejours() {
        try {
            // We can safely display the table of sejours
            sejourTable.setItems(sejourList);

            List<Sejour> sejours = sejourService.recupererTousSejours();
            if (sejours != null) {
                System.out.println("Loaded " + sejours.size() + " sejours");
                sejourList.clear();
                sejourList.addAll(sejours);
                sejourTable.refresh();
            } else {
                System.out.println("No sejours loaded - returned null list");
                sejourList.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Window owner = sejourTable.getScene() != null ? sejourTable.getScene().getWindow() : null;
            AlertUtil.showError(owner, "Erreur", "Erreur lors du chargement des séjours\n" + e.getMessage());
        }
    }

    private void setupComboBoxes() {
        // Types de séjour
        List<String> typesSejour = Arrays.asList(TYPE_SEJOUR_OPTIONS);
        comboTypeSejour.setItems(FXCollections.observableArrayList(typesSejour));

        // Moyens de paiement
        List<String> moyensPaiement = Arrays.asList(MOYEN_PAIEMENT_OPTIONS);
        comboMoyenPaiement.setItems(FXCollections.observableArrayList(moyensPaiement));

        // Statuts de paiement
        List<String> statutsPaiement = Arrays.asList(STATUT_PAIEMENT_OPTIONS);
        comboStatutPaiement.setItems(FXCollections.observableArrayList(statutsPaiement));

        // Dossiers médicaux - Load without sejours to prevent circular references
        try {
            List<DossierMedicale> dossiers = dossierMedicaleService.recupererTousDossiers(false);
            comboDossierMedicale.setItems(FXCollections.observableArrayList(dossiers));

            // Définir le converter pour afficher le nom du patient
            comboDossierMedicale.setConverter(new javafx.util.StringConverter<DossierMedicale>() {
                @Override
                public String toString(DossierMedicale dossier) {
                    if (dossier == null) return "";
                    if (dossier.getPatient() == null) return "Dossier #" + dossier.getId();
                    return dossier.getPatient().getNom() + " " + dossier.getPatient().getPrenom() + " (ID: " + dossier.getId() + ")";
                }

                @Override
                public DossierMedicale fromString(String string) {
                    return null; // Not needed for our purposes
                }
            });
        } catch (Exception e) {
            System.err.println("Error loading dossiers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        sejourTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                currentSejour = newSelection;
                populateForm(currentSejour);
                btnModifier.setDisable(false);
                btnSupprimer.setDisable(false);
                btnVoir.setDisable(false);
            } else {
                resetFormFields();
                btnModifier.setDisable(true);
                btnSupprimer.setDisable(true);
                btnVoir.setDisable(true);
            }
        });
    }

    private void populateForm(Sejour sejour) {
        dateEntreePicker.setValue(sejour.getDateEntree() != null ? sejour.getDateEntree().toLocalDate() : null);
        dateSortiePicker.setValue(sejour.getDateSortie() != null ? sejour.getDateSortie().toLocalDate() : null);
        comboTypeSejour.setValue(sejour.getTypeSejour());
        txtFraisSejour.setText(String.valueOf(sejour.getFraisSejour()));
        comboMoyenPaiement.setValue(sejour.getMoyenPaiement());
        comboStatutPaiement.setValue(sejour.getStatutPaiement());
        txtPrixExtras.setText(String.valueOf(sejour.getPrixExtras()));
        comboDossierMedicale.setValue(sejour.getDossierMedicale());
    }

    private void resetFormFields() {
        currentSejour = null;
        dateEntreePicker.setValue(LocalDate.now());
        dateSortiePicker.setValue(null);
        comboTypeSejour.setValue(null);
        txtFraisSejour.setText("0.0");
        comboMoyenPaiement.setValue(null);
        comboStatutPaiement.setValue("En attente");
        txtPrixExtras.setText("0.0");
        comboDossierMedicale.setValue(null);

        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        btnVoir.setDisable(true);
    }

    private void setupValidation() {
        // Register all error boxes with the validator but make them invisible as we'll use popup alerts instead
        validator.registerErrorBox("dateEntree", boxDateEntreeError);
        validator.registerErrorBox("dateSortie", boxDateSortieError);
        validator.registerErrorBox("typeSejour", boxTypeSejourError);
        validator.registerErrorBox("fraisSejour", boxFraisSejourError);
        validator.registerErrorBox("moyenPaiement", boxMoyenPaiementError);
        validator.registerErrorBox("statutPaiement", boxStatutPaiementError);
        validator.registerErrorBox("prixExtras", boxPrixExtrasError);
        validator.registerErrorBox("dossierMedicale", boxDossierMedicaleError);

        // Initialize all error boxes as hidden
        validator.clearAllErrors();

        // Hide the validation error label as we'll use a popup dialog instead
        lblValidationError.setVisible(false);
        lblValidationError.setManaged(false);
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        // Validate form before saving
        if (!validateForm()) {
            return;
        }

        try {
            Sejour sejour = new Sejour();
            updateSejourFromForm(sejour);

            // Print debug information
            System.out.println("===== ADDING NEW SEJOUR =====");
            System.out.println("Date entrée: " + sejour.getDateEntree());
            System.out.println("Date sortie: " + sejour.getDateSortie());
            System.out.println("Type séjour: " + sejour.getTypeSejour());
            System.out.println("Frais séjour: " + sejour.getFraisSejour());
            System.out.println("Moyen paiement: " + sejour.getMoyenPaiement());
            System.out.println("Statut paiement: " + sejour.getStatutPaiement());
            System.out.println("Prix extras: " + sejour.getPrixExtras());
            System.out.println("Dossier Medicale ID: " + (sejour.getDossierMedicale() != null ? sejour.getDossierMedicale().getId() : "null"));

            // For adding a new sejour, use modified methods to break circular references
            // Get the DossierMedicale with loadSejours=false to prevent circular references
            if (sejour.getDossierMedicale() != null) {
                DossierMedicale dossier = dossierMedicaleService.recupererDossierParId(
                        sejour.getDossierMedicale().getId(), false);
                sejour.setDossierMedicale(dossier);
            }

            boolean success = sejourService.ajouterSejour(sejour);

            if (success) {
                System.out.println("Sejour added successfully with ID: " + sejour.getId());
                // Reload the sejours to refresh the table
                loadSejours();

                // If this window was opened from DossierMedicaleController,
                // update the parent window's table as well
                if (comboDossierMedicale.getValue() != null && comboDossierMedicale.getItems().size() == 1) {
                    // It was likely opened from a specific dossier
                    resetFormFields();
                    Window owner = btnAjouter.getScene().getWindow();
                    AlertUtil.showInformation(owner, "Succès", "Séjour ajouté avec succès !");

                    // Close this window if it was opened as a modal dialog
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.close();
                } else {
                    resetFormFields();
                    Window owner = btnAjouter.getScene().getWindow();
                    AlertUtil.showInformation(owner, "Succès", "Séjour ajouté avec succès !");
                }
            } else {
                Window owner = btnAjouter.getScene().getWindow();
                AlertUtil.showError(owner, "Erreur", "Erreur lors de l'ajout du séjour. L'opération a échoué.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Window owner = btnAjouter.getScene().getWindow();
            AlertUtil.showError(owner, "Erreur", "Erreur lors de l'ajout du séjour\n" + e.getMessage());
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        // Validate form before saving
        if (!validateForm()) {
            return;
        }

        if (currentSejour == null) {
            Window owner = btnModifier.getScene().getWindow();
            AlertUtil.showWarning(owner, "Attention", "Veuillez sélectionner un séjour à modifier.");
            return;
        }

        try {
            updateSejourFromForm(currentSejour);

            boolean success = sejourService.modifierSejour(currentSejour);

            if (success) {
                // Reload the sejours to refresh the table
                loadSejours();
                resetFormFields();
                Window owner = btnModifier.getScene().getWindow();
                AlertUtil.showInformation(owner, "Succès", "Séjour mis à jour avec succès !");
            } else {
                Window owner = btnModifier.getScene().getWindow();
                AlertUtil.showError(owner, "Erreur", "Erreur lors de la mise à jour du séjour. L'opération a échoué.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Window owner = btnModifier.getScene().getWindow();
            AlertUtil.showError(owner, "Erreur", "Erreur lors de la mise à jour du séjour\n" + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        if (currentSejour == null) {
            Window owner = btnSupprimer.getScene().getWindow();
            AlertUtil.showWarning(owner, "Attention", "Veuillez sélectionner un séjour à supprimer.");
            return;
        }

        Window owner = btnSupprimer.getScene().getWindow();
        boolean confirm = AlertUtil.showConfirmation(owner, "Confirmation",
                "Êtes-vous sûr de vouloir supprimer ce séjour ? Cette action ne peut pas être annulée.");

        if (confirm) {
            try {
                sejourService.supprimerSejour(currentSejour.getId());
                loadSejours();
                resetFormFields();
                AlertUtil.showInformation(owner, "Succès", "Séjour supprimé avec succès !");
            } catch (Exception e) {
                AlertUtil.showError(owner, "Erreur", "Erreur lors de la suppression du séjour\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleVoir(ActionEvent event) {
        if (currentSejour == null) {
            Window owner = btnVoir.getScene().getWindow();
            AlertUtil.showWarning(owner, "Attention", "Veuillez sélectionner un séjour pour voir les détails.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sejour_detail.fxml"));
            Parent root = loader.load();

            SejourDetailController controller = loader.getController();
            controller.setSejour(currentSejour);

            Stage stage = new Stage();
            stage.setTitle("Détails du Séjour");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            Window owner = btnVoir.getScene().getWindow();
            AlertUtil.showError(owner, "Erreur", "Erreur lors de l'ouverture des détails du séjour\n" + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadSejours();
        resetFormFields();
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) sejourTable.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBackToHome(ActionEvent event) {
        try {
            // Load the interface.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interface.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Window owner = btnVoir.getScene().getWindow();
            AlertUtil.showError(owner, "Erreur", "Erreur lors du retour à l'interface principale\n" + e.getMessage());
        }
    }

    /**
     * Validate form fields before saving
     * @return true if the form is valid, false otherwise
     */
    private boolean validateForm() {
        // Clear previous validation errors
        validator.clearAllErrors();
        lblValidationError.setText("");

        StringBuilder errorMessages = new StringBuilder("Erreurs de validation:\n");
        boolean isValid = true;

        // Required field validation
        if (!validator.validateRequiredDatePicker("dateEntree", dateEntreePicker, "La date d'entrée est obligatoire")) {
            errorMessages.append("- La date d'entrée est obligatoire\n");
            isValid = false;
        }

        if (!validator.validateRequiredDatePicker("dateSortie", dateSortiePicker, "La date de sortie est obligatoire")) {
            errorMessages.append("- La date de sortie est obligatoire\n");
            isValid = false;
        }

        if (!validator.validateRequiredComboBox("typeSejour", comboTypeSejour, "Le type de séjour est obligatoire")) {
            errorMessages.append("- Le type de séjour est obligatoire\n");
            isValid = false;
        }

        if (!validator.validateNumericField("fraisSejour", txtFraisSejour, "Les frais doivent être un nombre valide")) {
            errorMessages.append("- Les frais de séjour doivent être un nombre valide\n");
            isValid = false;
        }

        if (!validator.validateRequiredComboBox("moyenPaiement", comboMoyenPaiement, "Le moyen de paiement est obligatoire")) {
            errorMessages.append("- Le moyen de paiement est obligatoire\n");
            isValid = false;
        }

        if (!validator.validateRequiredComboBox("statutPaiement", comboStatutPaiement, "Le statut de paiement est obligatoire")) {
            errorMessages.append("- Le statut de paiement est obligatoire\n");
            isValid = false;
        }

        if (!validator.validateRequiredComboBox("dossierMedicale", comboDossierMedicale, "Le dossier médical est obligatoire")) {
            errorMessages.append("- Le dossier médical est obligatoire\n");
            isValid = false;
        }

        // Numeric field validation for prix extras if not empty
        if (!txtPrixExtras.getText().isEmpty() && !validator.validateNumericField("prixExtras", txtPrixExtras, "Les prix extras doivent être un nombre valide")) {
            errorMessages.append("- Les prix extras doivent être un nombre valide\n");
            isValid = false;
        }

        // Date comparison validation
        LocalDate dateEntree = dateEntreePicker.getValue();
        LocalDate dateSortie = dateSortiePicker.getValue();
        if (dateEntree != null && dateSortie != null && !dateSortie.isAfter(dateEntree)) {
            errorMessages.append("- La date de sortie doit être postérieure à la date d'entrée\n");
            isValid = false;
        }

        // Show validation errors in a popup if validation fails
        if (!isValid) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText("Des erreurs ont été détectées dans le formulaire");
            alert.setContentText(errorMessages.toString());
            alert.showAndWait();

            // Keep all error boxes invisible
            boxDateEntreeError.setVisible(false);
            boxDateSortieError.setVisible(false);
            boxTypeSejourError.setVisible(false);
            boxFraisSejourError.setVisible(false);
            boxMoyenPaiementError.setVisible(false);
            boxStatutPaiementError.setVisible(false);
            boxPrixExtrasError.setVisible(false);
            boxDossierMedicaleError.setVisible(false);
        }

        return isValid;
    }

    private void updateSejourFromForm(Sejour sejour) {
        // Convert LocalDate to LocalDateTime by setting time to start/end of day
        LocalDate dateEntree = dateEntreePicker.getValue();
        if (dateEntree != null) {
            sejour.setDateEntree(LocalDateTime.of(dateEntree, LocalTime.of(0, 0)));
        } else {
            // Default to today as the date_entree is NOT NULL in the database
            sejour.setDateEntree(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)));
        }

        LocalDate dateSortie = dateSortiePicker.getValue();
        if (dateSortie != null) {
            sejour.setDateSortie(LocalDateTime.of(dateSortie, LocalTime.of(23, 59)));
        } else {
            // Default to date_entree + 1 day as date_sortie is NOT NULL in the database
            sejour.setDateSortie(sejour.getDateEntree().plusDays(1));
        }

        // Ensure type_sejour is not null (required by the database)
        String typeSejour = comboTypeSejour.getValue();
        sejour.setTypeSejour(typeSejour != null && !typeSejour.isEmpty() ? typeSejour : "Non spécifié");

        // Parse frais_sejour with default value if invalid
        try {
            double fraisSejour = Double.parseDouble(txtFraisSejour.getText());
            sejour.setFraisSejour(fraisSejour);
        } catch (NumberFormatException e) {
            sejour.setFraisSejour(0.0);
        }

        // Ensure moyen_paiement is not null (required by the database)
        String moyenPaiement = comboMoyenPaiement.getValue();
        sejour.setMoyenPaiement(moyenPaiement != null && !moyenPaiement.isEmpty() ? moyenPaiement : "Non spécifié");

        // Ensure statut_paiement is not null (required by the database)
        String statutPaiement = comboStatutPaiement.getValue();
        sejour.setStatutPaiement(statutPaiement != null && !statutPaiement.isEmpty() ? statutPaiement : "En attente");

        // Parse prix_extras with default value if invalid
        try {
            double prixExtras = Double.parseDouble(txtPrixExtras.getText());
            sejour.setPrixExtras(prixExtras);
        } catch (NumberFormatException e) {
            sejour.setPrixExtras(0.0);
        }

        // Set the dossier_medicale (required by the database)
        DossierMedicale dossier = comboDossierMedicale.getValue();
        if (dossier != null) {
            sejour.setDossierMedicale(dossier);
        } else {
            // This should not happen if the form validation works correctly
            System.err.println("Error: No DossierMedicale selected!");
        }
    }

    /**
     * Initialise le formulaire pour créer un nouveau séjour associé à un dossier médical spécifique
     * @param dossier Le dossier médical auquel associer le nouveau séjour
     */
    public void initNewSejour(DossierMedicale dossier) {
        resetFormFields();
        comboDossierMedicale.setValue(dossier);
        dateEntreePicker.setValue(LocalDate.now());
        comboStatutPaiement.setValue("En attente");
        txtFraisSejour.setText("0.0");
        txtPrixExtras.setText("0.0");
    }
} 