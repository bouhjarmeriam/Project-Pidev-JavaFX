package service;

import entite.etage;
import entite.departement;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EtageService {

    public void addEtage(etage e) {
        String query = "INSERT INTO etage (numero, departement_id) VALUES (?, ?)";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, e.getNumero());
            ps.setInt(2, e.getDepartement().getId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setId(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error adding etage: " + ex.getMessage());
        }
    }

    public List<etage> getAllEtages() {
        List<etage> list = new ArrayList<>();
        String query = "SELECT e.*, d.nom as departement_nom, d.adresse " +
                "FROM etage e JOIN departement d ON e.departement_id = d.id";

        try (Connection conn = DataSource.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                departement d = new departement(
                        rs.getInt("departement_id"),
                        rs.getString("departement_nom"),
                        rs.getString("adresse"),
                        null // image non récupérée ici
                );

                list.add(new etage(
                        rs.getInt("id"),
                        rs.getInt("numero"),
                        d
                ));
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching etages: " + ex.getMessage());
        }
        return list;
    }

    public void updateEtage(etage e) {
        String query = "UPDATE etage SET numero=?, departement_id=? WHERE id=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, e.getNumero());
            ps.setInt(2, e.getDepartement().getId());
            ps.setInt(3, e.getId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            System.err.println("Error updating etage: " + ex.getMessage());
        }
    }

    public void deleteEtage(int id) {
        String query = "DELETE FROM etage WHERE id=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error deleting etage: " + ex.getMessage());
        }
    }

    public List<etage> searchEtages(String searchTerm) {
        List<etage> results = new ArrayList<>();
        String query = "SELECT e.*, d.nom as departement_nom, d.adresse " +
                "FROM etage e JOIN departement d ON e.departement_id = d.id " +
                "WHERE e.numero LIKE ? OR d.nom LIKE ? OR d.adresse LIKE ?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            String like = "%" + searchTerm + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    departement d = new departement(
                            rs.getInt("departement_id"),
                            rs.getString("departement_nom"),
                            rs.getString("adresse"),
                            null // image non récupérée ici
                    );

                    results.add(new etage(
                            rs.getInt("id"),
                            rs.getInt("numero"),
                            d
                    ));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error searching etages: " + ex.getMessage());
        }

        return results;
    }

    public List<etage> getEtagesByDepartement(int departementId) {
        List<etage> list = new ArrayList<>();
        String query = "SELECT * FROM etage WHERE departement_id = ?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, departementId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // On suppose qu'on a déjà le département et on ne le recharge pas
                    departement d = new departement();
                    d.setId(departementId);

                    list.add(new etage(
                            rs.getInt("id"),
                            rs.getInt("numero"),
                            d
                    ));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error fetching etages by departement: " + ex.getMessage());
        }
        return list;
    }
}