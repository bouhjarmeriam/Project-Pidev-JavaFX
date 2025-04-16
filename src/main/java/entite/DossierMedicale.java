package entite;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a medical record containing all of a patient's medical information
 */
public class DossierMedicale {
    private int id;
    private User patient;        // corresponds to patient_id in SQL
    private User medecin;        // corresponds to medecin_id in SQL
    private LocalDateTime dateDeCreation;
    private String historiqueDesMaladies;
    private String operationsPassees;
    private String consultationsPassees;
    private String statutDossier;
    private String notes;
    private String image;
    private List<Sejour> sejours;
    
    // Default constructor
    public DossierMedicale() {
        this.sejours = new ArrayList<>();
    }
    
    // Constructor with all fields except ID and sejours
    public DossierMedicale(User patient, User medecin, LocalDateTime dateDeCreation, 
            String historiqueDesMaladies, String operationsPassees, String consultationsPassees, 
            String statutDossier, String notes, String image) {
        this.patient = patient;
        this.medecin = medecin;
        this.dateDeCreation = dateDeCreation;
        this.historiqueDesMaladies = historiqueDesMaladies;
        this.operationsPassees = operationsPassees;
        this.consultationsPassees = consultationsPassees;
        this.statutDossier = statutDossier;
        this.notes = notes;
        this.image = image;
        this.sejours = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public User getPatient() {
        return patient;
    }
    
    public void setPatient(User patient) {
        this.patient = patient;
    }
    
    public User getMedecin() {
        return medecin;
    }
    
    public void setMedecin(User medecin) {
        this.medecin = medecin;
    }
    
    public LocalDateTime getDateDeCreation() {
        return dateDeCreation;
    }
    
    public void setDateDeCreation(LocalDateTime dateDeCreation) {
        this.dateDeCreation = dateDeCreation;
    }
    
    public String getHistoriqueDesMaladies() {
        return historiqueDesMaladies;
    }
    
    public void setHistoriqueDesMaladies(String historiqueDesMaladies) {
        this.historiqueDesMaladies = historiqueDesMaladies;
    }
    
    public String getOperationsPassees() {
        return operationsPassees;
    }
    
    public void setOperationsPassees(String operationsPassees) {
        this.operationsPassees = operationsPassees;
    }
    
    public String getConsultationsPassees() {
        return consultationsPassees;
    }
    
    public void setConsultationsPassees(String consultationsPassees) {
        this.consultationsPassees = consultationsPassees;
    }
    
    public String getStatutDossier() {
        return statutDossier;
    }
    
    public void setStatutDossier(String statutDossier) {
        this.statutDossier = statutDossier;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public List<Sejour> getSejours() {
        return sejours;
    }
    
    public void setSejours(List<Sejour> sejours) {
        this.sejours = sejours;
    }
    
    public void addSejour(Sejour sejour) {
        if(sejours == null) {
            sejours = new ArrayList<>();
        }
        sejours.add(sejour);
        
        // Ensure bi-directional relationship
        if (sejour.getDossierMedicale() != this) {
            sejour.setDossierMedicale(this);
        }
    }
    
    // Helper method to get patient_id for database operations
    public int getPatientId() {
        return patient != null ? patient.getId() : 0;
    }
    
    // Helper method to get medecin_id for database operations
    public int getMedecinId() {
        return medecin != null ? medecin.getId() : 0;
    }
    
    @Override
    public String toString() {
        return "Dossier #" + id + " - " + 
               (patient != null ? patient.getPrenom() + " " + patient.getNom() : "Patient non attribué");
    }
} 