package controllers;

import entite.etage;
import entite.departement;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import service.EtageService;

public class ModifierEtageController {
    @FXML private TextField numeroField;
    @FXML private ComboBox<departement> departementCombo;

    private etage etage;
    private EtageService etageService;
    private ObservableList<departement> departements;

    @FXML
    public void initialize() {
        // Configurer le convertisseur pour afficher seulement le nom du département
        departementCombo.setConverter(new StringConverter<departement>() {
            @Override
            public String toString(departement departement) {
                return departement != null ? departement.getNom() : "";
            }

            @Override
            public departement fromString(String string) {
                return departementCombo.getItems().stream()
                        .filter(d -> d.getNom().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Configurer l'affichage des éléments dans la liste déroulante
        departementCombo.setCellFactory(param -> new ListCell<departement>() {
            @Override
            protected void updateItem(departement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());
                }
            }
        });
    }

    public void setEtageData(etage etage) {
        this.etage = etage;
        numeroField.setText(String.valueOf(etage.getNumero()));
        departementCombo.getSelectionModel().select(etage.getDepartement());
    }

    public void setDepartements(ObservableList<departement> departements) {
        this.departements = departements;
        departementCombo.setItems(departements);
    }

    public void setEtageService(EtageService etageService) {
        this.etageService = etageService;
    }

    public etage getUpdatedEtage() {
        try {
            etage.setNumero(Integer.parseInt(numeroField.getText()));
            etage.setDepartement(departementCombo.getValue());
            return etage;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}