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

    private BarChart<String, Number> barChart;
    private PieChart pieChart;

    // Initialize services directly
    private final DepartemntService departementService = new DepartemntService();
    private final EtageService etageService = new EtageService();
    private final SalleService salleService = new SalleService();

    public StatisticsDialogController() {
        // Dependencies are initialized directly
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
            showAlert("Erreur", "Erreur lors du chargement des données: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        barChart.getData().add(series);
        barChartPlaceholder.getChildren().add(barChart);
    }

    private void createPieChart() {
        pieChart = new PieChart();
        pieChart.setTitle("Nombre de Salles par Étage");

        try {
            List<etage> etages = etageService.getAllEtages();
            List<salle> salles = salleService.getAll();

            Map<Integer, Long> sallesPerEtage = salles.stream()
                    .filter(s -> s.getEtage() != null)
                    .collect(Collectors.groupingBy(
                            s -> s.getEtage().getNumero(),
                            Collectors.counting()
                    ));

            for (etage e : etages) {
                long count = sallesPerEtage.getOrDefault(e.getNumero(), 0L);
                pieChart.getData().add(new PieChart.Data("Étage " + e.getNumero(), count));
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des données: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        pieChartPlaceholder.getChildren().add(pieChart);
    }

    @FXML
    private void handleExportPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter en PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(exportPdfBtn.getScene().getWindow());
        if (file == null) return;

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Statistiques des Départements", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            addChartToDocument(document, barChart, "Nombre d'étages par département");
            document.add(Chunk.NEWLINE);
            addChartToDocument(document, pieChart, "Nombre de salles par étage");

            document.close();
            showAlert("Succès", "PDF exporté avec succès", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'export: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void addChartToDocument(Document document, Chart chart, String title) throws Exception {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph paragraph = new Paragraph(title, font);
        document.add(paragraph);

        WritableImage image = chart.snapshot(new SnapshotParameters(), null);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        com.itextpdf.text.Image pdfImage = com.itextpdf.text.Image.getInstance(baos.toByteArray());

        float documentWidth = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
        pdfImage.scaleToFit(documentWidth, 300);

        document.add(pdfImage);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}