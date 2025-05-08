module org.example.roar {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires javafx.swing;

    // Java
    requires java.sql;
    requires java.prefs;

    // Jakarta Mail
    requires jakarta.mail;

    // FXML access
    opens controllers to javafx.fxml;
    exports controllers;

    opens entite to javafx.base;
    exports entite;

    opens test to javafx.fxml, javafx.graphics;
    exports test;

    opens org.example.roar to javafx.fxml;
    exports org.example.roar;

    // Pour utiliser MailService ailleurs
    exports service;

    // Déclaration de la dépendance à itextpdf
    requires itextpdf;  // Ajoutez cette ligne pour déclarer la dépendance à itextpdf
}
