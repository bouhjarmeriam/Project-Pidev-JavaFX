package controllers;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;
import entite.CaptchaGenerator;

import java.io.File;
import java.io.IOException;

public class CaptchaController {
    @FXML
    private ImageView captchaImageView;
    @FXML
    private TextField captchaInputField;

    private String correctCaptcha;

    @FXML
    public void initialize() {
        generateNewCaptcha();
    }

    public void generateNewCaptcha() {
        try {
            correctCaptcha = CaptchaGenerator.generateCaptchaText();
            File captchaFile = CaptchaGenerator.generateCaptchaImage(correctCaptcha);
            captchaImageView.setImage(new Image(captchaFile.toURI().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void verifyCaptcha() {
        String userInput = captchaInputField.getText().trim();
        if (userInput.equals(correctCaptcha)) {
            showAlert("Succès", "Captcha correct !");
        } else {
            showAlert("Erreur", "Captcha incorrect. Réessayez !");
            generateNewCaptcha(); // Générer un nouveau captcha après une mauvaise réponse
        }
        captchaInputField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

