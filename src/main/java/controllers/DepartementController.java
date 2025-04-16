package controllers;

import entite.departement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import service.DepartementService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class DepartementController {

    @FXML private MenuBar menuBar;
    @FXML private TextField nomField;
    @FXML private TextField adresseField;
    @FXML private TextField imageField;
    @FXML private ImageView imagePreview;
    @FXML private Label nomError;
    @FXML private Label adresseError;
    @FXML private Label imageError;

    @FXML private TableView<departement> departementTable;
    @FXML private TableColumn<departement, String> nomColumn;
    @FXML private TableColumn<departement, String> adresseColumn;
    @FXML private TableColumn<departement, String> imageColumn;
    @FXML private TableColumn<departement, Void> modifierColumn;
    @FXML private TableColumn<departement, Void> supprimerColumn;

    private final DepartementService departementService = new DepartementService();
    private final ObservableList<departement> departementData = FXCollections.observableArrayList();
    private String imagePath = "";
    private static final String IMAGE_DIR = "src/main/resources/images/";

    @FXML
    public void initialize() {
        // Create images directory if it doesn't exist
        File imageDir = new File(IMAGE_DIR);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        setupTableColumns();
        loadDepartements();
        setupMenuBar();
    }

    private void loadDepartements() {
        departementData.clear();
        departementData.addAll(departementService.getAllDepartements());
        departementTable.setItems(departementData);
    }

    private void setupMenuBar() {
        // Activate Departments menu by default
        Menu navigationMenu = menuBar.getMenus().get(1);
        for (MenuItem item : navigationMenu.getItems()) {
            if (item.getText().equals("Départements")) {
                item.getStyleClass().add("active");
            }
        }
    }

    private void setupTableColumns() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));

        imageColumn.setCellFactory(col -> new TableCell<departement, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);
                imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1);");
            }

            @Override
            protected void updateItem(String imageName, boolean empty) {
                super.updateItem(imageName, empty);
                if (empty || imageName == null || imageName.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        File imageFile = new File(IMAGE_DIR + imageName);
                        if (imageFile.exists()) {
                            Image image = new Image(imageFile.toURI().toString());
                            imageView.setImage(image);
                            setGraphic(imageView);
                        } else {
                            setGraphic(new Label("No Image"));
                        }
                    } catch (Exception e) {
                        setGraphic(new Label("Error"));
                        e.printStackTrace();
                    }
                }
            }
        });

        addActionButtonsToTable();
    }

    private void addActionButtonsToTable() {
        modifierColumn.setCellFactory(col -> new TableCell<departement, Void>() {
            private final Button btn = new Button();
            {
                btn.getStyleClass().add("action-button");
                btn.getStyleClass().add("edit-button");
                Image editIcon = new Image(getClass().getResourceAsStream("src/main/resources/icons/edit-icon.png"));
                btn.setGraphic(new ImageView(editIcon));
                btn.setTooltip(new Tooltip("Modifier"));
                btn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        supprimerColumn.setCellFactory(col -> new TableCell<departement, Void>() {
            private final Button btn = new Button();
            {
                btn.getStyleClass().add("action-button");
                btn.getStyleClass().add("delete-button");
                Image deleteIcon = new Image(getClass().getResourceAsStream("src/main/resources/icons/delete-icon.png"));
                btn.setGraphic(new ImageView(deleteIcon));
                btn.setTooltip(new Tooltip("Supprimer"));
                btn.setOnAction(e -> confirmAndDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void showEditDialog(departement dept) {
        // Populate form with selected department data
        nomField.setText(dept.getNom());
        adresseField.setText(dept.getAdresse());
        imageField.setText(dept.getImage());

        // Load image preview if exists
        if (dept.getImage() != null && !dept.getImage().isEmpty()) {
            File imageFile = new File(IMAGE_DIR + dept.getImage());
            if (imageFile.exists()) {
                imagePreview.setImage(new Image(imageFile.toURI().toString()));
                imagePath = dept.getImage();
            }
        }
    }

    private void confirmAndDelete(departement dept) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le département");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le département " + dept.getNom() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            departementService.deleteDepartement(dept);
            loadDepartements();
            showAlert("Succès", "Département supprimé avec succès", Alert.AlertType.INFORMATION);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm() {
        nomField.clear();
        adresseField.clear();
        imageField.clear();
        imagePreview.setImage(null);
        imagePath = "";

        nomError.setText("");
        adresseError.setText("");
        imageError.setText("");
    }

    @FXML
    private void handleBrowse(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Copy file to images directory
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destFile = new File(IMAGE_DIR + fileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Update UI
                imageField.setText(fileName);
                imagePreview.setImage(new Image(destFile.toURI().toString()));
                imagePath = fileName;
            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors du chargement de l'image", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (validateForm()) {
            departement dept = new departement();
            dept.setNom(nomField.getText());
            dept.setAdresse(adresseField.getText());
            dept.setImage(imagePath);

            departementService.addDepartement(dept);
            loadDepartements();
            clearForm();
            showAlert("Succès", "Département enregistré avec succès", Alert.AlertType.INFORMATION);
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (nomField.getText().isEmpty()) {
            nomError.setText("Le nom est obligatoire");
            isValid = false;
        } else {
            nomError.setText("");
        }

        if (adresseField.getText().isEmpty()) {
            adresseError.setText("L'adresse est obligatoire");
            isValid = false;
        } else {
            adresseError.setText("");
        }

        return isValid;
    }

    // Navigation methods
    @FXML
    private void handleSalleNavigation() {
        updateActiveMenuItem("Salles");
        // Code to load Salle view
    }

    @FXML
    private void handleEtageNavigation() {
        updateActiveMenuItem("Étages");
        // Code to load Etage view
    }

    @FXML
    private void handleDepartementNavigation() {
        updateActiveMenuItem("Départements");
    }

    @FXML
    private void handleAbout() {
        showAlert("À propos", "Gestion des Départements\nVersion 1.0", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleQuit() {
        System.exit(0);
    }

    @FXML
    private void handleReset() {
        clearForm();
    }

    private void updateActiveMenuItem(String menuText) {
        Menu navigationMenu = menuBar.getMenus().get(1);
        for (MenuItem item : navigationMenu.getItems()) {
            if (item.getText().equals(menuText)) {
                item.getStyleClass().add("active");
            } else {
                item.getStyleClass().remove("active");
            }
        }
    }
}