package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import entite.Users;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckBox;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final UserService userService = new UserService();

    // Expression régulière pour la validation de l'email
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @FXML
    private void handleLogin() {
        // Récupérer les entrées utilisateur
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Réinitialiser le message d'erreur
        errorLabel.setText("");
        errorLabel.setVisible(false);

        // Validation des entrées
        if (email.isEmpty()) {
            errorLabel.setText("Veuillez entrer votre email.");
            errorLabel.setVisible(true);
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errorLabel.setText("Veuillez entrer un email valide.");
            errorLabel.setVisible(true);
            return;
        }
        if (password.isEmpty()) {
            errorLabel.setText("Veuillez entrer votre mot de passe.");
            errorLabel.setVisible(true);
            return;
        }


        // Procéder à l'authentification
        try {
            // Vérifier les informations d'identification
            Users user = userService.authenticate(email, password);
            if (user == null) {
                errorLabel.setText("Email ou mot de passe incorrect !");
                errorLabel.setVisible(true);
                return;
            }

            // Rediriger vers le tableau de bord approprié selon le rôle
            String dashboardFxml = getDashboardFxmlByRole(user);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(dashboardFxml));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(getDashboardTitleByRole(user));
            stage.show();

        } catch (SQLException | JsonProcessingException e) {
            errorLabel.setText("Erreur lors de l'authentification : " + e.getMessage());
            errorLabel.setVisible(true);
        } catch (IOException e) {
            errorLabel.setText("Erreur lors de la redirection : " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    // Déterminer le fichier FXML du tableau de bord selon le rôle de l'utilisateur
    private String getDashboardFxmlByRole(Users user) {
        List<String> roles = user.getRoles();
        if (roles.contains("ROLE_USER")) {
            return "/ListeUtilisateurs.fxml"; // Redirection vers la liste des utilisateurs pour les admins
        } else if (roles.contains("ROLE_PATIENT")) {
            return "/DashboardPatient.fxml";
        } else if (roles.contains("ROLE_PHARMACIEN")) {
            return "/DashboardPharmacien.fxml";
        } else if (roles.contains("ROLE_MEDECIN")) {
            return "/DashboardMedecin.fxml";
        } else if (roles.contains("ROLE_STAFF")) {
            return "/DashboardStaff.fxml";
        }
        return "/Home.fxml"; // Page par défaut si aucun rôle spécifique
    }

    // Déterminer le titre du tableau de bord selon le rôle de l'utilisateur
    private String getDashboardTitleByRole(Users user) {
        List<String> roles = user.getRoles();
        if (roles.contains("ROLE_ADMIN")) {
            return "Tableau de bord - Admin";
        } else if (roles.contains("ROLE_PATIENT")) {
            return "Tableau de bord - Patient";
        } else if (roles.contains("ROLE_PHARMACIEN")) {
            return "Tableau de bord - Pharmacien";
        } else if (roles.contains("ROLE_MEDECIN")) {
            return "Tableau de bord - Médecin";
        } else if (roles.contains("ROLE_STAFF")) {
            return "Tableau de bord - Staff";
        }
        return "Accueil";
    }
}