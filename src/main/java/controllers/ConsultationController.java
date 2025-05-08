package controllers;

import entite.Consultation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import service.ConsultationService;
import service.serviceservice;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class ConsultationController {
    // FXML injected fields
    @FXML private TextField loginPatientIdField;
    @FXML private GridPane consultationForm;
    @FXML private HBox actionButtonsContainer;
    @FXML private DatePicker datePicker;
    @FXML private TextField patientIdField;
    @FXML private TextField statusField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> serviceCombo;
    @FXML private TableView<Consultation> consultationTable;
    @FXML private TableColumn<Consultation, Integer> idColumn;
    @FXML private TableColumn<Consultation, String> serviceColumn;
    @FXML private TableColumn<Consultation, Date> dateColumn;
    @FXML private TableColumn<Consultation, String> statusColumn;
    @FXML private TableColumn<Consultation, String> phoneColumn;
    @FXML private TableColumn<Consultation, Integer> ratingColumn;
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;
    @FXML private Button rateButton;

    // Services and data
    private final ConsultationService consultationService = new ConsultationService();
    private final serviceservice serviceService = new serviceservice();
    private final Map<String, Integer> serviceNameToIdMap = new HashMap<>();
    private final Map<Integer, Integer> ratings = new HashMap<>(); // consultation ID -> rating
    private String currentPatientId;
    private Preferences prefs = Preferences.userNodeForPackage(ConsultationController.class);

    @FXML
    public void initialize() {
        // Initial UI state
        consultationForm.setVisible(false);
        actionButtonsContainer.setVisible(false);
        consultationTable.setVisible(false);
        toggleActionButtons(false);

        // Configure status field
        statusField.setText("En cours de traitement");
        statusField.setDisable(true);

        // Load services into combobox
        loadServices();

        // Set up table columns and selection listener
        setupTableColumns();
        setupTableSelectionListener();
        loadSavedRatings();

        // Enforce button styles after UI loads
        Platform.runLater(() -> {
            enforceButtonStyles();
            enforceBackButtonStyle();
            enforceRateButtonStyle();
        });
    }

    // ===== RATING FUNCTIONALITY =====
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        // Rating column setup
        ratingColumn.setCellValueFactory(cellData -> {
            Integer rating = ratings.get(cellData.getValue().getId());
            return new javafx.beans.property.SimpleObjectProperty<>(rating);
        });

        ratingColumn.setCellFactory(column -> new TableCell<Consultation, Integer>() {
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
    }

    private void loadSavedRatings() {
        try {
            String savedRatings = prefs.get("consultation_ratings", "");
            if (!savedRatings.isEmpty()) {
                String[] pairs = savedRatings.split(";");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":");
                    if (keyValue.length == 2) {
                        ratings.put(Integer.parseInt(keyValue[0]), Integer.parseInt(keyValue[1]));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading ratings: " + e.getMessage());
        }
    }

    private void saveRatings() {
        try {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Integer, Integer> entry : ratings.entrySet()) {
                sb.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
            }
            prefs.put("consultation_ratings", sb.toString());
        } catch (Exception e) {
            System.err.println("Error saving ratings: " + e.getMessage());
        }
    }

    @FXML
    private void handleRateService() {
        Consultation selected = consultationTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Create star rating dialog
            Dialog<Integer> dialog = new Dialog<>();
            dialog.setTitle("Rate Consultation");
            dialog.setHeaderText("How would you rate this service? (1-5 stars)");

            // Set dialog buttons
            ButtonType submitButton = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);

            // Create star rating UI
            HBox ratingBox = new HBox(5);
            ratingBox.setAlignment(javafx.geometry.Pos.CENTER);

            ToggleGroup starGroup = new ToggleGroup();
            for (int i = 1; i <= 5; i++) {
                ToggleButton starBtn = new ToggleButton("★");
                starBtn.setToggleGroup(starGroup);
                starBtn.setUserData(i);
                starBtn.setStyle("-fx-font-size: 24px; -fx-text-fill: " + (i <= 3 ? "gold" : "lightgray") + ";");
                starBtn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    if (isSelected) {
                        starBtn.setStyle("-fx-font-size: 24px; -fx-text-fill: gold;");
                    }
                });
                ratingBox.getChildren().add(starBtn);
            }

            dialog.getDialogPane().setContent(ratingBox);
            dialog.setResultConverter(buttonType -> {
                if (buttonType == submitButton) {
                    Toggle selectedToggle = starGroup.getSelectedToggle();
                    return selectedToggle != null ? (Integer) selectedToggle.getUserData() : null;
                }
                return null;
            });

            dialog.showAndWait().ifPresent(rating -> {
                if (rating != null) {
                    ratings.put(selected.getId(), rating);
                    saveRatings();
                    refreshTable();
                    showAlert("Thank You", "You rated this service " + rating + " stars");
                }
            });
        } else {
            showAlert("Error", "Please select a consultation to rate");
        }
    }

    private void enforceRateButtonStyle() {
        rateButton.setStyle(
                "-fx-background-color: #f39c12 !important; " +
                        "-fx-text-fill: white !important; " +
                        "-fx-background-radius: 5px !important; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0.5, 0, 1);"
        );

        rateButton.setOnMouseEntered(e -> rateButton.setStyle(
                "-fx-background-color: #e67e22 !important; " +
                        "-fx-text-fill: white !important; " +
                        "-fx-background-radius: 5px !important; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10px 20px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0.5, 0, 1);"
        ));

        rateButton.setOnMouseExited(e -> enforceRateButtonStyle());
    }

    // ===== EXISTING METHODS (PRESERVED) =====
    private void loadServices() {
        serviceService.getAllServices().forEach(service -> {
            serviceCombo.getItems().add(service.getName());
            serviceNameToIdMap.put(service.getName(), service.getId());
        });
    }

    private void setupTableSelectionListener() {
        consultationTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        populateFormWithSelectedConsultation(newValue);
                        toggleActionButtons(true);
                    } else {
                        toggleActionButtons(false);
                    }
                });
    }

    private void toggleActionButtons(boolean showUpdateDelete) {
        addButton.setVisible(!showUpdateDelete);
        updateButton.setVisible(showUpdateDelete);
        deleteButton.setVisible(showUpdateDelete);
        rateButton.setVisible(showUpdateDelete);
        enforceButtonStyles();
    }

    private void enforceButtonStyles() {
        // ADD BUTTON - BLUE
        if (addButton.isVisible()) {
            addButton.setStyle("-fx-background-color: #3498db !important; " +
                    "-fx-text-fill: white !important; " +
                    "-fx-background-radius: 5px !important; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0.5, 0, 1);");
        }

        // DELETE BUTTON - RED
        if (deleteButton.isVisible()) {
            deleteButton.setStyle("-fx-background-color: #e74c3c !important; " +
                    "-fx-text-fill: white !important; " +
                    "-fx-background-radius: 5px !important; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0.5, 0, 1);");
        }

        // UPDATE BUTTON - OUTLINE BLUE
        if (updateButton.isVisible()) {
            updateButton.setStyle("-fx-background-color: transparent !important; " +
                    "-fx-text-fill: #3498db !important; " +
                    "-fx-border-color: #3498db !important; " +
                    "-fx-border-width: 2px !important; " +
                    "-fx-border-radius: 5px !important; " +
                    "-fx-font-weight: bold; " +
                    "-fx-padding: 10px 20px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 1, 0, 0, 1);");
        }
    }

    private void enforceBackButtonStyle() {
        backButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #3498db; " +
                        "-fx-border-color: transparent; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 5px 10px;"
        );

        backButton.setOnMouseEntered(e -> backButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #2980b9; " +
                        "-fx-underline: true; " +
                        "-fx-border-color: transparent; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 5px 10px;"
        ));

        backButton.setOnMouseExited(e -> enforceBackButtonStyle());
    }

    @FXML
    private void handleBackButton() {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/front.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not load the dashboard");
        }
    }

    @FXML
    private void handlePatientLogin() {
        String patientId = loginPatientIdField.getText().trim();
        if (!patientId.isEmpty()) {
            currentPatientId = patientId;
            consultationForm.setVisible(true);
            actionButtonsContainer.setVisible(true);
            consultationTable.setVisible(true);
            patientIdField.setText(patientId);
            patientIdField.setDisable(true);
            refreshTable();
            clearForm();
        } else {
            showAlert("Error", "Please enter a valid patient ID");
        }
    }

    @FXML
    private void handleAddConsultation() {
        if (validateForm()) {
            Consultation consultation = new Consultation();
            consultation.setServiceId(serviceNameToIdMap.get(serviceCombo.getValue()));
            consultation.setDate(Date.valueOf(datePicker.getValue()));
            consultation.setPatientIdentifier(currentPatientId);
            consultation.setStatus("En cours de traitement");
            consultation.setPhoneNumber(phoneField.getText());

            consultationService.addConsultation(consultation);
            refreshTable();
            clearForm();
            showAlert("Success", "Consultation added successfully");
        }
    }

    @FXML
    private void handleUpdateConsultation() {
        Consultation selected = consultationTable.getSelectionModel().getSelectedItem();
        if (selected != null && validateForm()) {
            selected.setServiceId(serviceNameToIdMap.get(serviceCombo.getValue()));
            selected.setDate(Date.valueOf(datePicker.getValue()));
            selected.setPhoneNumber(phoneField.getText());

            consultationService.updateConsultation(selected);
            refreshTable();
            clearForm();
            showAlert("Success", "Consultation updated successfully");
        }
    }

    @FXML
    private void handleDeleteConsultation() {
        Consultation selected = consultationTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Are you sure you want to delete this consultation?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    consultationService.deleteConsultation(selected.getId(), currentPatientId);
                    ratings.remove(selected.getId());
                    saveRatings();
                    refreshTable();
                    clearForm();
                    showAlert("Success", "Consultation deleted successfully");
                }
            });
        }
    }

    private void refreshTable() {
        if (currentPatientId != null) {
            consultationTable.getItems().setAll(
                    consultationService.getConsultationsByPatient(currentPatientId)
            );
        }
    }

    private void populateFormWithSelectedConsultation(Consultation consultation) {
        datePicker.setValue(consultation.getDate().toLocalDate());
        patientIdField.setText(consultation.getPatientIdentifier());
        statusField.setText(consultation.getStatus());
        phoneField.setText(consultation.getPhoneNumber());
        serviceCombo.setValue(consultation.getServiceName());
    }

    private void clearForm() {
        datePicker.setValue(null);
        statusField.setText("En cours de traitement");
        phoneField.clear();
        serviceCombo.getSelectionModel().clearSelection();
        consultationTable.getSelectionModel().clearSelection();
        toggleActionButtons(false);
    }

    private boolean validateForm() {
        if (datePicker.getValue() == null) {
            showAlert("Error", "Please select a date");
            return false;
        }
        if (serviceCombo.getValue() == null) {
            showAlert("Error", "Please select a service");
            return false;
        }
        if (phoneField.getText().isEmpty()) {
            showAlert("Error", "Please enter a phone number");
            return false;
        }  else if (!phoneField.getText().matches("^\\+216[\\s-]?\\d{2}[\\s-]?\\d{3}[\\s-]?\\d{3}$")) {
            showAlert("Error", "Phone number must be in Tunisian format (e.g., +21612345678, +216 12 345 678, or +216-12-345-678)");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}