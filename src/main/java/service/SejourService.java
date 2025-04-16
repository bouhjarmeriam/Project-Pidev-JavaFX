package service;

import entite.Sejour;
import entite.DossierMedicale;
import util.DataSource;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling database operations for the Sejour entity
 */
public class SejourService {
    private Connection connection;
    private DossierMedicaleService dossierService;
    
    public SejourService() {
        connection = DataSource.getInstance().getConnection();
    }
    
    public void setDossierService(DossierMedicaleService dossierService) {
        this.dossierService = dossierService;
    }
    
    /**
     * Add a new stay to the database
     * @param sejour the stay to add
     * @return true if successful, false otherwise
     */
    public boolean ajouterSejour(Sejour sejour) {
        // Validate that exit date is after entry date
        if (sejour.getDateSortie() != null && sejour.getDateEntree() != null &&
                sejour.getDateSortie().isBefore(sejour.getDateEntree())) {
            System.err.println("Erreur: La date de sortie doit être après la date d'entrée");
            return false;
        }
        
        // Validate that costs are positive
        if (sejour.getFraisSejour() < 0 || sejour.getPrixExtras() < 0) {
            System.err.println("Erreur: Les frais doivent être positifs");
            return false;
        }
        
        // Validate that dossierMedicale is not null
        if (sejour.getDossierMedicale() == null) {
            System.err.println("Erreur: Un dossier médical doit être associé au séjour");
            return false;
        }
        
        // Match exactly the columns in the database
        String sql = "INSERT INTO sejour (dossier_medicale_id, date_entree, date_sortie, type_sejour, " +
                "frais_sejour, moyen_paiement, statut_paiement, prix_extras) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Ensure dateEntree is not null
            if (sejour.getDateEntree() == null) {
                sejour.setDateEntree(LocalDateTime.now());
            }
            
            // Ensure date_sortie is not null (as required by the database)
            if (sejour.getDateSortie() == null) {
                // Set a default date_sortie to the same as date_entree plus 1 day
                sejour.setDateSortie(sejour.getDateEntree().plusDays(1));
            }
            
            // Set parameters in the same order as in the SQL statement
            pstmt.setInt(1, sejour.getDossierMedicale().getId());
            pstmt.setTimestamp(2, Timestamp.valueOf(sejour.getDateEntree()));
            pstmt.setTimestamp(3, Timestamp.valueOf(sejour.getDateSortie()));
            pstmt.setString(4, sejour.getTypeSejour() != null ? sejour.getTypeSejour() : "");
            pstmt.setDouble(5, sejour.getFraisSejour());
            
            // Ensure moyen_paiement is not null (as required by the database)
            if (sejour.getMoyenPaiement() != null) {
                pstmt.setString(6, sejour.getMoyenPaiement());
            } else {
                pstmt.setString(6, "Non spécifié");
            }
            
            // Ensure statut_paiement is not null (as required by the database)
            if (sejour.getStatutPaiement() != null) {
                pstmt.setString(7, sejour.getStatutPaiement());
            } else {
                pstmt.setString(7, "En attente");
            }
            
            // prix_extras can be null in the database
            if (sejour.getPrixExtras() > 0) {
                pstmt.setDouble(8, sejour.getPrixExtras());
            } else {
                pstmt.setNull(8, Types.DOUBLE);
            }
            
            System.out.println("Executing SQL: " + sql);
            System.out.println("With values: " + sejour.getDossierMedicale().getId() + ", " 
                + sejour.getDateEntree() + ", " + sejour.getDateSortie() + ", " 
                + sejour.getTypeSejour() + ", " + sejour.getFraisSejour() + ", " 
                + sejour.getMoyenPaiement() + ", " + sejour.getStatutPaiement() + ", " 
                + sejour.getPrixExtras());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        sejour.setId(generatedKeys.getInt(1));
                        
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout d'un séjour: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Update an existing stay in the database
     * @param sejour the stay to update
     * @return true if successful, false otherwise
     */
    public boolean modifierSejour(Sejour sejour) {
        // Validate that exit date is after entry date
        if (sejour.getDateSortie() != null && sejour.getDateEntree() != null &&
                sejour.getDateSortie().isBefore(sejour.getDateEntree())) {
            System.err.println("Erreur: La date de sortie doit être après la date d'entrée");
            return false;
        }
        
        // Validate that costs are positive
        if (sejour.getFraisSejour() < 0 || sejour.getPrixExtras() < 0) {
            System.err.println("Erreur: Les frais doivent être positifs");
            return false;
        }
        
        // Validate that dossierMedicale is not null
        if (sejour.getDossierMedicale() == null) {
            System.err.println("Erreur: Un dossier médical doit être associé au séjour");
            return false;
        }
        
        // Ensure date_sortie is not null (as required by the database)
        if (sejour.getDateSortie() == null) {
            // Set a default date_sortie to the same as date_entree plus 1 day
            sejour.setDateSortie(sejour.getDateEntree().plusDays(1));
        }
        
        // Match exactly the columns in the database
        String sql = "UPDATE sejour SET dossier_medicale_id = ?, date_entree = ?, date_sortie = ?, " +
                "type_sejour = ?, frais_sejour = ?, moyen_paiement = ?, statut_paiement = ?, " +
                "prix_extras = ? WHERE id = ?";
                
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sejour.getDossierMedicale().getId());
            pstmt.setTimestamp(2, Timestamp.valueOf(sejour.getDateEntree()));
            pstmt.setTimestamp(3, Timestamp.valueOf(sejour.getDateSortie()));
            pstmt.setString(4, sejour.getTypeSejour() != null ? sejour.getTypeSejour() : "");
            pstmt.setDouble(5, sejour.getFraisSejour());
            
            // Ensure moyen_paiement is not null (as required by the database)
            if (sejour.getMoyenPaiement() != null) {
                pstmt.setString(6, sejour.getMoyenPaiement());
            } else {
                pstmt.setString(6, "Non spécifié");
            }
            
            // Ensure statut_paiement is not null (as required by the database)
            if (sejour.getStatutPaiement() != null) {
                pstmt.setString(7, sejour.getStatutPaiement());
            } else {
                pstmt.setString(7, "En attente");
            }
            
            // prix_extras can be null in the database
            if (sejour.getPrixExtras() > 0) {
                pstmt.setDouble(8, sejour.getPrixExtras());
            } else {
                pstmt.setNull(8, Types.DOUBLE);
            }
            
            pstmt.setInt(9, sejour.getId());
            
            System.out.println("Executing SQL update: " + sql);
            System.out.println("With values: " + sejour.getDossierMedicale().getId() + ", " 
                + sejour.getDateEntree() + ", " + sejour.getDateSortie() + ", " 
                + sejour.getTypeSejour() + ", " + sejour.getFraisSejour() + ", " 
                + sejour.getMoyenPaiement() + ", " + sejour.getStatutPaiement() + ", " 
                + sejour.getPrixExtras() + ", " + sejour.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification d'un séjour: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Delete a stay from the database
     * @param sejourId the ID of the stay to delete
     * @return true if successful, false otherwise
     */
    public boolean supprimerSejour(int sejourId) {
        String sql = "DELETE FROM sejour WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sejourId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression d'un séjour: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get a stay by its ID
     * @param sejourId the ID of the stay to retrieve
     * @return the Sejour object if found, null otherwise
     */
    public Sejour recupererSejourParId(int sejourId) {
        String sql = "SELECT * FROM sejour WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sejourId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractSejourFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération d'un séjour: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all stays from the database
     * @return a list of all stays
     */
    public List<Sejour> recupererTousSejours() {
        List<Sejour> sejours = new ArrayList<>();
        String sql = "SELECT * FROM sejour";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Sejour sejour = extractSejourFromResultSet(rs);
                sejours.add(sejour);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des séjours: " + e.getMessage());
            e.printStackTrace();
        }
        
        return sejours;
    }
    
    /**
     * Get all stays for a specific medical record
     * @param dossierId the ID of the medical record
     * @return a list of stays for the given medical record
     */
    public List<Sejour> recupererSejoursParDossier(int dossierId) {
        List<Sejour> sejours = new ArrayList<>();
        String sql = "SELECT * FROM sejour WHERE dossier_medicale_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, dossierId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Sejour sejour = extractSejourFromResultSet(rs);
                    sejours.add(sejour);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des séjours par dossier: " + e.getMessage());
            e.printStackTrace();
        }
        
        return sejours;
    }
    
    /**
     * Get all stays with a specific payment status
     * @param statutPaiement the payment status to filter by
     * @return a list of stays with the specified payment status
     */
    public List<Sejour> recupererSejoursParStatutPaiement(String statutPaiement) {
        List<Sejour> sejours = new ArrayList<>();
        String sql = "SELECT * FROM sejour WHERE statut_paiement = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, statutPaiement);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Sejour sejour = extractSejourFromResultSet(rs);
                    sejours.add(sejour);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des séjours par statut de paiement: " + e.getMessage());
            e.printStackTrace();
        }
        
        return sejours;
    }
    
    /**
     * Helper method to extract a Sejour object from a ResultSet
     * @param rs the ResultSet containing stay data
     * @return a Sejour object populated with data from the ResultSet
     * @throws SQLException if there is an error accessing the ResultSet
     */
    private Sejour extractSejourFromResultSet(ResultSet rs) throws SQLException {
        Sejour sejour = new Sejour();
        sejour.setId(rs.getInt("id"));
        
        Timestamp dateEntree = rs.getTimestamp("date_entree");
        if (dateEntree != null) {
            sejour.setDateEntree(dateEntree.toLocalDateTime());
        } else {
            sejour.setDateEntree(LocalDateTime.now());
        }
        
        Timestamp dateSortie = rs.getTimestamp("date_sortie");
        if (dateSortie != null) {
            sejour.setDateSortie(dateSortie.toLocalDateTime());
        }
        
        sejour.setTypeSejour(rs.getString("type_sejour"));
        sejour.setFraisSejour(rs.getDouble("frais_sejour"));
        
        String moyenPaiement = rs.getString("moyen_paiement");
        sejour.setMoyenPaiement(moyenPaiement);
        
        String statutPaiement = rs.getString("statut_paiement");
        sejour.setStatutPaiement(statutPaiement != null ? statutPaiement : "En attente");
        
        double prixExtras = rs.getDouble("prix_extras");
        if (!rs.wasNull()) {
            sejour.setPrixExtras(prixExtras);
        }
        
        int dossierMedicaleId = rs.getInt("dossier_medicale_id");
        
        // Only set the ID of the dossier without loading the entire object to prevent circular references
        if (dossierMedicaleId > 0) {
            DossierMedicale dossier = new DossierMedicale();
            dossier.setId(dossierMedicaleId);
            sejour.setDossierMedicale(dossier);
        }
        
        return sejour;
    }
} 