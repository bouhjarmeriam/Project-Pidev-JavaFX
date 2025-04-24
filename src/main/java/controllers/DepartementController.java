package controllers;

import entite.departement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.DepartementService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class DepartementController {

    // Champs du formulaire
    @FXML private TextField nomField;
    @FXML private TextField adresseField;
    @FXML private TextField imageField;
    @FXML private ImageView imagePreview;

    // Messages d'erreur
    @FXML private Label nomError;
    @FXML private Label adresseError;
    @FXML private Label imageError;

    // TableView et colonnes
    @FXML private TableView<departement> departementTable;
    @FXML private TableColumn<departement, String> nomColumn;
    @FXML private TableColumn<departement, String> adresseColumn;
    @FXML private TableColumn<departement, String> imageColumn;
    @FXML private TableColumn<departement, Void> actionsColumn;

    // Boutons
    @FXML private Button saveBtn;
    @FXML private Button clearBtn;
    @FXML private Button browseBtn;

    private final DepartementService departementService = new DepartementService();
    private final ObservableList<departement> departementData = FXCollections.observableArrayList();
    private String imagePath = "";
    private static final String IMAGE_DIR = "src/main/resources/images/";

    @FXML
    public void initialize() {
        createImageDirectory();
        setupTable();
        loadDepartements();
    }

    private void createImageDirectory() {
        try {
            File imageDir = new File(IMAGE_DIR);
            if (!imageDir.exists()) {
                boolean created = imageDir.mkdirs();
                if (!created) {
                    System.err.println("Échec de la création du dossier images");
                }
            }
        } catch (SecurityException e) {
            System.err.println("Erreur de permission: " + e.getMessage());
        }
    }

    private void setupTable() {
        // Configuration des colonnes
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

        // Configuration de la colonne image
        imageColumn.setCellFactory(column -> new TableCell<departement, String>() {
            private final ImageView imageView = new ImageView();
            private final Label label = new Label();

            {
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);
                label.setStyle("-fx-text-fill: gray;");
            }

            @Override
            protected void updateItem(String imageName, boolean empty) {
                super.updateItem(imageName, empty);
                setGraphic(null);
                setText(null);

                if (!empty && imageName != null && !imageName.isEmpty()) {
                    try {
                        File imageFile = new File(IMAGE_DIR + imageName);
                        if (imageFile.exists()) {
                            imageView.setImage(new Image(imageFile.toURI().toString()));
                            setGraphic(imageView);
                        } else {
                            label.setText("Image manquante");
                            setGraphic(label);
                        }
                    } catch (Exception e) {
                        label.setText("Erreur");
                        setGraphic(label);
                    }
                }
            }
        });

        // Configuration de la colonne d'actions
        actionsColumn.setCellFactory(column -> new TableCell<departement, Void>() {
            private final HBox buttons = new HBox(5);
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");

            {
                buttons.setStyle("-fx-alignment: CENTER;");
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    departement dept = getTableView().getItems().get(getIndex());
                    showEditDialog(dept);
                });

                deleteBtn.setOnAction(event -> {
                    departement dept = getTableView().getItems().get(getIndex());
                    confirmAndDelete(dept);
                });

                buttons.getChildren().addAll(editBtn, deleteBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });

        departementTable.setStyle("-fx-font-size: 14px;");
    }

    private void loadDepartements() {
        departementData.clear();
        departementData.addAll(departementService.getAllDepartements());
        departementTable.setItems(departementData);
    }

    @FXML
    private void handleBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            try {
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destFile = new File(IMAGE_DIR + fileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                imagePath = fileName;
                imageField.setText(fileName);
                imagePreview.setImage(new Image(destFile.toURI().toString()));
                imageError.setText("");
            } catch (IOException e) {
                showAlert("Erreur", "Impossible de charger l'image: " + e.getMessage(), Alert.AlertType.ERROR);
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

            try {
                departementService.addDepartement(dept);
                showAlert("Succès", "Département ajouté avec succès", Alert.AlertType.INFORMATION);
                resetForm();
                loadDepartements();
            } catch (Exception e) {
                showAlert("Erreur", "Échec de l'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        clearErrors();

        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            nomError.setText("Le nom est obligatoire");
            isValid = false;
        }

        if (adresseField.getText() == null || adresseField.getText().trim().isEmpty()) {
            adresseError.setText("L'adresse est obligatoire");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        nomError.setText("");
        adresseError.setText("");
        imageError.setText("");
    }

    @FXML
    private void handleClear(ActionEvent event) {
        resetForm();
    }

    private void resetForm() {
        nomField.clear();
        adresseField.clear();
        imageField.clear();
        imagePreview.setImage(null);
        imagePath = "";
        clearErrors();
    }

    private void showEditDialog(departement dept) {
        try {
            // Charger le FXML en utilisant getResourceAsStream pour plus de fiabilité
            InputStream fxmlStream = getClass().getResourceAsStream("/editDepartement.fxml");
            if (fxmlStream == null) {
                throw new IOException("Fichier editDepartement.fxml introuvable dans les ressources");
            }

            FXMLLoader loader = new FXMLLoader();
            Parent root = loader.load(fxmlStream);

            // Créer la boîte de dialogue
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier Département");
            dialog.setHeaderText("Modification du département " + dept.getNom());
            dialog.getDialogPane().setContent(root);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Configurer le contrôleur
            EditDepartementController controller = loader.getController();
            controller.setDepartementData(dept);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                departement updatedDept = controller.getUpdatedDepartement();
                departementService.updateDepartement(updatedDept);
                loadDepartements();
                showAlert("Succès", "Département mis à jour", Alert.AlertType.INFORMATION);
            }

        } catch (IOException e) {
            showAlert("Erreur Critique",
                    "Échec du chargement de l'éditeur:\n" + e.getMessage() +
                            "\nVérifiez que editDepartement.fxml existe dans src/main/resources",
                    Alert.AlertType.ERROR);
        }
    }

    private void confirmAndDelete(departement dept) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le département " + dept.getNom());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce département ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                departementService.deleteDepartement(dept);
                loadDepartements();
                showAlert("Succès", "Département supprimé", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Échec de la suppression: " + e.getMessage(), Alert.AlertType.ERROR);
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

    // Navigation
    @FXML
    private void showEtages(ActionEvent event) {
        loadView("/etage.fxml", event);
    }

    @FXML
    private void showSalles(ActionEvent event) {
        loadView("/salle.fxml", event);
    }

    @FXML
    public void Acceuil(ActionEvent event) {
        loadView("/interface.fxml", event);
    }

    private void loadView(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur Critique",
                    "Impossible de charger " + fxmlPath +
                            "\nVérifiez que le fichier existe dans src/main/resources\n" +
                            "Erreur complète: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }
}