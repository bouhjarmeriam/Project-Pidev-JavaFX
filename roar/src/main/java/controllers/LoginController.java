package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import entite.Users;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import service.UserService;
import entite.CaptchaGenerator;

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
    @FXML private ImageView captchaImageView;
    @FXML private TextField captchaInputField;

    private String correctCaptcha;

    private final UserService userService = new UserService();

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @FXML
    public void initialize() {
        generateNewCaptcha();
    }

    private void generateNewCaptcha() {
        try {
            correctCaptcha = CaptchaGenerator.generateCaptchaText();
            captchaImageView.setImage(CaptchaGenerator.getCaptchaImage(correctCaptcha));
        } catch (IOException e) {
            errorLabel.setText("Erreur lors de la génération du CAPTCHA : " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshCaptcha() {
        generateNewCaptcha();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String userCaptcha = captchaInputField.getText().trim();

        errorLabel.setText("");
        errorLabel.setVisible(false);

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
        if (userCaptcha.isEmpty()) {
            errorLabel.setText("Veuillez entrer le code CAPTCHA.");
            errorLabel.setVisible(true);
            return;
        }

        if (!userCaptcha.equals(correctCaptcha)) {
            errorLabel.setText("Le CAPTCHA est incorrect.");
            errorLabel.setVisible(true);
            generateNewCaptcha();
            return;
        }

        try {
            Users user = userService.authenticate(email, password);
            if (user == null) {
                errorLabel.setText("Email ou mot de passe incorrect !");
                errorLabel.setVisible(true);
                generateNewCaptcha();
                return;
            }

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
            generateNewCaptcha();
        } catch (IOException e) {
            errorLabel.setText("Erreur lors de la redirection : " + e.getMessage());
            errorLabel.setVisible(true);
            generateNewCaptcha();
        }
    }

    private String getDashboardFxmlByRole(Users user) {
        List<String> roles = user.getRoles();
        if (roles.contains("ROLE_USER")) {
            return "/DashbordAdmin.fxml";
        } else if (roles.contains("ROLE_PATIENT")) {
            return "/DashboardPatient.fxml";
        } else if (roles.contains("ROLE_PHARMACIEN")) {
            return "/DashboardPharmacien.fxml";
        } else if (roles.contains("ROLE_MEDECIN")) {
            return "/DashboardMedecin.fxml";
        } else if (roles.contains("ROLE_STAFF")) {
            return "/DashboardStaff.fxml";
        }
        return "/Home.fxml";
    }

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
    @FXML
    void ForgetPsswdButtonOnAction(ActionEvent event) {
        ouvrirInterface("ForgetPassword.fxml", "🔑 Réinitialisation du mot de passe", event);
    }
    private void ouvrirInterface(String fxmlFile, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "❌ Erreur", "Impossible d'ouvrir l'interface : " + fxmlFile);
            e.printStackTrace();
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}