module org.example.roar {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.prefs;
    requires java.desktop;
    // Fix: Open controllers package for JavaFX reflection
    opens controllers to javafx.fxml;
    exports controllers;
    opens entite to javafx.base;
    exports entite;
    // Fix: Ensure JavaFX can access main application package
    opens test to javafx.fxml, javafx.graphics;
    exports test;
}
