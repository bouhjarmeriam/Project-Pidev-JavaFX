package service;

import entite.Service;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class serviceservice {
    private Connection conn = DataSource.getInstance().getConnection();

    // Add a new service to the database
    public void addService(Service s) {
        String query = "INSERT INTO service (name, description, duration) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getDescription());
            ps.setInt(3, s.getDuration());  // Directly set the duration as an integer
            ps.executeUpdate();

            // Retrieve generated ID
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                s.setId(rs.getInt(1));
            }

            System.out.println("Service added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve all services from the database
    public List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        String query = "SELECT * FROM service";  // Ensure the correct table name
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Service s = new Service(
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("duration")  // Retrieve duration as integer
                );
                s.setId(rs.getInt("id"));
                services.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }

    // Update an existing service
    public void updateService(Service s) {
        String query = "UPDATE service SET name=?, description=?, duration=? WHERE id=?";  // Corrected table name
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getDescription());
            ps.setInt(3, s.getDuration());  // Set the duration as an integer
            ps.setInt(4, s.getId());
            ps.executeUpdate();
            System.out.println("Service updated successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete a service by ID
    public void deleteService(int id) {
        String query = "DELETE FROM service WHERE id=?";  // Corrected table name
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Service deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Add to serviceservice.java
    public List<String> getAllServiceNames() {
        List<String> serviceNames = new ArrayList<>();
        String query = "SELECT name FROM service";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                serviceNames.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return serviceNames;
    }

    public int getServiceIdByName(String name) {
        String query = "SELECT id FROM service WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if not found
    }
}
