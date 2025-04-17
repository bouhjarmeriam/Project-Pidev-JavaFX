package service;

import entite.Consultation;
import util.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultationService {

    // Existing method - Add new consultation
    public void addConsultation(Consultation c) {
        String query = "INSERT INTO consultation (service_id, date, patient_identifier, status, phone_number) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, c.getServiceId());
            ps.setDate(2, c.getDate());
            ps.setString(3, c.getPatientIdentifier());
            ps.setString(4, "En cours de traitement"); // Default status
            ps.setString(5, c.getPhoneNumber());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding consultation: " + e.getMessage());
        }
    }

    // Existing method - Get consultations by patient
    public List<Consultation> getConsultationsByPatient(String patientIdentifier) {
        List<Consultation> consultations = new ArrayList<>();
        String query = "SELECT c.*, s.name as service_name FROM consultation c " +
                "LEFT JOIN service s ON c.service_id = s.id " +
                "WHERE c.patient_identifier = ? " +
                "ORDER BY c.date DESC";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, patientIdentifier);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    consultations.add(extractConsultationFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching consultations: " + e.getMessage());
        }
        return consultations;
    }

    // Existing method - Update consultation
    public void updateConsultation(Consultation c) {
        String query = "UPDATE consultation SET service_id=?, date=?, phone_number=? " +
                "WHERE id=? AND patient_identifier=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, c.getServiceId());
            ps.setDate(2, c.getDate());
            ps.setString(3, c.getPhoneNumber());
            ps.setInt(4, c.getId());
            ps.setString(5, c.getPatientIdentifier());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.err.println("No rows affected - update failed");
            }
        } catch (SQLException e) {
            System.err.println("Error updating consultation: " + e.getMessage());
        }
    }

    // Existing method - Delete consultation
    public void deleteConsultation(int id, String patientIdentifier) {
        String query = "DELETE FROM consultation WHERE id=? AND patient_identifier=?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.setString(2, patientIdentifier);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.err.println("No rows affected - delete failed");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting consultation: " + e.getMessage());
        }
    }

    // NEW METHOD - Update only status
    public boolean updateConsultationStatus(int consultationId, String newStatus) {
        String query = "UPDATE consultation SET status = ? WHERE id = ?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, newStatus);
            ps.setInt(2, consultationId);

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating consultation status: " + e.getMessage());
            return false;
        }
    }

    // NEW METHOD - Get all consultations
    public List<Consultation> getAllConsultations() {
        List<Consultation> consultations = new ArrayList<>();
        String query = "SELECT c.*, s.name as service_name FROM consultation c " +
                "LEFT JOIN service s ON c.service_id = s.id " +
                "ORDER BY c.date DESC";

        try (Connection conn = DataSource.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                consultations.add(extractConsultationFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all consultations: " + e.getMessage());
        }
        return consultations;
    }

    // NEW METHOD - Search consultations
    public List<Consultation> searchConsultations(String searchTerm) {
        List<Consultation> consultations = new ArrayList<>();
        String query = "SELECT c.*, s.name as service_name FROM consultation c " +
                "LEFT JOIN service s ON c.service_id = s.id " +
                "WHERE c.patient_identifier LIKE ? OR c.phone_number LIKE ? OR s.name LIKE ? " +
                "ORDER BY c.date DESC";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            String likeTerm = "%" + searchTerm + "%";
            ps.setString(1, likeTerm);
            ps.setString(2, likeTerm);
            ps.setString(3, likeTerm);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    consultations.add(extractConsultationFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching consultations: " + e.getMessage());
        }
        return consultations;
    }

    // Helper method to extract consultation from ResultSet
    private Consultation extractConsultationFromResultSet(ResultSet rs) throws SQLException {
        Consultation c = new Consultation();
        c.setId(rs.getInt("id"));
        c.setServiceId(rs.getInt("service_id"));
        c.setServiceName(rs.getString("service_name"));
        c.setDate(rs.getDate("date"));
        c.setPatientIdentifier(rs.getString("patient_identifier"));
        c.setStatus(rs.getString("status"));
        c.setPhoneNumber(rs.getString("phone_number"));
        return c;
    }



}