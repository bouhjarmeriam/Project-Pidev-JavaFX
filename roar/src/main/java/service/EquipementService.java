package service;

import entite.Equipement;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipementService {
    private final Connection connection;

    public EquipementService() {
        connection = DataSource.getInstance().getConnection();
    }

    public void ajouterEquipement(Equipement e) {
        String sql = "INSERT INTO equipement (nom, type, statut, category) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, e.getNom());
            pst.setString(2, e.getType());
            pst.setString(3, e.getStatut());
            pst.setString(4, e.getCategory());
            pst.executeUpdate();
            System.out.println("✅ Équipement ajouté avec succès !");
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de l'ajout de l'équipement : " + ex.getMessage());
        }
    }

    public List<Equipement> getAllEquipements() {
        List<Equipement> result = new ArrayList<>();
        String sql = "SELECT * FROM equipement";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Equipement e = new Equipement(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("type"),
                        rs.getString("statut"),
                        rs.getString("category")
                );
                result.add(e);
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la récupération des équipements : " + ex.getMessage());
        }
        return result;
    }

    public Equipement getEquipementById(int id) {
        String sql = "SELECT * FROM equipement WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Equipement(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("type"),
                            rs.getString("statut"),
                            rs.getString("category")
                    );
                }
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la récupération de l'équipement par ID : " + ex.getMessage());
        }
        return null;
    }

    public void updateEquipement(Equipement equipement) {
        String query = "UPDATE equipement SET nom=?, type=?, statut=?, category=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, equipement.getNom());
            ps.setString(2, equipement.getType());
            ps.setString(3, equipement.getStatut());
            ps.setString(4, equipement.getCategory());
            ps.setInt(5, equipement.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.err.println("❌ Aucune ligne affectée - échec de la mise à jour");
            } else {
                System.out.println("✅ L'équipement a été mis à jour avec succès !");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de l'équipement : " + e.getMessage());
        }
    }

    public void supprimerEquipement(int id) {
        String sql = "DELETE FROM equipement WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.err.println("❌ Aucune ligne supprimée - ID introuvable : " + id);
            } else {
                System.out.println("✅ Équipement supprimé avec succès !");
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la suppression de l'équipement : " + ex.getMessage());
        }
    }

    public void deleteEquipementAndDependents(int equipementId) {
        String deleteEntretienSql = "DELETE FROM entretien WHERE equipement_id = ?";
        String deleteEquipementSql = "DELETE FROM equipement WHERE id = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement psEntretien = connection.prepareStatement(deleteEntretienSql)) {
                psEntretien.setInt(1, equipementId);
                psEntretien.executeUpdate();
            }

            try (PreparedStatement psEquipement = connection.prepareStatement(deleteEquipementSql)) {
                psEquipement.setInt(1, equipementId);
                int rows = psEquipement.executeUpdate();
                if (rows == 0) {
                    System.out.println("❌ Aucun équipement trouvé avec l'ID " + equipementId);
                } else {
                    System.out.println("✅ Équipement et ses entretiens supprimés !");
                }
            }

            connection.commit();
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                System.err.println("❌ Erreur rollback : " + e.getMessage());
            }
            System.err.println("❌ Erreur lors de la suppression : " + ex.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("❌ Erreur reset auto-commit : " + e.getMessage());
            }
        }
    }

    public List<Equipement> getEquipementsByCategory(String category) {
        List<Equipement> equipements = new ArrayList<>();
        String sql = "SELECT * FROM equipement WHERE category = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Equipement e = new Equipement(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("type"),
                            rs.getString("statut"),
                            rs.getString("category")
                    );
                    equipements.add(e);
                }
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la récupération par catégorie : " + ex.getMessage());
        }
        return equipements;
    }
}
