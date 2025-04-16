package service;

import entite.User;
import util.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Service class for handling database operations for the User entity
 */
public class UserService {
    private Connection connection;
    
    public UserService() {
        connection = DataSource.getInstance().getConnection();
    }
    
    /**
     * Add a new user to the database
     * @param user the user to add
     * @return true if successful, false otherwise
     */
    public boolean ajouterUser(User user) {
        String sql = "INSERT INTO users (nom, prenom, email, telephone, type, adresse, date_naissance, roles, password) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getTelephone());
            pstmt.setString(5, user.getType());
            pstmt.setString(6, user.getAdresse());
            
            // Handle date conversion
            if (user.getDateNaissance() != null) {
                pstmt.setDate(7, java.sql.Date.valueOf(user.getDateNaissance()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }
            
            // Convert roles list to JSON
            String rolesJson = convertRolesToJson(user.getRoles());
            pstmt.setString(8, rolesJson);
            
            // Add password
            pstmt.setString(9, user.getPassword());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout d'un utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Update an existing user in the database
     * @param user the user to update
     * @return true if successful, false otherwise
     */
    public boolean modifierUser(User user) {
        String sql = "UPDATE users SET nom = ?, prenom = ?, email = ?, telephone = ?, " +
                "type = ?, adresse = ?, date_naissance = ?, roles = ?, password = ?, specialite = ? WHERE id = ?";
                
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getTelephone());
            pstmt.setString(5, user.getType());
            pstmt.setString(6, user.getAdresse());
            
            // Handle date conversion
            if (user.getDateNaissance() != null) {
                pstmt.setDate(7, java.sql.Date.valueOf(user.getDateNaissance()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }
            
            // Convert roles list to JSON
            String rolesJson = convertRolesToJson(user.getRoles());
            pstmt.setString(8, rolesJson);
            
            // Add password
            pstmt.setString(9, user.getPassword());
            
            // Add specialite
            pstmt.setString(10, user.getSpecialite());
            
            pstmt.setInt(11, user.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification d'un utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Delete a user from the database
     * @param userId the ID of the user to delete
     * @return true if successful, false otherwise
     */
    public boolean supprimerUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression d'un utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get a user by their ID
     * @param userId the ID of the user to retrieve
     * @return the User object if found, null otherwise
     */
    public User recupererUserParId(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération d'un utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all users from the database
     * @return a list of all users
     */
    public List<User> recupererTousUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = extractUserFromResultSet(rs);
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return users;
    }
    
    /**
     * Get all users with a specific type
     * @param type the user type to filter by (e.g., "patient", "medecin")
     * @return a list of users with the specified type
     */
    public List<User> recupererUsersParRole(String type) {
        List<User> users = new ArrayList<>();
        // Query users based on the type column
        String sql = "SELECT * FROM users WHERE type = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, type);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = extractUserFromResultSet(rs);
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs par type: " + e.getMessage());
            e.printStackTrace();
        }
        
        return users;
    }
    
    /**
     * Convert a list of roles to a JSON string for database storage
     * @param roles List of role strings
     * @return JSON formatted string of roles
     */
    private String convertRolesToJson(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < roles.size(); i++) {
            sb.append("\"").append(roles.get(i)).append("\"");
            if (i < roles.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Convert a JSON string from database to a list of roles
     * @param rolesJson JSON string from database
     * @return List of role strings
     */
    private List<String> convertJsonToRoles(String rolesJson) {
        List<String> roles = new ArrayList<>();
        
        if (rolesJson != null && !rolesJson.isEmpty()) {
            try {
                // Simple parsing for JSON array of strings
                String cleanJson = rolesJson.replace("[", "").replace("]", "").replace("\"", "");
                if (!cleanJson.isEmpty()) {
                    String[] roleArray = cleanJson.split(",");
                    for (String role : roleArray) {
                        roles.add(role.trim());
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la conversion JSON vers roles: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return roles;
    }
    
    /**
     * Helper method to extract a User object from a ResultSet
     * @param rs the ResultSet containing user data
     * @return a User object populated with data from the ResultSet
     * @throws SQLException if there is an error accessing the ResultSet
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setTelephone(rs.getString("telephone"));
        
        // Set the type directly from the database column
        String type = rs.getString("type");
        user.setType(type != null ? type : "");
        
        // Handle specialite column separately, which might not exist in some queries
        try {
            String specialite = rs.getString("specialite");
            user.setSpecialite(specialite);
        } catch (SQLException e) {
            // Column might not exist, ignore
            System.out.println("Note: specialite column not found for user: " + user.getId());
        }
        
        user.setAdresse(rs.getString("adresse"));
        
        // More robust date parsing to avoid regex-related stack overflow
        java.sql.Date sqlDate = rs.getDate("date_naissance");
        if (sqlDate != null) {
            user.setDateNaissance(sqlDate.toLocalDate());
        }
        
        // Parse the roles JSON from the database
        String rolesJson = rs.getString("roles");
        List<String> roles = convertJsonToRoles(rolesJson);
        user.setRoles(roles);
        
        System.out.println("Loaded user from DB: ID=" + user.getId() + 
                           ", Type=" + user.getType() + 
                           ", Name=" + user.getPrenom() + " " + user.getNom() +
                           ", Specialite=" + user.getSpecialite() +
                           ", Roles=" + roles);
        
        return user;
    }
} 