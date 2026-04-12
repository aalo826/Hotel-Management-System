package ca.senecacollege.application.hotelmanagementsystem.controller;

import ca.senecacollege.application.hotelmanagementsystem.model.User;
import ca.senecacollege.application.hotelmanagementsystem.repository.GenericRepository;
import ca.senecacollege.application.hotelmanagementsystem.service.LogService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.List;

public class LoginController {

    // Fields
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Label statusLabel;

    // Services
    private final GenericRepository<User> userRepository = new GenericRepository<>(User.class);
    private final LogService logService = new LogService();

    // Login
    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter all fields.");
            return;
        }

        User targetUser = null;
        List<User> users = userRepository.findAll();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                targetUser = u;
                break;
            }
        }

        if (targetUser != null && BCrypt.checkpw(password, targetUser.getPassword())) {
            SessionManager.setCurrentUser(targetUser);
            logService.log(username, "LOGIN", "Successful login", targetUser);
            navigateToAdminDashboard();
        } else {
            showError("Invalid username or password.");
            // Pass null user — we don't have a valid user object for failed attempts
            logService.log(username, "LOGIN_FAILED", "Failed login attempt for username: " + username, null);
        }
    }

    // Forgot Password
    @FXML
    private void handleForgotPassword(MouseEvent event) {
        showError("Please contact the IT department to reset your password.");
    }

    // Back to Kiosk
    @FXML
    private void handleBackToKiosk() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/ca/senecacollege/application/hotelmanagementsystem/view/kiosk-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 900));
            stage.show();
        } catch (IOException e) {
            showError("Navigation failed: " + e.getMessage());
        }
    }

    // Helpers
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void navigateToAdminDashboard() {
        try {
            String fxmlPath = "/ca/senecacollege/application/hotelmanagementsystem/view/admin-view.fxml";
            java.net.URL resource = getClass().getResource(fxmlPath);

            if (resource == null) {
                fxmlPath = "ca/senecacollege/application/hotelmanagementsystem/view/admin-view.fxml";
                resource = getClass().getClassLoader().getResource(fxmlPath);
            }

            if (resource == null) {
                showError("Cannot find admin-view.fxml.");
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 900));
            stage.setTitle("Admin Dashboard - Aurora Grand Hotel");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            showError("Failed to load Admin Dashboard: " + e.getMessage());
        }
    }
}