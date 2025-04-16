package controllers;

import entite.DossierMedicale;
import entite.Sejour;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.SejourService;
import util.AlertUtil;
import util.ImageUploadUtil;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.stage.FileChooser;
import java.util.logging.Level;
import java.util.logging.Logger;
import service.DossierMedicaleService;
import javafx.stage.StageStyle;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.control.Button;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DossierDetailController implements Initializable {
    
    @FXML
    private Label lblId;
    
    @FXML
    private Label lblDateCreation;
    
    @FXML
    private Label lblPatient;
    
    @FXML
    private Label lblMedecin;
    
    @FXML
    private Label lblStatut;
    
    @FXML
    private Label lblHistoriqueMaladies;
    
    @FXML
    private Label lblOperationsPassees;
    
    @FXML
    private Label lblConsultationsPassees;
    
    @FXML
    private Label lblNotes;
    
    @FXML
    private ImageView imageView;
    
    @FXML
    private TableView<Sejour> sejourTable;
    
    @FXML
    private TableColumn<Sejour, String> colDateEntree;
    
    @FXML
    private TableColumn<Sejour, String> colDateSortie;
    
    @FXML
    private TableColumn<Sejour, String> colTypeSejour;
    
    @FXML
    private TableColumn<Sejour, Double> colFraisSejour;
    
    @FXML
    private TableColumn<Sejour, String> colStatutPaiement;
    
    @FXML
    private FontAwesomeIconView placeholderIcon;
    
    @FXML
    private Button btnFullscreenImage;
    
    private DossierMedicale dossier;
    private ObservableList<Sejour> sejourList;
    private SejourService sejourService;
    private DossierMedicaleService dossierService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Logger logger = Logger.getLogger(DossierDetailController.class.getName());
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services
        sejourService = new SejourService();
        dossierService = new DossierMedicaleService();
        
        // Initialize list
        sejourList = FXCollections.observableArrayList();
        
        // Set up séjour table columns
        colDateEntree.setCellValueFactory(cellData -> {
            Sejour sejour = cellData.getValue();
            LocalDateTime date = sejour.getDateEntree();
            return new SimpleStringProperty(date != null ? date.format(DATE_FORMATTER) : "");
        });
        colDateSortie.setCellValueFactory(cellData -> {
            Sejour sejour = cellData.getValue();
            LocalDateTime date = sejour.getDateSortie();
            return new SimpleStringProperty(date != null ? date.format(DATE_FORMATTER) : "");
        });
        colTypeSejour.setCellValueFactory(new PropertyValueFactory<>("typeSejour"));
        colFraisSejour.setCellValueFactory(new PropertyValueFactory<>("fraisSejour"));
        colStatutPaiement.setCellValueFactory(new PropertyValueFactory<>("statutPaiement"));
        
        // Add double-click handler for séjour details
        sejourTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && sejourTable.getSelectionModel().getSelectedItem() != null) {
                handleVoirSejour(new ActionEvent(sejourTable, null));
            }
        });
    }
    
    public void setDossier(DossierMedicale dossier) {
        this.dossier = dossier;
        displayDossierDetails();
        loadSejours();
    }
    
    private void displayDossierDetails() {
        if (dossier == null) {
            System.err.println("Error: dossier is null in displayDossierDetails");
            return;
        }
        
        // For debugging
        System.out.println("Displaying dossier details for ID: " + dossier.getId());
        System.out.println("Historique: " + (dossier.getHistoriqueDesMaladies() != null ? dossier.getHistoriqueDesMaladies() : "null"));
        System.out.println("Operations: " + (dossier.getOperationsPassees() != null ? dossier.getOperationsPassees() : "null"));
        System.out.println("Consultations: " + (dossier.getConsultationsPassees() != null ? dossier.getConsultationsPassees() : "null"));
        System.out.println("Notes: " + (dossier.getNotes() != null ? dossier.getNotes() : "null"));
        
        lblId.setText(String.valueOf(dossier.getId()));
        
        LocalDateTime dateCreation = dossier.getDateDeCreation();
        lblDateCreation.setText(dateCreation != null ? dateCreation.format(DATE_FORMATTER) : "Non défini");
        
        if (dossier.getPatient() != null) {
            lblPatient.setText(dossier.getPatient().getPrenom() + " " + dossier.getPatient().getNom());
        } else {
            lblPatient.setText("Non défini");
        }
        
        if (dossier.getMedecin() != null) {
            lblMedecin.setText(dossier.getMedecin().getPrenom() + " " + dossier.getMedecin().getNom());
        } else {
            lblMedecin.setText("Non défini");
        }
        
        // Use a default text for empty fields
        String defaultText = "Aucune information";
        
        // Handle statut
        String statut = dossier.getStatutDossier();
        lblStatut.setText(statut != null && !statut.trim().isEmpty() ? statut : "Non défini");
        
        // Handle historique
        String historique = dossier.getHistoriqueDesMaladies();
        lblHistoriqueMaladies.setText(historique != null && !historique.trim().isEmpty() ? historique : defaultText);
        
        // Handle operations
        String operations = dossier.getOperationsPassees();
        lblOperationsPassees.setText(operations != null && !operations.trim().isEmpty() ? operations : defaultText);
        
        // Handle consultations
        String consultations = dossier.getConsultationsPassees();
        lblConsultationsPassees.setText(consultations != null && !consultations.trim().isEmpty() ? consultations : defaultText);
        
        // Handle notes
        String notes = dossier.getNotes();
        lblNotes.setText(notes != null && !notes.trim().isEmpty() ? notes : defaultText);
        
        // Display image if available
        displayImage(dossier.getImage());
    }
    
    private void displayImage(String filename) {
        boolean hasImage = false;
        
        if (filename != null && !filename.isEmpty()) {
            String imagePath = null;
            
            // Check if the file is a direct path or needs to be loaded from the upload directory
            if (ImageUploadUtil.imageExists(filename)) {
                imagePath = ImageUploadUtil.getImagePath(filename);
            } else {
                // Check if it's a direct path
                File directFile = new File(filename);
                if (directFile.exists()) {
                    imagePath = filename;
                }
            }
            
            if (imagePath != null) {
                try {
                    Image image = new Image(new File(imagePath).toURI().toString());
                    imageView.setImage(image);
                    imageView.setPreserveRatio(true);
                    imageView.setFitWidth(400);
                    hasImage = true;
                    
                    // Hide placeholder
                    if (placeholderIcon != null) {
                        placeholderIcon.setVisible(false);
                    }
                    
                    // Show fullscreen button when image is available
                    if (btnFullscreenImage != null) {
                        btnFullscreenImage.setVisible(true);
                    }
                    
                    System.out.println("Successfully loaded image: " + imagePath);
                } catch (Exception e) {
                    System.err.println("Error loading image: " + e.getMessage());
                    imageView.setImage(null);
                }
            }
        }
        
        if (!hasImage) {
            imageView.setImage(null);
            
            // Show placeholder
            if (placeholderIcon != null) {
                placeholderIcon.setVisible(true);
            }
            
            // Hide fullscreen button when no image
            if (btnFullscreenImage != null) {
                btnFullscreenImage.setVisible(false);
            }
            
            System.out.println("No image available to display");
        }
    }
    
    private void loadSejours() {
        if (dossier != null) {
            sejourList.setAll(dossier.getSejours());
            sejourTable.setItems(sejourList);
        }
    }
    
    @FXML
    private void handleAjouterSejour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sejour_form.fxml"));
            Parent root = loader.load();
            
            SejourController controller = loader.getController();
            controller.initNewSejour(dossier);
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un Séjour");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.setOnHidden(e -> refreshSejours());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError(((Node) event.getSource()).getScene().getWindow(), 
                    "Erreur", "Impossible d'ouvrir le formulaire de séjour.");
        }
    }
    
    @FXML
    private void handleVoirSejour(ActionEvent event) {
        Sejour selectedSejour = sejourTable.getSelectionModel().getSelectedItem();
        
        if (selectedSejour == null) {
            AlertUtil.showError(((Node) event.getSource()).getScene().getWindow(), 
                    "Erreur", "Veuillez sélectionner un séjour à visualiser.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sejour_detail.fxml"));
            Parent root = loader.load();
            
            SejourDetailController controller = loader.getController();
            controller.setSejour(selectedSejour);
            
            Stage stage = new Stage();
            stage.setTitle("Détails du Séjour #" + selectedSejour.getId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError(((Node) event.getSource()).getScene().getWindow(), 
                    "Erreur", "Impossible d'ouvrir la vue détaillée du séjour.");
        }
    }
    
    @FXML
    private void handleFermer(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
    
    @FXML
    private void handleUpdateImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                // Save the new image and get the filename
                String newImageFilename = ImageUploadUtil.saveImage(selectedFile, selectedFile.getName());
                
                if (newImageFilename != null) {
                    // If there's an existing image, delete it
                    if (dossier.getImage() != null && !dossier.getImage().isEmpty()) {
                        ImageUploadUtil.deleteImage(dossier.getImage());
                    }
                    
                    // Update the dossier with the new image filename
                    dossier.setImage(newImageFilename);
                    
                    // Update the dossier in the database
                    boolean updated = dossierService.modifierDossier(dossier);
                    
                    if (updated) {
                        // Display the new image
                        displayImage(newImageFilename);
                        AlertUtil.showInformation(stage, "Succès", "L'image a été mise à jour avec succès.");
                    } else {
                        AlertUtil.showError(stage, "Erreur", "Impossible de mettre à jour l'image dans la base de données.");
                    }
                } else {
                    AlertUtil.showError(stage, "Erreur", "Impossible de sauvegarder l'image.");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erreur lors de la mise à jour de l'image", e);
                AlertUtil.showError(stage, "Erreur", "Une erreur est survenue lors de la mise à jour de l'image: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleFullscreenImage(ActionEvent event) {
        if (imageView.getImage() == null) {
            return;
        }
        
        try {
            // Create a new stage for fullscreen display
            Stage fullscreenStage = new Stage();
            fullscreenStage.initModality(Modality.APPLICATION_MODAL);
            fullscreenStage.initStyle(StageStyle.UNDECORATED);
            
            // Create a new ImageView with the same image
            ImageView fullImageView = new ImageView(imageView.getImage());
            fullImageView.setPreserveRatio(true);
            
            // Calculate dimensions to fit screen
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double maxWidth = screenBounds.getWidth() * 0.9;
            double maxHeight = screenBounds.getHeight() * 0.9;
            
            if (imageView.getImage().getWidth() > maxWidth || imageView.getImage().getHeight() > maxHeight) {
                if (imageView.getImage().getWidth() / maxWidth > imageView.getImage().getHeight() / maxHeight) {
                    fullImageView.setFitWidth(maxWidth);
                } else {
                    fullImageView.setFitHeight(maxHeight);
                }
            } else {
                // If image is smaller than screen, show at original size
                fullImageView.setFitWidth(imageView.getImage().getWidth());
                fullImageView.setFitHeight(imageView.getImage().getHeight());
            }
            
            // Create a BorderPane to hold the image and a close button
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");
            root.setCenter(fullImageView);
            
            // Add a close button
            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
            closeButton.setOnAction(e -> fullscreenStage.close());
            
            HBox bottomBox = new HBox(closeButton);
            bottomBox.setAlignment(javafx.geometry.Pos.CENTER);
            bottomBox.setPadding(new Insets(20));
            bottomBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
            
            root.setBottom(bottomBox);
            
            // Allow closing on click
            root.setOnMouseClicked(e -> fullscreenStage.close());
            
            // Create scene and show
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            
            fullscreenStage.setScene(scene);
            fullscreenStage.setMaximized(true);
            fullscreenStage.show();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de l'affichage en plein écran", e);
            AlertUtil.showError(((Node) event.getSource()).getScene().getWindow(), 
                    "Erreur", "Impossible d'afficher l'image en plein écran: " + e.getMessage());
        }
    }
    
    private void refreshSejours() {
        if (dossier != null) {
            // Refresh the dossier to get updated séjours
            sejourList.setAll(sejourService.recupererSejoursParDossier(dossier.getId()));
            sejourTable.setItems(sejourList);
        }
    }
} 