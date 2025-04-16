package controllers;

import entite.DossierMedicale;
import entite.Sejour;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import service.SejourService;
import util.AlertUtil;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;

public class SejourDetailController {

    @FXML private Label lblId;
    @FXML private Label lblDateEntree;
    @FXML private Label lblDateSortie;
    @FXML private Label lblTypeSejour;
    @FXML private Label lblFraisSejour;
    @FXML private Label lblMoyenPaiement;
    @FXML private Label lblStatutPaiement;
    @FXML private Label lblPrixExtras;
    @FXML private Label lblPrixTotal;
    @FXML private Label lblDossierMedicale;
    @FXML private Label lblPatient;
    @FXML private Text txtInfosComplementaires;
    @FXML private Button btnFermer;

    private Sejour sejour;
    private final SejourService sejourService = new SejourService();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();

    @FXML
    public void initialize() {
        btnFermer.setOnAction(event -> handleCloseButtonAction());
    }

    public void setSejour(Sejour sejour) {
        this.sejour = sejour;
        displaySejourDetails();
    }

    private void displaySejourDetails() {
        if (sejour == null) {
            return;
        }

        lblId.setText(String.valueOf(sejour.getId()));
        
        if (sejour.getDateEntree() != null) {
            lblDateEntree.setText(sejour.getDateEntree().format(DATE_FORMATTER));
        } else {
            lblDateEntree.setText("Non définie");
        }
        
        if (sejour.getDateSortie() != null) {
            lblDateSortie.setText(sejour.getDateSortie().format(DATE_FORMATTER));
        } else {
            lblDateSortie.setText("Non définie");
        }
        
        lblTypeSejour.setText(sejour.getTypeSejour() != null ? sejour.getTypeSejour() : "Non défini");
        lblFraisSejour.setText(CURRENCY_FORMATTER.format(sejour.getFraisSejour()));
        lblMoyenPaiement.setText(sejour.getMoyenPaiement() != null ? sejour.getMoyenPaiement() : "Non défini");
        lblStatutPaiement.setText(sejour.getStatutPaiement() != null ? sejour.getStatutPaiement() : "Non défini");
        lblPrixExtras.setText(CURRENCY_FORMATTER.format(sejour.getPrixExtras()));
        lblPrixTotal.setText(CURRENCY_FORMATTER.format(sejour.calculateTotalCost()));
        
        DossierMedicale dossier = sejour.getDossierMedicale();
        if (dossier != null) {
            lblDossierMedicale.setText("Dossier #" + dossier.getId());
            
            if (dossier.getPatient() != null) {
                lblPatient.setText(dossier.getPatient().getNom() + " " + dossier.getPatient().getPrenom());
            } else {
                lblPatient.setText("Information patient non disponible");
            }
        } else {
            lblDossierMedicale.setText("Non associé");
            lblPatient.setText("Non disponible");
        }
        
        // Affichage des informations complémentaires - aucun champ trouvé dans Sejour
        txtInfosComplementaires.setText("Aucune information complémentaire disponible.");
    }

    @FXML
    private void handleCloseButtonAction() {
        Stage stage = (Stage) btnFermer.getScene().getWindow();
        stage.close();
    }
} 