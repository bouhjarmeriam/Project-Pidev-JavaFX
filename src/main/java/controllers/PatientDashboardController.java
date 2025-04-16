package controllers;

import entite.DossierMedicale;
import entite.Sejour;
import entite.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.DossierMedicaleService;
import service.SejourService;
import service.UserService;
import util.AlertUtil;
import util.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PatientDashboardController implements Initializable {
    
    // Dashboard elements
    @FXML
    private Label lblPatientName;
    
    @FXML
    private Label lblWelcome;
    
    @FXML
    private Label lblDossierCount;
    
    @FXML
    private Label lblSejourCount;
    
    @FXML
    private TableView<Sejour> recentSejoursTable;
    
    @FXML
    private TableColumn<Sejour, String> colRecentDateEntree;
    
    @FXML
    private TableColumn<Sejour, String> colRecentDateSortie;
    
    @FXML
    private TableColumn<Sejour, String> colRecentTypeSejour;
    
    @FXML
    private TableColumn<Sejour, String> colRecentFraisSejour;
    
    @FXML
    private TableColumn<Sejour, String> colRecentStatutPaiement;
    
    // Dossier medical elements
    @FXML
    private ScrollPane dossierContent;
    
    @FXML
    private Label lblDossierId;
    
    @FXML
    private Label lblDossierDateCreation;
    
    @FXML
    private Label lblDossierMedecin;
    
    @FXML
    private Label lblDossierStatut;
    
    @FXML
    private Label lblHistoriqueMaladies;
    
    @FXML
    private Label lblOperationsPassees;
    
    @FXML
    private Label lblConsultationsPassees;
    
    @FXML
    private Label lblNotes;
    
    // Séjours elements
    @FXML
    private VBox sejoursContent;
    
    @FXML
    private TableView<Sejour> allSejoursTable;
    
    @FXML
    private TableColumn<Sejour, String> colAllDateEntree;
    
    @FXML
    private TableColumn<Sejour, String> colAllDateSortie;
    
    @FXML
    private TableColumn<Sejour, String> colAllTypeSejour;
    
    @FXML
    private TableColumn<Sejour, String> colAllFraisSejour;
    
    @FXML
    private TableColumn<Sejour, String> colAllMoyenPaiement;
    
    @FXML
    private TableColumn<Sejour, String> colAllStatutPaiement;
    
    @FXML
    private VBox dashboardContent;
    
    @FXML
    private TabPane tabPane;
    
    @FXML
    private Button btnVoirDetailSejour;
    
    // Services
    private DossierMedicaleService dossierService;
    private SejourService sejourService;
    private UserService userService;
    
    // Data
    private User currentUser;
    private DossierMedicale patientDossier;
    private ObservableList<Sejour> recentSejours;
    private ObservableList<Sejour> allSejours;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Logger logger = Logger.getLogger(PatientDashboardController.class.getName());
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        dossierService = new DossierMedicaleService();
        sejourService = new SejourService();
        userService = new UserService();
        
        // Set circular references
        dossierService.setSejourService(sejourService);
        sejourService.setDossierService(dossierService);
        
        // Initialize lists
        recentSejours = FXCollections.observableArrayList();
        allSejours = FXCollections.observableArrayList();
        
        // Setup table columns for recent sejours
        setupRecentSejoursTable();
        
        // Setup table columns for all sejours
        setupAllSejoursTable();
        
        // Get current user from session
        currentUser = SessionManager.getCurrentUser();
        
        // Check if user is logged in
        if (currentUser != null) {
            // Verify the user exists in the database
            User dbUser = userService.recupererUserParId(currentUser.getId());
            
            if (dbUser == null) {
                // For testing/development purposes, try to load a valid patient from the database
                logger.warning("User ID " + currentUser.getId() + " not found in database. Attempting to load a valid patient user.");
                
                // Try to find a valid patient user
                List<User> patients = userService.recupererUsersParRole("patient");
                if (!patients.isEmpty()) {
                    currentUser = patients.get(0);
                    logger.info("Loaded alternative patient user: ID=" + currentUser.getId() + 
                              ", Name=" + currentUser.getPrenom() + " " + currentUser.getNom());
                    
                    // Update session
                    SessionManager.setCurrentUser(currentUser);
                    loadUserData();
                } else {
                    logger.severe("No patient users found in the database");
                    AlertUtil.showError(null, "Erreur de Connexion", 
                            "Aucun utilisateur de type patient n'est disponible dans la base de données.");
                }
            } else {
                // Use the user from the database
                currentUser = dbUser;
                loadUserData();
            }
        } else {
            logger.warning("No user is currently logged in");
            AlertUtil.showError(null, "Erreur d'authentification", "Aucun utilisateur n'est connecté.");
            // Here you could redirect to login
        }
        
        // Tab pane listeners for content switching
        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            int index = newValue.intValue();
            updateVisibleContent(index);
        });
        
        // Set initial content visibility (Dashboard)
        updateVisibleContent(0);
        
        // Setup button disability for séjours details
        btnVoirDetailSejour.setDisable(true);
        allSejoursTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnVoirDetailSejour.setDisable(newSelection == null);
        });
        
        recentSejoursTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && dashboardContent.isVisible()) {
                btnVoirDetailSejour.setDisable(false);
            }
        });
    }
    
    private void loadUserData() {
        // Update UI with user info
        lblPatientName.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        lblWelcome.setText("Heureux de vous revoir, " + currentUser.getPrenom() + "!");
        
        // Load patient's medical record
        loadPatientDossier();
        
        // Load patient's séjours
        loadPatientSejours();
    }
    
    /**
     * Load patient's medical record
     */
    private void loadPatientDossier() {
        try {
            logger.info("Loading medical records for patient ID: " + currentUser.getId());
            
            // Verify that the user exists in the database
            User dbUser = userService.recupererUserParId(currentUser.getId());
            if (dbUser == null) {
                logger.severe("Failed to load dossier: User with ID " + currentUser.getId() + " does not exist in the database");
                AlertUtil.showError(null, "Erreur de Chargement", 
                        "Impossible de charger le dossier médical. L'utilisateur n'existe pas dans la base de données.");
                return;
            }
            
            // Update current user with database info to ensure consistency
            currentUser = dbUser;
            
            // Ensure the current user has patient role or type
            if (!currentUser.isPatient() && !"patient".equalsIgnoreCase(currentUser.getType())) {
                logger.warning("Current user is not a patient. Type: " + currentUser.getType() + ", Roles: " + currentUser.getRoles());
                AlertUtil.showWarning(null, "Attention", 
                        "Votre compte n'est pas configuré comme un compte patient. Veuillez contacter l'administration.");
                return;
            }
            
            // Get patient's dossiers - this will fetch all medical records where patient_id equals current user id
            List<DossierMedicale> dossiers = dossierService.recupererDossiersParPatient(currentUser.getId());
            
            if (dossiers != null && !dossiers.isEmpty()) {
                logger.info("Found " + dossiers.size() + " medical records for patient ID: " + currentUser.getId());
                
                // A patient should typically have one dossier, but we'll take the most recent one if there are multiple
                patientDossier = dossiers.stream()
                        .max(Comparator.comparing(DossierMedicale::getDateDeCreation))
                        .orElse(null);
                
                if (patientDossier != null) {
                    // Log more detailed information for debugging
                    logger.info("Selected dossier ID: " + patientDossier.getId() + 
                               ", Created: " + patientDossier.getDateDeCreation() + 
                               ", Status: " + patientDossier.getStatutDossier());
                    
                    // Make sure patient object is set in the dossier
                    if (patientDossier.getPatient() == null) {
                        patientDossier.setPatient(currentUser);
                    }
                    
                    // Make sure medecin object is loaded if not already present
                    if (patientDossier.getMedecin() == null && patientDossier.getMedecinId() > 0) {
                        User medecin = userService.recupererUserParId(patientDossier.getMedecinId());
                        if (medecin != null) {
                            patientDossier.setMedecin(medecin);
                            logger.info("Loaded doctor: " + medecin.getPrenom() + " " + medecin.getNom() + 
                                      ", Specialite: " + medecin.getSpecialite());
                        } else {
                            logger.warning("Failed to load doctor with ID: " + patientDossier.getMedecinId());
                        }
                    }
                    
                    // Update dashboard counts
                    lblDossierCount.setText("1");
                    
                    // Update dossier details in the UI
                    updateDossierDetails();
                    
                    // Load associated stays
                    loadPatientSejours();
                } else {
                    logger.warning("Failed to select a dossier from " + dossiers.size() + " records");
                    createNewDossierForPatient();
                }
            } else {
                logger.info("No medical records found for patient ID: " + currentUser.getId() + ", creating new one");
                createNewDossierForPatient();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading patient dossier", e);
            AlertUtil.showError(null, "Erreur", 
                    "Une erreur est survenue lors du chargement de votre dossier médical: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new medical record for the current patient and saves it to the database
     */
    private void createNewDossierForPatient() {
        try {
            logger.info("Creating new medical record for patient ID: " + currentUser.getId());
            
            // First, verify that the user exists in the database
            User dbUser = userService.recupererUserParId(currentUser.getId());
            if (dbUser == null) {
                logger.severe("Failed to create dossier: User with ID " + currentUser.getId() + " does not exist in the database");
                AlertUtil.showError(null, "Erreur de Création", 
                        "Impossible de créer un dossier médical. L'utilisateur n'existe pas dans la base de données.");
                return;
            }
            
            // If the user isn't a patient, we can't create a dossier
            if (!dbUser.isPatient() && !"patient".equalsIgnoreCase(dbUser.getType())) {
                logger.warning("Failed to create dossier: User with ID " + dbUser.getId() + " is not a patient (Type: " + dbUser.getType() + ")");
                AlertUtil.showWarning(null, "Erreur de Création", 
                        "Impossible de créer un dossier médical. L'utilisateur n'est pas un patient.");
                return;
            }
            
            // Create a new dossier for the patient
            DossierMedicale newDossier = new DossierMedicale();
            newDossier.setPatient(dbUser); // Use the database user to ensure it exists
            newDossier.setDateDeCreation(LocalDateTime.now());
            newDossier.setStatutDossier("Actif");
            
            // Find a doctor to assign (any doctor will do for now)
            List<User> doctors = userService.recupererUsersParRole("medecin");
            if (!doctors.isEmpty()) {
                newDossier.setMedecin(doctors.get(0));
            } else {
                // If no doctor is found, handle the situation - we can't create a dossier without a doctor
                logger.warning("No doctors found in the database, cannot create medical record");
                AlertUtil.showWarning(null, "Erreur de Création", 
                        "Impossible de créer un dossier médical. Aucun médecin n'est disponible dans le système.");
                return;
            }
            
            // Set default values for text fields
            newDossier.setHistoriqueDesMaladies("Aucun historique médical enregistré");
            newDossier.setOperationsPassees("Aucune opération enregistrée");
            newDossier.setConsultationsPassees("Aucune consultation enregistrée");
            newDossier.setNotes("Dossier créé automatiquement le " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
            
            // Save the new dossier to the database
            boolean success = dossierService.ajouterDossier(newDossier);
            
            if (success) {
                logger.info("Successfully created new dossier with ID: " + newDossier.getId());
                
                // Set the newly created dossier as the current one
                patientDossier = newDossier;
                
                // Update dashboard counts
                lblDossierCount.setText("1");
                
                // Update dossier details in the UI
                updateDossierDetails();
                
                // Show a success message
                AlertUtil.showInformation(null, "Dossier Médical Créé", 
                        "Un nouveau dossier médical a été créé pour vous. Veuillez contacter l'administration pour plus de détails.");
            } else {
                logger.warning("Failed to create new dossier for patient ID: " + currentUser.getId());
                AlertUtil.showWarning(null, "Erreur de Création", 
                        "Impossible de créer un nouveau dossier médical. Veuillez contacter l'administration.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating new dossier", e);
            AlertUtil.showError(null, "Erreur", 
                    "Une erreur est survenue lors de la création d'un nouveau dossier médical: " + e.getMessage());
        }
    }
    
    /**
     * Update the UI with medical record details
     */
    private void updateDossierDetails() {
        if (patientDossier == null) {
            logger.warning("Cannot update dossier details: patientDossier is null");
            // Set default "no data" values for all fields
            lblDossierId.setText("N/A");
            lblDossierDateCreation.setText("Non définie");
            lblDossierMedecin.setText("Non assigné");
            lblDossierStatut.setText("Non défini");
            lblHistoriqueMaladies.setText("Aucun historique enregistré");
            lblOperationsPassees.setText("Aucune opération enregistrée");
            lblConsultationsPassees.setText("Aucune consultation enregistrée");
            lblNotes.setText("Aucune note complémentaire");
            return;
        }
        
        logger.info("Updating UI with dossier details for ID: " + patientDossier.getId());
        
        // Update dossier info labels
        lblDossierId.setText(String.valueOf(patientDossier.getId()));
        
        // Format date
        if (patientDossier.getDateDeCreation() != null) {
            lblDossierDateCreation.setText(patientDossier.getDateDeCreation().format(DATE_FORMATTER));
        } else {
            lblDossierDateCreation.setText("Non définie");
        }
        
        // Get medecin name
        if (patientDossier.getMedecin() != null) {
            User medecin = patientDossier.getMedecin();
            String medecinFullName;
            
            if (medecin.getPrenom() != null && medecin.getNom() != null) {
                medecinFullName = "Dr. " + medecin.getPrenom() + " " + medecin.getNom();
                if (medecin.getSpecialite() != null && !medecin.getSpecialite().isEmpty()) {
                    medecinFullName += " (" + medecin.getSpecialite() + ")";
                }
                lblDossierMedecin.setText(medecinFullName);
            } else {
                lblDossierMedecin.setText("Dr. #" + medecin.getId() + " (détails non disponibles)");
            }
        } else {
            // If medecin is not loaded with the dossier, try to load it
            try {
                int medecinId = patientDossier.getMedecinId();
                if (medecinId > 0) {
                    User medecin = userService.recupererUserParId(medecinId);
                    if (medecin != null) {
                        String medecinFullName = "Dr. " + medecin.getPrenom() + " " + medecin.getNom();
                        if (medecin.getSpecialite() != null && !medecin.getSpecialite().isEmpty()) {
                            medecinFullName += " (" + medecin.getSpecialite() + ")";
                        }
                        lblDossierMedecin.setText(medecinFullName);
                        
                        // Store the doctor in the dossier object for future reference
                        patientDossier.setMedecin(medecin);
                    } else {
                        lblDossierMedecin.setText("Non assigné (ID: " + medecinId + ")");
                    }
                } else {
                    lblDossierMedecin.setText("Non assigné");
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error loading medecin for dossier", e);
                lblDossierMedecin.setText("Non assigné (erreur de chargement)");
            }
        }
        
        // Update statut with appropriate styling
        String statut = patientDossier.getStatutDossier();
        if (statut != null && !statut.isEmpty()) {
            lblDossierStatut.setText(statut);
            
            // Apply color based on status
            switch (statut.toLowerCase()) {
                case "actif":
                    lblDossierStatut.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    break;
                case "archivé":
                    lblDossierStatut.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
                    break;
                case "en attente":
                    lblDossierStatut.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    break;
                case "urgent":
                    lblDossierStatut.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    break;
                default:
                    lblDossierStatut.setStyle("-fx-text-fill: #37474f;"); // Default color
                    break;
            }
        } else {
            lblDossierStatut.setText("Non défini");
            lblDossierStatut.setStyle("-fx-text-fill: #37474f;"); // Default color
        }
        
        // Update text areas with formatting to make them more readable
        String historique = patientDossier.getHistoriqueDesMaladies();
        lblHistoriqueMaladies.setText(historique != null && !historique.trim().isEmpty() ? 
                historique : "Aucun historique médical enregistré");
        
        String operations = patientDossier.getOperationsPassees();
        lblOperationsPassees.setText(operations != null && !operations.trim().isEmpty() ? 
                operations : "Aucune opération chirurgicale enregistrée");
        
        String consultations = patientDossier.getConsultationsPassees();
        lblConsultationsPassees.setText(consultations != null && !consultations.trim().isEmpty() ? 
                consultations : "Aucune consultation précédente enregistrée");
        
        String notes = patientDossier.getNotes();
        lblNotes.setText(notes != null && !notes.trim().isEmpty() ? 
                notes : "Aucune note complémentaire");
    }
    
    /**
     * Load patient's séjours (stays) from the database
     */
    private void loadPatientSejours() {
        if (patientDossier == null) {
            logger.warning("Cannot load séjours: patientDossier is null");
            lblSejourCount.setText("0");
            recentSejours.clear();
            allSejours.clear();
            return;
        }
        
        try {
            logger.info("Loading séjours for dossier ID: " + patientDossier.getId());
            
            // Load séjours from database
            List<Sejour> sejours = sejourService.recupererSejoursParDossier(patientDossier.getId());
            
            if (sejours != null && !sejours.isEmpty()) {
                logger.info("Found " + sejours.size() + " séjours for dossier ID: " + patientDossier.getId());
                
                // Set the DossierMedicale reference in each Sejour for proper loading in detail view
                for (Sejour sejour : sejours) {
                    if (sejour.getDossierMedicale() == null) {
                        sejour.setDossierMedicale(patientDossier);
                    }
                    
                    // Log details for debugging
                    logger.fine("Loaded séjour ID: " + sejour.getId() + 
                             ", Type: " + sejour.getTypeSejour() + 
                             ", Entry: " + sejour.getDateEntree() + 
                             ", Exit: " + sejour.getDateSortie() + 
                             ", Status: " + sejour.getStatutPaiement());
                }
                
                // Clear previous data
                recentSejours.clear();
                allSejours.clear();
                
                // Add all séjours to the allSejours list
                allSejours.addAll(sejours);
                
                // Take only the 5 most recent séjours for the recent list
                List<Sejour> recentList = sejours.stream()
                        .sorted(Comparator.comparing(Sejour::getDateEntree).reversed())
                        .limit(5)
                        .collect(Collectors.toList());
                
                if (!recentList.isEmpty()) {
                    recentSejours.addAll(recentList);
                    logger.info("Added " + recentList.size() + " recent séjours to dashboard");
                } else {
                    logger.info("No recent séjours to display on dashboard");
                }
                
                // Update the count label
                lblSejourCount.setText(String.valueOf(sejours.size()));
            } else {
                // No séjours found
                logger.info("No séjours found for dossier ID: " + patientDossier.getId());
                lblSejourCount.setText("0");
                recentSejours.clear();
                allSejours.clear();
            }
            
            // Force refresh of table views
            recentSejoursTable.refresh();
            allSejoursTable.refresh();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading patient séjours", e);
            AlertUtil.showError(null, "Erreur", 
                    "Une erreur est survenue lors du chargement de vos séjours: " + e.getMessage());
            
            // Clear tables on error
            recentSejours.clear();
            allSejours.clear();
            lblSejourCount.setText("0");
        }
    }
    
    private void setupRecentSejoursTable() {
        // Setup column cell value factories with appropriate formatting
        colRecentDateEntree.setCellValueFactory(param -> {
            if (param.getValue().getDateEntree() != null) {
                return new SimpleStringProperty(param.getValue().getDateEntree().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("Non définie");
        });
        
        colRecentDateSortie.setCellValueFactory(param -> {
            if (param.getValue().getDateSortie() != null) {
                return new SimpleStringProperty(param.getValue().getDateSortie().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("Non définie");
        });
        
        colRecentTypeSejour.setCellValueFactory(param -> {
            String typeSejour = param.getValue().getTypeSejour();
            return new SimpleStringProperty(typeSejour != null ? typeSejour : "");
        });
        
        colRecentFraisSejour.setCellValueFactory(param -> {
            Double frais = param.getValue().getFraisSejour();
            if (frais != null) {
                return new SimpleStringProperty(String.format("%.2f €", frais));
            }
            return new SimpleStringProperty("0.00 €");
        });
        
        colRecentStatutPaiement.setCellValueFactory(param -> {
            String statut = param.getValue().getStatutPaiement();
            return new SimpleStringProperty(statut != null ? statut : "");
        });
        
        // Set custom cell factory for statut column to apply different colors
        colRecentStatutPaiement.setCellFactory(column -> new TableCell<Sejour, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    // Set style based on payment status
                    if (item.equalsIgnoreCase("Payé")) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                    } else if (item.equalsIgnoreCase("En attente")) {
                        setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                    } else if (item.equalsIgnoreCase("Annulé")) {
                        setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                    } else if (item.equalsIgnoreCase("Partiel")) {
                        setStyle("-fx-background-color: #cce5ff; -fx-text-fill: #004085;");
                    } else if (item.equalsIgnoreCase("Remboursé")) {
                        setStyle("-fx-background-color: #e0cfef; -fx-text-fill: #4b2354;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        // Set the items
        recentSejoursTable.setItems(recentSejours);
    }
    
    private void setupAllSejoursTable() {
        // Setup column cell value factories with appropriate formatting
        colAllDateEntree.setCellValueFactory(param -> {
            if (param.getValue().getDateEntree() != null) {
                return new SimpleStringProperty(param.getValue().getDateEntree().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("Non définie");
        });
        
        colAllDateSortie.setCellValueFactory(param -> {
            if (param.getValue().getDateSortie() != null) {
                return new SimpleStringProperty(param.getValue().getDateSortie().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("Non définie");
        });
        
        colAllTypeSejour.setCellValueFactory(param -> {
            String typeSejour = param.getValue().getTypeSejour();
            return new SimpleStringProperty(typeSejour != null ? typeSejour : "");
        });
        
        colAllFraisSejour.setCellValueFactory(param -> {
            Double frais = param.getValue().getFraisSejour();
            if (frais != null) {
                return new SimpleStringProperty(String.format("%.2f €", frais));
            }
            return new SimpleStringProperty("0.00 €");
        });
        
        colAllMoyenPaiement.setCellValueFactory(param -> {
            String moyenPaiement = param.getValue().getMoyenPaiement();
            return new SimpleStringProperty(moyenPaiement != null ? moyenPaiement : "");
        });
        
        colAllStatutPaiement.setCellValueFactory(param -> {
            String statut = param.getValue().getStatutPaiement();
            return new SimpleStringProperty(statut != null ? statut : "");
        });
        
        // Set custom cell factory for statut column to apply different colors
        colAllStatutPaiement.setCellFactory(column -> new TableCell<Sejour, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    // Set style based on payment status
                    if (item.equalsIgnoreCase("Payé")) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                    } else if (item.equalsIgnoreCase("En attente")) {
                        setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                    } else if (item.equalsIgnoreCase("Annulé")) {
                        setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                    } else if (item.equalsIgnoreCase("Partiel")) {
                        setStyle("-fx-background-color: #cce5ff; -fx-text-fill: #004085;");
                    } else if (item.equalsIgnoreCase("Remboursé")) {
                        setStyle("-fx-background-color: #e0cfef; -fx-text-fill: #4b2354;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        // Set the items
        allSejoursTable.setItems(allSejours);
    }
    
    private void updateVisibleContent(int tabIndex) {
        // Hide all content first
        dashboardContent.setVisible(false);
        dashboardContent.setManaged(false);
        dossierContent.setVisible(false);
        dossierContent.setManaged(false);
        sejoursContent.setVisible(false);
        sejoursContent.setManaged(false);
        
        // Show selected content
        switch (tabIndex) {
            case 0: // Dashboard
                dashboardContent.setVisible(true);
                dashboardContent.setManaged(true);
                break;
            case 1: // Dossier médical
                dossierContent.setVisible(true);
                dossierContent.setManaged(true);
                break;
            case 2: // Séjours
                sejoursContent.setVisible(true);
                sejoursContent.setManaged(true);
                break;
        }
    }
    
    @FXML
    private void handleDossierTabSelected(javafx.event.Event event) {
        if (patientDossier == null) {
            // If we're selecting the dossier tab but no dossier is loaded, try to load it
            loadPatientDossier();
        }
        
        Tab tab = (Tab) event.getSource();
        if (tab.isSelected()) {
            updateVisibleContent(1);
        }
    }
    
    @FXML
    private void handleSejoursTabSelected(javafx.event.Event event) {
        if (allSejours.isEmpty()) {
            // If we're selecting the séjours tab but no séjours are loaded, try to load them
            loadPatientSejours();
        }
        
        Tab tab = (Tab) event.getSource();
        if (tab.isSelected()) {
            updateVisibleContent(2);
        }
    }
    
    @FXML
    private void handleVoirDetailSejour(ActionEvent event) {
        Sejour selectedSejour = null;
        
        // Check which table is visible and get the selected sejour
        if (dashboardContent.isVisible()) {
            selectedSejour = recentSejoursTable.getSelectionModel().getSelectedItem();
        } else if (sejoursContent.isVisible()) {
            selectedSejour = allSejoursTable.getSelectionModel().getSelectedItem();
        }
        
        if (selectedSejour == null) {
            AlertUtil.showError(((Node) event.getSource()).getScene().getWindow(), 
                    "Erreur", "Veuillez sélectionner un séjour à visualiser.");
            return;
        }
        
        try {
            // Ensure DossierMedicale is set in the sejour if not already loaded
            if (selectedSejour.getDossierMedicale() == null && patientDossier != null) {
                selectedSejour.setDossierMedicale(patientDossier);
            }
            
            // Load the full Sejour with all details if needed
            if (selectedSejour.getTypeSejour() == null || selectedSejour.getMoyenPaiement() == null) {
                Sejour fullSejour = sejourService.recupererSejourParId(selectedSejour.getId());
                if (fullSejour != null) {
                    selectedSejour = fullSejour;
                    
                    // Make sure DossierMedicale is set in case it wasn't loaded from database
                    if (fullSejour.getDossierMedicale() == null && patientDossier != null) {
                        fullSejour.setDossierMedicale(patientDossier);
                    }
                }
            }
            
            // Load the sejour detail view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sejour_detail_patient.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the sejour
            SejourDetailPatientController controller = loader.getController();
            controller.setSejour(selectedSejour);
            
            // Show the stage
            Stage stage = new Stage();
            stage.setTitle("Détails du Séjour #" + selectedSejour.getId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.show();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error opening sejour detail view", e);
            AlertUtil.showError(((Node) event.getSource()).getScene().getWindow(), 
                    "Erreur", "Impossible d'ouvrir la vue détaillée du séjour: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout(ActionEvent event) {
        // Clear current session
        SessionManager.logout();

        try {
            // Load the front view instead of login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front.fxml"));
            Parent root = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Set the new scene
            stage.setScene(new Scene(root));
            stage.setTitle("Accueil");
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error navigating to front view", e);
            AlertUtil.showError(((Node) event.getSource()).getScene().getWindow(), 
                    "Erreur", "Impossible de retourner à l'accueil: " + e.getMessage());
        }
    }
} 