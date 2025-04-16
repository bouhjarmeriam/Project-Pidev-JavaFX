package entite;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user in the system who can be either a patient or a medical professional
 */
public class User {
    private int id;
    private String email;
    private String password;
    private List<String> roles;
    private String nom;
    private String prenom;
    private String type; // "patient", "medecin", etc.
    private String specialite; // only for medecins
    private String telephone;
    private String adresse;
    private LocalDate dateNaissance;
    
    // Default constructor
    public User() {
        this.roles = new ArrayList<>();
    }
    
    // Parameterized constructor
    public User(String email, String password, List<String> roles, String nom, String prenom, 
                String type, String specialite, String telephone, String adresse, LocalDate dateNaissance) {
        this.email = email;
        this.password = password;
        this.roles = roles != null ? roles : new ArrayList<>();
        this.nom = nom;
        this.prenom = prenom;
        this.type = type;
        this.specialite = specialite;
        this.telephone = telephone;
        this.adresse = adresse;
        this.dateNaissance = dateNaissance;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    public void addRole(String role) {
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        this.roles.add(role);
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getPrenom() {
        return prenom;
    }
    
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSpecialite() {
        return specialite;
    }
    
    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    public String getAdresse() {
        return adresse;
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    
    public LocalDate getDateNaissance() {
        return dateNaissance;
    }
    
    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
    
    public boolean isPatient() {
        return "patient".equalsIgnoreCase(type) || (roles != null && roles.contains("ROLE_PATIENT"));
    }
    
    public boolean isMedecin() {
        return "medecin".equalsIgnoreCase(type) || (roles != null && roles.contains("ROLE_MEDECIN"));
    }
    
    @Override
    public String toString() {
        return prenom + " " + nom;
    }
} 