package controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import entite.departement;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.DepartemntService;
import service.EtageService;
import service.SalleService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @FXML private TableColumn<departement, Integer> nbrEtageColumn;
    @FXML private TableColumn<departement, String> imageColumn;
    @FXML private TableColumn<departement, Void> actionsColumn;

    @FXML private Button saveBtn;
    @FXML private Button clearBtn;
    @FXML private Button browseBtn;
    @FXML private Button statsBtn;
    @FXML private Button exportPdfBtn;

    @FXML private TextField searchField;

    private final DepartemntService departementService = new DepartemntService();
    private final EtageService etageService = new EtageService();
    private final SalleService salleService = new SalleService();
    private final ObservableList<departement> departementData = FXCollections.observableArrayList();
    private final ObservableList<departement> filteredDepartementData = FXCollections.observableArrayList();
    private String imagePath = "";
    public static final String IMAGE_DIR = "src/main/resources/images/";

    @FXML
    public void initialize() {
        createImageDirectory();
        setupTable();
        loadDepartements();
        setupSearch();
    }

    private void createImageDirectory() {
        File imageDir = new File(IMAGE_DIR);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
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
        departementData.addAll(departementService.getAllDepartements());
        filteredDepartementData.setAll(departementData);
        departementTable.setItems(filteredDepartementData);
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
            String lowerCaseFilter = searchText.toLowerCase();
            List<departement> filteredList = departementData.stream()
                    .filter(departement -> {
                        boolean matchesNom = departement.getNom() != null &&
                                departement.getNom().toLowerCase().contains(lowerCaseFilter);
                        boolean matchesAdresse = departement.getAdresse() != null &&
                                departement.getAdresse().toLowerCase().contains(lowerCaseFilter);
                        return matchesNom || matchesAdresse;
                    })
                    .collect(Collectors.toList());
            filteredDepartementData.setAll(filteredList);
        }
    }

    @FXML
    private void showStatisticsDialog(ActionEvent event) {
        try {
            URL fxmlResource = getClass().getResource("/eya.fxml");
            if (fxmlResource == null) {
                throw new IOException("Le fichier stat.fxml n'a pas été trouvé dans le classpath");
            }

            FXMLLoader loader = new FXMLLoader(fxmlResource);
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Statistiques des Départements");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialogStage.setScene(new Scene(root,600,800));
            dialogStage.showAndWait();

        } catch (Exception e) {
            String errorMsg = "Erreur lors du chargement de l'interface:\n"
                    + e.getMessage() + "\n\n"
                    + "Vérifiez que:\n"
                    + "1. Le fichier stat.fxml existe dans src/main/resources/\n"
                    + "2. Le nom du fichier est exact (sensible à la casse)\n"
                    + "3. Vous avez fait un clean/rebuild du projet";

            System.err.println(errorMsg);
            e.printStackTrace();

            showAlert("Erreur Critique", errorMsg, Alert.AlertType.ERROR);
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

        departement departement = new departement();
        departement.setNom(nomField.getText());
        departement.setAdresse(adresseField.getText());
        departement.setImage(imagePath);

        departementService.addDepartement(departement);
        showAlert("Succès", "Département ajouté avec succès", Alert.AlertType.INFORMATION);
        resetForm();
        loadDepartements();
    }

    @FXML
    private void handleExportToPDF(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("departements.pdf");

        File file = fileChooser.showSaveDialog(departementTable.getScene().getWindow());

        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
                Paragraph title = new Paragraph("Liste des Départements", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
                Paragraph date = new Paragraph("Généré le: " + new java.util.Date(), dateFont);
                date.setAlignment(Element.ALIGN_RIGHT);
                date.setSpacingAfter(20);
                document.add(date);

                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                String[] headers = {"Nom", "Adresse", "Nombre d'étages", "Image"};
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);

                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setBackgroundColor(new BaseColor(51, 122, 183));
                    cell.setPadding(5);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                }

                Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                for (departement d : departementTable.getItems()) {
                    table.addCell(new Phrase(d.getNom(), dataFont));
                    table.addCell(new Phrase(d.getAdresse(), dataFont));
                    table.addCell(new Phrase(String.valueOf(d.getNbr_etage()), dataFont));
                    table.addCell(new Phrase(d.getImage() != null ? d.getImage() : "N/A", dataFont));
                }

                document.add(table);

                document.close();

                showAlert("Succès", "Export PDF réalisé avec succès", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de l'export PDF: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showEditDialog(departement departement) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier Département");
            dialog.setHeaderText("Modification du département " + departement.getNom());

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editDepartement.fxml"));
            Parent root = loader.load();

            EditDepartementController controller = loader.getController();
            controller.setDepartementData(departement);

            dialog.getDialogPane().setContent(root);
            dialog.getDialogPane().setPrefSize(600, 500);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                departement updatedDepartement = controller.getUpdatedDepartement();
                departementService.updateDepartement(updatedDepartement);
                loadDepartements();
                showAlert("Succès", "Département mis à jour avec succès", Alert.AlertType.INFORMATION);
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void confirmAndDelete(departement departement) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le département " + departement.getNom());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce département ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            departementService.deleteDepartement(departement.getId());
            loadDepartements();
            showAlert("Succès", "Département supprimé avec succès", Alert.AlertType.INFORMATION);
        }
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
        loadView("/departement.fxml", event);
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
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la vue: " + fxmlPath, Alert.AlertType.ERROR);
        }
    }
}