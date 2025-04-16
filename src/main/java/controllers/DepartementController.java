package controllers;

import entite.departement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.scene.layout.GridPane;
import service.DepartementService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.net.URL;

public class DepartementController {

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

    @FXML
    public void initialize() {
        setupTableColumns();
        loadDepartements();
    }

    private void setupTableColumns() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));

        imageColumn.setCellFactory(col -> new TableCell<departement, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image image = loadImageFromPath(imagePath);
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        System.err.println("Error loading image: " + e.getMessage());
                        setGraphic(new Label("Image non trouvée"));
                    }
                }
            }
        });

        addActionButtonsToTable();
    }

    private Image loadImageFromPath(String imagePath) {
        // Essayer de charger depuis le système de fichiers
        File file = new File(imagePath);
        if (file.exists()) {
            return new Image(file.toURI().toString(), 50, 50, true, true);
        }

        // Essayer de charger depuis les ressources
        String resourcePath = imagePath.startsWith("images/") ? imagePath : "images/" + imagePath;
        URL imageUrl = getClass().getClassLoader().getResource(resourcePath);
        if (imageUrl != null) {
            return new Image(imageUrl.toString(), 50, 50, true, true);
        }

        // Retourner une image vide si non trouvée
        return null;
    }

    private void addActionButtonsToTable() {
        modifierColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Modifier");
            {
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        supprimerColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Supprimer");
            {
                btn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                btn.setOnAction(e -> confirmAndDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void loadDepartements() {
        departementData.setAll(departementService.getAllDepartements());
        departementTable.setItems(departementData);
    }

    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            imageField.setText(imagePath);
            imagePreview.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void handleSave() {
        if (validateForm()) {
            try {
                String savedImagePath = saveImageToProject();
                departement d = new departement(
                        nomField.getText().trim(),
                        adresseField.getText().trim(),
                        savedImagePath
                );
                departementService.addDepartement(d);
                showAlert("Département ajouté !", Alert.AlertType.INFORMATION);
                clearForm();
                loadDepartements();
            } catch (IOException e) {
                showAlert("Erreur lors de la sauvegarde de l'image", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        if (nomField.getText().trim().isEmpty()) {
            nomError.setText("Le nom est requis !");
            valid = false;
        } else {
            nomError.setText("");
        }

        if (adresseField.getText().trim().isEmpty()) {
            adresseError.setText("L'adresse est requise !");
            valid = false;
        } else {
            adresseError.setText("");
        }

        if (imagePath.isEmpty()) {
            imageError.setText("Veuillez sélectionner une image !");
            valid = false;
        } else {
            imageError.setText("");
        }

        return valid;
    }

    private String saveImageToProject() throws IOException {
        // Créer le dossier images dans resources s'il n'existe pas
        File resourcesDir = new File("src/main/resources/images");
        if (!resourcesDir.exists()) {
            resourcesDir.mkdirs();
        }

        File originalFile = new File(imagePath);
        String newFileName = "dep_" + System.currentTimeMillis() + getFileExtension(originalFile);
        File destFile = new File(resourcesDir, newFileName);

        Files.copy(originalFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return "images/" + newFileName; // Retourne le chemin relatif
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return (lastDot == -1) ? "" : name.substring(lastDot);
    }

    private void showEditDialog(departement d) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier Département");

        GridPane grid = createEditForm(d);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            updateDepartement(d, grid);
        }
    }

    private GridPane createEditForm(departement d) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nomField = new TextField(d.getNom());
        TextField adresseField = new TextField(d.getAdresse());
        TextField imageField = new TextField(d.getImage());
        imageField.setEditable(false);

        ImageView imagePreview = new ImageView();
        imagePreview.setFitHeight(100);
        imagePreview.setFitWidth(100);
        loadPreviewImage(d.getImage(), imagePreview);

        Button browseBtn = new Button("Parcourir");
        browseBtn.setOnAction(e -> handleImageBrowse(imageField, imagePreview));

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Adresse:"), 0, 1);
        grid.add(adresseField, 1, 1);
        grid.add(new Label("Image:"), 0, 2);
        grid.add(imageField, 1, 2);
        grid.add(browseBtn, 2, 2);
        grid.add(imagePreview, 1, 3);

        return grid;
    }

    private void loadPreviewImage(String imagePath, ImageView imageView) {
        try {
            // Essayer de charger depuis le système de fichiers
            File file = new File(imagePath);
            if (file.exists()) {
                imageView.setImage(new Image(file.toURI().toString()));
                return;
            }

            // Essayer de charger depuis les ressources
            String resourcePath = imagePath.startsWith("images/") ? imagePath : "images/" + imagePath;
            URL imageUrl = getClass().getClassLoader().getResource(resourcePath);
            if (imageUrl != null) {
                imageView.setImage(new Image(imageUrl.toString()));
            } else {
                System.err.println("Image not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading preview image: " + e.getMessage());
        }
    }

    private void handleImageBrowse(TextField imageField, ImageView imagePreview) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            imageField.setText(selectedFile.getAbsolutePath());
            imagePreview.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    private void updateDepartement(departement d, GridPane grid) {
        try {
            String newNom = ((TextField)grid.getChildren().get(1)).getText().trim();
            String newAdresse = ((TextField)grid.getChildren().get(3)).getText().trim();
            String newImagePath = ((TextField)grid.getChildren().get(5)).getText().trim();

            // Si nouvelle image sélectionnée
            if (!newImagePath.equals(d.getImage())) {
                // Vérifier si c'est un nouveau fichier ou déjà dans resources
                if (newImagePath.startsWith("images/")) {
                    // L'image est déjà dans resources
                } else {
                    // Sauvegarder la nouvelle image
                    File originalFile = new File(newImagePath);
                    if (originalFile.exists()) {
                        File resourcesDir = new File("src/main/resources/images");
                        String newFileName = "dep_" + System.currentTimeMillis() + getFileExtension(originalFile);
                        File destFile = new File(resourcesDir, newFileName);
                        Files.copy(originalFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        newImagePath = "images/" + newFileName;
                    }
                }
            }

            d.setNom(newNom);
            d.setAdresse(newAdresse);
            d.setImage(newImagePath);

            departementService.updateDepartement(d);
            showAlert("Département mis à jour !", Alert.AlertType.INFORMATION);
            loadDepartements();
        } catch (IOException e) {
            showAlert("Erreur lors de la mise à jour de l'image", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void confirmAndDelete(departement d) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer ce département ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            departementService.deleteDepartement(d.getId());
            showAlert("Département supprimé !", Alert.AlertType.INFORMATION);
            loadDepartements();
        }
    }

    private void clearForm() {
        nomField.clear();
        adresseField.clear();
        imageField.clear();
        imagePreview.setImage(null);
        imagePath = "";
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}