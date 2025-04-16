package controllers;

import entite.salle;
import entite.etage;
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
import service.EtageService;
import service.SalleService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

public class SalleController {

    // Champs du formulaire
    @FXML private TextField nomField;
    @FXML private TextField capaciteField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Spinner<Integer> prioriteSpinner;
    @FXML private ComboBox<etage> etageCombo;
    @FXML private TextField imageField;
    @FXML private ImageView imagePreview;

    // Messages d'erreur
    @FXML private Label nomError;
    @FXML private Label capaciteError;
    @FXML private Label typeError;
    @FXML private Label statusError;
    @FXML private Label prioriteError;
    @FXML private Label etageError;
    @FXML private Label imageError;

    // TableView et colonnes
    @FXML private TableView<salle> salleTable;
    @FXML private TableColumn<salle, String> nomColumn;
    @FXML private TableColumn<salle, Integer> capaciteColumn;
    @FXML private TableColumn<salle, String> typeColumn;
    @FXML private TableColumn<salle, String> statusColumn;
    @FXML private TableColumn<salle, Integer> prioriteColumn;
    @FXML private TableColumn<salle, etage> etageColumn;
    @FXML private TableColumn<salle, String> imageColumn;
    @FXML private TableColumn<salle, Void> actionsColumn;

    // Boutons
    @FXML private Button saveBtn;
    @FXML private Button clearBtn;
    @FXML private Button browseBtn;

    // Services et données
    private final SalleService salleService = new SalleService();
    private final EtageService etageService = new EtageService();
    private final ObservableList<salle> salleData = FXCollections.observableArrayList();
    private String imagePath = "";
    private static final String IMAGE_DIR = "src/main/resources/images/";

    @FXML
    public void initialize() {
        createImageDirectory();
        setupForm();
        setupTable();
        loadSalles();
    }

    private void createImageDirectory() {
        File imageDir = new File(IMAGE_DIR);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
    }

