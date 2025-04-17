package test;

import com.fasterxml.jackson.core.JsonProcessingException;
import entite.Users;
import entite.Users;
import service.UserService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService();

        // Création d’un utilisateur
        Users newUser = new Users();
        newUser.setNom("Guitouni");
        newUser.setPrenom("ala");
        newUser.setEmail("amine@example.com");
        newUser.setPassword("motdepasse123"); // Le mot de passe sera hashé automatiquement

        // Choisir un rôle valide
        String type = "medecin";
        List<String> rolesValides = List.of("medecin", "patient", "pharmacien", "staff");

        if (!rolesValides.contains(type.toLowerCase())) {
            System.err.println("❌ Type de rôle invalide !");
            return;
        }

        try {
            userService.ajouterUtilisateur(newUser, type);

            // Affichage
            System.out.println("✔ Utilisateur ajouté avec succès !");
            System.out.println("🆔 ID : " + newUser.getId());
            System.out.println("📧 Email : " + newUser.getEmail());
            System.out.println("🔐 Mot de passe hashé : " + newUser.getPassword());
            System.out.println("👤 Rôles : " + newUser.getRoles());
            System.out.println("👤 Type : " + newUser.getType());
            newUser.setNom("Guitounii Updated");
            newUser.setPrenom("Alaa Updated");
            newUser.setEmail("elaa_updated@example.com");
            userService.updateUtilisateur(newUser);
            int userIdToDelete = newUser.getId(); // L'ID de l'utilisateur ajouté
            userService.supprimer(174); // Appel à la méthode de suppression
            System.out.println("✅ Suppression de l'utilisateur effectuée avec succès !");
            System.out.println("\n📋 Affichage de tous les utilisateurs restants :");
            List<Users> utilisateurs = userService.listerUtilisateurs();
            if (utilisateurs.isEmpty()) {
                System.out.println("⚠ Aucun utilisateur trouvé.");
            } else {
                for (Users u : utilisateurs) {
                    System.out.println("────────────────────────────");
                    System.out.println("🆔 ID : " + u.getId());
                    System.out.println("👤 Nom : " + u.getNom());
                    System.out.println("👥 Prénom : " + u.getPrenom());
                    System.out.println("📧 Email : " + u.getEmail());
                    System.out.println("🔐 Rôles : " + u.getRoles());
                    System.out.println("🏷 Type : " + u.getType());
                }
                System.out.println("────────────────────────────");
                System.out.println("📦 Total : " + utilisateurs.size() + " utilisateur(s).");
            }


        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("⚠ Erreur de validation : " + e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}