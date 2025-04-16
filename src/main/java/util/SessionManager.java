package util;

import entite.User;
import java.time.LocalDate;

/**
 * Utility class to manage user sessions
 */
public class SessionManager {
    
    private static User currentUser;
    
    static {
        // Initialize with a default user since we're removing login
        initializeDefaultUser();
    }
    
    /**
     * Initialize a default user for testing without login
     */
    private static void initializeDefaultUser() {
        // Create a default patient user
        User defaultUser = new User();
        defaultUser.setId(1);
        defaultUser.setNom("Dupont");
        defaultUser.setPrenom("Jean");
        defaultUser.setEmail("jean.dupont@example.com");
        defaultUser.setTelephone("01 23 45 67 89");
        defaultUser.setType("patient");
        defaultUser.setAdresse("123 Avenue de la République, 75011 Paris");
        defaultUser.setDateNaissance(LocalDate.of(1980, 1, 15));
        
        // Note: No need to create a dossier medicale here as we now create it on demand
        // in the PatientDashboardController.createDemoData() method
        
        currentUser = defaultUser;
    }
    
    /**
     * Set the current logged-in user
     * @param user The user to set as current
     */
    public static void login(User user) {
        currentUser = user;
    }
    
    /**
     * Explicitly set the current user
     * @param user The user to set as current
     */
    public static void setCurrentUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Current user cannot be set to null");
        }
        currentUser = user;
    }
    
    /**
     * Clear the current user session and set back to default user
     */
    public static void logout() {
        // Instead of nulling the user, restore the default one
        initializeDefaultUser();
    }
    
    /**
     * Get the current logged-in user
     * @return The current user, never null
     */
    public static User getCurrentUser() {
        // Always provide at least the default user
        if (currentUser == null) {
            initializeDefaultUser();
        }
        return currentUser;
    }
    
    /**
     * Check if a user is currently logged in
     * @return true if a user is logged in, false otherwise
     */
    public static boolean isLoggedIn() {
        return true; // Always return true since we always have a default user
    }
    
    /**
     * Check if the current user is a patient
     * @return true if the current user is a patient, false otherwise
     */
    public static boolean isPatient() {
        return "patient".equalsIgnoreCase(getCurrentUser().getType());
    }
    
    /**
     * Check if the current user is a doctor
     * @return true if the current user is a doctor, false otherwise
     */
    public static boolean isDoctor() {
        return "medecin".equalsIgnoreCase(getCurrentUser().getType());
    }
    
    /**
     * Check if the current user is an admin
     * @return true if the current user is an admin, false otherwise
     */
    public static boolean isAdmin() {
        return "admin".equalsIgnoreCase(getCurrentUser().getType());
    }
} 