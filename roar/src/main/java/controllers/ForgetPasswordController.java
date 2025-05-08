package controllers;

import javafx.event.ActionEvent;
import service.EmailSender;
import util.DataSource ;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import javafx.application.Platform;

public class ForgetPasswordController {

    @FXML
    private TextField emailField, codeField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private Label statusLabel;

    private String generatedCode;
    private String userEmail;
    public void onLogoutButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("ForgetPassword");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
    @FXML
    private void sendCode() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            setStatus("Veuillez entrer votre email.", Color.RED);
            return;
        }

        if (!isEmailRegistered(email)) {
            setStatus("Email non trouvé !", Color.RED);
            return;
        }

        generatedCode = generateVerificationCode();
        userEmail = email;

        if (EmailSender.sendEmail(email, "Code de vérification", "Votre code : " + generatedCode)) {
            setStatus("Code envoyé. Vérifiez votre email !", Color.GREEN);
            System.out.println("📩 Code envoyé : " + generatedCode);
        } else {
            setStatus("Échec de l'envoi du code.", Color.RED);
        }
    }
    @FXML
    private void verifyCodeAndChangePassword() {
        String enteredCode = codeField.getText().trim();
        String newPassword = newPasswordField.getText().trim();

        if (enteredCode.isEmpty() || newPassword.isEmpty()) {
            setStatus("Remplissez tous les champs !", Color.RED);
            return;
        }

        if (!enteredCode.equals(generatedCode)) {
            setStatus("Code incorrect !", Color.RED);
            return;
        }

        if (updatePassword(userEmail, newPassword)) {
            setStatus("Mot de passe mis à jour !", Color.GREEN);
            System.out.println("✅ Mot de passe mis à jour pour : " + userEmail);

            // ✅ Retourner à la page de connexion après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Pause de 2 secondes
                    returnToLogin();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            setStatus("Erreur lors de la mise à jour.", Color.RED);
        }
    }

    // ✅ Retourner à la page de connexion
    @FXML
    private void returnToLogin() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
                if (loader.getLocation() == null) {
                    System.out.println("❌ Erreur : Fichier Login.fxml introuvable !");
                    setStatus("Erreur : Page de connexion introuvable.", Color.RED);
                    return;
                }
                Parent root = loader.load();
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
                System.out.println("🔄 Redirection vers la page de connexion...");
            } catch (IOException e) {
                e.printStackTrace();
                setStatus("Erreur lors du chargement de la page de connexion.", Color.RED);
                System.out.println("❌ Erreur lors du chargement de la page de connexion : " + e.getMessage());
            }
        });
    }


    private boolean isEmailRegistered(String email) {
        String query = "SELECT email FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection con = DataSource.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            if (con == null || con.isClosed()) {
                System.out.println("❌ Connexion MySQL fermée !");
                return false;
            }

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("✅ Email trouvé en base : " + rs.getString("email"));
                return true;
            } else {
                System.out.println("❌ Aucune correspondance trouvée.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }



    private boolean updatePassword(String email, String newPassword) {
        // Récupérer une connexion active
        Connection conn = DataSource.getInstance().getConnection();

        if (conn == null) {
            System.out.println("❌ Erreur : Impossible d'obtenir la connexion MySQL !");
            return false;
        }

        try {
            // Vérifier si la connexion est fermée
            if (conn.isClosed()) {
                System.out.println("❌ Connexion MySQL fermée !");
                return false;
            }

            // 🔐 Hachage du mot de passe pour la sécurité
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            // Requête SQL pour mettre à jour le mot de passe
            String query = "UPDATE users SET password = ? WHERE email = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, hashedPassword); // Utilisation du mot de passe haché
                ps.setString(2, email);

                int updatedRows = ps.executeUpdate();
                if (updatedRows > 0) {
                    System.out.println("✅ Mot de passe mis à jour avec succès pour : " + email);
                    return true;
                } else {
                    System.out.println("❌ Aucun utilisateur mis à jour !");
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private String generateVerificationCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(color);
    }
}
