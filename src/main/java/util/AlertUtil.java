package util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Utility class for displaying alert messages
 */
public class AlertUtil {
    
    /**
     * Show an information alert
     * @param owner the owner window
     * @param title the title of the alert
     * @param message the message to display
     */
    public static void showInformation(Window owner, String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }
    
    /**
     * Show an error alert
     * @param owner the owner window
     * @param title the title of the alert
     * @param message the message to display
     */
    public static void showError(Window owner, String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }
    
    /**
     * Show a warning alert
     * @param owner the owner window
     * @param title the title of the alert
     * @param message the message to display
     */
    public static void showWarning(Window owner, String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.showAndWait();
    }
    
    /**
     * Show a confirmation dialog
     * @param owner the owner window
     * @param title the title of the dialog
     * @param message the message to display
     * @return true if the user confirmed, false otherwise
     */
    public static boolean showConfirmation(Window owner, String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
} 