    private void setupForm() {
        // Configuration des ComboBox
        typeCombo.setItems(FXCollections.observableArrayList(
                "Consultation", "Bloc opératoire", "Réanimation", "Chambre"));
        statusCombo.setItems(FXCollections.observableArrayList(
                "Disponible", "Occupée", "En maintenance"));

        // Configuration du Spinner pour la priorité (1-10)
        prioriteSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));

        // Chargement des étages
        List<etage> etages = etageService.getAllEtages();
        etageCombo.setItems(FXCollections.observableArrayList(etages));
    }

    private void setupTable() {
        // Configuration des largeurs de colonnes
        nomColumn.setPrefWidth(150);
        capaciteColumn.setPrefWidth(80);
        typeColumn.setPrefWidth(150);
        statusColumn.setPrefWidth(100);
        prioriteColumn.setPrefWidth(80);
        etageColumn.setPrefWidth(80);
        imageColumn.setPrefWidth(150);
        actionsColumn.setPrefWidth(150);

        // Configuration des colonnes de données
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        capaciteColumn.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type_salle"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        prioriteColumn.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        etageColumn.setCellValueFactory(new PropertyValueFactory<>("etage"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

        // Configuration de la colonne image améliorée
        imageColumn.setCellFactory(column -> new TableCell<salle, String>() {
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

        // Configuration de la colonne d'étage améliorée
        etageColumn.setCellFactory(column -> new TableCell<salle, etage>() {
            @Override
            protected void updateItem(etage etage, boolean empty) {
                super.updateItem(etage, empty);
                if (empty || etage == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(etage.getNumero()));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        // Configuration améliorée de la colonne d'actions
        actionsColumn.setCellFactory(column -> new TableCell<salle, Void>() {
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
                    salle salle = getTableView().getItems().get(getIndex());
                    showEditDialog(salle);
                });

                deleteBtn.setOnAction(event -> {
                    salle salle = getTableView().getItems().get(getIndex());
                    confirmAndDelete(salle);
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
        salleTable.setStyle("-fx-font-size: 14px;");
        salleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadSalles() {
        salleData.clear();
        salleData.addAll(salleService.getAll());
        salleTable.setItems(salleData);
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

        salle salle = new salle();
        salle.setNom(nomField.getText());
        salle.setCapacite(Integer.parseInt(capaciteField.getText()));
        salle.setType_salle(typeCombo.getValue());
        salle.setStatus(statusCombo.getValue());
        salle.setPriorite(prioriteSpinner.getValue());
        salle.setEtage(etageCombo.getValue());
        salle.setImage(imagePath);

        salle selected = salleTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            salle.setId(selected.getId());
            salleService.updateSalle(salle);
            showAlert("Succès", "Salle mise à jour", Alert.AlertType.INFORMATION);
        } else {
            salleService.addSalle(salle);
            showAlert("Succès", "Salle ajoutée", Alert.AlertType.INFORMATION);
        }

        resetForm();
        loadSalles();
    }

    private boolean validateForm() {
        boolean isValid = true;
        clearErrors();

        // Validation du nom
        if (nomField.getText().isEmpty()) {
            nomError.setText("Le nom est obligatoire");
            isValid = false;
        }

        // Validation de la capacité
        if (capaciteField.getText().isEmpty()) {
            capaciteError.setText("La capacité est obligatoire");
            isValid = false;
        } else {
            try {
                Integer.parseInt(capaciteField.getText());
            } catch (NumberFormatException e) {
                capaciteError.setText("Doit être un nombre valide");
                isValid = false;
            }
        }

        // Validation du type
        if (typeCombo.getValue() == null) {
            typeError.setText("Le type est obligatoire");
            isValid = false;
        }

        // Validation du statut
        if (statusCombo.getValue() == null) {
            statusError.setText("Le statut est obligatoire");
            isValid = false;
        }

        // Validation de l'étage
        if (etageCombo.getValue() == null) {
            etageError.setText("L'étage est obligatoire");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        nomError.setText("");
        capaciteError.setText("");
        typeError.setText("");
        statusError.setText("");
        prioriteError.setText("");
        etageError.setText("");
        imageError.setText("");
    }

    @FXML
    private void handleClear(ActionEvent event) {
        resetForm();
    }

    private void resetForm() {
        nomField.clear();
        capaciteField.clear();
        typeCombo.getSelectionModel().clearSelection();
        statusCombo.getSelectionModel().clearSelection();
        prioriteSpinner.getValueFactory().setValue(1);
        etageCombo.getSelectionModel().clearSelection();
        imageField.clear();
        imagePreview.setImage(null);
        imagePath = "";
        salleTable.getSelectionModel().clearSelection();
        clearErrors();
    }

    private void showEditDialog(salle salle) {
        nomField.setText(salle.getNom());
        capaciteField.setText(String.valueOf(salle.getCapacite()));
        typeCombo.setValue(salle.getType_salle());
        statusCombo.setValue(salle.getStatus());
        prioriteSpinner.getValueFactory().setValue(salle.getPriorite());
        etageCombo.setValue(salle.getEtage());
        imageField.setText(salle.getImage());

        if (salle.getImage() != null && !salle.getImage().isEmpty()) {
            File imageFile = new File(IMAGE_DIR + salle.getImage());
            if (imageFile.exists()) {
                imagePreview.setImage(new Image(imageFile.toURI().toString()));
                imagePath = salle.getImage();
            }
        }
    }

    private void confirmAndDelete(salle salle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la salle");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la salle " + salle.getNom() + "?");

        ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            salleService.deleteSalle(salle.getId());
            loadSalles();
            showAlert("Succès", "Salle supprimée", Alert.AlertType.INFORMATION);
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/departement.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger l'interface des départements", Alert.AlertType.ERROR);
        }
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
        // Already on salle view
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