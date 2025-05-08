package controllers;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPTable;
import entite.Entretien;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import service.EntretienService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class ListeEntretienController {

    @FXML
    private TableView<Entretien> entretienTable;

    @FXML
    private TableColumn<Entretien, String> nomColumn;

    @FXML
    private TableColumn<Entretien, LocalDate> dateColumn;

    @FXML
    private TableColumn<Entretien, String> descriptionColumn;

    @FXML
    private TableColumn<Entretien, Void> actionsColumn;

    @FXML
    private TableColumn<Entretien, Void> pdfColumn;

    private final EntretienService entretienService = new EntretienService();

    @FXML
    public void initialize() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomEquipement"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        addActionButtonsToTable();
        addPdfButtonToTable();

        loadEntretienData();
    }

    private void loadEntretienData() {
        entretienTable.getItems().setAll(entretienService.getAllEntretiens());
    }

    private void addActionButtonsToTable() {
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button btnModifier = new Button("Modifier");
            private final Button btnSupprimer = new Button("Supprimer");
            private final HBox hbox = new HBox(10, btnModifier, btnSupprimer);

            {
                btnModifier.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnSupprimer.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");

                btnModifier.setOnAction(event -> {
                    Entretien entretien = getTableView().getItems().get(getIndex());
                    handleModifier(entretien);
                });

                btnSupprimer.setOnAction(event -> {
                    Entretien entretien = getTableView().getItems().get(getIndex());
                    handleSupprimer(entretien);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
    }

    private void addPdfButtonToTable() {
        pdfColumn.setCellFactory(column -> new TableCell<>() {
            private final Button btnGenererPdf = new Button("Générer PDF");
            private final HBox hbox = new HBox(10, btnGenererPdf);

            {
                btnGenererPdf.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                btnGenererPdf.setOnAction(event -> {
                    Entretien entretien = getTableView().getItems().get(getIndex());
                    handleGenererPdf(entretien);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
            }
        });
    }

    private void handleModifier(Entretien entretien) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEntretien.fxml"));
            Parent root = loader.load();

            ModifierEntretienController controller = loader.getController();
            controller.initData(entretien);
            controller.setOnEntretienModifie(() -> loadEntretienData());

            Stage stage = new Stage();
            stage.setTitle("Modifier Entretien");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la page de modification : " + e.getMessage());
        }
    }

    private void handleSupprimer(Entretien entretien) {
        entretienService.deleteEntretien(entretien.getId());
        entretienTable.getItems().remove(entretien);
        System.out.println("🗑 Entretien supprimé : " + entretien.getNomEquipement());
    }

    private void handleGenererPdf(Entretien entretien) {
        Document document = new Document();
        try {
            // Nom du fichier PDF
            String fileName = "entretien_" + entretien.getId() + ".pdf";
            // Création du PDF
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Définir des polices personnalisées
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.DARK_GRAY); // Titre plus grand
            Font subTitleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLUE); // Sous-titre
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 14, Font.NORMAL, BaseColor.BLACK); // Texte normal
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK); // Texte en gras

            // Ajouter le titre
            Paragraph title = new Paragraph("Rapport d'Entretien d'Équipement", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(20f);  // Espace après le titre
            document.add(title);

            // Ajouter le nom de l'équipement
            Paragraph nomEquipement = new Paragraph("🔧 Nom de l'équipement : ", boldFont);
            nomEquipement.add(new Paragraph(entretien.getNomEquipement(), normalFont));
            nomEquipement.setSpacingAfter(10f);  // Espace après le nom de l'équipement
            document.add(nomEquipement);

            // Ajouter la date de l'entretien
            Paragraph dateEntretien = new Paragraph("📅 Date de l'entretien : ", boldFont);
            dateEntretien.add(new Paragraph(entretien.getDate().toString(), normalFont));
            dateEntretien.setSpacingAfter(10f);  // Espace après la date
            document.add(dateEntretien);

            // Ajouter la description de l'entretien avec fond
            // Créer une cellule pour simuler le fond de couleur
            PdfPTable table = new PdfPTable(1); // Table à une seule colonne
            table.setWidthPercentage(100); // La table occupe toute la largeur du document
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(BaseColor.WHITE); // Définir la couleur de fond pour la cellule
            cell.setBorder(PdfPCell.NO_BORDER); // Supprimer les bordures
            cell.setPadding(10f); // Espacement dans la cellule

            // Ajouter la description du texte dans cette cellule
            Paragraph descriptionEntretien = new Paragraph("📝 Description de l'entretien :\n", boldFont);
            descriptionEntretien.add(new Paragraph(entretien.getDescription(), normalFont));
            cell.addElement(descriptionEntretien);
            table.addCell(cell);
            document.add(table); // Ajouter la table (avec fond) au document

            // Ajouter une ligne de séparation
            Paragraph separator = new Paragraph("------------------------------------------------------", normalFont);
            separator.setSpacingBefore(20f); // Espacement avant la ligne
            separator.setSpacingAfter(20f); // Espacement après la ligne
            separator.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(separator);

            // Ajouter la mention automatique de génération
            Paragraph generatedBy = new Paragraph("Généré automatiquement par l'équipe des techniciens de gestion de clinique.", subTitleFont);
            generatedBy.setAlignment(Paragraph.ALIGN_CENTER);
            generatedBy.setSpacingBefore(20f);  // Espacement avant la mention
            document.add(generatedBy);

            // Fermer le document PDF
            document.close(); // Toujours fermer avant d’ouvrir

            // ✅ Ouvrir le fichier PDF automatiquement
            File pdfFile = new File(fileName);
            if (pdfFile.exists()) {
                // Pour Linux : utilise xdg-open
                new ProcessBuilder("xdg-open", pdfFile.getAbsolutePath()).start();
                System.out.println("✅ PDF généré et ouvert : " + fileName);
            } else {
                System.err.println("❌ Le fichier PDF n'a pas pu être généré.");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }

}

