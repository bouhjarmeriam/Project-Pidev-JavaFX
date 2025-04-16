package service;

import entite.etage;
import entite.salle;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalleService {

    public void addSalle(salle s) {
        String query = "INSERT INTO salle (nom, capacite, type_salle, status, etage_id, image,priorite) VALUES (?, ?, ?, ?, ?, ?,?)";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getNom());
            ps.setInt(2, s.getCapacite());
            ps.setString(3, s.getType_salle());
            ps.setString(4, s.getStatus());
            ps.setInt(5, s.getEtage().getId());
            ps.setString(6, s.getImage());
            ps.setInt(7, s.getPriorite());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    s.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding salle: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add salle", e);
        }
    }

    public List<salle> getAll() {
        List<salle> list = new ArrayList<>();
        String query = "SELECT s.*, e.numero as etage_numero FROM salle s LEFT JOIN etage e ON s.etage_id = e.id";

        try (Connection conn = DataSource.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                salle s = new salle();
                s.setId(rs.getInt("id"));
                s.setNom(rs.getString("nom"));
                s.setCapacite(rs.getInt("capacite"));
                s.setType_salle(rs.getString("type_salle"));
                s.setStatus(rs.getString("status"));
                s.setImage(rs.getString("image"));
                s.setPriorite(rs.getInt("priorite"));

                etage e = new etage();
                e.setId(rs.getInt("etage_id"));
                e.setNumero(rs.getInt("etage_numero"));
                s.setEtage(e);

                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching salles: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch salles", e);
        }
        return list;
    }

    public void updateSalle(salle s) {
        String query = "UPDATE salle SET nom=?, capacite=?, type_salle=?, status=?, etage_id=?, image=? ,priorite=? WHERE id=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, s.getNom());
            ps.setInt(2, s.getCapacite());
            ps.setString(3, s.getType_salle());
            ps.setString(4, s.getStatus());
            ps.setInt(5, s.getEtage().getId());
            ps.setString(6, s.getImage());
            ps.setInt(7, s.getId());
            ps.setInt(8, s.getPriorite());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating salle: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update salle", e);
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
            e.printStackTrace();
            throw new RuntimeException("Failed to delete salle", e);
        }
    }
}