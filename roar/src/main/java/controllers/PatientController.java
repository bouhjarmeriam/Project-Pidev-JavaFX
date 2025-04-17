package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import entite.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.UserService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class PatientController {

    @FXML private TextField adresseField;
    @FXML private TextField telephoneField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private Label adresseError;
    @FXML private Label telephoneError;
    @FXML private Label dateNaissanceError;
    @FXML private Button btnEnregistrer;

    private Patient patient;
    private final UserService userService = new UserService();

    // Expressions régulières pour la validation
    private static final String ADRESSE_REGEX = "^[A-Za-z0-9À-ÿ\\s\\-,'.#]+$";
    private static final Pattern ADRESSE_PATTERN = Pattern.compile(ADRESSE_REGEX);
    private static final String TELEPHONE_REGEX = "^(\\+[0-9]{1,3}\\s?)?[0-9]{8,10}$";
    private static final Pattern TELEPHONE_PATTERN = Pattern.compile(TELEPHONE_REGEX);

    public void setUtilisateur(Patient patient) {
        this.patient = patient;
    }

    @FXML
    private void handleEnregistrer() {
        try {
            // Réinitialiser les messages d'erreur
            resetErrorLabels();

            // Récupérer les entrées
            String adresse = adresseField.getText().trim();
            String telephone = telephoneField.getText().trim();
            LocalDate dateNaissance = dateNaissancePicker.getValue();

            // Valider les entrées
            boolean isValid = true;

            // Validation de l'adresse
            if (adresse.isEmpty()) {
                adresseError.setText("L'adresse est requise.");
                adresseError.setVisible(true);
                isValid = false;
            } else if (!ADRESSE_PATTERN.matcher(adresse).matches()) {
                adresseError.setText("L'adresse contient des caractères invalides.");
                adresseError.setVisible(true);
                isValid = false;
            }

            // Validation du téléphone
            if (telephone.isEmpty()) {
                telephoneError.setText("Le téléphone est requis.");
                telephoneError.setVisible(true);
                isValid = false;
            } else if (!TELEPHONE_PATTERN.matcher(telephone).matches()) {
                telephoneError.setText("Format de téléphone invalide (ex. +212612345678 ou 0612345678).");
                telephoneError.setVisible(true);
                isValid = false;
            }

            // Validation de la date de naissance
            if (dateNaissance == null) {
                dateNaissanceError.setText("La date de naissance est requise.");
                dateNaissanceError.setVisible(true);
                isValid = false;
            } else {
                LocalDate today = LocalDate.now();
                LocalDate minDate = today.minusYears(120); // Limite raisonnable pour l'âge
                if (dateNaissance.isAfter(today)) {
                    dateNaissanceError.setText("La date de naissance ne peut pas être dans le futur.");
                    dateNaissanceError.setVisible(true);
                    isValid = false;
                } else if (dateNaissance.isBefore(minDate)) {
                    dateNaissanceError.setText("La date de naissance est trop ancienne.");
                    dateNaissanceError.setVisible(true);
                    isValid = false;
                }
            }

            // Si une validation échoue, arrêter le traitement
            if (!isValid) {
                return;
            }

            // Mettre à jour les attributs spécifiques
            patient.setAdresse(adresse);
            patient.setTelephone(telephone);
            patient.setDateNaissance(dateNaissance);

            // Enregistrer dans la base (création)
            userService.ajouterUtilisateur(patient, "patient");

            // Afficher une confirmation
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Patient créé avec succès !");

            // Redirection vers la liste des utilisateurs
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeUtilisateurs.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnEnregistrer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste Utilisateurs");
            stage.show();

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", e.getMessage());
        } catch (SQLException | JsonProcessingException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création du patient : " + e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection : " + e.getMessage());
        }
    }

    private void resetErrorLabels() {
        adresseError.setText("");
        adresseError.setVisible(false);
        telephoneError.setText("");
        telephoneError.setVisible(false);
        dateNaissanceError.setText("");
        dateNaissanceError.setVisible(false);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}