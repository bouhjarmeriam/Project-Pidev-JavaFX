module org.example.roar {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires jbcrypt;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires java.prefs;
    requires jakarta.persistence;
    // Fix: Open controllers package for JavaFX reflection
    opens controllers to javafx.fxml;
    exports controllers;
    opens entite to javafx.base;
    exports entite;
    // Fix: Ensure JavaFX can access main application package
    opens test to javafx.fxml, javafx.graphics;
    exports test;
}
