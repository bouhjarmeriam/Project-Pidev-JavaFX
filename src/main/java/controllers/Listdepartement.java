package controllers;

import entite.departement;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.List;

public class Listdepartement {

    @FXML
    private TextField searchBar;

    @FXML
    private FlowPane departementContainer;

    @FXML
    private VBox mainContainer;

    private List<departement> departements = new ArrayList<>();

    @FXML
    public void initialize() {
        // Initialize sample data
        loadDepartements();

        // Set up search functionality
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filterDepartements(newValue.toLowerCase());
        });

        // Display initial departments
        displayDepartements(departements);
    }

    private void loadDepartements() {
        // In a real application, this would come from a service or database
        departements.add(new departement(
                "Département Informatique",
                "123 Rue de la Technologie, Paris",
                "/img/blog1.jpg"
        ));

        departements.add(new departement(
                "Département Médecine",
                "456 Avenue de la Santé, Lyon",
                "/img/blog1.jpg"
        ));


    }

    private void displayDepartements(List<departement> depts) {
        departementContainer.getChildren().clear();

        if (depts.isEmpty()) {
            Label noResults = new Label("Aucun département disponible pour le moment.");
            noResults.setStyle("-fx-text-fill: #6c757d;");
            departementContainer.getChildren().add(noResults);
            return;
        }

        for (departement dept : depts) {
            departementContainer.getChildren().add(createDepartementCard(dept));
        }
    }

    private VBox createDepartementCard(departement dept) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(15)); // Fixed: Using javafx.geometry.Insets
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(300);
        card.setPrefHeight(350);

        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image(getClass().getResourceAsStream(dept.getImage())));
        } catch (Exception e) {
            // Fallback image
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default.jpg")));
        }
        imageView.setFitWidth(270);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(false);

        Label title = new Label(dept.getNom());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18)); // Fixed: Using javafx.scene.text.Font
        title.setTextFill(Color.DODGERBLUE); // Fixed: Using javafx.scene.paint.Color

        HBox addressBox = new HBox(5);
        Label addressIcon = new Label("📍");
        Label address = new Label(dept.getAdresse());
        address.setStyle("-fx-text-fill: #6c757d;");
        addressBox.getChildren().addAll(addressIcon, address);

        card.getChildren().addAll(imageView, title, addressBox);

        card.setOnMouseClicked(e -> {
            handleDepartmentSelection(dept);
        });

        return card;
    }

    private void filterDepartements(String filter) {
        if (filter.isEmpty()) {
            displayDepartements(departements);
            return;
        }

        List<departement> filtered = new ArrayList<>();
        for (departement dept : departements) {
            if (dept.getNom().toLowerCase().contains(filter)) {
                filtered.add(dept);
            }
        }

        displayDepartements(filtered);
    }


    private void handleDepartmentSelection(departement dept) {
        // Handle what happens when a department is clicked
        System.out.println("Selected department: " + dept.getNom());

        // You would typically:
        // 1. Load a new FXML with department details
        // 2. Pass the selected department to the new controller
        // 3. Show the new scene

        // Example:
        // try {
        //     FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/departmentDetails.fxml"));
        //     Parent root = loader.load();
        //     DepartmentDetailsController controller = loader.getController();
        //     controller.setDepartment(dept);
        //     Stage stage = (Stage) mainContainer.getScene().getWindow();
        //     stage.setScene(new Scene(root));
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }

    public void initializeData(List<departement> departements) {
        this.departements = departements;
        // Rafraîchir l'affichage
        displayDepartements(departements);
    }
}

