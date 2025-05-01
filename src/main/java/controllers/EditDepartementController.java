package controllers;

import entite.departement;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class EditDepartementController {
    @FXML private TextField nomField;
    @FXML private TextField adresseField;
    @FXML private TextField imageField;
    @FXML private ImageView imagePreview;

    private departement departement;
    private String imagePath;
    private static final String IMAGE_DIR = "src/main/resources/images/";

    @FXML
    public void initialize() {
        // Initialisation si nécessaire
    }

    public void setDepartementData(departement departement) {
        this.departement = departement;
        nomField.setText(departement.getNom());
        adresseField.setText(departement.getAdresse());
        imageField.setText(departement.getImage());
        this.imagePath = departement.getImage();

        if (departement.getImage() != null && !departement.getImage().isEmpty()) {
            File imageFile = new File(IMAGE_DIR + departement.getImage());
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

    public departement getUpdatedDepartement() {
        departement.setNom(nomField.getText());
        departement.setAdresse(adresseField.getText());
        departement.setImage(imagePath);
        return departement;
    }
}