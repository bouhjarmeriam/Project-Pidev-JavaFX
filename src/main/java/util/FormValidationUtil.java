package util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for form validation that handles displaying and hiding error messages.
 */
public class FormValidationUtil {

    private final Map<String, HBox> errorBoxes = new HashMap<>();
    private final List<String> errors = new ArrayList<>();

    /**
     * Register an error container for a specific field
     * @param fieldName The name of the field
     * @param errorBox The HBox container for the error message
     */
    public void registerErrorBox(String fieldName, HBox errorBox) {
        errorBoxes.put(fieldName, errorBox);
    }

    /**
     * Clear all error messages and hide error containers
     */
    public void clearAllErrors() {
        errors.clear();
        for (HBox errorBox : errorBoxes.values()) {
            errorBox.setVisible(false);
            errorBox.setManaged(false);
        }
    }

    /**
     * Show an error message for a specific field
     * @param fieldName The name of the field
     * @param errorMessage The error message to display (optional, uses default if null)
     */
    public void showError(String fieldName, String errorMessage) {
        HBox errorBox = errorBoxes.get(fieldName);
        if (errorBox != null) {
            // If a custom error message is provided, update the label
            if (errorMessage != null && !errorMessage.isEmpty()) {
                for (javafx.scene.Node node : errorBox.getChildren()) {
                    if (node instanceof Label) {
                        ((Label) node).setText(errorMessage);
                        break;
                    }
                }
            }

            // Show the error box
            errorBox.setVisible(true);
            errorBox.setManaged(true);

            // Add to errors list
            errors.add(fieldName);
        }
    }

    /**
     * Hide error message for a specific field
     * @param fieldName The name of the field
     */
    public void hideError(String fieldName) {
        HBox errorBox = errorBoxes.get(fieldName);
        if (errorBox != null) {
            errorBox.setVisible(false);
            errorBox.setManaged(false);
            errors.remove(fieldName);
        }
    }

    /**
     * Check if there are any validation errors
     * @return true if there are errors, false otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Get the list of field names with errors
     * @return List of field names with errors
     */
    public List<String> getErrorFields() {
        return new ArrayList<>(errors);
    }

    /**
     * Validate a required text field
     * @param fieldName The name of the field
     * @param textField The text field to validate
     * @param errorMessage Optional error message
     * @return true if valid, false otherwise
     */
    public boolean validateRequiredTextField(String fieldName, TextField textField, String errorMessage) {
        if (textField.getText() == null || textField.getText().trim().isEmpty()) {
            showError(fieldName, errorMessage != null ? errorMessage : "Ce champ est requis");
            return false;
        }
        hideError(fieldName);
        return true;
    }

    /**
     * Validate a numeric text field
     * @param fieldName The name of the field
     * @param textField The text field to validate
     * @param errorMessage Optional error message
     * @return true if valid, false otherwise
     */
    public boolean validateNumericField(String fieldName, TextField textField, String errorMessage) {
        try {
            if (textField.getText() == null || textField.getText().trim().isEmpty()) {
                showError(fieldName, "Ce champ est requis et doit être un nombre");
                return false;
            }

            double value = Double.parseDouble(textField.getText().trim());
            hideError(fieldName);
            return true;
        } catch (NumberFormatException e) {
            showError(fieldName, errorMessage != null ? errorMessage : "Veuillez entrer une valeur numérique valide");
            return false;
        }
    }

    /**
     * Validate a required combo box
     * @param fieldName The name of the field
     * @param comboBox The combo box to validate
     * @param errorMessage Optional error message
     * @return true if valid, false otherwise
     */
    public boolean validateRequiredComboBox(String fieldName, ComboBox<?> comboBox, String errorMessage) {
        if (comboBox.getValue() == null) {
            showError(fieldName, errorMessage != null ? errorMessage : "Veuillez sélectionner une option");
            return false;
        }
        hideError(fieldName);
        return true;
    }

    /**
     * Validate a required date picker
     * @param fieldName The name of the field
     * @param datePicker The date picker to validate
     * @param errorMessage Optional error message
     * @return true if valid, false otherwise
     */
    public boolean validateRequiredDatePicker(String fieldName, DatePicker datePicker, String errorMessage) {
        if (datePicker.getValue() == null) {
            showError(fieldName, errorMessage != null ? errorMessage : "Veuillez sélectionner une date");
            return false;
        }
        hideError(fieldName);
        return true;
    }

    /**
     * Validate that the second date is after the first date
     * @param fieldName The name of the field for the second date
     * @param firstDate The first date
     * @param secondDate The second date to validate
     * @param errorMessage Optional error message
     * @return true if valid, false otherwise
     */
    public boolean validateDateAfter(String fieldName, LocalDate firstDate, LocalDate secondDate, String errorMessage) {
        if (firstDate != null && secondDate != null && !secondDate.isAfter(firstDate)) {
            showError(fieldName, errorMessage != null ? errorMessage : "La date doit être postérieure à la première date");
            return false;
        }
        hideError(fieldName);
        return true;
    }
}