package controllers;

import entite.Consultation;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import service.ConsultationService;
import service.TwilioSMSService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class AdminConsultationController {
    @FXML private TableView<Consultation> consultationTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Button statsButton;
    @FXML private Button pieChartButton;
    @FXML private Button exportPdfButton;

    private final ConsultationService consultationService = new ConsultationService();
    private final TwilioSMSService twilioService = new TwilioSMSService();
    private final Preferences prefs = Preferences.userNodeForPackage(AdminConsultationController.class);

    @FXML
    public void initialize() {
        verifyTwilioSetup();
        setupTableColumns();
        setupStatusFilter();
        refreshTable();

        // Set up buttons
        statsButton.setOnAction(event -> showHorizontalBarChartStatistics());
        pieChartButton.setOnAction(event -> showPieChartStatistics());
        exportPdfButton.setOnAction(event -> exportToPdf());
    }

    private void verifyTwilioSetup() {
        if (!twilioService.isConfigured()) {
            System.err.println("Twilio not properly configured in config.properties!");
            showAlert("Configuration Error",
                    "Twilio SMS is not configured properly. Check config.properties file.");
        }
    }

    private void setupTableColumns() {
        consultationTable.getColumns().clear();

        TableColumn<Consultation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Consultation, String> serviceCol = new TableColumn<>("Service");
        serviceCol.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        serviceCol.setPrefWidth(150);

        TableColumn<Consultation, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(120);

        TableColumn<Consultation, String> patientCol = new TableColumn<>("Patient ID");
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientIdentifier"));
        patientCol.setPrefWidth(100);

        TableColumn<Consultation, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneCol.setPrefWidth(100);

        TableColumn<Consultation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(new StatusCellFactory());
        statusCol.setPrefWidth(100);
        statusCol.setEditable(true);

        TableColumn<Consultation, Integer> ratingCol = new TableColumn<>("Rating");
        ratingCol.setPrefWidth(80);
        ratingCol.setCellValueFactory(cellData -> {
            Consultation c = cellData.getValue();
            return new SimpleIntegerProperty(getRatingForConsultation(c.getId())).asObject();
        });
        ratingCol.setCellFactory(column -> new TableCell<Consultation, Integer>() {
            private final HBox starsContainer = new HBox(2);

            @Override
            protected void updateItem(Integer rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setGraphic(null);
                } else {
                    starsContainer.getChildren().clear();
                    for (int i = 1; i <= 5; i++) {
                        SVGPath star = new SVGPath();
                        star.setContent("M12 .587l3.668 7.568 8.332 1.151-6.064 5.828 1.48 8.279-7.416-3.967-7.417 3.967 1.481-8.279-6.064-5.828 8.332-1.151z");
                        star.setFill(i <= rating ? Color.GOLD : Color.LIGHTGRAY);
                        starsContainer.getChildren().add(star);
                    }
                    setGraphic(starsContainer);
                }
            }
        });

        consultationTable.getColumns().addAll(idCol, serviceCol, dateCol, patientCol, phoneCol, statusCol, ratingCol);
        consultationTable.setEditable(true);
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

    private int getRatingForConsultation(int consultationId) {
        String savedRatings = prefs.get("consultation_ratings", "");
        if (!savedRatings.isEmpty()) {
            String[] pairs = savedRatings.split(";");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2 && Integer.parseInt(keyValue[0]) == consultationId) {
                    return Integer.parseInt(keyValue[1]);
                }
            }
        }
        return 0;
    }

    private void setupStatusFilter() {
        statusFilterCombo.getItems().addAll("All", "En cours de traitement", "Confirmed", "Done");
        statusFilterCombo.getSelectionModel().selectFirst();
        statusFilterCombo.setOnAction(event -> refreshTable());
    }

    @FXML
    private void handleSearch() {
        refreshTable();
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        statusFilterCombo.getSelectionModel().selectFirst();
        refreshTable();
    }

    private void refreshTable() {
        String searchTerm = searchField.getText().trim();
        String statusFilter = statusFilterCombo.getValue();

        List<Consultation> consultations;
        if (searchTerm.isEmpty()) {
            consultations = consultationService.getAllConsultations();
        } else {
            consultations = consultationService.searchConsultations(searchTerm);
        }

        if (!"All".equals(statusFilter)) {
            consultations.removeIf(c -> !c.getStatus().equals(statusFilter));
        }

        consultationTable.getItems().setAll(FXCollections.observableArrayList(consultations));
    }

    private void showHorizontalBarChartStatistics() {
        List<Consultation> consultations = consultationService.getAllConsultations();

        Map<String, Long> serviceCounts = consultations.stream()
                .collect(Collectors.groupingBy(
                        Consultation::getServiceName,
                        Collectors.counting()
                ));

        // Create axes - using Number for X and Category for Y for horizontal bars
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Number of Consultations");
        xAxis.setTickUnit(1); // Show every integer value
        xAxis.setMinorTickVisible(false);

        CategoryAxis yAxis = new CategoryAxis();
        yAxis.setLabel("Service");
        yAxis.setTickLabelRotation(0); // Horizontal labels

        // Create horizontal bar chart
        BarChart<Number, String> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Consultations by Service (Horizontal)");
        barChart.setCategoryGap(20); // Space between bars
        barChart.setLegendVisible(false); // No need for legend with single series

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName("Services");

        // Sort services by count (descending)
        serviceCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry ->
                        series.getData().add(new XYChart.Data<>(entry.getValue(), entry.getKey()))
                );

        barChart.getData().add(series);

        // Customize bar colors
        for (XYChart.Data<Number, String> data : series.getData()) {
            data.getNode().setStyle("-fx-bar-fill: #3498db;");
        }

        Stage chartStage = new Stage();
        chartStage.setTitle("Consultation Statistics - Horizontal Bar Chart");
        VBox vbox = new VBox(barChart);
        Scene scene = new Scene(vbox, 800, 600);
        chartStage.setScene(scene);
        chartStage.show();
    }

    private void showPieChartStatistics() {
        List<Consultation> consultations = consultationService.getAllConsultations();

        Map<String, Long> serviceCounts = consultations.stream()
                .collect(Collectors.groupingBy(
                        Consultation::getServiceName,
                        Collectors.counting()
                ));

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Consultations by Service");
        pieChart.setLabelLineLength(10);
        pieChart.setLegendVisible(true);

        serviceCounts.forEach((service, count) ->
                pieChart.getData().add(new PieChart.Data(service + " (" + count + ")", count))
        );

        Stage chartStage = new Stage();
        chartStage.setTitle("Consultation Statistics - Pie Chart");
        VBox vbox = new VBox(pieChart);
        Scene scene = new Scene(vbox, 600, 500);
        chartStage.setScene(scene);
        chartStage.show();
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

    private class StatusCellFactory implements Callback<TableColumn<Consultation, String>,
            TableCell<Consultation, String>> {

        @Override
        public TableCell<Consultation, String> call(TableColumn<Consultation, String> param) {
            return new TableCell<>() {
                private final ComboBox<String> comboBox = new ComboBox<>();

                {
                    comboBox.getItems().addAll("En cours de traitement", "Confirmed", "Done");
                    comboBox.setOnAction(event -> {
                        if (getTableRow() != null && getTableRow().getItem() != null) {
                            Consultation consultation = getTableRow().getItem();
                            String oldStatus = consultation.getStatus();
                            String newStatus = comboBox.getValue();

                            if (!newStatus.equals(oldStatus)) {
                                if (consultationService.updateConsultationStatus(
                                        consultation.getId(), newStatus)) {
                                    consultation.setStatus(newStatus);

                                    handleSmsNotification(consultation, newStatus);
                                    refreshTable();
                                } else {
                                    showAlert("Error", "Failed to update status");
                                }
                            }
                        }
                    });
                }

                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        comboBox.setValue(status);
                        setGraphic(comboBox);

                        switch (status) {
                            case "En cours de traitement":
                                setStyle("-fx-background-color: #fff3cd;");
                                break;
                            case "Confirmed":
                                setStyle("-fx-background-color: #d4edda;");
                                break;
                            case "Done":
                                setStyle("-fx-background-color: #f8d7da;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            };
        }
    }

    private void handleSmsNotification(Consultation consultation, String newStatus) {
        if (!twilioService.isConfigured()) {
            showNotification("Status updated (SMS not configured)");
            return;
        }

        try {
            String phoneNumber = consultation.getPhoneNumber();
            String message = buildSmsMessage(consultation, newStatus);

            String smsResult = twilioService.sendSMS(phoneNumber, message);

            if (smsResult.contains("successfully")) {
                showNotification("Status updated & SMS sent to " +
                        consultation.getPatientIdentifier());
            } else {
                showAlert("SMS Failed", "Status updated but SMS failed: " + smsResult);
            }
        } catch (Exception e) {
            showAlert("SMS Error", "Error sending SMS: " + e.getMessage());
        }
    }

    private String buildSmsMessage(Consultation consultation, String newStatus) {
        return String.format(
                "Dear %s,%n" +
                        "Your consultation status is now: %s%n" +
                        "%s%n" +
                        "Thank you!",
                consultation.getPatientIdentifier(),
                newStatus,
                getStatusSpecificMessage(newStatus)
        );
    }

    private String getStatusSpecificMessage(String status) {
        switch (status) {
            case "En cours de traitement":
                return "We are currently processing your request. Thank you for your patience.";
            case "Confirmed":
                return "Your appointment has been confirmed. We look forward to seeing you.";
            case "Done":
                return "We hope you had a great experience with us!";
            default:
                return "";
        }
    }
}