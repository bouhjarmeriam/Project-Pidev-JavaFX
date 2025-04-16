package controllers;

import entite.departement;
import entite.etage;
import entite.salle;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.geometry.Pos;
import service.DepartementService;
import service.EtageService;
import service.SalleService;

import java.io.File;
import java.util.List;

public class Listdepartement {

    private final DepartementService departementService = new DepartementService();
    private final EtageService etageService = new EtageService();
    private final SalleService salleService = new SalleService();

    @FXML private FlowPane departementContainer;
    @FXML private FlowPane floorContainer;
    @FXML private FlowPane roomContainer;
    @FXML private VBox floorsContainer;
    @FXML private VBox roomsContainer;
    @FXML private Button backButton;
    @FXML private TextField searchBar;

    private departement selectedDepartment;
    private etage selectedFloor;

    @FXML
    public void initialize() {
        loadAllDepartments(); // Charger tous les départements
        backButton.setOnAction(e -> handleBack());
        searchBar.textProperty().addListener((obs, oldVal, newVal) -> searchDepartments(newVal));
    }

    private Node createDepartmentCard(departement department) {
        VBox card = new VBox(10);
        card.getStyleClass().add("department-card");

        // Image du département
        ImageView imageView = new ImageView();
        try {
            if (department.getImage() != null && !department.getImage().isEmpty()) {
                String imagePath = "file:" + new File(department.getImage()).getAbsolutePath();
                Image image = new Image(imagePath, 150, 100, true, true);
                imageView.setImage(image);
            } else {
                // Image par défaut
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/default_dept.png")));
            }
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default_dept.png")));
        }
        imageView.setFitWidth(150);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(false);
        imageView.getStyleClass().add("department-image");

        // Titre
        Label title = new Label(department.getNom());
        title.getStyleClass().add("department-card-title");

        // Détails
        int floorCount = etageService.getEtagesByDepartement(department.getId()).size();
        Label details = new Label(String.format("%d étages • %s", floorCount, department.getAdresse()));
        details.getStyleClass().add("department-card-details");

        card.getChildren().addAll(imageView, title, details);
        card.setAlignment(Pos.CENTER);
        card.setOnMouseClicked(e -> showDepartmentDetails(department));

        return card;
    }

    private void showDepartmentDetails(departement department) {
        selectedDepartment = department;
        int floorCount = etageService.getEtagesByDepartement(department.getId()).size();

        floorContainer.getChildren().clear();

        if (floorCount > 0) {
            List<etage> floors = etageService.getEtagesByDepartement(department.getId());
            for (etage floor : floors) {
                floorContainer.getChildren().add(createFloorCard(floor));
            }
        } else {
            Label noFloorsLabel = new Label("Ce département n'a pas encore d'étages");
            noFloorsLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
            floorContainer.getChildren().add(noFloorsLabel);
        }

        floorsContainer.setVisible(true);
        floorsContainer.setManaged(true);
        roomsContainer.setVisible(false);
        roomsContainer.setManaged(false);
        backButton.setVisible(true);
    }

    private Node createFloorCard(etage floor) {
        VBox card = new VBox(10);
        card.getStyleClass().add("floor-card");

        SVGPath icon = new SVGPath();
        icon.setContent("M19,3H5C3.9,3,3,3.9,3,5v14c0,1.1,0.9,2,2,2h14c1.1,0,2-0.9,2-2V5C21,3.9,20.1,3,19,3z M19,19H5V5h14V19z");
        icon.getStyleClass().add("card-icon");
        icon.setStyle("-fx-fill: #ff7e5f;");

        Label title = new Label("Étage " + floor.getNumero());
        title.getStyleClass().add("floor-card-title");

        int roomCount = salleService.getAll().stream()
                .filter(r -> r.getEtage() != null && r.getEtage().getId() == floor.getId())
                .toList().size();

        Label details = new Label(String.format("%d salles", roomCount));
        details.setStyle("-fx-text-fill: #888;");

        card.getChildren().addAll(icon, title, details);
        card.setAlignment(Pos.CENTER);
        card.setOnMouseClicked(e -> showRooms(floor));

        return card;
    }

    private Node createRoomCard(salle room) {
        VBox card = new VBox(10);
        card.getStyleClass().add("room-card");

        // Image de la salle
        ImageView imageView = new ImageView();
        try {
            if (room.getImage() != null && !room.getImage().isEmpty()) {
                String imagePath = "file:" + new File(room.getImage()).getAbsolutePath();
                Image image = new Image(imagePath, 120, 80, true, true);
                imageView.setImage(image);
            } else {
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/default_room.png")));
            }
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default_room.png")));
        }
        imageView.setFitWidth(120);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(false);

        Label title = new Label(room.getNom());
        title.getStyleClass().add("room-card-title");

        Label details = new Label(String.format("%s • %d places • %s",
                room.getType_salle(), room.getCapacite(), room.getStatus()));
        details.setStyle("-fx-text-fill: #888;");

        card.getChildren().addAll(imageView, title, details);
        card.setAlignment(Pos.CENTER);

        return card;
    }

    private void showRooms(etage floor) {
        selectedFloor = floor;
        roomContainer.getChildren().clear();

        List<salle> rooms = salleService.getAll().stream()
                .filter(r -> r.getEtage() != null && r.getEtage().getId() == floor.getId())
                .toList();

        if (rooms.isEmpty()) {
            Label noRoomsLabel = new Label("Cet étage n'a pas encore de salles");
            noRoomsLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
            roomContainer.getChildren().add(noRoomsLabel);
        } else {
            for (salle room : rooms) {
                roomContainer.getChildren().add(createRoomCard(room));
            }
        }

        roomsContainer.setVisible(true);
        roomsContainer.setManaged(true);
        backButton.setVisible(true);
    }

    private void handleBack() {
        if (roomsContainer.isVisible()) {
            roomsContainer.setVisible(false);
            roomsContainer.setManaged(false);
        } else if (floorsContainer.isVisible()) {
            floorsContainer.setVisible(false);
            floorsContainer.setManaged(false);
            backButton.setVisible(false);
        }
    }

    private void loadAllDepartments() {
        departementContainer.getChildren().clear();
        List<departement> allDepartments = departementService.getAllDepartements();

        // Afficher tous les départements, avec ou sans étages
        for (departement dept : allDepartments) {
            departementContainer.getChildren().add(createDepartmentCard(dept));
        }
    }

    private void searchDepartments(String searchTerm) {
        departementContainer.getChildren().clear();

        List<departement> results;
        if (searchTerm == null || searchTerm.isEmpty()) {
            results = departementService.getAllDepartements();
        } else {
            results = departementService.searchDepartements(searchTerm);
        }

        for (departement dept : results) {
            departementContainer.getChildren().add(createDepartmentCard(dept));
        }
    }
}