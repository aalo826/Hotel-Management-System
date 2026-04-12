package ca.senecacollege.application.hotelmanagementsystem.controller;

import ca.senecacollege.application.hotelmanagementsystem.model.*;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;

public class AdminController {

    @FXML private DashboardController         dashboardController;
    @FXML private ReservationController       reservationController;
    @FXML private PaymentController           paymentController;
    @FXML private WaitlistController          waitlistController;
    @FXML private FeedbackAdminController     feedbackAdminController;
    @FXML private ReportsController           reportsController;

    @FXML private TabPane mainTabPane;

    private static final int TAB_RESERVATION = 1;
    private static final int TAB_PAYMENT     = 2;


    // Initialize
    @FXML
    public void initialize() {
        wireDashboardToPayments();
        wireDashboardToReservations();
        wireWaitlistToReservations();
        wireReservationsToDashboard();
    }

    private void wireDashboardToPayments() {
        if (dashboardController == null || paymentController == null) return;
        dashboardController.setOnPayRequested(reservation -> {
            paymentController.loadReservation(reservation);
            selectTab(TAB_PAYMENT);
        });
    }

    private void wireDashboardToReservations() {}

    private void wireWaitlistToReservations() {
        if (waitlistController == null || reservationController == null) return;
        waitlistController.setOnConvertToReservation(waitlist -> {
            String[] parts = waitlist.getGuestName().split(" ", 2);
            reservationController.prefill(
                    parts[0],
                    parts.length > 1 ? parts[1] : "",
                    waitlist.getPhone(),
                    waitlist.getDesiredCheckIn(),
                    waitlist.getDesiredCheckOut(),
                    waitlist.getDesiredRoomType()
            );
            selectTab(TAB_RESERVATION);
        });
    }

    private void wireReservationsToDashboard() {
        if (reservationController == null || dashboardController == null) return;
        reservationController.setOnReservationCreated(() -> {
            // DashboardController will reload from DB on next tab visit; an explicit reload here
            // keeps the table in sync even if the user stays on the Reservations tab.
            dashboardController.handleDashboardReset();
        });
    }

    // Helper
    private void selectTab(int index) {
        if (mainTabPane != null)
            mainTabPane.getSelectionModel().select(index);
    }
}
