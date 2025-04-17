package form;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import util.DataSource;

public class FormulairePharmacien {
    public static void afficherFormulaire(int userId) {
        String telephone = JOptionPane.showInputDialog("Numéro de téléphone du pharmacien :");

        try (Connection conn = DataSource.getInstance().getConnection()) {
            String req = "UPDATE users SET telephone = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(req);
            pstmt.setString(1, telephone);
            pstmt.setInt(2, userId);

            pstmt.executeUpdate();
            System.out.println("✅ Pharmacien mis à jour dans la table `users` !");
        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour pharmacien : " + e.getMessage());
        }
    }
}
