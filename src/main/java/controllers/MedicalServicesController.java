package controllers;

import entite.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import service.serviceservice;

import java.util.List;
import java.util.Optional;

public class MedicalServicesController {

    private final serviceservice serviceService = new serviceservice();
    private final ObservableList<Service> servicesData = FXCollections.observableArrayList();

    @FXML private TextField serviceName;
    @FXML private TextArea serviceDescription;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private TableView<Service> servicesTable;
    @FXML private TableColumn<Service, String> nameColumn;
    @FXML private TableColumn<Service, String> descriptionColumn;
    @FXML private TableColumn<Service, Integer> durationColumn;
    @FXML private TableColumn<Service, Void> modifyColumn;
    @FXML private TableColumn<Service, Void> deleteColumn;

    @FXML
    private Label serviceNameError;
    @FXML
    private Label serviceDescriptionError;
    @FXML
    private Label durationError;

    @FXML
    private void handleSubmit() {
        // Clear previous error messages
        serviceNameError.setText("");
        serviceDescriptionError.setText("");
        durationError.setText("");

        try {
            String name = serviceName.getText().trim();
            String description = serviceDescription.getText().trim();
            int duration = durationSpinner.getValue();

            boolean valid = true;

            // Validate service name
            if (name.isEmpty()) {
                serviceNameError.setText("Service name cannot be blank!");
                valid = false;
            } else if (name.length() < 3) {
                serviceNameError.setText("Service name must be at least 3 characters long!");
                valid = false;
            } else if (name.matches(".*\\d.*")) {  // Check if name contains any digit
                serviceNameError.setText("Service name cannot contain numbers!");
                valid = false;
            }



            // Validate description
            if (description.isEmpty()) {
                serviceDescriptionError.setText("Description cannot be blank!");
                valid = false;
            } else if (!description.contains(" ")) {
                serviceDescriptionError.setText("Description must contain at least one space!");
                valid = false;
            }

            // Validate duration
            if (duration <= 0) {
                durationError.setText("Duration must be greater than 0!");
                valid = false;
            }

            // If all inputs are valid
            if (valid) {
                // Create and add service
                Service newService = new Service(name, description, duration);
                serviceService.addService(newService);
                refreshTableData();
                clearForm();
                showAlert("Success", "Service added successfully!");
            }

        } catch (Exception e) {
            showAlert("Database Error", "Failed to add service: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private void refreshTableData() {
        try {
            List<Service> services = serviceService.getAllServices();
            servicesData.setAll(services);
            servicesTable.setItems(servicesData);
            servicesTable.refresh();
        } catch (Exception e) {
            showAlert("Error", "Failed to load services: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearForm() {
        serviceName.clear();
        serviceDescription.clear();
        durationSpinner.getValueFactory().setValue(1);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void initialize() {
        durationSpinner.getValueFactory().setValue(1);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        servicesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        addModifyButtonToTable();
        addDeleteButtonToTable();
        refreshTableData();
    }

    private void addModifyButtonToTable() {
        modifyColumn.setCellFactory(param -> new TableCell<>() {
            private final Button modifyButton = new Button("Modify");
            {
                modifyButton.setOnAction(event -> handleModify(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : modifyButton);
            }
        });
    }

    private void addDeleteButtonToTable() {
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
    }

    private void handleModify(Service service) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modify Service");
        dialog.setHeaderText("Edit Service Details");

        TextField nameField = new TextField(service.getName());
        TextArea descriptionField = new TextArea(service.getDescription());
        Spinner<Integer> durationSpinner = new Spinner<>(1, 1000, service.getDuration());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Duration (minutes):"), 0, 2);
        grid.add(durationSpinner, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            service.setName(nameField.getText().trim());
            service.setDescription(descriptionField.getText().trim());
            service.setDuration(durationSpinner.getValue());
            serviceService.updateService(service);
            refreshTableData();
        }
    }

    private void handleDelete(Service service) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Confirmation");
        confirmation.setHeaderText("Are you sure you want to delete this service?");
        confirmation.setContentText("Service: " + service.getName());

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceService.deleteService(service.getId());
            refreshTableData();
        }
    }
}
