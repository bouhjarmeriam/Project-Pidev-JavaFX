package controllers;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class AdminConsultationController {
    @FXML private TableView<?> consultationTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Button statsButton;
    @FXML private Button pieChartButton;
    @FXML private Button exportPdfButton;

    @FXML
    public void initialize() {
        setupStatusFilter();

        // Set up buttons
        statsButton.setOnAction(event -> showHorizontalBarChartStatistics());
        pieChartButton.setOnAction(event -> showPieChartStatistics());
        exportPdfButton.setOnAction(event -> exportToPdf());
    }

    @FXML
    private void exportToPdf() {
        // Create a printer job for PDF printing
        javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();

        if (job != null && job.showPrintDialog(consultationTable.getScene().getWindow())) {
            // Print the table to PDF
            boolean success = job.printPage(consultationTable);
            if (success) {
                job.endJob();
                showNotification("PDF exported successfully");
            } else {
                showAlert("PDF Export Error", "Failed to export PDF");
            }
        }
    }

    private void setupStatusFilter() {
        statusFilterCombo.getItems().addAll("All", "En cours de traitement", "Confirmed", "Done");
        statusFilterCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleSearch() {
        // Search functionality would go here
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        statusFilterCombo.getSelectionModel().selectFirst();
    }

    private void showHorizontalBarChartStatistics() {
        // Statistics functionality would go here
    }

    private void showPieChartStatistics() {
        // Pie chart functionality would go here
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showNotification(String message) {
        Stage notificationStage = new Stage();
        notificationStage.initStyle(StageStyle.TRANSPARENT);

        Label label = new Label(message);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px;");

        StackPane root = new StackPane(label);
        root.setStyle("-fx-background-color: rgba(0, 100, 200, 0.8); -fx-background-radius: 10;");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        notificationStage.setScene(scene);

        notificationStage.setX(javafx.stage.Screen.getPrimary().getVisualBounds().getWidth() - 350);
        notificationStage.setY(20);

        notificationStage.show();

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> notificationStage.close());
        delay.play();
    }
}