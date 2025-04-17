package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import entite.Staff;
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

public class StaffController {

    @FXML private TextField telephoneField;
    @FXML private Label telephoneError;
    @FXML private Button btnEnregistrer;

    private Staff staff;
    private final UserService userService = new UserService();

    // Expression régulière pour valider le numéro de téléphone
    private static final String TELEPHONE_REGEX = "^(\\+[0-9]{1,3}\\s?)?[0-9]{8,10}$";
    private static final Pattern TELEPHONE_PATTERN = Pattern.compile(TELEPHONE_REGEX);

    public void setUtilisateur(Staff staff) {
        this.staff = staff;
    }

    @FXML
    private void handleEnregistrer() {
        try {
            // Réinitialiser le message d'erreur
            resetErrorLabel();

            // Récupérer l'entrée
            String telephone = telephoneField.getText().trim();

            // Valider le téléphone
            if (telephone.isEmpty()) {
                telephoneError.setText("Le téléphone est requis.");
                telephoneError.setVisible(true);
                return;
            } else if (!TELEPHONE_PATTERN.matcher(telephone).matches()) {
                telephoneError.setText("Format de téléphone invalide (ex. +212612345678 ou 0612345678).");
                telephoneError.setVisible(true);
                return;
            }

            // Mettre à jour l'attribut spécifique
            staff.setTelephone(telephone);

            // Enregistrer dans la base (création)
            userService.ajouterUtilisateur(staff, "staff");

            // Afficher une confirmation
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Staff créé avec succès !");

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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création du staff : " + e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection : " + e.getMessage());
        }
    }

    private void resetErrorLabel() {
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