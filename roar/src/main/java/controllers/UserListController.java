package controllers;

import entite.Users;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import service.UserService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UserListController {

    @FXML
    private VBox VBoxId;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        chargerUtilisateurs();
    }

    @FXML
    private void ajouterUtilisateur() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter_utilisateur.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) VBoxId.getScene().getWindow(); // récupère la fenêtre actuelle
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Ajouter un utilisateur");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Impossible de charger la vue ajouter_utilisateur.fxml : " + e.getMessage());

        }
    }
    @FXML
    private void deconnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) VBoxId.getScene().getWindow(); // récupère la fenêtre actuelle
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Impossible de charger la vue front.fxml : " + e.getMessage());
        }
    }

    private void chargerUtilisateurs() {
        try {
            List<Users> utilisateurs = userService.listerUtilisateurs();
            listUsersInVBox(utilisateurs);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du chargement des utilisateurs : " + e.getMessage());
        }
    }

    private void listUsersInVBox(List<Users> users) {
        VBoxId.getChildren().clear();

        for (Users user : users) {
            HBox userBox = new HBox(20);
            userBox.setPadding(new Insets(10));
            userBox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #f9f9f9;");
            userBox.setAlignment(Pos.CENTER_LEFT);

            Label nomLabel = new Label("👤 " + user.getNom());
            Label prenomLabel = new Label(user.getPrenom());
            Label emailLabel = new Label("✉ " + user.getEmail());

            String rolesText = String.join(", ", user.getRoles());
            Label roleLabel = new Label("🔑 " + rolesText);
            Label typeLabel = new Label("📌 " + user.getType());

            Button btnModifier = new Button("✏ Modifier");
            btnModifier.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white;");
            btnModifier.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUtilisateur.fxml"));
                    Parent root = loader.load();
                    ModifierUtilisateurController controller = loader.getController();
                    controller.setUtilisateur(user);
                    Stage stage = (Stage) btnModifier.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Modifier Utilisateur");
                    stage.show();
                } catch (IOException ex) {
                    System.err.println("❌ Impossible de charger la vue ModifierUtilisateur.fxml : " + ex.getMessage());
                }
            });

            Button btnSupprimer = new Button("🗑 Supprimer");
            btnSupprimer.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
            btnSupprimer.setOnAction(e -> {
                try {
                    userService.supprimer(user.getId()); // appel de la méthode
                    chargerUtilisateurs(); // rechargement après suppression
                } catch (SQLException ex) {
                    System.err.println("❌ Échec de la suppression : " + ex.getMessage());
                }
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            userBox.getChildren().addAll(nomLabel, prenomLabel, emailLabel, roleLabel, typeLabel, spacer, btnModifier, btnSupprimer);
            VBoxId.getChildren().add(userBox);
        }
    }
}