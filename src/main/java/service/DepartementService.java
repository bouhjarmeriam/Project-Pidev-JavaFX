package service;

import entite.departement;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartementService {

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
        } catch (SQLException e) {
            System.err.println("Error adding departement: " + e.getMessage());
        }
    }

    public List<departement> getAllDepartements() {
        List<departement> list = new ArrayList<>();
        String query = "SELECT * FROM departement";

        try (Connection conn = DataSource.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                list.add(new departement(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("adresse"),
                        rs.getString("image")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching departements: " + e.getMessage());
        }
        return list;
    }

    public void updateDepartement(departement d) {
        String query = "UPDATE departement SET nom=?, adresse=?, image=? WHERE id=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, d.getNom());
            ps.setString(2, d.getAdresse());
            ps.setString(3, d.getImage());
            ps.setInt(4, d.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating departement: " + e.getMessage());
        }
    }

    public void deleteDepartement(int id) {
        String query = "DELETE FROM departement WHERE id=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting departement: " + e.getMessage());
        }
    }

    public List<departement> searchDepartements(String searchTerm) {
        List<departement> results = new ArrayList<>();
        String query = "SELECT * FROM departement WHERE nom LIKE ? OR adresse LIKE ?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            String like = "%" + searchTerm + "%";
            ps.setString(1, like);
            ps.setString(2, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new departement(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("adresse"),
                            rs.getString("image")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching departements: " + e.getMessage());
        }

        return results;
    }
}