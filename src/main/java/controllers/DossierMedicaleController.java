package controllers;

import entite.DossierMedicale;
import entite.Sejour;
import entite.User;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import service.DossierMedicaleService;
import service.SejourService;
import service.UserService;
import util.AlertUtil;
import util.ImageUploadUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DossierMedicaleController implements Initializable {

    // All existing @FXML declarations remain exactly the same
    @FXML private TableView<DossierMedicale> dossierTable;
    @FXML private TableColumn<DossierMedicale, Integer> colId;
    @FXML private TableColumn<DossierMedicale, String> colDateCreation;
    @FXML private TableColumn<DossierMedicale, String> colPatient;
    @FXML private TableColumn<DossierMedicale, String> colMedecin;
    @FXML private TableColumn<DossierMedicale, String> colStatut;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnVoir;
    @FXML private DatePicker dateCreationPicker;
    @FXML private TextArea txtHistoriqueMaladies;
    @FXML private TextArea txtOperationsPassees;
    @FXML private TextArea txtConsultationsPassees;
    @FXML private ComboBox<String> comboStatutDossier;
    @FXML private TextArea txtNotes;
    @FXML private ComboBox<User> comboPatient;
    @FXML private ComboBox<User> comboMedecin;
    @FXML private Button btnChooseImage;
    @FXML private ImageView imagePreview;
    @FXML private TableView<Sejour> sejourTable;
    @FXML private TableColumn<Sejour, String> colDateEntree;
    @FXML private TableColumn<Sejour, String> colDateSortie;
    @FXML private TableColumn<Sejour, String> colTypeSejour;
    @FXML private TableColumn<Sejour, Double> colFraisSejour;
    @FXML private TableColumn<Sejour, String> colStatutPaiement;
    @FXML private Label lblImagePath;
    @FXML private Label lblValidationError;

    private DossierMedicaleService dossierService;
    private SejourService sejourService;
    private UserService userService;
    private ObservableList<DossierMedicale> dossierList;
    private ObservableList<Sejour> sejourList;
    private DossierMedicale currentDossier;
    private File selectedImageFile;
    private String currentImageFilename;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] STATUT_OPTIONS = {"Actif", "Archivé", "En attente"};

    // Added validation constants
    private static final int MAX_HISTORY_LENGTH = 2000;
    private static final int MAX_OPERATIONS_LENGTH = 2000;
    private static final int MAX_CONSULTATIONS_LENGTH = 2000;
    private static final int MAX_NOTES_LENGTH = 1000;
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(".png", ".jpg", ".jpeg", ".gif");
    private static final long MAX_IMAGE_SIZE_KB = 2048; // 2MB

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Existing initialization code remains exactly the same
        dossierService = new DossierMedicaleService();
        sejourService = new SejourService();
        userService = new UserService();

        dossierService.setSejourService(sejourService);
        sejourService.setDossierService(dossierService);

        dossierList = FXCollections.observableArrayList();
        sejourList = FXCollections.observableArrayList();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDateCreation.setCellValueFactory(cellData -> {
            DossierMedicale dossier = cellData.getValue();
            LocalDateTime date = dossier.getDateDeCreation();
            return new SimpleStringProperty(date != null ? date.format(DATE_FORMATTER) : "");
        });
        colPatient.setCellValueFactory(cellData -> {
            DossierMedicale dossier = cellData.getValue();
            User patient = dossier.getPatient();
            return new SimpleStringProperty(patient != null ? patient.getPrenom() + " " + patient.getNom() : "");
        });
        colMedecin.setCellValueFactory(cellData -> {
            DossierMedicale dossier = cellData.getValue();
            User medecin = dossier.getMedecin();
            return new SimpleStringProperty(medecin != null ? medecin.getPrenom() + " " + medecin.getNom() : "");
        });
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutDossier"));

        colDateEntree.setCellValueFactory(cellData -> {
            Sejour sejour = cellData.getValue();
            LocalDateTime date = sejour.getDateEntree();
            return new SimpleStringProperty(date != null ? date.format(DATE_FORMATTER) : "");
        });
        colDateSortie.setCellValueFactory(cellData -> {
            Sejour sejour = cellData.getValue();
            LocalDateTime date = sejour.getDateSortie();
            return new SimpleStringProperty(date != null ? date.format(DATE_FORMATTER) : "");
        });
        colTypeSejour.setCellValueFactory(new PropertyValueFactory<>("typeSejour"));
        colFraisSejour.setCellValueFactory(new PropertyValueFactory<>("fraisSejour"));
        colStatutPaiement.setCellValueFactory(new PropertyValueFactory<>("statutPaiement"));

        sejourTable.setItems(sejourList);
        dossierTable.setItems(dossierList);

        comboStatutDossier.setItems(FXCollections.observableArrayList(STATUT_OPTIONS));

        // Added text limits setup
        setupTextLimits();

        loadUsers();
        loadDossiers();

        dossierTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                currentDossier = newSelection;
                loadSejoursForCurrentDossier();
                displayDossierDetails(newSelection);
                enableDossierButtons(true);
            } else {
                currentDossier = null;
                sejourList.clear();
                clearDossierForm();
                enableDossierButtons(false);
            }
        });

        enableDossierButtons(false);
    }

    private void setupTextLimits() {
        txtHistoriqueMaladies.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > MAX_HISTORY_LENGTH) {
                txtHistoriqueMaladies.setText(oldValue);
            }
        });

        txtOperationsPassees.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > MAX_OPERATIONS_LENGTH) {
                txtOperationsPassees.setText(oldValue);
            }
        });

        txtConsultationsPassees.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > MAX_CONSULTATIONS_LENGTH) {
                txtConsultationsPassees.setText(oldValue);
            }
        });

        txtNotes.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > MAX_NOTES_LENGTH) {
                txtNotes.setText(oldValue);
            }
        });
    }

    // Existing displayDossierDetails method remains exactly the same
    private void displayDossierDetails(DossierMedicale dossier) {
        if (dossier == null) {
            clearDossierForm();
            return;
        }

        LocalDateTime dateCreation = dossier.getDateDeCreation();
        // TODO: Set date picker value when implemented

        txtHistoriqueMaladies.setText(dossier.getHistoriqueDesMaladies());
        txtOperationsPassees.setText(dossier.getOperationsPassees());
        txtConsultationsPassees.setText(dossier.getConsultationsPassees());
        comboStatutDossier.setValue(dossier.getStatutDossier());
        txtNotes.setText(dossier.getNotes());
        comboPatient.setValue(dossier.getPatient());
        comboMedecin.setValue(dossier.getMedecin());

        // Display image if available
        currentImageFilename = dossier.getImage();
        displayImage(currentImageFilename);
    }

    // All other existing methods remain exactly the same
    private void loadUsers() {
        // Debug: Get ALL users to check their types
        List<User> allUsers = userService.recupererTousUsers();
        System.out.println("Total users in database: " + allUsers.size());
        System.out.println("User types in database:");

        // Count users by type
        for (User user : allUsers) {
            System.out.println("User ID: " + user.getId() +
                    ", Name: " + user.getPrenom() + " " + user.getNom() +
                    ", Type: " + user.getType() +
                    ", Specialite: " + user.getSpecialite());
        }

        // Load patients using the type field
        List<User> patients = userService.recupererUsersParRole("patient");
        System.out.println("Retrieved " + patients.size() + " patients from database");
        comboPatient.setItems(FXCollections.observableArrayList(patients));

        // Try loading doctors with a different approach
        // First try with "medcin" (not "medecin" - note the spelling from your database output)
        List<User> medecins = userService.recupererUsersParRole("medcin");

        // If that doesn't work, try with the type "bvnh" based on your database screenshot
        if (medecins.isEmpty()) {
            System.out.println("No doctors found with type 'medcin', trying 'bvnh'");
            medecins = userService.recupererUsersParRole("bvnh");
        }

        // As a last resort, find users who have a specialite field set
        if (medecins.isEmpty()) {
            System.out.println("Still no doctors, finding users with specialite set");
            for (User user : allUsers) {
                if (user.getSpecialite() != null && !user.getSpecialite().isEmpty() &&
                        !user.getType().equals("patient")) {
                    medecins.add(user);
                }
            }
        }

        System.out.println("Retrieved " + medecins.size() + " doctors from database");
        comboMedecin.setItems(FXCollections.observableArrayList(medecins));

        // Setup string converters for both combo boxes to display user names
        javafx.util.StringConverter<User> userStringConverter = new javafx.util.StringConverter<User>() {
            @Override
            public String toString(User user) {
                if (user == null) return "";
                return user.getPrenom() + " " + user.getNom();
            }

            @Override
            public User fromString(String string) {
                // This is not needed for our use case
                return null;
            }
        };

        comboPatient.setConverter(userStringConverter);
        comboMedecin.setConverter(userStringConverter);
    }

    private void loadDossiers() {
        try {
            List<DossierMedicale> dossiers = dossierService.recupererTousDossiers();
            if (dossiers != null) {
                System.out.println("Loaded " + dossiers.size() + " dossiers");
                dossierList.clear();
                dossierList.addAll(dossiers);
                dossierTable.refresh();
            } else {
                System.out.println("No dossiers loaded - returned null list");
                dossierList.clear();
            }
        } catch (Exception e) {
            System.err.println("Error loading dossiers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSejoursForCurrentDossier() {
        sejourList.clear();  // Always clear the list first

        if (currentDossier != null) {
            try {
                // Set the items again to be safe
                sejourTable.setItems(sejourList);

                List<Sejour> sejours = sejourService.recupererSejoursParDossier(currentDossier.getId());
                System.out.println("Loading sejours for dossier ID: " + currentDossier.getId() +
                        " - Found: " + (sejours != null ? sejours.size() : 0));

                if (sejours != null && !sejours.isEmpty()) {
                    sejourList.addAll(sejours);
                    System.out.println("Added " + sejours.size() + " sejours to the observable list");
                }

                // Force a refresh
                sejourTable.refresh();
            } catch (Exception e) {
                System.err.println("Error loading sejours for dossier: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void clearDossierForm() {
        dateCreationPicker.setValue(null);
        txtHistoriqueMaladies.clear();
        txtOperationsPassees.clear();
        txtConsultationsPassees.clear();
        comboStatutDossier.setValue(null);
        txtNotes.clear();
        comboPatient.setValue(null);
        comboMedecin.setValue(null);
        imagePreview.setImage(null);
        currentImageFilename = null;
        selectedImageFile = null;
    }

    private void enableDossierButtons(boolean enable) {
        btnModifier.setDisable(!enable);
        btnSupprimer.setDisable(!enable);
        btnVoir.setDisable(!enable);
    }

    private void displayImage(String filename) {
        if (filename != null && !filename.isEmpty() && ImageUploadUtil.imageExists(filename)) {
            String imagePath = ImageUploadUtil.getImagePath(filename);
            if (imagePath != null) {
                try {
                    Image image = new Image(new File(imagePath).toURI().toString());
                    imagePreview.setImage(image);
                    imagePreview.setPreserveRatio(true);
                    imagePreview.setFitWidth(200);
                } catch (Exception e) {
                    System.err.println("Error loading image: " + e.getMessage());
                    imagePreview.setImage(null);
                }
            }
        } else {
            imagePreview.setImage(null);
        }
    }

    @FXML
    private void handleChooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", ".png", ".jpg", ".jpeg", ".gif")
        );

        Window owner = ((Node) event.getSource()).getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(owner);

        if (selectedImageFile != null) {
            // Added validation for image type and size
            String fileName = selectedImageFile.getName().toLowerCase();
            boolean validType = ALLOWED_IMAGE_TYPES.stream().anyMatch(fileName::endsWith);
            boolean validSize = selectedImageFile.length() <= MAX_IMAGE_SIZE_KB * 1024;

            if (!validType || !validSize) {
                AlertUtil.showError(owner, "Erreur",
                        validType ? "La taille de l'image doit être inférieure à 2MB" :
                                "Type d'image non supporté. Formats acceptés: PNG, JPG, JPEG, GIF");
                return;
            }

            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                imagePreview.setImage(image);
                imagePreview.setPreserveRatio(true);
                imagePreview.setFitWidth(200);
            } catch (Exception e) {
                AlertUtil.showError(owner, "Erreur", "Impossible de charger l'image sélectionnée.");
            }
        }
    }

    /**
     * Validate form fields before saving
     * @return true if the form is valid, false otherwise
     */
    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();

        // Check required fields
        if (dateCreationPicker.getValue() == null) {
            errorMessage.append("- La date de création est obligatoire\n");
        }

        if (comboStatutDossier.getValue() == null || comboStatutDossier.getValue().isEmpty()) {
            errorMessage.append("- Le statut du dossier est obligatoire\n");
        }

        if (comboPatient.getValue() == null) {
            errorMessage.append("- Le patient est obligatoire\n");
        }

        if (comboMedecin.getValue() == null) {
            errorMessage.append("- Le médecin est obligatoire\n");
        }

        // Added validation for text fields
        if (txtHistoriqueMaladies.getText() == null || txtHistoriqueMaladies.getText().trim().isEmpty()) {
            errorMessage.append("- L'historique des maladies est obligatoire\n");
        }

        if (txtOperationsPassees.getText() == null || txtOperationsPassees.getText().trim().isEmpty()) {
            errorMessage.append("- Les opérations passées sont obligatoires\n");
        }

        if (txtConsultationsPassees.getText() == null || txtConsultationsPassees.getText().trim().isEmpty()) {
            errorMessage.append("- Les consultations passées sont obligatoires\n");
        }

        if (txtNotes.getText() == null || txtNotes.getText().trim().isEmpty()) {
            errorMessage.append("- Les notes sont obligatoires\n");
        }

        // Added validation for image
        if (selectedImageFile == null && (currentDossier == null || currentDossier.getImage() == null)) {
            errorMessage.append("- Une image est obligatoire\n");
        }

        // Display error message if validation fails
        if (errorMessage.length() > 0) {
            lblValidationError.setText("Veuillez corriger les erreurs suivantes:\n" + errorMessage.toString());
            lblValidationError.setVisible(true);
            lblValidationError.setManaged(true);
            return false;
        }

        // Hide error message if validation passes
        lblValidationError.setVisible(false);
        lblValidationError.setManaged(false);
        return true;
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        // Validate form before saving
        if (!validateForm()) {
            return;
        }

        Window owner = ((Node) event.getSource()).getScene().getWindow();

        // Create new dossier
        DossierMedicale dossier = new DossierMedicale();
        dossier.setDateDeCreation(LocalDateTime.now());
        dossier.setHistoriqueDesMaladies(txtHistoriqueMaladies.getText());
        dossier.setOperationsPassees(txtOperationsPassees.getText());
        dossier.setConsultationsPassees(txtConsultationsPassees.getText());
        dossier.setStatutDossier(comboStatutDossier.getValue());
        dossier.setNotes(txtNotes.getText());
        dossier.setPatient(comboPatient.getValue());
        dossier.setMedecin(comboMedecin.getValue());

        // Handle image upload
        if (selectedImageFile != null) {
            String newFilename = ImageUploadUtil.saveImage(selectedImageFile, selectedImageFile.getName());
            if (newFilename != null) {
                dossier.setImage(newFilename);
            }
        }

        // Save dossier
        boolean success = dossierService.ajouterDossier(dossier);

        if (success) {
            AlertUtil.showInformation(owner, "Succès", "Le dossier médical a été créé avec succès.");
            loadDossiers();
            clearDossierForm();
        } else {
            AlertUtil.showError(owner, "Erreur", "Une erreur s'est produite lors de la création du dossier médical.");
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        // Validate form before saving
        if (!validateForm()) {
            return;
        }

        Window owner = ((Node) event.getSource()).getScene().getWindow();

        if (currentDossier == null) {
            AlertUtil.showError(owner, "Erreur", "Veuillez sélectionner un dossier à modifier.");
            return;
        }

        // Update dossier
        currentDossier.setHistoriqueDesMaladies(txtHistoriqueMaladies.getText());
        currentDossier.setOperationsPassees(txtOperationsPassees.getText());
        currentDossier.setConsultationsPassees(txtConsultationsPassees.getText());
        currentDossier.setStatutDossier(comboStatutDossier.getValue());
        currentDossier.setNotes(txtNotes.getText());
        currentDossier.setPatient(comboPatient.getValue());
        currentDossier.setMedecin(comboMedecin.getValue());

        // Handle image upload
        if (selectedImageFile != null) {
            String newFilename = ImageUploadUtil.saveImage(selectedImageFile, selectedImageFile.getName());
            if (newFilename != null) {
                // Delete old image if exists
                if (currentImageFilename != null && !currentImageFilename.isEmpty()) {
                    ImageUploadUtil.deleteImage(currentImageFilename);
                }
                currentDossier.setImage(newFilename);
            }
        }

        // Save dossier
        boolean success = dossierService.modifierDossier(currentDossier);

        if (success) {
            AlertUtil.showInformation(owner, "Succès", "Le dossier médical a été mis à jour avec succès.");
            loadDossiers();
        } else {
            AlertUtil.showError(owner, "Erreur", "Une erreur s'est produite lors de la mise à jour du dossier médical.");
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        Window owner = ((Node) event.getSource()).getScene().getWindow();

        if (currentDossier == null) {
            AlertUtil.showError(owner, "Erreur", "Veuillez sélectionner un dossier à supprimer.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation(owner, "Confirmation",
                "Êtes-vous sûr de vouloir supprimer ce dossier médical ? Cette action est irréversible.");

        if (!confirm) {
            return;
        }

        // Delete image if exists
        if (currentDossier.getImage() != null && !currentDossier.getImage().isEmpty()) {
            ImageUploadUtil.deleteImage(currentDossier.getImage());
        }

        // Delete dossier
        boolean success = dossierService.supprimerDossier(currentDossier.getId());

        if (success) {
            AlertUtil.showInformation(owner, "Succès", "Le dossier médical a été supprimé avec succès.");
            loadDossiers();
            clearDossierForm();
        } else {
            AlertUtil.showError(owner, "Erreur", "Une erreur s'est produite lors de la suppression du dossier médical.");
        }
    }

    @FXML
    private void handleVoir(ActionEvent event) {
        if (currentDossier == null) {
            AlertUtil.showError(((Node) event.getSource()).getScene().getWindow(),
                    "Erreur", "Veuillez sélectionner un dossier à visualiser.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dossier_detail.fxml"));
            Parent root = loader.load();

            DossierDetailController controller = loader.getController();
            controller.setDossier(currentDossier);

            Stage stage = new Stage();
            stage.setTitle("Détails du Dossier Médical #" + currentDossier.getId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError(((Node) event.getSource()).getScene().getWindow(),
                    "Erreur", "Impossible d'ouvrir la vue détaillée du dossier.");
        }
    }

    @FXML
    private void handleAjouterSejour(ActionEvent event) {
        if (currentDossier == null) {
            AlertUtil.showError(((Node) event.getSource()).getScene().getWindow(),
                    "Erreur", "Veuillez d'abord sélectionner un dossier médical.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sejour_form.fxml"));
            Parent root = loader.load();

            SejourController controller = loader.getController();
            controller.initNewSejour(currentDossier);

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Séjour");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.setOnHidden(e -> loadSejoursForCurrentDossier());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError(((Node) event.getSource()).getScene().getWindow(),
                    "Erreur", "Impossible d'ouvrir le formulaire de séjour.");
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadDossiers();
        clearDossierForm();
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
            AlertUtil.showError(null, "Erreur", "Erreur lors du retour à l'interface principale\n" + e.getMessage());
        }
    }
}
