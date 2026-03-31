package ca.senecacollege.application.hotelmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    // Form Fields
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Label statusLabel;

    // Login Handler
    @FXML
    public void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            errorLabel.setVisible(true);
            return;
        }

        if (user.equals("admin") && pass.equals("123")) {
            // Show success message then navigate to admin dashboard
            statusLabel.setText("Login Successful! Entering Dashboard..");
            statusLabel.setVisible(true);
            errorLabel.setVisible(false);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/ca/senecacollege/application/hotelmanagementsystem/view/admin-view.fxml"));
                Scene scene = new Scene(loader.load());
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setTitle("Aurora Grand Hotel - Admin Dashboard");
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                errorLabel.setText("Failed to load dashboard: " + e.getMessage());
                errorLabel.setVisible(true);
            }

        } else {
            errorLabel.setText("Login Failed! Username or password are incorrect");
            errorLabel.setVisible(true);
            if (statusLabel != null) statusLabel.setVisible(false);
        }
    }

    // Forgot Password Handler
    @FXML
    public void handleForgotPassword() {
        errorLabel.setStyle("-fx-text-fill: #2563eb;");
        errorLabel.setText("Please contact your system administrator.");
        errorLabel.setVisible(true);
    }

    // Back to Kiosk Handler
    @FXML
    public void handleBackToKiosk() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(
                    "/ca/senecacollege/application/hotelmanagementsystem/view/kiosk-view.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) usernameField.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), stage.getWidth(), stage.getHeight());
            stage.setTitle("Aurora Grand Hotel");
            stage.setScene(scene);
            stage.show();
        } catch (java.io.IOException e) {
            System.err.println("Failed to load kiosk: " + e.getMessage());
        }
    }
}