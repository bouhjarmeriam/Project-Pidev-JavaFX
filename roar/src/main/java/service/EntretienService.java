package service;

import entite.Entretien;
import util.DataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EntretienService {
    private final Connection connection;

    public EntretienService() {
        this.connection = DataSource.getInstance().getConnection();
    }

    // Ajouter un entretien
    public void ajouterEntretien(Entretien e) {
        String sql = "INSERT INTO entretien (equipement_id, date, description, nom_equipement, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, e.getEquipementId());
            pst.setDate(2, Date.valueOf(e.getDate()));
            pst.setString(3, e.getDescription());
            pst.setString(4, e.getNomEquipement());
            pst.setTimestamp(5, Timestamp.valueOf(e.getCreatedAt()));
            pst.executeUpdate();
            System.out.println("✅ Entretien ajouté avec succès !");
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de l'ajout de l'entretien : " + ex.getMessage());
        }
    }

    // Récupérer tous les entretiens
    public List<Entretien> getAllEntretiens() {
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT * FROM entretien";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Entretien e = new Entretien(
                        rs.getInt("id"),
                        rs.getInt("equipement_id"),
                        rs.getDate("date").toLocalDate(),
                        rs.getString("description"),
                        rs.getString("nom_equipement"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                entretiens.add(e);
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la récupération des entretiens : " + ex.getMessage());
        }
        return entretiens;
    }

    // Mise à jour d’un entretien
    public void updateEntretien(Entretien entretien) {
        String query = "UPDATE entretien SET equipement_id=?, date=?, description=?, nom_equipement=?, created_at=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, entretien.getEquipementId());
            ps.setDate(2, Date.valueOf(entretien.getDate()));
            ps.setString(3, entretien.getDescription());
            ps.setString(4, entretien.getNomEquipement());
            ps.setTimestamp(5, Timestamp.valueOf(entretien.getCreatedAt()));
            ps.setInt(6, entretien.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.err.println("❌ Aucune ligne affectée - échec de la mise à jour");
            } else {
                System.out.println("✅ L'entretien a été mis à jour avec succès !");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour de l'entretien : " + e.getMessage());
        }
    }

    // Supprimer un entretien
    public void deleteEntretien(int id) {
        String sql = "DELETE FROM entretien WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.err.println("❌ Aucune ligne supprimée - aucun entretien trouvé avec l'ID " + id);
            } else {
                System.out.println("✅ Entretien supprimé avec succès !");
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la suppression de l'entretien : " + ex.getMessage());
        }
    }

    // 🔍 Récupérer un entretien par son ID
    public Entretien getEntretienById(int id) {
        String sql = "SELECT * FROM entretien WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Entretien(
                            rs.getInt("id"),
                            rs.getInt("equipement_id"),
                            rs.getDate("date").toLocalDate(),
                            rs.getString("description"),
                            rs.getString("nom_equipement"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur lors de la récupération de l'entretien par ID : " + ex.getMessage());
        }
        return null;
    }
}

