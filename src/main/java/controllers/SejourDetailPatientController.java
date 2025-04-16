package controllers;

import entite.Sejour;
import entite.DossierMedicale;
import entite.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import service.UserService;
import service.DossierMedicaleService;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SejourDetailPatientController implements Initializable {
    
    @FXML
    private Label lblId;
    
    @FXML
    private Label lblDateEntree;
    
    @FXML
    private Label lblDateSortie;
    
    @FXML
    private Label lblTypeSejour;
    
    @FXML
    private Label lblFraisSejour;
    
    @FXML
    private Label lblPrixExtras;
    
    @FXML
    private Label lblMoyenPaiement;
    
    @FXML
    private Label lblStatutPaiement;
    
    @FXML
    private Label lblStatutIndicator;
    
    @FXML
    private Label lblTotal;
    
    @FXML
    private Label lblDuree;
    
    @FXML
    private Label lblPatientNom;
    
    private Sejour sejour;
    private UserService userService;
    private DossierMedicaleService dossierService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Logger logger = Logger.getLogger(SejourDetailPatientController.class.getName());
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = new UserService();
        dossierService = new DossierMedicaleService();
    }
    
    /**
     * Set the sejour and display its details
     * @param sejour The sejour to display
     */
    public void setSejour(Sejour sejour) {
        this.sejour = sejour;
        displaySejourDetails();
    }
    
    /**
     * Display the details of the current sejour
     */
    private void displaySejourDetails() {
        if (sejour == null) {
            logger.severe("Error: sejour is null in displaySejourDetails");
            return;
        }
        
        // Basic Info
        lblId.setText(String.valueOf(sejour.getId()));
        
        LocalDateTime dateEntree = sejour.getDateEntree();
        lblDateEntree.setText(dateEntree != null ? dateEntree.format(DATE_FORMATTER) : "Non définie");
        
        LocalDateTime dateSortie = sejour.getDateSortie();
        lblDateSortie.setText(dateSortie != null ? dateSortie.format(DATE_FORMATTER) : "Non définie");
        
        lblTypeSejour.setText(sejour.getTypeSejour() != null ? sejour.getTypeSejour() : "Non défini");
        
        // Calculate and display stay duration if both dates are available
        if (dateEntree != null && dateSortie != null) {
            long days = ChronoUnit.DAYS.between(dateEntree.toLocalDate(), dateSortie.toLocalDate());
            if (lblDuree != null) {
                lblDuree.setText(days + " jour" + (days > 1 ? "s" : ""));
            }
        } else if (lblDuree != null) {
            lblDuree.setText("Non calculable");
        }
        
        // Get patient information
        loadPatientInfo();
        
        // Payment Info
        Double fraisSejour = sejour.getFraisSejour();
        lblFraisSejour.setText(fraisSejour != null ? String.format("%.2f €", fraisSejour) : "0.00 €");
        
        Double prixExtras = sejour.getPrixExtras();
        lblPrixExtras.setText(prixExtras != null ? String.format("%.2f €", prixExtras) : "0.00 €");
        
        lblMoyenPaiement.setText(sejour.getMoyenPaiement() != null ? sejour.getMoyenPaiement() : "Non défini");
        
        // Status with colored indicator
        String statut = sejour.getStatutPaiement();
        lblStatutPaiement.setText(statut != null ? statut : "Non défini");
        
        // Set the right style and color for the status indicator
        setStatusIndicator(statut);
        
        // Calculate and display total
        double total = (fraisSejour != null ? fraisSejour : 0) + (prixExtras != null ? prixExtras : 0);
        lblTotal.setText(String.format("%.2f €", total));
    }
    
    /**
     * Loads patient information based on the Sejour's DossierMedicale
     */
    private void loadPatientInfo() {
        if (lblPatientNom == null) {
            return;
        }
        
        try {
            // First, check if Sejour has a DossierMedicale with a patient
            if (sejour.getDossierMedicale() != null && sejour.getDossierMedicale().getPatient() != null) {
                User patient = sejour.getDossierMedicale().getPatient();
                lblPatientNom.setText(patient.getPrenom() + " " + patient.getNom());
                return;
            }
            
            // If DossierMedicale is not fully loaded, try to get it by ID
            if (sejour.getDossierMedicale() != null && sejour.getDossierMedicale().getId() > 0) {
                int dossierId = sejour.getDossierMedicale().getId();
                
                // Load the full DossierMedicale
                DossierMedicale dossier = dossierService.recupererDossierParId(dossierId, false);
                if (dossier != null && dossier.getPatient() != null) {
                    User patient = dossier.getPatient();
                    lblPatientNom.setText(patient.getPrenom() + " " + patient.getNom());
                    return;
                }
                
                // If DossierMedicale loaded but patient is not loaded, try to get patient by ID
                if (dossier != null && dossier.getPatientId() > 0) {
                    User patient = userService.recupererUserParId(dossier.getPatientId());
                    if (patient != null) {
                        lblPatientNom.setText(patient.getPrenom() + " " + patient.getNom());
                        return;
                    }
                }
            }
            
            // If all else fails
            lblPatientNom.setText("Information patient non disponible");
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error loading patient info", e);
            lblPatientNom.setText("Erreur de chargement");
        }
    }
    
    /**
     * Sets the status indicator style based on payment status
     * @param statut the payment status
     */
    private void setStatusIndicator(String statut) {
        if (lblStatutIndicator == null) {
            return;
        }
        
        if (statut != null) {
            lblStatutIndicator.setText(statut);
            
            switch (statut.toLowerCase()) {
                case "payé":
                    lblStatutIndicator.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 3 10;");
                    break;
                case "en attente":
                    lblStatutIndicator.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 3 10;");
                    break;
                case "partiel":
                    lblStatutIndicator.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 3 10;");
                    break;
                case "annulé":
                    lblStatutIndicator.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 3 10;");
                    break;
                case "remboursé":
                    lblStatutIndicator.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 3 10;");
                    break;
                default:
                    lblStatutIndicator.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 3 10;");
                    break;
            }
        } else {
            lblStatutIndicator.setText("Non défini");
            lblStatutIndicator.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 3 10;");
        }
    }
    
    /**
     * Close the window
     * @param event The action event
     */
    @FXML
    private void handleClose(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
} 