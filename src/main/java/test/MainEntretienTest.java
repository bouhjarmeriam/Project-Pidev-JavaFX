package test;

import entite.Entretien;
import service.EntretienService;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MainEntretienTest {
    public static void main(String[] args) {
        EntretienService entretienService = new EntretienService();

        // ✅ Ajouter un entretien
        Entretien entretien = new Entretien(26, LocalDate.now(), "Maintenance préventive", "Équipement A", LocalDateTime.now());
        entretienService.ajouterEntretien(entretien);

        // 📋 Afficher tous les entretiens avant modification
        System.out.println("📋 Liste des entretiens avant modification :");
        for (Entretien e : entretienService.getAllEntretiens()) {
            System.out.println(e);
        }

        // 🔄 Mise à jour d’un entretien
        int entretienId = 16;  // Remplacer par un ID existant dans ta BDD
        Entretien entretienToUpdate = null;

        for (Entretien e : entretienService.getAllEntretiens()) {
            if (e.getId() == entretienId) {
                entretienToUpdate = e;
                break;
            }
        }

        if (entretienToUpdate != null) {
            entretienToUpdate.setDescription("✅ Description mise à jour !");
            entretienToUpdate.setNomEquipement("Équipement A modifié");
            entretienToUpdate.setDate(LocalDate.of(2025, 4, 20));
            entretienToUpdate.setCreatedAt(LocalDateTime.now());

            entretienService.updateEntretien(entretienToUpdate);

            System.out.println("\n📋 Liste des entretiens après modification :");
            for (Entretien e : entretienService.getAllEntretiens()) {
                System.out.println(e);
            }
        } else {
            System.out.println("❌ Aucun entretien trouvé avec l'ID " + entretienId + ".");
        }

        // 🗑️ Suppression simple
        int idToDelete = 14; // Remplacer par un ID valide
        entretienService.deleteEntretien(idToDelete);

        System.out.println("\n📋 Liste des entretiens après suppression :");
        for (Entretien e : entretienService.getAllEntretiens()) {
            System.out.println(e);
        }
    }
}
