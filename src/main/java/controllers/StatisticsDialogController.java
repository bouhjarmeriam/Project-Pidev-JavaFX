package controllers;

import entite.departement;
import entite.etage;
import entite.salle;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.DepartemntService;
import service.EtageService;
import service.SalleService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

public class StatisticsDialogController {

    @FXML private VBox barChartPlaceholder;
    @FXML private VBox pieChartPlaceholder;
    @FXML private Button exportPdfBtn;

    private final DepartemntService departementService;
    private final EtageService etageService;
    private final SalleService salleService;

    private BarChart<String, Number> barChart;
    private PieChart pieChart;

    public StatisticsDialogController(DepartemntService departementService, EtageService etageService, SalleService salleService) {
        this.departementService = departementService;
        this.etageService = etageService;
        this.salleService = salleService;
    }

    @FXML
    public void initialize() {
        createBarChart();
        createPieChart();
    }

    private void createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Départements");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nombre d'Étages");

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Nombre d'Étages par Département");
        barChart.setPrefSize(500, 300);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Étages");

        try {
            List<departement> departements = departementService.getAllDepartements();
            if (departements != null) {
                for (departement d : departements) {
                    series.getData().add(new XYChart.Data<>(d.getNom(), d.getNbr_etage()));
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des données pour le graphique à barres: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        barChart.getData().add(series);
        if (barChartPlaceholder != null) {
            barChartPlaceholder.getChildren().add(barChart);
        }
    }

    private void createPieChart() {
        pieChart = new PieChart();
        pieChart.setTitle("Nombre de Salles par Étage");
        pieChart.setPrefSize(500, 300);

        try {
            List<etage> etages = etageService.getAllEtages();
            List<salle> salles = salleService.getAll();

            Map<Integer, Long> sallesPerEtage = salles.stream()
                    .filter(s -> s.getEtage() != null)
                    .collect(Collectors.groupingBy(
                            s -> s.getEtage().getNumero(),
                            Collectors.counting()
                    ));

            if (etages != null) {
                for (etage e : etages) {
                    long count = sallesPerEtage.getOrDefault(e.getNumero(), 0L);
                    if (count > 0) {
                        pieChart.getData().add(new PieChart.Data("Étage " + e.getNumero(), count));
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des données pour le graphique en secteurs: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        if (pieChartPlaceholder != null) {
            pieChartPlaceholder.getChildren().add(pieChart);
        }
    }

    @FXML
    private void handleExportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les statistiques en PDF");
        fileChooser.setInitialFileName("statistiques.pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );

        Stage stage = (Stage) exportPdfBtn.getScene().getWindow();
        File pdfFile = fileChooser.showSaveDialog(stage);

        if (pdfFile != null) {
            Document document = null;
            try {
                document = new Document(PageSize.A4, 36, 36, 36, 36);
                PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                document.open();

                Paragraph title = new Paragraph("Statistiques", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph("\n"));

                Paragraph barChartTitle = new Paragraph("Nombre d'Étages par Département", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                barChartTitle.setSpacingAfter(10);
                document.add(barChartTitle);

                WritableImage barChartSnapshot = barChart.snapshot(new SnapshotParameters(), null);
                BufferedImage barChartImage = SwingFXUtils.fromFXImage(barChartSnapshot, null);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(barChartImage, "png", baos);
                byte[] barChartBytes = baos.toByteArray();

                com.itextpdf.text.Image barChartPdfImage = com.itextpdf.text.Image.getInstance(barChartBytes);
                float pageWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
                barChartPdfImage.scaleToFit(pageWidth, barChartPdfImage.getHeight());
                document.add(barChartPdfImage);
                document.add(new Paragraph("\n"));

                Paragraph pieChartTitle = new Paragraph("Nombre de Salles par Étage", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                pieChartTitle.setSpacingAfter(10);
                document.add(pieChartTitle);

                WritableImage pieChartSnapshot = pieChart.snapshot(new SnapshotParameters(), null);
                BufferedImage pieChartImage = SwingFXUtils.fromFXImage(pieChartSnapshot, null);

                baos.reset();
                ImageIO.write(pieChartImage, "png", baos);
                byte[] pieChartBytes = baos.toByteArray();

                com.itextpdf.text.Image pieChartPdfImage = com.itextpdf.text.Image.getInstance(pieChartBytes);
                pieChartPdfImage.scaleToFit(pageWidth, pieChartPdfImage.getHeight());
                document.add(pieChartPdfImage);

                showAlert("Succès", "Les statistiques ont été exportées avec succès dans " + pdfFile.getAbsolutePath(), Alert.AlertType.INFORMATION);
            } catch (DocumentException | IOException e) {
                showAlert("Erreur", "Erreur lors de l'exportation PDF: " + e.getMessage(), Alert.AlertType.ERROR);
            } finally {
                if (document != null && document.isOpen()) {
                    document.close();
                }
            }
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}