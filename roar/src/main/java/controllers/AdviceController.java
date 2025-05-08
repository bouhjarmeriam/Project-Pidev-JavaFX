package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class AdviceController {

    @FXML
    private Button btnGetAdvice;

    @FXML
    private Label adviceLabel;

    @FXML
    private void initialize() {
        btnGetAdvice.setOnAction(event -> getAdvice());
    }

    private void getAdvice() {
        try {
            URL url = new URL("https://api.adviceslip.com/advice");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                responseBuilder.append(inputLine);
            }
            in.close();

            JSONObject json = new JSONObject(responseBuilder.toString());
            String advice = json.getJSONObject("slip").getString("advice");

            // Afficher dans le Label
            adviceLabel.setText("Conseil : " + advice);

        } catch (Exception e) {
            showError("Erreur", "Impossible de récupérer un conseil : " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
