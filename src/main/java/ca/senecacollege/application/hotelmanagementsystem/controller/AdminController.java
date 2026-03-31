package ca.senecacollege.application.hotelmanagementsystem.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminController {

    // Top Bar
    @FXML private Label adminTitleLabel;

    // Dashboard Tab
    @FXML private ComboBox<String> dashStatusFilter;
    @FXML private ComboBox<String> dashRoomTypeFilter;

    // Reservations Tab
    @FXML private Spinner<Integer> resAdults;
    @FXML private Spinner<Integer> resChildren;

    //  Payments Tab
    @FXML private ComboBox<String> paymentTypeCombo;

    // Waitlist Tab
    @FXML private ComboBox<String> waitlistRoomTypeFilter;

    // Feedback Tab
    @FXML private ComboBox<String> feedbackRatingFilter;
    @FXML private ComboBox<String> feedbackSentimentFilter;

    // Reports Tab
    @FXML private ComboBox<String> revPeriodFilter;
    @FXML private ComboBox<String> revRoomTypeFilter;
    @FXML private ComboBox<String> occPeriodFilter;
    @FXML private ComboBox<String> occRoomTypeFilter;
    @FXML private ComboBox<String> actActionFilter;

    // Initialize
    @FXML
    public void initialize() {

        // Dashboard
        if (dashStatusFilter != null) {
            dashStatusFilter.setItems(FXCollections.observableArrayList(
                    "All", "Confirmed", "Pending", "Cancelled"));
            dashStatusFilter.setValue("All");
        }
        if (dashRoomTypeFilter != null) {
            dashRoomTypeFilter.setItems(FXCollections.observableArrayList(
                    "All", "Single", "Double", "Penthouse"));
            dashRoomTypeFilter.setValue("All");
        }

        // Reservations
        if (resAdults != null)
            resAdults.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        if (resChildren != null)
            resChildren.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));

        // Payments
        if (paymentTypeCombo != null) {
            paymentTypeCombo.setItems(FXCollections.observableArrayList(
                    "Cash", "Card", "Online"));
        }

        // Waitlist
        if (waitlistRoomTypeFilter != null) {
            waitlistRoomTypeFilter.setItems(FXCollections.observableArrayList(
                    "All Types", "Single", "Double", "Penthouse"));
            waitlistRoomTypeFilter.setValue("All Types");
        }

        // Feedback
        if (feedbackRatingFilter != null) {
            feedbackRatingFilter.setItems(FXCollections.observableArrayList(
                    "All", "1", "2", "3", "4", "5"));
            feedbackRatingFilter.setValue("All");
        }
        if (feedbackSentimentFilter != null) {
            feedbackSentimentFilter.setItems(FXCollections.observableArrayList(
                    "All Tags", "Positive", "Neutral", "Negative"));
            feedbackSentimentFilter.setValue("All Tags");
        }

        // Reports - Revenue
        if (revPeriodFilter != null) {
            revPeriodFilter.setItems(FXCollections.observableArrayList(
                    "Daily", "Weekly", "Monthly"));
            revPeriodFilter.setValue("Weekly");
        }
        if (revRoomTypeFilter != null) {
            revRoomTypeFilter.setItems(FXCollections.observableArrayList(
                    "All Types", "Single", "Double", "Penthouse"));
            revRoomTypeFilter.setValue("All Types");
        }

        // Reports - Occupancy
        if (occPeriodFilter != null) {
            occPeriodFilter.setItems(FXCollections.observableArrayList(
                    "Daily", "Weekly", "Monthly"));
            occPeriodFilter.setValue("Weekly");
        }
        if (occRoomTypeFilter != null) {
            occRoomTypeFilter.setItems(FXCollections.observableArrayList(
                    "All Types", "Single", "Double", "Penthouse"));
            occRoomTypeFilter.setValue("All Types");
        }

        // Reports - Activity Log
        if (actActionFilter != null) {
            actActionFilter.setItems(FXCollections.observableArrayList(
                    "All Actions", "LOGIN", "RESERVATION",
                    "PAYMENT", "REFUND", "CANCELLATION", "FEEDBACK", "DISCOUNTS"));
            actActionFilter.setValue("All Actions");
        }
    }

    // Top Bar Handlers
    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/ca/senecacollege/application/hotelmanagementsystem/view/login-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) adminTitleLabel.getScene().getWindow();
            stage.setTitle("Aurora Grand Hotel - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Logout failed: " + e.getMessage());
        }
    }

    @FXML
    public void handleSettings() {
        System.out.println("Settings Clicked");
    }

    // Dashboard Tab Handlers
    @FXML public void handleDashboardSearch() { System.out.println("Search clicked."); }
    @FXML public void handleDashboardReset() {
        if (dashStatusFilter != null) dashStatusFilter.setValue("All");
        if (dashRoomTypeFilter != null) dashRoomTypeFilter.setValue("All");
    }
    @FXML public void handleNewReservation() { System.out.println("New Reservation clicked."); }

    // Reservations Tab Handlers
    @FXML public void handleCreateReservation() { System.out.println("Create Reservation clicked."); }

    // Payments Tab Handlers
    @FXML public void handleProcessPayment() { System.out.println("Process Payment clicked."); }
    @FXML public void handleProcessRefund() { System.out.println("Process Refund clicked."); }

    // Waitlist Tab Handlers
    @FXML public void handleWaitlistFilter() { System.out.println("Waitlist Filter clicked."); }
    @FXML public void handleAddToWaitlist() { System.out.println("Add to Waitlist clicked."); }
    @FXML public void handleConvertWaitlist() { System.out.println("Convert Waitlist clicked."); }
    @FXML public void handleRemoveWaitlist() { System.out.println("Remove from Waitlist clicked."); }

    //  Feedback Tab Handlers
    @FXML public void handleFeedbackFilter() { System.out.println("Feedback Filter clicked."); }

    // Reports Tab Handlers
    @FXML public void handleGenerateRevenue() { System.out.println("Generate Revenue clicked."); }
    @FXML public void handleGenerateOccupancy() { System.out.println("Generate Occupancy clicked."); }
    @FXML public void handleGenerateActivity() { System.out.println("Generate Activity Log clicked."); }

    // Legacy
    @FXML public void handleOpenLogs() { System.out.println("Logs tab opened."); }
    @FXML public void handleOpenRefunds() { System.out.println("Refunds tab opened."); }
}