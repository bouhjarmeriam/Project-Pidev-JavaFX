package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import entite.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import service.EmailSender;
import service.UserService;


import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AjouterUtilisateurController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private Button btnCreer;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label typeError;

    private final UserService userService = new UserService();

    // Expressions régulières pour la validation
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final String NAME_REGEX = "^[A-Za-zÀ-ÿ\\s-]+$";
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le ComboBox avec les types d'utilisateurs
        typeComboBox.setItems(FXCollections.observableArrayList(
                "medecin",
                "patient",
                "pharmacien",
                "staff",
                "admin"
        ));
        typeComboBox.setPromptText("Sélectionnez un type");

        // Réinitialiser les messages d'erreur
        resetErrorLabels();
    }

    @FXML
    private void handleAjouterUtilisateur() {
        try {
            // Réinitialiser les messages d'erreur
            resetErrorLabels();

            // Récupérer les entrées
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String nom = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String type = typeComboBox.getValue();

            // Valider les entrées
            boolean isValid = true;

            // Validation du type
            if (type == null) {
                typeError.setText("Veuillez sélectionner un type d'utilisateur.");
                typeError.setVisible(true);
                isValid = false;
            }

            // Validation de l'email
            if (email.isEmpty()) {
                emailError.setText("L'email est requis.");
                emailError.setVisible(true);
                isValid = false;
            } else if (!EMAIL_PATTERN.matcher(email).matches()) {
                emailError.setText("Veuillez entrer un email valide.");
                emailError.setVisible(true);
                isValid = false;
            }

            // Validation du mot de passe
            if (password.isEmpty()) {
                passwordError.setText("Le mot de passe est requis.");
                passwordError.setVisible(true);
                isValid = false;
            } else if (password.length() < 8) {
                passwordError.setText("Le mot de passe doit contenir au moins 8 caractères.");
                passwordError.setVisible(true);
                isValid = false;
            }

            // Validation du nom
            if (nom.isEmpty()) {
                nomError.setText("Le nom est requis.");
                nomError.setVisible(true);
                isValid = false;
            } else if (!NAME_PATTERN.matcher(nom).matches()) {
                nomError.setText("Le nom ne doit contenir que des lettres et des espaces.");
                nomError.setVisible(true);
                isValid = false;
            }

            // Validation du prénom
            if (prenom.isEmpty()) {
                prenomError.setText("Le prénom est requis.");
                prenomError.setVisible(true);
                isValid = false;
            } else if (!NAME_PATTERN.matcher(prenom).matches()) {
                prenomError.setText("Le prénom ne doit contenir que des lettres et des espaces.");
                prenomError.setVisible(true);
                isValid = false;
            }

            // Si une validation échoue, arrêter le traitement
            if (!isValid) {
                return;
            }

            // Instancier la sous-classe appropriée
            Users user;
            switch (type.toLowerCase()) {
                case "patient":
                    user = new Patient();
                    break;
                case "medecin":
                    user = new Medecin();
                    break;
                case "pharmacien":
                    user = new Pharmacien();
                    break;
                case "staff":
                    user = new Staff();
                    break;
                case "admin":
                    user = new Users();
                    break;
                default:
                    user = new Users();
                    break;
            }

            // Remplir les champs communs
            user.setEmail(email);
            user.setPassword(password);
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setType(type);
            EmailSender.envoyerEmailInscription(email, nom, password, type);

            // Si le type est "admin", enregistrer directement et rediriger
            if (type.toLowerCase().equals("admin")) {
                userService.ajouterUtilisateur(user, type);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Admin créé avec succès !");// Redirection vers la liste des utilisateurs

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ListeUtilisateurs.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) btnCreer.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Liste Utilisateurs");
                stage.show();
            } else {
                // Redirection vers l'interface appropriée pour les autres types
                String fxmlFile;
                String title;
                switch (type.toLowerCase()) {
                    case "medecin":
                        fxmlFile = "/ajouterMedecin.fxml";
                        title = "Ajout Médecin";
                        break;
                    case "patient":
                        fxmlFile = "/ajouterPatient.fxml";
                        title = "Ajout Patient";
                        break;
                    case "pharmacien":
                        fxmlFile = "/ajouterPharmacien.fxml";
                        title = "Ajout Pharmacien";
                        break;
                    case "staff":
                        fxmlFile = "/ajouterStaff.fxml";
                        title = "Ajout Staff";
                        break;
                    default:
                        fxmlFile = "/ListeUtilisateurs.fxml";
                        title = "Liste Utilisateurs";
                        break;
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent root = loader.load();


                // Passer l'utilisateur au contrôleur
                Object controller = loader.getController();
                if (controller instanceof MedecinController) {
                    ((MedecinController) controller).setUtilisateur((Medecin) user);
                } else if (controller instanceof PatientController) {
                    ((PatientController) controller).setUtilisateur((Patient) user);
                } else if (controller instanceof PharmacienController) {
                    ((PharmacienController) controller).setUtilisateur((Pharmacien) user);
                } else if (controller instanceof StaffController) {
                    ((StaffController) controller).setUtilisateur((Staff) user);
                }

                Stage stage = (Stage) btnCreer.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle(title);
                stage.show();
            }

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur FXML", "Erreur lors du chargement de l'interface : " + e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création de l'utilisateur : " + e.getMessage());
        }
    }

    private void resetErrorLabels() {
        emailError.setText("");
        emailError.setVisible(false);
        passwordError.setText("");
        passwordError.setVisible(false);
        nomError.setText("");
        nomError.setVisible(false);
        prenomError.setText("");
        prenomError.setVisible(false);
        typeError.setText("");
        typeError.setVisible(false);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}