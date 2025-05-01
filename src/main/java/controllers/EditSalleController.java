package controllers;

import entite.salle;
import entite.etage;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import service.EtageService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class EditSalleController {
    @FXML private TextField nomField;
    @FXML private TextField capaciteField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Spinner<Integer> prioriteSpinner;
    @FXML private ComboBox<etage> etageCombo;
    @FXML private TextField imageField;
    @FXML private ImageView imagePreview;

    private salle salle;
    private String imagePath;
    private static final String IMAGE_DIR = "src/main/resources/images/";
    private final EtageService etageService = new EtageService();

    @FXML
    public void initialize() {
        // Initialisation des ComboBox
        typeCombo.getItems().addAll("Consultation", "Bloc opératoire", "Réanimation", "Chambre");
        statusCombo.getItems().addAll("Disponible", "Occupée", "En maintenance");

        // Configuration du Spinner
        prioriteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));

        // Chargement des étages
        List<etage> etages = etageService.getAllEtages();
        etageCombo.getItems().addAll(etages);
    }

    public void setSalleData(salle salle) {
        this.salle = salle;
        nomField.setText(salle.getNom());
        capaciteField.setText(String.valueOf(salle.getCapacite()));
        typeCombo.setValue(salle.getType_salle());
        statusCombo.setValue(salle.getStatus());
        prioriteSpinner.getValueFactory().setValue(salle.getPriorite());
        etageCombo.setValue(salle.getEtage());
        imageField.setText(salle.getImage());
        this.imagePath = salle.getImage();

        if (salle.getImage() != null && !salle.getImage().isEmpty()) {
            File imageFile = new File(IMAGE_DIR + salle.getImage());
            if (imageFile.exists()) {
                imagePreview.setImage(new Image(imageFile.toURI().toString()));
            }
        }
    }

    @FXML
    private void handleBrowseImage() {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public salle getUpdatedSalle() {
        salle.setNom(nomField.getText());
        salle.setCapacite(Integer.parseInt(capaciteField.getText()));
        salle.setType_salle(typeCombo.getValue());
        salle.setStatus(statusCombo.getValue());
        salle.setPriorite(prioriteSpinner.getValue());
        salle.setEtage(etageCombo.getValue());
        salle.setImage(imagePath);
        return salle;
    }
}