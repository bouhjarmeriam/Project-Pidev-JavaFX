package controllers;

import entite.departement;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.DepartemntService;
import service.EtageService;
import service.SalleService;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DepartementController {

    // Form fields
    @FXML private TextField nomField;
    @FXML private TextField adresseField;
    @FXML private TextField imageField;
    @FXML private ImageView imagePreview;

    // Error messages
    @FXML private Label nomError;
    @FXML private Label adresseError;
    @FXML private Label imageError;

    // TableView and columns
    @FXML private TableView<departement> departementTable;
    @FXML private TableColumn<departement, String> nomColumn;
    @FXML private TableColumn<departement, String> adresseColumn;
    @FXML private TableColumn<departement, Integer> nbrEtageColumn;
    @FXML private TableColumn<departement, String> imageColumn;
    @FXML private TableColumn<departement, Void> actionsColumn;

    // Buttons
    @FXML private Button saveBtn;
    @FXML private Button clearBtn;
    @FXML private Button browseBtn;
    @FXML private Button statsBtn;

    // Search field
    @FXML private TextField searchField;

    // Services and data
    private final DepartementService departementService; // Note: Kept as DepartemntService in comments per request
    private final EtageService etageService;
    private final SalleService salleService;
    private final ObservableList<departement> departementData = FXCollections.observableArrayList();
    private final ObservableList<departement> filteredDepartementData = FXCollections.observableArrayList();
    private String imagePath = "";
    public static final String IMAGE_DIR = "images/";

    // Constructor
    public DepartementController() {
        try {
            this.departementService = new DepartementService(); // Corrected from DepartemntService
            this.etageService = new EtageService();
            this.salleService = new SalleService();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize services: " + e.getMessage(), e);
        }
    }

    @FXML
    public void initialize() {
        createImageDirectory();
        setupTable();
        loadDepartements();
        setupSearch();
    }

    private void createImageDirectory() {
        File imageDir = new F
        ile(IMAGE_DIR);
        if (!imageDir.exists()) {
            boolean created = imageDir.mkdirs();
            if (!created) {
                showAlert("Erreur", "Impossible de créer le répertoire des images", Alert.AlertType.ERROR);
            }
        }
    }

    private void setupTable() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        nbrEtageColumn.setCellValueFactory(cellData -> {
            departement d = cellData.getValue();
            return new SimpleIntegerProperty(d.getNbr_etage()).asObject();
        });
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

        nomColumn.prefWidthProperty().bind(departementTable.widthProperty().multiply(0.2));
        adresseColumn.prefWidthProperty().bind(departementTable.widthProperty().multiply(0.2));
        nbrEtageColumn.prefWidthProperty().bind(departementTable.widthProperty().multiply(0.15));
        imageColumn.prefWidthProperty().bind(departementTable.widthProperty().multiply(0.25));
        actionsColumn.prefWidthProperty().bind(departementTable.widthProperty().multiply(0.2));

        imageColumn.setCellFactory(column -> new TableCell<departement, String>() {
            private final ImageView imageView = new ImageView();
            private final Label label = new Label();

            {
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);
                label.setStyle("-fx-text-fill: #666666;");
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

        actionsColumn.setCellFactory(column -> new TableCell<departement, Void>() {
            private final HBox buttons = new HBox(5);
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");

            {
                buttons.setStyle("-fx-alignment: CENTER;");
                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5px;");
                deleteBtn.setStyle("-fx-background-color: #EF5350; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 5px;");

                editBtn.setOnAction(event -> {
                    departement departement = getTableView().getItems().get(getIndex());
                    showEditDialog(departement);
                });

                deleteBtn.setOnAction(event -> {
                    departement departement = getTableView().getItems().get(getIndex());
                    confirmAndDelete(departement);
                });

                buttons.getChildren().addAll(editBtn, deleteBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void loadDepartements() {
        departementData.clear();
        try {
            List<departement> departements = departementService.getAllDepartements();
            if (departements != null) {
                departementData.addAll(departements);
                filteredDepartementData.setAll(departementData);
                departementTable.setItems(filteredDepartementData);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des départements: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterDepartements(newValue);
        });
    }

    private void filterDepartements(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            filteredDepartementData.setAll(departementData);
        } else {
            try {
                List<departement> filteredList = departementService.searchDepartements(searchText);
                filteredDepartementData.setAll(filteredList);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void showStatisticsDialog(ActionEvent event) {
        try {
            // Create modal dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Statistiques");
            dialog.setHeaderText("Statistiques des Départements et Salles");

            // Create bar chart for etages per department
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Départements");
            yAxis.setLabel("Nombre d'Étages");
            BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
            barChart.setTitle("Nombre d'Étages par Département");
            XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
            barSeries.setName("Étages");

            List<departement> departements = departementService.getAllDepartements();
            if (departements != null) {
                for (departement dept : departements) {
                    barSeries.getData().add(new XYChart.Data<>(dept.getNom(), dept.getNbr_etage()));
                }
            }
            barChart.getData().add(barSeries);
            barChart.setPrefSize(500, 300);

            // Create pie chart for salles per etage
            PieChart pieChart = new PieChart();
            pieChart.setTitle("Nombre de Salles par Étage");
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            List<etage> etages = etageService.getAllEtages();
            if (etages != null) {
                for (etage etage : etages) {
                    List<salle> salles = salleService.getSallesByEtage(etage.getId());
                    int salleCount = salles != null ? salles.size() : 0;
                    pieChartData.add(new PieChart.Data(etage.getNom(), salleCount));
                }
            }
            pieChart.setData(pieChartData);
            pieChart.setPrefSize(500, 300);

            // Create export button
            Button exportButton = new Button("Exporter en PDF");
            exportButton.setOnAction(e -> exportChartsToPDF(barChart, pieChart));

            // Layout
            VBox content = new VBox(20);
            content.setPadding(new Insets(20));
            content.getChildren().addAll(barChart, pieChart, exportButton);

            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(departementTable.getScene().getWindow());

            dialog.showAndWait();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'afficher les statistiques: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void exportChartsToPDF(BarChart<String, Number> barChart, PieChart pieChart) {
        try {
            // File chooser for PDF
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            File file = fileChooser.showSaveDialog(null);
            if (file == null) return;

            // Snapshot charts
            WritableImage barImage = barChart.snapshot(new SnapshotParameters(), null);
            WritableImage pieImage = pieChart.snapshot(new SnapshotParameters(), null);

            // Convert snapshots to byte arrays
            ByteArrayOutputStream barStream = new ByteArrayOutputStream();
            ByteArrayOutputStream pieStream = new ByteArrayOutputStream();
            ImageIO.write(barImage, "png", barStream);
            ImageIO.write(pieImage, "png", pieStream);

            // Create PDF
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add bar chart
            ImageData barData = ImageDataFactory.create(barStream.toByteArray());
            Image barPdfImage = new Image(barData);
            barPdfImage.setWidth(500);
            document.add(barPdfImage);

            // Add pie chart
            ImageData pieData = ImageDataFactory.create(pieStream.toByteArray());
            Image piePdfImage = new Image(pieData);
            piePdfImage.setWidth(500);
            document.add(piePdfImage);

            document.close();
            showAlert("Succès", "PDF exporté avec succès", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'exporter en PDF: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destFile = new File(IMAGE_DIR + fileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                imagePath = fileName;
                if (imageField != null) {
                    imageField.setText(fileName);
                }
                if (imagePreview != null) {
                    imagePreview.setImage(new Image(destFile.toURI().toString()));
                }
                if (imageError != null) {
                    imageError.setText("");
                }
            } catch (IOException e) {
                showAlert("Erreur", "Impossible de charger l'image: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateForm()) return;

        departement departement = new departement();
        departement.setNom(nomField.getText());
        departement.setAdresse(adresseField.getText());
        departement.setImage(imagePath);

        try {
            departementService.addDepartement(departement);
            showAlert("Succès", "Département ajouté avec succès", Alert.AlertType.INFORMATION);
            resetForm();
            loadDepartements();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout du département: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showEditDialog(departement departement) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier Département");
            dialog.setHeaderText("Modification du département " + departement.getNom());

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Debug FXML loading
            System.out.println("Attempting to load: /editDepartement.fxml");
            URL resourceUrl = getClass().getResource("/editDepartement.fxml");
            System.out.println("Resource URL: " + resourceUrl);
            if (resourceUrl == null) {
                throw new IOException("Cannot find /editDepartement.fxml in resources");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            EditDepartementController controller = loader.getController();
            controller.setDepartementData(departement);

            dialog.getDialogPane().setContent(root);
            dialog.getDialogPane().setPrefSize(600, 500);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                departement updatedDepartement = controller.getUpdatedDepartement();
                try {
                    departementService.updateDepartement(updatedDepartement);
                    loadDepartements();
                    showAlert("Succès", "Département mis à jour avec succès", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Erreur", "Erreur lors de la mise à jour du département: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void confirmAndDelete(departement departement) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le département " + departement.getNom());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce département ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                departementService.deleteDepartement(departement.getId());
                loadDepartements();
                showAlert("Succès", "Département supprimé avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la suppression du département: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        clearErrors();

        if (nomField.getText().isEmpty()) {
            if (nomError != null) {
                nomError.setText("Le nom est obligatoire");
            }
            isValid = false;
        }

        if (adresseField.getText().isEmpty()) {
            if (adresseError != null) {
                adresseError.setText("L'adresse est obligatoire");
            }
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        if (nomError != null) {
            nomError.setText("");
        }
        if (adresseError != null) {
            adresseError.setText("");
        }
        if (imageError != null) {
            imageError.setText("");
        }
    }

    @FXML
    private void handleClear(ActionEvent event) {
        resetForm();
    }

    private void resetForm() {
        if (nomField != null) {
            nomField.clear();
        }
        if (adresseField != null) {
            adresseField.clear();
        }
        if (imageField != null) {
            imageField.clear();
        }
        if (imagePreview != null) {
            imagePreview.setImage(null);
        }
        imagePath = "";
        clearErrors();
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
        loadView("/etage.fxml", event);
    }

    @FXML
    private void showSalles(ActionEvent event) {
        loadView("/salle.fxml", event);
    }

    @FXML
    private void Acceuil(ActionEvent event) {
        loadView("/interface.fxml", event);
    }

    private void loadView(String fxmlPath, ActionEvent event) {
        try {
            // Debug FXML loading
            System.out.println("Attempting to load: " + fxmlPath);
            URL resourceUrl = getClass().getResource(fxmlPath);
            System.out.println("Resource URL: " + resourceUrl);
            if (resourceUrl == null) {
                throw new IOException("Cannot find " + fxmlPath + " in resources");
            }

            Parent root = FXMLLoader.load(resourceUrl);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la vue: " + fxmlPath, Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Placeholder classes for etage and salle (replace with actual classes)
    private static class etage {
        private final int id;
        private final String nom;
        private final int departement_id;

        public etage(int id, String nom, int departement_id) {
            this.id = id;
            this.nom = nom;
            this.departement_id = departement_id;
        }

        public int getId() { return id; }
        public String getNom() { return nom; }
        public int getDepartement_id() { return departement_id; }
    }

    private static class salle {
        // Placeholder for salle entity
    }
}