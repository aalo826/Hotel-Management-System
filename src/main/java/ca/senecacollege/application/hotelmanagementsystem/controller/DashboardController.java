package ca.senecacollege.application.hotelmanagementsystem.controller;

import ca.senecacollege.application.hotelmanagementsystem.model.*;
import ca.senecacollege.application.hotelmanagementsystem.service.*;
import ca.senecacollege.application.hotelmanagementsystem.events.RoomAvailabilityEvent;
import ca.senecacollege.application.hotelmanagementsystem.events.RoomAvailabilityNotifier;
import ca.senecacollege.application.hotelmanagementsystem.events.RoomAvailabilityObserver;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DashboardController implements RoomAvailabilityObserver {

    // ── Services
    private final AdminService   adminService   = new AdminService();
    private final BookingService bookingService = new BookingService();
    private final LogService     logService     = new LogService();

    private java.util.function.Consumer<Reservation> onPayRequested;

    // FXML Fields
    @FXML private Label adminTitleLabel;

    @FXML private TextField        dashNameFilter;
    @FXML private TextField        dashPhoneFilter;
    @FXML private DatePicker       dashDateFilter;
    @FXML private ComboBox<String> dashStatusFilter;
    @FXML private ComboBox<String> dashRoomTypeFilter;

    @FXML private TableView<Reservation>          dashReservationTable;
    @FXML private TableColumn<Reservation,String> dashColName;
    @FXML private TableColumn<Reservation,String> dashColPhone;
    @FXML private TableColumn<Reservation,String> dashColRoom;
    @FXML private TableColumn<Reservation,String> dashColCheckIn;
    @FXML private TableColumn<Reservation,String> dashColCheckOut;
    @FXML private TableColumn<Reservation,String> dashColStatus;
    @FXML private TableColumn<Reservation,String> dashColBalance;
    @FXML private TableColumn<Reservation,Void>   dashColActions;

    public void setOnPayRequested(java.util.function.Consumer<Reservation> callback) {
        this.onPayRequested = callback;
    }

    // Initialize
    @FXML
    public void initialize() {
        set(dashStatusFilter,   "All", "Confirmed", "Pending", "Checked In", "Checked Out", "Cancelled");
        set(dashRoomTypeFilter, "All", "Single", "Double", "Penthouse");
        setupDashboardTable();

        try {
            List<Reservation> reservations = adminService.getAllReservations();
            if (reservations != null) {
                loadDashboardData(reservations);
            }
        } catch (Exception e) {
            System.err.println("Failed to load reservations for " + currentActor());
        }

        User u = SessionManager.getCurrentUser();
        if (u != null && adminTitleLabel != null)
            adminTitleLabel.setText("AURORA GRAND HOTEL  |  " + u.getUsername() + " (" + u.getRole() + ")");

        RoomAvailabilityNotifier.getInstance().subscribe(this);

        logService.log(currentActor(), "DASHBOARD_VIEW",
                "Admin dashboard loaded", SessionManager.getCurrentUser());
    }

    // RoomAvailabilityObserver

    @Override
    public void onRoomAvailabilityChanged(RoomAvailabilityEvent event) {
        if (event.getType() == RoomAvailabilityEvent.Type.ROOM_BECAME_AVAILABLE) {
            String msg = "[" + event.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] "
                    + "Room " + event.getRoomNumber() + " (" + event.getRoomType() + ") is now available.";
            javafx.application.Platform.runLater(() -> {
                loadDashboardData(adminService.getAllReservations());
                showInfo("Room Available", msg);
            });
        }
    }

    // Setup
    private void setupDashboardTable() {
        if (dashReservationTable == null) return;

        col(dashColName,     r -> r.getGuest() != null
                ? r.getGuest().getFirstName() + " " + r.getGuest().getLastName() : "—");
        col(dashColPhone,    r -> r.getGuest() != null && r.getGuest().getPhone() != null
                ? r.getGuest().getPhone() : "—");
        col(dashColRoom,     r -> r.getRooms().isEmpty() ? "—"
                : r.getRooms().stream().map(Room::getRoomNumber).collect(Collectors.joining(", ")));
        col(dashColCheckIn,  r -> r.getCheckInDate()  != null ? r.getCheckInDate().toString()  : "—");
        col(dashColCheckOut, r -> r.getCheckOutDate() != null ? r.getCheckOutDate().toString() : "—");
        col(dashColStatus,   r -> r.getStatus() != null ? r.getStatus() : "—");
        col(dashColBalance,  r -> String.format("$%.2f", adminService.getNetPaid(r.getId())));

        if (dashColActions != null) {
            dashColActions.setCellFactory(col -> new TableCell<>() {
                private final Button btnIn     = smallButton("Check-In",  "#2e7d32", "#fff");
                private final Button btnOut    = smallButton("Check-Out", "#c9a84c", "#fff");
                private final Button btnCancel = smallButton("Cancel",    "#c62828", "#fff");
                private final Button btnPay    = smallButton("Pay",       "#1565c0", "#fff");
                private final HBox   box       = new HBox(4, btnIn, btnOut, btnCancel, btnPay);

                {
                    btnIn.setOnAction(e -> handleCheckIn(getTableView().getItems().get(getIndex())));
                    btnOut.setOnAction(e -> handleCheckOut(getTableView().getItems().get(getIndex())));
                    btnCancel.setOnAction(e -> handleCancel(getTableView().getItems().get(getIndex())));
                    btnPay.setOnAction(e -> requestPayment(getTableView().getItems().get(getIndex())));
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : box);
                }
            });
        }

        dashReservationTable.setRowFactory(tv -> {
            TableRow<Reservation> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty())
                    requestPayment(row.getItem());
            });
            return row;
        });
    }

    public void loadDashboardData(List<Reservation> list) {
        if (dashReservationTable != null)
            dashReservationTable.setItems(FXCollections.observableArrayList(list));
    }

    // FXML Handlers
    @FXML public void handleDashboardSearch() {
        String name     = txt(dashNameFilter);
        String phone    = txt(dashPhoneFilter);
        LocalDate date  = dashDateFilter != null ? dashDateFilter.getValue() : null;
        String status   = val(dashStatusFilter);
        String roomType = val(dashRoomTypeFilter);

        List<Reservation> results = adminService.searchReservations(name, phone, date, status, roomType);
        loadDashboardData(results);

        logService.log(currentActor(), "SEARCH", "Reservation", "",
                "Dashboard search — name=" + name + " status=" + status,
                SessionManager.getCurrentUser());
    }

    @FXML public void handleDashboardReset() {
        if (dashNameFilter    != null) dashNameFilter.clear();
        if (dashPhoneFilter   != null) dashPhoneFilter.clear();
        if (dashDateFilter    != null) dashDateFilter.setValue(null);
        if (dashStatusFilter  != null) dashStatusFilter.setValue("All");
        if (dashRoomTypeFilter!= null) dashRoomTypeFilter.setValue("All");
        loadDashboardData(adminService.getAllReservations());
    }

    @FXML public void handleNewReservation() {
        if (dashReservationTable == null) return;
        TabPane tp = (TabPane) dashReservationTable.getScene().lookup(".tab-pane");
        if (tp != null) tp.getSelectionModel().select(1);
    }

    @FXML public void handleLogout() {
        logService.log(currentActor(), "LOGOUT", "Session", "",
                currentActor() + " logged out", SessionManager.getCurrentUser());
        RoomAvailabilityNotifier.getInstance().unsubscribe(this);
        SessionManager.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/ca/senecacollege/application/hotelmanagementsystem/view/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 900);
            Stage stage = (Stage) adminTitleLabel.getScene().getWindow();
            stage.setTitle("Aurora Grand Hotel - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Logout navigation failed: " + e.getMessage());
        }
    }

    @FXML public void handleSettings() {
        showInfo("Settings", "Settings panel — coming soon.");
    }

    // Private helpers
    private void requestPayment(Reservation r) {
        if (onPayRequested != null) onPayRequested.accept(r);
        logService.log(currentActor(), "PAYMENT", "Reservation",
                String.valueOf(r.getId()),
                "Opened payment for reservation #" + r.getId(),
                SessionManager.getCurrentUser());
    }

    private void handleCheckIn(Reservation r) {
        if (!"Confirmed".equalsIgnoreCase(r.getStatus())) {
            showError("Only Confirmed reservations can be checked in.");
            return;
        }
        try {
            bookingService.checkInReservation(r.getId(), currentActor());
            logService.log(currentActor(), "CHECKIN", "Reservation",
                    String.valueOf(r.getId()),
                    "Checked in reservation #" + r.getId(),
                    SessionManager.getCurrentUser());
            loadDashboardData(adminService.getAllReservations());
            showInfo("Checked In", "Reservation #" + r.getId() + " is now Checked In.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void handleCheckOut(Reservation r) {
        if (!"Checked In".equalsIgnoreCase(r.getStatus())) {
            showError("Only Checked-In reservations can be checked out.");
            return;
        }
        double outstanding = r.getTotalPrice() - adminService.getNetPaid(r.getId());
        if (outstanding > 0.01) {
            showError(String.format(
                    "Cannot check out: outstanding balance of $%.2f must be settled first.", outstanding));
            return;
        }
        try {
            bookingService.checkoutReservation(r.getId(), currentActor());
            logService.log(currentActor(), "CHECKOUT", "Reservation",
                    String.valueOf(r.getId()),
                    "Checked out reservation #" + r.getId(),
                    SessionManager.getCurrentUser());
            loadDashboardData(adminService.getAllReservations());
            showInfo("Checked Out",
                    "Reservation #" + r.getId() + " checked out.\nPlease invite the guest to leave feedback at the kiosk.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void handleCancel(Reservation r) {
        if ("Checked Out".equalsIgnoreCase(r.getStatus())) {
            showError("Cannot cancel a reservation that is already checked out.");
            return;
        }
        Optional<ButtonType> confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancel reservation #" + r.getId() + " for " + r.getGuestFullName() + "?",
                ButtonType.YES, ButtonType.NO).showAndWait();
        if (confirm.isEmpty() || confirm.get() != ButtonType.YES) return;
        try {
            bookingService.cancelReservation(r.getId(), currentActor());
            logService.log(currentActor(), "CANCELLATION", "Reservation",
                    String.valueOf(r.getId()),
                    "Cancelled reservation #" + r.getId(),
                    SessionManager.getCurrentUser());
            loadDashboardData(adminService.getAllReservations());
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // Utility
    private <S> void col(TableColumn<S, String> column,
                         java.util.function.Function<S, String> extractor) {
        if (column != null)
            column.setCellValueFactory(cd ->
                    new SimpleStringProperty(extractor.apply(cd.getValue())));
    }

    private Button smallButton(String label, String bg, String fg) {
        Button b = new Button(label);
        b.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; "
                + "-fx-font-size: 10px; -fx-padding: 3 7; -fx-background-radius: 4; -fx-cursor: hand;");
        return b;
    }

    private void set(ComboBox<String> cb, String... items) {
        if (cb != null) {
            cb.setItems(FXCollections.observableArrayList(items));
            cb.setValue(items[0]);
        }
    }

    private String txt(TextField f)          { return f != null ? f.getText().trim() : ""; }
    private String val(ComboBox<String> cb)  { return cb != null && cb.getValue() != null ? cb.getValue() : ""; }
    private String currentActor() {
        User u = SessionManager.getCurrentUser();
        return u != null ? u.getUsername() : "unknown";
    }
    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }
}
