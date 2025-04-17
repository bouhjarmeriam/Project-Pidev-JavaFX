package form;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import util.DataSource;

public class FormulairePatient {
    public static void afficherFormulaire(int userId) {
        String adresse = JOptionPane.showInputDialog("Adresse du patient :");
        String telephone = JOptionPane.showInputDialog("Numéro de téléphone :");
        String dateNaissanceStr = JOptionPane.showInputDialog("Date de naissance (format: yyyy-MM-dd) :");

        try (Connection conn = DataSource.getInstance().getConnection()) {
            String req = "UPDATE users SET adresse = ?, telephone = ?, date_naissance = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(req);
            pstmt.setString(1, adresse);
            pstmt.setString(2, telephone);
            pstmt.setDate(3, java.sql.Date.valueOf(LocalDate.parse(dateNaissanceStr)));
            pstmt.setInt(4, userId);

            pstmt.executeUpdate();
            System.out.println("✅ Patient mis à jour dans la table `users` !");
        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour patient : " + e.getMessage());
        }
    }
}
