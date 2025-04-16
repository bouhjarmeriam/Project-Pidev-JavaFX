package service;

import entite.departement;
import entite.etage;
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

    public List<etage> getEtagesByDepartement(int departementId) {
        List<etage> list = new ArrayList<>();
        String query = "SELECT * FROM etage WHERE departement_id=?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, departementId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    etage e = new etage();
                    e.setId(rs.getInt("id"));
                    e.setNumero(rs.getInt("numero"));

                    departement d = new departement();
                    d.setId(rs.getInt("departement_id"));
                    e.setDepartement(d);

                    list.add(e);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching etages: " + e.getMessage());
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
        } catch (SQLException e) {
            System.err.println("Error deleting etage: " + e.getMessage());
        }
    }
    public static List<etage> getAll() {
        List<etage> list = new ArrayList<>();
        String query = "SELECT * FROM etage";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                etage e = new etage();
                e.setId(rs.getInt("id"));
                e.setNumero(rs.getInt("numero")); // Assure-toi que la colonne 'nom' existe dans ta table 'etage'
                list.add(e);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching étages: " + e.getMessage());
        }

        return list;
    }


}