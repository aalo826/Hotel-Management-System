package ca.senecacollege.application.hotelmanagementsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import ca.senecacollege.application.hotelmanagementsystem.service.FeedbackService;

public class FeedbackController {

    // Panes
    @FXML private VBox feedbackFormPane;
    @FXML private VBox feedbackConfirmedPane;

    // Star Rating Labels
    @FXML private Label star1;
    @FXML private Label star2;
    @FXML private Label star3;
    @FXML private Label star4;
    @FXML private Label star5;

    // Form Fields
    @FXML private TextArea commentsArea;
    @FXML private Label errorLabel;

    // State
    private int selectedRating = 0;
    private final FeedbackService feedbackService = new FeedbackService();

    private static final String STAR_FILLED   = "\u2605";
    private static final String STAR_EMPTY    = "\u2606";
    private static final String STYLE_FILLED  = "-fx-text-fill: #f59e0b; -fx-font-size: 32px; -fx-cursor: hand;";
    private static final String STYLE_EMPTY   = "-fx-text-fill: #d1d5db; -fx-font-size: 32px; -fx-cursor: hand;";

    // Initialize
    @FXML
    public void initialize() {
        feedbackFormPane.setVisible(true);
        feedbackConfirmedPane.setVisible(false);
        updateStars(0);
    }

    // Star Handlers
    @FXML public void handleStar1() { setRating(1); }
    @FXML public void handleStar2() { setRating(2); }
    @FXML public void handleStar3() { setRating(3); }
    @FXML public void handleStar4() { setRating(4); }
    @FXML public void handleStar5() { setRating(5); }

    private void setRating(int rating) {
        selectedRating = rating;
        updateStars(rating);
    }

    private void updateStars(int rating) {
        Label[] stars = { star1, star2, star3, star4, star5 };
        for (int i = 0; i < 5; i++) {
            if (stars[i] != null) {
                stars[i].setText(i < rating ? STAR_FILLED : STAR_EMPTY);
                stars[i].setStyle(i < rating ? STYLE_FILLED : STYLE_EMPTY);
            }
        }
    }

    // Submit
    @FXML
    public void handleSubmitFeedback() {
        if (selectedRating == 0) {
            errorLabel.setText("Please select a star rating.");
            errorLabel.setVisible(true);
            return;
        }
        errorLabel.setVisible(false);

        try {
            // Retrieve the text from your TextArea
            String guestComment = commentsArea.getText();

            // SAVE DATA: Use the service to persist to the database
            // Note: Replace 'null' with actual Guest/Reservation objects if you have them in session
            feedbackService.submitFeedback(selectedRating, guestComment, null, null);

            // UI Transition
            feedbackFormPane.setVisible(false);
            feedbackConfirmedPane.setVisible(true);

        } catch (Exception e) {
            errorLabel.setText("System error: Could not save feedback.");
            errorLabel.setVisible(true);
        }
    }

    // Back to Welcome
    @FXML
    public void handleBackToWelcome() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(
                    "/ca/senecacollege/application/hotelmanagementsystem/view/kiosk-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load(), 1400, 900);
            javafx.stage.Stage stage = (javafx.stage.Stage) feedbackFormPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Failed to navigate back: " + e.getMessage());
        }
    }
}