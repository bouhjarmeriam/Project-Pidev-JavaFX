package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import entite.Medecin;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.UserService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class MedecinController {

    @FXML private TextField specialiteField;
    @FXML private TextField telephoneField;
    @FXML private Label specialiteError;
    @FXML private Label telephoneError;
    @FXML private Button btnEnregistrer;

    private Medecin medecin;
    private final UserService userService = new UserService();

    // Expressions régulières pour la validation
    private static final String SPECIALITE_REGEX = "^[A-Za-zÀ-ÿ\\s-]+$";
    private static final Pattern SPECIALITE_PATTERN = Pattern.compile(SPECIALITE_REGEX);
    private static final String TELEPHONE_REGEX = "^\\+?[0-9]{10,13}$";
    private static final Pattern TELEPHONE_PATTERN = Pattern.compile(TELEPHONE_REGEX);

    public void setUtilisateur(Medecin medecin) {
        this.medecin = medecin;
    }

    @FXML
    private void handleEnregistrer() {
        try {
            // Réinitialiser les messages d'erreur
            resetErrorLabels();

            // Récupérer les entrées
            String specialite = specialiteField.getText().trim();
            String telephone = telephoneField.getText().trim();

            // Valider les entrées
            boolean isValid = true;

            // Validation de la spécialité
            if (specialite.isEmpty()) {
                specialiteError.setText("La spécialité est requise.");
                specialiteError.setVisible(true);
                isValid = false;
            } else if (!SPECIALITE_PATTERN.matcher(specialite).matches()) {
                specialiteError.setText("La spécialité ne doit contenir que des lettres et des espaces.");
                specialiteError.setVisible(true);
                isValid = false;
            }

            // Validation du téléphone
            if (telephone.isEmpty()) {
                telephoneError.setText("Le téléphone est requis.");
                telephoneError.setVisible(true);
                isValid = false;
            } else if (!TELEPHONE_PATTERN.matcher(telephone).matches()) {
                telephoneError.setText("Format de téléphone invalide (ex. +212612345678).");
                telephoneError.setVisible(true);
                isValid = false;
            }

            // Si une validation échoue, arrêter le traitement
            if (!isValid) {
                return;
            }

            // Mettre à jour les attributs spécifiques
            medecin.setSpecialite(specialite);
            medecin.setTelephone(telephone);

            // Enregistrer dans la base (création)
            userService.ajouterUtilisateur(medecin, "medecin");

            // Afficher une confirmation
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Médecin créé avec succès !");

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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création du médecin : " + e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection : " + e.getMessage());
        }
    }

    private void resetErrorLabels() {
        specialiteError.setText("");
        specialiteError.setVisible(false);
        telephoneError.setText("");
        telephoneError.setVisible(false);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}