package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import entite.*;
import util.DataSource;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    public Users authenticate(String email, String password) throws SQLException, JsonProcessingException {
        String query = "SELECT id, nom, prenom, email, password, roles, type, adresse, telephone, date_naissance, specialite FROM users WHERE email = ?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Vérifier le mot de passe
                    String hashedPassword = rs.getString("password");
                    if (!BCrypt.checkpw(password, hashedPassword)) {
                        return null; // Mot de passe incorrect
                    }

                    // Créer l'objet utilisateur
                    String type = rs.getString("type");
                    Users user;
                    switch (type) {
                        case "PATIENT":
                            user = new Patient();
                            ((Patient) user).setAdresse(rs.getString("adresse"));
                            ((Patient) user).setTelephone(rs.getString("telephone"));
                            ((Patient) user).setDateNaissance(rs.getDate("date_naissance") != null ?
                                    rs.getDate("date_naissance").toLocalDate() : null);
                            break;
                        case "MEDECIN":
                            user = new Medecin();
                            ((Medecin) user).setSpecialite(rs.getString("specialite"));
                            ((Medecin) user).setTelephone(rs.getString("telephone"));
                            break;
                        case "PHARMACIEN":
                            user = new Pharmacien();
                            ((Pharmacien) user).setTelephone(rs.getString("telephone"));
                            break;
                        case "STAFF":
                            user = new Staff();
                            ((Staff) user).setTelephone(rs.getString("telephone"));
                            break;
                        case "ADMIN":
                            user = new Users();
                            break;
                        default:
                            user = new Users();
                            break;
                    }

                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(hashedPassword);
                    user.setType(type);

                    String rolesJson = rs.getString("roles");
                    ObjectMapper mapper = new ObjectMapper();
                    List<String> roles = mapper.readValue(rolesJson, new TypeReference<List<String>>() {});
                    user.setRoles(roles);

                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'authentification : " + e.getMessage());
            throw e;
        } catch (JsonProcessingException e) {
            System.err.println("❌ Erreur lors de la désérialisation des rôles : " + e.getMessage());
            throw e;
        }

        return null; // Utilisateur non trouvé
    }

    public void ajouterUtilisateur(Users user, String type) throws SQLException, JsonProcessingException {
        // Validation
        if (user == null || user.getEmail() == null || user.getEmail().isEmpty() ||
                user.getPassword() == null || user.getPassword().isEmpty() ||
                user.getNom() == null || user.getNom().isEmpty() ||
                user.getPrenom() == null || user.getPrenom().isEmpty()) {
            throw new IllegalArgumentException("Tous les champs communs doivent être remplis !");
        }

        // Déterminer le rôle
        String role = switch (type.toLowerCase() + "") {
            case "patient" -> "ROLE_PATIENT";
            case "medecin" -> "ROLE_MEDECIN";
            case "pharmacien" -> "ROLE_PHARMACIEN";
            case "staff" -> "ROLE_STAFF";
            default -> "ROLE_USER";
        };
        user.setRoles(List.of(role));
        user.setType(type);

        // Requête SQL
        String req = "INSERT INTO users (nom, prenom, email, password, roles, type";
        if (user instanceof Patient) {
            req += ", adresse, telephone, date_naissance";
        } else if (user instanceof Medecin) {
            req += ", specialite, telephone";
        } else if (user instanceof Pharmacien || user instanceof Staff) {
            req += ", telephone";
        }
        req += ") VALUES (?,?, ?, ?, ?, ?";

        if (user instanceof Patient) {
            req += ", ?, ?, ?";
        } else if (user instanceof Medecin) {
            req += ", ?, ?";
        } else if (user instanceof Pharmacien || user instanceof Staff) {
            req += ", ?";
        }
        req += ")";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            // Champs communs
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
            pstmt.setString(4, hashedPassword);
            ObjectMapper mapper = new ObjectMapper();
            String rolesJson = mapper.writeValueAsString(user.getRoles());
            pstmt.setString(5, rolesJson);
            pstmt.setString(6, type.toUpperCase());

            // Champs spécifiques
            int index = 7;
            if (user instanceof Patient patient) {
                pstmt.setString(index++, patient.getAdresse() != null ? patient.getAdresse() : "");
                pstmt.setString(index++, patient.getTelephone() != null ? patient.getTelephone() : "");
                pstmt.setObject(index++, patient.getDateNaissance() != null ? Date.valueOf(patient.getDateNaissance()) : null);
            } else if (user instanceof Medecin medecin) {
                pstmt.setString(index++, medecin.getSpecialite() != null ? medecin.getSpecialite() : "");
                pstmt.setString(index++, medecin.getTelephone() != null ? medecin.getTelephone() : "");
            } else if (user instanceof Pharmacien pharmacien) {
                pstmt.setString(index++, pharmacien.getTelephone() != null ? pharmacien.getTelephone() : "");
            } else if (user instanceof Staff staff) {
                pstmt.setString(index++, staff.getTelephone() != null ? staff.getTelephone() : "");
            }

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }

            System.out.println("✅ Utilisateur ajouté avec succès ! ID: " + user.getId());
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
            throw e;
        }
    }

    public void supprimer(int id) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM users WHERE id = ?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    System.err.println("⚠️ Aucun utilisateur trouvé avec cet ID.");
                    return;
                }
            }
        }

        String req = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(req)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("✅ Utilisateur supprimé avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
            throw e;
        }
    }

    public List<Users> listerUtilisateurs() throws SQLException {
        List<Users> utilisateurs = new ArrayList<>();
        String req = "SELECT id, nom, prenom, email, roles, type, adresse, telephone, date_naissance, specialite FROM users";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(req);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String type = rs.getString("type");
                Users user;

                switch (type) {
                    case "PATIENT":
                        user = new Patient();
                        ((Patient) user).setAdresse(rs.getString("adresse"));
                        ((Patient) user).setTelephone(rs.getString("telephone"));
                        ((Patient) user).setDateNaissance(rs.getDate("date_naissance") != null ?
                                rs.getDate("date_naissance").toLocalDate() : null);
                        break;
                    case "MEDECIN":
                        user = new Medecin();
                        ((Medecin) user).setSpecialite(rs.getString("specialite"));
                        ((Medecin) user).setTelephone(rs.getString("telephone"));
                        break;
                    case "PHARMACIEN":
                        user = new Pharmacien();
                        ((Pharmacien) user).setTelephone(rs.getString("telephone"));
                        break;
                    case "STAFF":
                        user = new Staff();
                        ((Staff) user).setTelephone(rs.getString("telephone"));
                        break;
                    default:
                        user = new Users();
                        break;
                }

                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setType(type);

                String rolesJson = rs.getString("roles");
                ObjectMapper mapper = new ObjectMapper();
                List<String> roles = mapper.readValue(rolesJson, new TypeReference<List<String>>() {});
                user.setRoles(roles);

                utilisateurs.add(user);
            }

            System.out.println("📋 " + utilisateurs.size() + " utilisateur(s) trouvé(s).");
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la lecture : " + e.getMessage());
            throw e;
        } catch (JsonProcessingException e) {
            System.err.println("❌ Erreur lors de la désérialisation des rôles : " + e.getMessage());
            throw new RuntimeException(e);
        }

        return utilisateurs;
    }

    // La méthode updateUtilisateur peut être conservée si vous voulez modifier des utilisateurs existants
    public void updateUtilisateur(Users user) throws SQLException, JsonProcessingException {
        // Validation des champs communs, y compris password
        if (user == null || user.getId() <= 0 ||
                user.getEmail() == null || user.getEmail().isEmpty() ||
                user.getPassword() == null || user.getPassword().isEmpty() ||  // Ajout de la validation pour password
                user.getNom() == null || user.getNom().isEmpty() ||
                user.getPrenom() == null || user.getPrenom().isEmpty()) {
            throw new IllegalArgumentException("Tous les champs requis doivent être remplis !");
        }

        // Requête SQL avec password inclus
        String req = "UPDATE users SET nom = ?, prenom = ?, email = ?, password = ?, roles = ?, type = ?";
        if (user instanceof Patient) {
            req += ", adresse = ?, telephone = ?, date_naissance = ?";
        } else if (user instanceof Medecin) {
            req += ", specialite = ?, telephone = ?";
        } else if (user instanceof Pharmacien || user instanceof Staff) {
            req += ", telephone = ?";
        }
        req += " WHERE id = ?";

        try (Connection conn = DataSource.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(req)) {
            // Champs communs
            pstmt.setString(1, user.getNom());
            pstmt.setString(2, user.getPrenom());
            pstmt.setString(3, user.getEmail());
            // Hacher le mot de passe avant la mise à jour
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
            pstmt.setString(4, hashedPassword);
            ObjectMapper mapper = new ObjectMapper();
            String rolesJson = mapper.writeValueAsString(user.getRoles());
            pstmt.setString(5, rolesJson);
            pstmt.setString(6, user.getType().toUpperCase());

            // Champs spécifiques
            int index = 7;
            if (user instanceof Patient patient) {
                pstmt.setString(index++, patient.getAdresse() != null ? patient.getAdresse() : "");
                pstmt.setString(index++, patient.getTelephone() != null ? patient.getTelephone() : "");
                pstmt.setObject(index++, patient.getDateNaissance() != null ? Date.valueOf(patient.getDateNaissance()) : null);
            } else if (user instanceof Medecin medecin) {
                pstmt.setString(index++, medecin.getSpecialite() != null ? medecin.getSpecialite() : "");
                pstmt.setString(index++, medecin.getTelephone() != null ? medecin.getTelephone() : "");
            } else if (user instanceof Pharmacien pharmacien) {
                pstmt.setString(index++, pharmacien.getTelephone() != null ? pharmacien.getTelephone() : "");
            } else if (user instanceof Staff staff) {
                pstmt.setString(index++, staff.getTelephone() != null ? staff.getTelephone() : "");
            }

            // Clause WHERE
            pstmt.setInt(index, user.getId());

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Utilisateur mis à jour avec succès ! ID: " + user.getId());
            } else {
                throw new SQLException("Aucun utilisateur trouvé avec l'ID: " + user.getId());
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour : " + e.getMessage());
            throw e;
        }
    }
}