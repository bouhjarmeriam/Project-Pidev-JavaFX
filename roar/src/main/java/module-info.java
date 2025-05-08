module org.example.roar {
    requires javafx.controls;
    requires javafx.fxml;
    requires jbcrypt;
    requires com.fasterxml.jackson.databind;
    requires jakarta.mail;
    requires java.prefs;
    requires jakarta.persistence;
    requires mysql.connector.j;
    requires com.google.protobuf;
    requires javafx.swing;
    requires org.json;
    // Fix: Open controllers package for JavaFX reflection
    opens controllers to javafx.fxml;
    exports controllers;
    opens entite to javafx.base;
    exports entite;
    // Fix: Ensure JavaFX can access main application package
    opens test to javafx.fxml, javafx.graphics;

    exports test;
}
