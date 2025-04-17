package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import entite.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.UserService;

import java.io.IOException;
import java.sql.SQLException;

public class ModifierUtilisateurController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;
    @FXML private Label typeLabel;
    @FXML private TextField specialiteField;
    @FXML private TextField adresseField;
    @FXML private TextField telephoneField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private Button btnEnregistrer;

    private Users user;
    private final UserService userService = new UserService();

    public void setUtilisateur(Users user) {
        this.user = user;

        // Pré-remplir les champs communs
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        passwordField.setText(user.getPassword());
        typeLabel.setText("Type: " + user.getType());

        // Afficher les champs spécifiques selon le type
        switch (user.getType().toLowerCase()) {
            case "patient":
                adresseField.setVisible(true);
                telephoneField.setVisible(true);
                dateNaissancePicker.setVisible(true);
                Patient patient = (Patient) user;
                adresseField.setText(patient.getAdresse());
                telephoneField.setText(patient.getTelephone());
                dateNaissancePicker.setValue(patient.getDateNaissance());
                break;
            case "medecin":
                specialiteField.setVisible(true);
                telephoneField.setVisible(true);
                Medecin medecin = (Medecin) user;
                specialiteField.setText(medecin.getSpecialite());
                telephoneField.setText(medecin.getTelephone());
                break;
            case "pharmacien":
            case "staff":
                telephoneField.setVisible(true);
                if (user instanceof Pharmacien pharmacien) {
                    telephoneField.setText(pharmacien.getTelephone());
                } else if (user instanceof Staff staff) {
                    telephoneField.setText(staff.getTelephone());
                }
                break;
            case "admin":
                // Aucun champ spécifique pour admin
                break;
        }
    }

    @FXML
    private void handleEnregistrer() {
        try {
            // Mettre à jour les champs communs
            user.setNom(nomField.getText());
            user.setPrenom(prenomField.getText());
            user.setEmail(emailField.getText());
            user.setPassword(passwordField.getText());

            // Validation des champs communs
            if (user.getEmail() == null || user.getEmail().isEmpty() ||
                    user.getPassword() == null || user.getPassword().isEmpty() ||
                    user.getNom() == null || user.getNom().isEmpty() ||
                    user.getPrenom() == null || user.getPrenom().isEmpty()) {
                throw new IllegalArgumentException("Tous les champs communs doivent être remplis !");
            }

            // Mettre à jour les champs spécifiques
            switch (user.getType().toLowerCase()) {
                case "patient":
                    Patient patient = (Patient) user;
                    patient.setAdresse(adresseField.getText());
                    patient.setTelephone(telephoneField.getText());
                    patient.setDateNaissance(dateNaissancePicker.getValue());

                    // Validation des champs spécifiques
                    if (patient.getAdresse() == null || patient.getAdresse().isEmpty() ||
                            patient.getTelephone() == null || patient.getTelephone().isEmpty() ||
                            patient.getDateNaissance() == null) {
                        throw new IllegalArgumentException("L'adresse, le téléphone et la date de naissance doivent être remplis !");
                    }
                    break;
                case "medecin":
                    Medecin medecin = (Medecin) user;
                    medecin.setSpecialite(specialiteField.getText());
                    medecin.setTelephone(telephoneField.getText());

                    // Validation des champs spécifiques
                    if (medecin.getSpecialite() == null || medecin.getSpecialite().isEmpty() ||
                            medecin.getTelephone() == null || medecin.getTelephone().isEmpty()) {
                        throw new IllegalArgumentException("La spécialité et le téléphone doivent être remplis !");
                    }
                    break;
                case "pharmacien":
                case "staff":
                    if (user instanceof Pharmacien pharmacien) {
                        pharmacien.setTelephone(telephoneField.getText());
                        if (pharmacien.getTelephone() == null || pharmacien.getTelephone().isEmpty()) {
                            throw new IllegalArgumentException("Le téléphone doit être rempli !");
                        }
                    } else if (user instanceof Staff staff) {
                        staff.setTelephone(telephoneField.getText());
                        if (staff.getTelephone() == null || staff.getTelephone().isEmpty()) {
                            throw new IllegalArgumentException("Le téléphone doit être rempli !");
                        }
                    }
                    break;
                case "admin":
                    // Aucun champ spécifique pour admin
                    break;
            }

            // Mettre à jour dans la base
            userService.updateUtilisateur(user);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText(user.getType() + " mis à jour avec succès !");
            alert.showAndWait();

            // Redirection vers la liste des utilisateurs
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeUtilisateurs.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnEnregistrer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste Utilisateurs");
            stage.show();

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", e.getMessage());
        } catch (SQLException e) {
            if (e.getMessage().contains("Aucun utilisateur trouvé")) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non trouvé dans la base de données !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
            }
        } catch (JsonProcessingException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la sérialisation des rôles : " + e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la redirection : " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}