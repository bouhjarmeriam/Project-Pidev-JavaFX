package test;

import entite.Equipement;
import service.EquipementService;

public class Main {
    public static void main(String[] args) {
        EquipementService equipementService = new EquipementService();

        // ✅ Ajouter un équipement
        Equipement eq = new Equipement("radiographie", "Appareil médical", "Fonctionnel", "Cardiologie");
        equipementService.ajouterEquipement(eq);

        // 📋 Afficher tous les équipements avant modification
        System.out.println("📋 Liste des équipements avant modification :");
        for (Equipement e : equipementService.getAllEquipements()) {
            System.out.println(e);
        }

        // 🔄 Mise à jour d’un équipement
        int equipementId = 4;  // Modifier selon les IDs existants dans ta BD
        Equipement equipementToUpdate = null;

        for (Equipement e : equipementService.getAllEquipements()) {
            if (e.getId() == equipementId) {
                equipementToUpdate = e;
                break;
            }
        }

        if (equipementToUpdate != null) {
            equipementToUpdate.setNom("photoscan_updated");
            equipementToUpdate.setType("Appareil médical avancé");
            equipementToUpdate.setStatut("En maintenance");
            equipementToUpdate.setCategory("Cardiologie Pro");

            equipementService.updateEquipement(equipementToUpdate);

            System.out.println("\n📋 Liste des équipements après modification :");
            for (Equipement e : equipementService.getAllEquipements()) {
                System.out.println(e);
            }
        } else {
            System.out.println("❌ Aucun équipement trouvé avec l'ID " + equipementId + ".");
        }

        // 🗑️ Suppression simple
        int idToDelete = 25;
        equipementService.supprimerEquipement(idToDelete);

        System.out.println("\n📋 Liste des équipements après suppression simple :");
        for (Equipement e : equipementService.getAllEquipements()) {
            System.out.println(e);
        }

        // 🧹 Suppression avec dépendances (entretien + équipement)
        int idToDeleteWithDeps = 25;
        equipementService.deleteEquipementAndDependents(idToDeleteWithDeps);

        System.out.println("\n📋 Liste des équipements après suppression avec dépendances :");
        for (Equipement e : equipementService.getAllEquipements()) {
            System.out.println(e);
        }
    }
}
