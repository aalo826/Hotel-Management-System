package ca.senecacollege.application.hotelmanagementsystem.controller;

import ca.senecacollege.application.hotelmanagementsystem.model.*;
import ca.senecacollege.application.hotelmanagementsystem.service.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PaymentController {

    // Services
    private final AdminService   adminService   = new AdminService();
    private final PaymentService paymentService = new PaymentService();
    private final LoyaltyService loyaltyService = new LoyaltyService();
    private final LogService     logService     = new LogService();

    private Reservation selectedReservation = null;

    // FXML Fields
    @FXML private Label payGuestName;
    @FXML private Label payRoomSummary;
    @FXML private Label payBase;
    @FXML private Label payWeekendRate;
    @FXML private Label payAddOns;
    @FXML private Label paySubtotal;
    @FXML private Label payTax;
    @FXML private Label payDiscount;
    @FXML private Label payTotal;
    @FXML private Label payPaid;
    @FXML private Label payOutstanding;

    @FXML private ComboBox<String> paymentTypeCombo;
    @FXML private TextField        paymentAmountField;
    @FXML private Label            payErrorLabel;

    @FXML private TableView<Payment>          paymentHistoryTable;
    @FXML private TableColumn<Payment,String> payColDate;
    @FXML private TableColumn<Payment,String> payColType;
    @FXML private TableColumn<Payment,String> payColCategory;
    @FXML private TableColumn<Payment,String> payColAmount;
    @FXML private TableColumn<Payment,String> payColBy;

    // Initialize
    @FXML
    public void initialize() {
        set(paymentTypeCombo, "Cash", "Card", "Loyalty Points");
        setupPaymentHistoryTable();
    }

    // Public API
    public void loadReservation(Reservation r) {
        selectedReservation = r;
        refreshPaymentSummary();
    }

    // Setup
    private void setupPaymentHistoryTable() {
        if (paymentHistoryTable == null) return;
        col(payColDate,     p -> p.getPaymentDate() != null ? p.getPaymentDate().toString() : "—");
        col(payColType,     p -> p.getAmount() < 0 ? "Refund" : "Payment");
        col(payColCategory, p -> p.getMethod() != null ? p.getMethod() : "—");
        col(payColAmount,   p -> String.format("$%.2f", p.getAmount()));
        col(payColBy,       p -> currentActor());
    }

    // Data
    private void refreshPaymentSummary() {
        if (selectedReservation == null) return;
        Reservation r = adminService.getReservationById(selectedReservation.getId());
        if (r == null) return;
        selectedReservation = r;

        String guestName = r.getGuest() != null
                ? r.getGuest().getFirstName() + " " + r.getGuest().getLastName() : "—";
        String rooms = r.getRooms().isEmpty() ? "—"
                : r.getRooms().stream().map(Room::getType).collect(Collectors.joining(", "));

        double total       = r.getTotalPrice();
        double paid        = adminService.getNetPaid(r.getId());
        double outstanding = total - paid;

        setText(payGuestName,   "Guest: " + guestName);
        setText(payRoomSummary, "Room(s): " + rooms);
        setText(paySubtotal,    String.format("Subtotal: $%.2f", total / 1.13));
        setText(payTax,         String.format("Tax (13%%): $%.2f", total - total / 1.13));
        setText(payDiscount,    "Discount: see reservation");
        setText(payTotal,       String.format("Total: $%.2f", total));
        setText(payPaid,        String.format("Paid: $%.2f", paid));
        setText(payOutstanding, String.format("Outstanding: $%.2f", outstanding));

        List<Payment> history = paymentService.getAllPayments().stream()
                .filter(p -> p.getReservation() != null
                        && p.getReservation().getId() == r.getId())
                .collect(Collectors.toList());
        if (paymentHistoryTable != null)
            paymentHistoryTable.setItems(FXCollections.observableArrayList(history));

        if (paymentAmountField != null && outstanding > 0)
            paymentAmountField.setText(String.format("%.2f", outstanding));
    }

    //  FXML Handlers
    @FXML public void handleProcessPayment() {
        if (selectedReservation == null) {
            setPayError("Select a reservation from the Dashboard first.");
            return;
        }
        String method = val(paymentTypeCombo);
        double amount;
        try {
            amount = Double.parseDouble(txt(paymentAmountField));
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            setPayError("Enter a valid positive amount.");
            return;
        }

        try {
            if ("Loyalty Points".equals(method)) {
                Guest guest = selectedReservation.getGuest();
                if (guest == null || guest.getLoyaltyNumber() == null) {
                    setPayError("Guest is not a loyalty member.");
                    return;
                }
                int pointsNeeded = (int)(amount * 100);
                double discount = loyaltyService.redeemPoints(guest, pointsNeeded);
                paymentService.recordPayment(selectedReservation.getId(), discount, "Loyalty Points");
                logService.log(currentActor(), "PAYMENT", "Reservation",
                        String.valueOf(selectedReservation.getId()),
                        "Loyalty redemption: " + pointsNeeded + " pts = $" + discount,
                        SessionManager.getCurrentUser());
            } else {
                paymentService.recordPayment(selectedReservation.getId(), amount, method);
                logService.log(currentActor(), "PAYMENT", "Reservation",
                        String.valueOf(selectedReservation.getId()),
                        "Payment $" + amount + " via " + method,
                        SessionManager.getCurrentUser());
            }

            if (payErrorLabel != null) payErrorLabel.setVisible(false);
            refreshPaymentSummary();
            showInfo("Payment Recorded", String.format("$%.2f recorded via %s.", amount, method));

        } catch (Exception ex) {
            setPayError(ex.getMessage());
        }
    }

    @FXML public void handleProcessRefund() {
        String role = SessionManager.getCurrentUser() != null
                ? SessionManager.getCurrentUser().getRole() : "";
        if (!"Manager".equalsIgnoreCase(role)) {
            setPayError("Only Managers can process refunds.");
            return;
        }
        if (selectedReservation == null) {
            setPayError("Select a reservation from the Dashboard first.");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(txt(paymentAmountField));
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            setPayError("Enter a valid positive refund amount.");
            return;
        }

        Optional<ButtonType> confirm = new Alert(Alert.AlertType.CONFIRMATION,
                String.format("Process refund of $%.2f for reservation #%d?",
                        amount, selectedReservation.getId()),
                ButtonType.YES, ButtonType.NO).showAndWait();
        if (confirm.isEmpty() || confirm.get() != ButtonType.YES) return;

        try {
            paymentService.recordRefund(selectedReservation.getId(), amount, "Admin refund");
            logService.log(currentActor(), "REFUND", "Reservation",
                    String.valueOf(selectedReservation.getId()),
                    "Refund $" + amount + " processed by " + currentActor(),
                    SessionManager.getCurrentUser());
            if (payErrorLabel != null) payErrorLabel.setVisible(false);
            refreshPaymentSummary();
            showInfo("Refund Recorded", String.format("$%.2f refund recorded.", amount));
        } catch (Exception ex) {
            setPayError(ex.getMessage());
        }
    }

    // Utility
    private <S> void col(TableColumn<S, String> column,
                         java.util.function.Function<S, String> extractor) {
        if (column != null)
            column.setCellValueFactory(cd ->
                    new SimpleStringProperty(extractor.apply(cd.getValue())));
    }

    private void set(ComboBox<String> cb, String... items) {
        if (cb != null) {
            cb.setItems(FXCollections.observableArrayList(items));
            cb.setValue(items[0]);
        }
    }

    private void setPayError(String msg) {
        if (payErrorLabel != null) {
            payErrorLabel.setText(msg);
            payErrorLabel.setStyle("-fx-text-fill: #c62828;");
            payErrorLabel.setVisible(true);
        }
    }

    private String txt(TextField f)         { return f != null ? f.getText().trim() : ""; }
    private String val(ComboBox<String> cb) { return cb != null && cb.getValue() != null ? cb.getValue() : ""; }
    private void   setText(Label l, String t) { if (l != null) l.setText(t); }
    private String currentActor() {
        User u = SessionManager.getCurrentUser();
        return u != null ? u.getUsername() : "unknown";
    }
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }
}
