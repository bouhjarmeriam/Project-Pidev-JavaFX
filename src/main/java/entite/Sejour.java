package entite;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents a patient's stay at the medical facility
 * This class matches the sejour table in the database:
 * - id
 * - dossier_medicale_id (DossierMedicale reference)
 * - date_entree
 * - date_sortie
 * - type_sejour
 * - frais_sejour
 * - moyen_paiement
 * - statut_paiement
 * - prix_extras
 */
public class Sejour {
    private int id;
    private LocalDateTime dateEntree;
    private LocalDateTime dateSortie;
    private String typeSejour;
    private double fraisSejour;
    private String moyenPaiement;
    private String statutPaiement;
    private double prixExtras;
    private DossierMedicale dossierMedicale;  // corresponds to dossier_medicale_id in SQL
    
    // These fields don't exist in the database and should be marked as transient
    // so they aren't involved in database operations
    private transient String chambre;  // Not in database
    private transient String notes;    // Not in database
    
    // Default constructor
    public Sejour() {
        // Initialize default values
        this.fraisSejour = 0.0;
        this.prixExtras = 0.0;
        this.statutPaiement = "En attente";
    }
    
    // Constructor with all fields that exist in the database
    public Sejour(LocalDateTime dateEntree, LocalDateTime dateSortie, String typeSejour, 
            double fraisSejour, String moyenPaiement, String statutPaiement, double prixExtras, 
            DossierMedicale dossierMedicale) {
        this.dateEntree = dateEntree;
        this.dateSortie = dateSortie;
        this.typeSejour = typeSejour;
        this.fraisSejour = fraisSejour;
        this.moyenPaiement = moyenPaiement;
        this.statutPaiement = statutPaiement;
        this.prixExtras = prixExtras;
        this.dossierMedicale = dossierMedicale;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public LocalDateTime getDateEntree() {
        return dateEntree;
    }
    
    public void setDateEntree(LocalDateTime dateEntree) {
        this.dateEntree = dateEntree;
    }
    
    public LocalDateTime getDateSortie() {
        return dateSortie;
    }
    
    public void setDateSortie(LocalDateTime dateSortie) {
        this.dateSortie = dateSortie;
    }
    
    public String getTypeSejour() {
        return typeSejour;
    }
    
    public void setTypeSejour(String typeSejour) {
        this.typeSejour = typeSejour;
    }
    
    public double getFraisSejour() {
        return fraisSejour;
    }
    
    public void setFraisSejour(double fraisSejour) {
        this.fraisSejour = fraisSejour;
    }
    
    public String getMoyenPaiement() {
        return moyenPaiement;
    }
    
    public void setMoyenPaiement(String moyenPaiement) {
        this.moyenPaiement = moyenPaiement;
    }
    
    public String getStatutPaiement() {
        return statutPaiement;
    }
    
    public void setStatutPaiement(String statutPaiement) {
        this.statutPaiement = statutPaiement;
    }
    
    public double getPrixExtras() {
        return prixExtras;
    }
    
    public void setPrixExtras(double prixExtras) {
        this.prixExtras = prixExtras;
    }
    
    public DossierMedicale getDossierMedicale() {
        return dossierMedicale;
    }
    
    public void setDossierMedicale(DossierMedicale dossierMedicale) {
        this.dossierMedicale = dossierMedicale;
        
        // Ensure bi-directional relationship
        if (dossierMedicale != null && !dossierMedicale.getSejours().contains(this)) {
            dossierMedicale.addSejour(this);
        }
    }
    
    // Transient getters and setters (not in database)
    public String getChambre() {
        return chambre;
    }
    
    public void setChambre(String chambre) {
        this.chambre = chambre;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    // Helper method to get dossier_medicale_id for database operations
    public int getDossierMedicaleId() {
        return dossierMedicale != null ? dossierMedicale.getId() : 0;
    }
    
    /**
     * Calculate the total cost of the stay including base cost and extras
     * @return The total cost
     */
    public double calculateTotalCost() {
        return fraisSejour + prixExtras;
    }
    
    /**
     * Calculate the duration of the stay in days
     * @return The number of days of the stay
     */
    public long calculateStayDuration() {
        if (dateEntree == null || dateSortie == null) {
            return 0;
        }
        
        LocalDate dateIn = dateEntree.toLocalDate();
        LocalDate dateOut = dateSortie.toLocalDate();
        return ChronoUnit.DAYS.between(dateIn, dateOut);
    }
    
    @Override
    public String toString() {
        return "Séjour #" + id + " - " + typeSejour + 
               " (" + (dateEntree != null ? dateEntree.toLocalDate() : "N/A") + 
               " à " + (dateSortie != null ? dateSortie.toLocalDate() : "N/A") + ")";
    }
} 