package service;

import entite.departement;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartemntService {

    public void addDepartement(departement d) {
        String query = "INSERT INTO departement (nom, adresse, image) VALUES (?, ?, ?)";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, d.getNom());
            ps.setString(2, d.getAdresse());
            ps.setString(3, d.getImage());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    d.setId(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error adding departement: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to add departement", ex);
        }
    }

    public List<departement> getAllDepartements() {
        List<departement> list = new ArrayList<>();
        String query = "SELECT d.*, COALESCE(COUNT(e.id), 0) as etage_count " +
                "FROM departement d " +
                "LEFT JOIN etage e ON d.id = e.departement_id " +
                "GROUP BY d.id, d.nom, d.adresse, d.image";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                departement d = new departement(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("adresse"),
                        rs.getString("image")
                );
                int count = rs.getInt("etage_count");
                d.setNbr_etage(count);
                System.out.println("Departement " + d.getNom() + " (ID: " + d.getId() + ") has " + count + " etages");
                list.add(d);
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching departements: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to fetch departements", ex);
        }
        return list;
    }

    public void updateDepartement(departement d) {
        String query = "UPDATE departement SET nom = ?, adresse = ?, image = ? WHERE id = ?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, d.getNom());
            ps.setString(2, d.getAdresse());
            ps.setString(3, d.getImage());
            ps.setInt(4, d.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error updating departement: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to update departement", ex);
        }
    }

    public void deleteDepartement(departement d) {
        deleteDepartement(d.getId()); // Delegate to the int version
    }

    public void deleteDepartement(int id) {
        String query = "DELETE FROM departement WHERE id = ?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error deleting departement: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to delete departement", ex);
        }
    }

    public static departement getDepartementById(int departementId) {
        String query = "SELECT d.*, COALESCE(COUNT(e.id), 0) as etage_count " +
                "FROM departement d " +
                "LEFT JOIN etage e ON d.id = e.departement_id " +
                "WHERE d.id = ? " +
                "GROUP BY d.id, d.nom, d.adresse, d.image";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, departementId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    departement d = new departement(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("adresse"),
                            rs.getString("image")
                    );
                    int count = rs.getInt("etage_count");
                    d.setNbr_etage(count);
                    System.out.println("Departement ID " + departementId + " has " + count + " etages");
                    return d;
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching departement by id: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to fetch departement by id", ex);
        }
        return null;
    }

    public List<departement> searchDepartements(String searchTerm) {
        List<departement> results = new ArrayList<>();
        String query = "SELECT d.*, COALESCE(COUNT(e.id), 0) as etage_count " +
                "FROM departement d " +
                "LEFT JOIN etage e ON d.id = e.departement_id " +
                "WHERE d.nom LIKE ? OR d.adresse LIKE ? OR d.image LIKE ? " +
                "GROUP BY d.id, d.nom, d.adresse, d.image";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            String like = "%" + searchTerm + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    departement d = new departement(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("adresse"),
                            rs.getString("image")
                    );
                    int count = rs.getInt("etage_count");
                    d.setNbr_etage(count);
                    System.out.println("Search result: Departement " + d.getNom() + " (ID: " + d.getId() + ") has " + count + " etages");
                    results.add(d);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error searching departements: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to search departements", ex);
        }
        return results;
    }
}
