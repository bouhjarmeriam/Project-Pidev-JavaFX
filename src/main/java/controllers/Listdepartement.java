package controllers;

import entite.departement;
import entite.etage;
import entite.salle;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import service.DepartemntService;
import service.EtageService;
import service.SalleService;

import java.io.InputStream;
import java.util.List;

public class Listdepartement {

    private final DepartemntService departementService = new DepartemntService();
    private final EtageService etageService = new EtageService();
    private final SalleService salleService = new SalleService();

    @FXML private FlowPane departementContainer;
    @FXML private TextField searchBar;

    @FXML
    public void initialize() {
        loadAllDepartments();
        searchBar.textProperty().addListener((obs, oldVal, newVal) -> searchDepartments(newVal));

        departementContainer.setHgap(20);
        departementContainer.setVgap(20);
        departementContainer.setPadding(new Insets(15));
    }

    private void loadAllDepartments() {
        departementContainer.getChildren().clear();
        List<departement> allDepartments = departementService.getAllDepartements();
        for (departement dept : allDepartments) {
            departementContainer.getChildren().add(createDepartmentCard(dept));
        }
    }

    private Node createDepartmentCard(departement department) {
        VBox container = new VBox(10); // Nouveau conteneur pour département + étages
        container.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10);
        card.getStyleClass().add("department-card");
        card.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView();
        loadDepartmentImage(imageView, department.getImage());
        imageView.setFitWidth(150);
        imageView.setFitHeight(100);

        Label title = new Label(department.getNom());
        title.getStyleClass().add("department-card-title");

        int floorCount = etageService.getEtagesByDepartement(department.getId()).size();
        Label details = new Label(String.format("%d étages • %s", floorCount, department.getAdresse()));
        details.getStyleClass().add("department-card-details");

        VBox floorsContainerForThisDepartment = new VBox(10);
        floorsContainerForThisDepartment.setPadding(new Insets(10, 20, 10, 50));
        floorsContainerForThisDepartment.setVisible(false);

        card.getChildren().addAll(imageView, title, details);

        card.setOnMouseClicked(e -> {
            if (floorsContainerForThisDepartment.isVisible()) {
                floorsContainerForThisDepartment.setVisible(false);
                floorsContainerForThisDepartment.getChildren().clear();
            } else {
                floorsContainerForThisDepartment.setVisible(true);
                showFloorsUnderDepartment(department, floorsContainerForThisDepartment);
            }
        });

        container.getChildren().addAll(card, floorsContainerForThisDepartment);
        return container;
    }

    private void showFloorsUnderDepartment(departement department, VBox floorsContainer) {
        floorsContainer.getChildren().clear();
        List<etage> floors = etageService.getEtagesByDepartement(department.getId());

        if (floors.isEmpty()) {
            Label noFloorsLabel = new Label("Ce département n'a pas encore d'étages");
            noFloorsLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
            floorsContainer.getChildren().add(noFloorsLabel);
        } else {
            for (etage floor : floors) {
                VBox floorContainer = new VBox(5);

                Node floorCard = createFloorCard(floor);
                VBox roomsContainer = new VBox(5);
                roomsContainer.setPadding(new Insets(5, 20, 5, 50));
                roomsContainer.setVisible(false);

                floorCard.setOnMouseClicked(event -> {
                    if (roomsContainer.isVisible()) {
                        roomsContainer.setVisible(false);
                        roomsContainer.getChildren().clear();
                    } else {
                        roomsContainer.setVisible(true);
                        loadRoomsUnderFloor(floor, roomsContainer);
                    }
                });

                floorContainer.getChildren().addAll(floorCard, roomsContainer);
                floorsContainer.getChildren().add(floorContainer);
            }
        }
    }

    private void loadRoomsUnderFloor(etage floor, VBox roomsContainer) {
        roomsContainer.getChildren().clear();
        List<salle> rooms = salleService.getAll().stream()
                .filter(r -> r.getEtage() != null && r.getEtage().getId() == floor.getId())
                .toList();

        if (rooms.isEmpty()) {
            Label noRoomsLabel = new Label("Aucune salle sur cet étage");
            noRoomsLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 13px;");
            roomsContainer.getChildren().add(noRoomsLabel);
        } else {
            for (salle room : rooms) {
                roomsContainer.getChildren().add(createRoomCard(room));
            }
        }
    }

    private Node createFloorCard(etage floor) {
        VBox card = new VBox(8);
        card.getStyleClass().add("floor-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);

        SVGPath icon = new SVGPath();
        icon.setContent("M19,3H5C3.9,3,3,3.9,3,5v14c0,1.1,0.9,2,2,2h14c1.1,0,2-0.9,2-2V5C21,3.9,20.1,3,19,3z M19,19H5V5h14V19z");
        icon.setStyle("-fx-fill: #4a90e2;");

        Label title = new Label("Étage " + floor.getNumero());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        long roomCount = salleService.getAll().stream()
                .filter(r -> r.getEtage() != null && r.getEtage().getId() == floor.getId())
                .count();

        Label details = new Label(roomCount + (roomCount > 1 ? " salles" : " salle"));
        details.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 13px;");

        card.getChildren().addAll(icon, title, details);
        return card;
    }

    private Node createRoomCard(salle room) {
        VBox card = new VBox(5);
        card.getStyleClass().add("room-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(150);

        ImageView imageView = new ImageView();
        loadRoomImage(imageView, room.getImage());
        imageView.setFitWidth(120);
        imageView.setFitHeight(80);

        Label title = new Label(room.getNom());
        title.setStyle("-fx-font-weight: bold; -fx-text-alignment: center;");

        Label details = new Label(
                String.format("%s\n%d places\n%s",
                        room.getType_salle(),
                        room.getCapacite(),
                        room.getStatus())
        );
        details.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12px; -fx-text-alignment: center;");

        card.getChildren().addAll(imageView, title, details);
        return card;
    }

    private void loadDepartmentImage(ImageView imageView, String imageName) {
        try {
            InputStream is = getClass().getResourceAsStream("/images/" + imageName);
            if (is != null) {
                imageView.setImage(new Image(is, 150, 100, true, true));
                return;
            }
            InputStream defaultIs = getClass().getResourceAsStream("/images/default_dept.png");
            if (defaultIs != null) {
                imageView.setImage(new Image(defaultIs, 150, 100, true, true));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRoomImage(ImageView imageView, String imageName) {
        try {
            InputStream is = getClass().getResourceAsStream("/images/" + imageName);
            if (is != null) {
                imageView.setImage(new Image(is, 120, 80, true, true));
                return;
            }
            InputStream defaultIs = getClass().getResourceAsStream("/images/default_room.png");
            if (defaultIs != null) {
                imageView.setImage(new Image(defaultIs, 120, 80, true, true));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchDepartments(String searchTerm) {
        departementContainer.getChildren().clear();
        List<departement> results = (searchTerm == null || searchTerm.isEmpty())
                ? departementService.getAllDepartements()
                : departementService.searchDepartements(searchTerm);
        for (departement dept : results) {
            departementContainer.getChildren().add(createDepartmentCard(dept));
        }
    }
}
