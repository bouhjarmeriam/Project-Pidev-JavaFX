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
        File imageDir = new File(IMAGE_DIR);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
    }

    private void setupTable() {
        // Configuration des largeurs de colonnes
        nomColumn.setPrefWidth(200);
        adresseColumn.setPrefWidth(250);
        imageColumn.setPrefWidth(150);
        actionsColumn.setPrefWidth(150);

        // Configuration des colonnes de données
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

        // Configuration améliorée de la colonne image
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

        // Configuration améliorée de la colonne d'actions
        actionsColumn.setCellFactory(column -> new TableCell<departement, Void>() {
            private final HBox buttons = new HBox(5);
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");

            {
                buttons.setStyle("-fx-alignment: CENTER;");
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                editBtn.setTooltip(new Tooltip("Modifier"));
                deleteBtn.setTooltip(new Tooltip("Supprimer"));

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
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });

        // Style général du tableau
        departementTable.setStyle("-fx-font-size: 14px;");
        departementTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
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
        if (!validateForm()) return;

        departement dept = new departement();
        dept.setNom(nomField.getText());
        dept.setAdresse(adresseField.getText());
        dept.setImage(imagePath);

        departement selected = departementTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            dept.setId(selected.getId());
            departementService.updateDepartement(dept);
            showAlert("Succès", "Département mis à jour", Alert.AlertType.INFORMATION);
        } else {
            departementService.addDepartement(dept);
            showAlert("Succès", "Département ajouté", Alert.AlertType.INFORMATION);
        }

        resetForm();
        loadDepartements();
    }

    private boolean validateForm() {
        boolean isValid = true;
        clearErrors();

        if (nomField.getText().isEmpty()) {
            nomError.setText("Le nom est obligatoire");
            isValid = false;
        }

        if (adresseField.getText().isEmpty()) {
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
        departementTable.getSelectionModel().clearSelection();
        clearErrors();
    }

    private void showEditDialog(departement dept) {
        nomField.setText(dept.getNom());
        adresseField.setText(dept.getAdresse());
        imageField.setText(dept.getImage());

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

        ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            departementService.deleteDepartement(dept);
            loadDepartements();
            showAlert("Succès", "Département supprimé", Alert.AlertType.INFORMATION);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void showDepartements(ActionEvent event) {
        // Already on departement view
    }

    @FXML
    private void showEtages(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/etage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger l'interface des étages", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showSalles(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/salle.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger l'interface des salles", Alert.AlertType.ERROR);
        }
    }

    public void Acceuil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/interface.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger l'interface des salles", Alert.AlertType.ERROR);
        }
    }
}