package form;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import util.DataSource;

public class FormulaireMedecin {
    public static void afficherFormulaire(int userId) {
        String specialite = JOptionPane.showInputDialog("Spécialité du médecin :");
        String telephone = JOptionPane.showInputDialog("Numéro de téléphone :");

        try (Connection conn = DataSource.getInstance().getConnection()) {
            // On met à jour les colonnes dans la table `users`
            String req = "UPDATE users SET specialite = ?, telephone = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(req);
            pstmt.setString(1, specialite);
            pstmt.setString(2, telephone);
            pstmt.setInt(3, userId);
            pstmt.executeUpdate();

            System.out.println("✅ Médecin mis à jour dans la table `users` !");
        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour médecin : " + e.getMessage());
        }
    }
}
