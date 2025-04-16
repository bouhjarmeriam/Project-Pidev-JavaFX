package service;

import entite.etage;
import entite.salle;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalleService {

    public void addSalle(salle s) {
        String query = "INSERT INTO salle (nom, capacite, type, status, priorite, etage_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getNom());
            ps.setInt(2, s.getCapacite());
            ps.setString(3, s.getType());
            ps.setString(4, s.getStatus());
            ps.setInt(5, s.getPriorite());
            ps.setInt(6, s.getEtage().getId());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    s.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding salle: " + e.getMessage());
        }
    }

    public List<salle> getSallesByEtage(int etageId) {
        List<salle> list = new ArrayList<>();
        String query = "SELECT * FROM salle WHERE etage_id=?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, etageId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    salle s = new salle();
                    s.setId(rs.getInt("id"));
                    s.setNom(rs.getString("nom"));
                    s.setCapacite(rs.getInt("capacite"));
                    s.setType(rs.getString("type"));
                    s.setStatus(rs.getString("status"));
                    s.setPriorite(rs.getInt("priorite"));

                    etage e = new etage();
                    e.setId(rs.getInt("etage_id"));
                    s.setEtage(e);

                    list.add(s);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching salles: " + e.getMessage());
        }
        return list;
    }

    public List<salle> getAll() {
        List<salle> list = new ArrayList<>();
        String query = "SELECT * FROM salle";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                salle s = new salle();
                s.setId(rs.getInt("id"));
                s.setNom(rs.getString("nom"));
                s.setCapacite(rs.getInt("capacite"));
                s.setType(rs.getString("type"));
                s.setStatus(rs.getString("status"));
                s.setPriorite(rs.getInt("priorite"));

                etage e = new etage();
                e.setId(rs.getInt("etage_id"));
                s.setEtage(e);

                list.add(s);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching salles: " + e.getMessage());
        }

        return list;
    }

    public void updateSalle(salle s) {
        String query = "UPDATE salle SET nom=?, capacite=?, type=?, status=?, priorite=?, etage_id=? WHERE id=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, s.getNom());
            ps.setInt(2, s.getCapacite());
            ps.setString(3, s.getType());
            ps.setString(4, s.getStatus());
            ps.setInt(5, s.getPriorite());
            ps.setInt(6, s.getEtage().getId());
            ps.setInt(7, s.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating salle: " + e.getMessage());
        }
    }

    public void deleteSalle(int id) {
        String query = "DELETE FROM salle WHERE id=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting salle: " + e.getMessage());
        }
    }
}
