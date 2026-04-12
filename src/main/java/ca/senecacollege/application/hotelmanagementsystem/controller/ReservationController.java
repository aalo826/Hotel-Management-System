package ca.senecacollege.application.hotelmanagementsystem.controller;

import ca.senecacollege.application.hotelmanagementsystem.model.*;
import ca.senecacollege.application.hotelmanagementsystem.service.*;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationController {

    // Services
    private final AdminService   adminService   = new AdminService();
    private final BookingService bookingService = new BookingService();
    private final LogService     logService     = new LogService();

    // FXML Fields
    @FXML private TextField  resFirstName;
    @FXML private TextField  resLastName;
    @FXML private TextField  resPhone;
    @FXML private TextField  resEmail;
    @FXML private CheckBox   resLoyaltyStatus;
    @FXML private DatePicker resCheckIn;
    @FXML private DatePicker resCheckOut;
    @FXML private Spinner<Integer> resAdults;
    @FXML private Spinner<Integer> resChildren;
    @FXML private Spinner<Integer> resSingleRoom;
    @FXML private Spinner<Integer> resDoubleRoom;
    @FXML private Spinner<Integer> resDeluxeRoom;
    @FXML private CheckBox   resWifi;
    @FXML private CheckBox   resBreakfast;
    @FXML private CheckBox   resParking;
    @FXML private CheckBox   resSpa;
    @FXML private Label      resTotalLabel;
    @FXML private Label      resErrorLabel;
    @FXML private Label      resSuccessLabel;
    @FXML private Label      previewRooms;
    @FXML private Label      previewNights;
    @FXML private Label      previewStrategy;
    @FXML private Label      previewAddOns;
    @FXML private Label      previewSubtotal;
    @FXML private Label      previewTax;

    // Callback
    private Runnable onReservationCreated;

    public void setOnReservationCreated(Runnable callback) {
        this.onReservationCreated = callback;
    }

    public void prefill(String firstName, String lastName, String phone,
                        LocalDate checkIn, LocalDate checkOut, String roomsSummary) {
        if (resFirstName != null) resFirstName.setText(firstName);
        if (resLastName  != null) resLastName.setText(lastName);
        if (resPhone     != null) resPhone.setText(phone != null ? phone : "");
        if (resCheckIn   != null) resCheckIn.setValue(checkIn);
        if (resCheckOut  != null) resCheckOut.setValue(checkOut);

        // Reset all room spinners first
        if (resSingleRoom != null) resSingleRoom.getValueFactory().setValue(0);
        if (resDoubleRoom != null) resDoubleRoom.getValueFactory().setValue(0);
        if (resDeluxeRoom != null) resDeluxeRoom.getValueFactory().setValue(0);

        if (roomsSummary == null || roomsSummary.isBlank()) return;

        // Check if it's the new multi-room format (contains "x" followed by a digit)
        if (roomsSummary.contains(" x")) {
            // Parse "Single x2, Double x1, Penthouse x1"
            for (String part : roomsSummary.split(",")) {
                part = part.trim();
                String[] tokens = part.split(" x");
                if (tokens.length != 2) continue;
                String type = tokens[0].trim();
                int qty;
                try { qty = Integer.parseInt(tokens[1].trim()); }
                catch (NumberFormatException e) { continue; }

                switch (type) {
                    case "Single"    -> { if (resSingleRoom != null) resSingleRoom.getValueFactory().setValue(qty); }
                    case "Double"    -> { if (resDoubleRoom != null) resDoubleRoom.getValueFactory().setValue(qty); }
                    case "Penthouse" -> { if (resDeluxeRoom != null) resDeluxeRoom.getValueFactory().setValue(qty); }
                }
            }
        } else {
            // Legacy single-type string (backwards compat)
            switch (roomsSummary) {
                case "Double"    -> { if (resDoubleRoom != null) resDoubleRoom.getValueFactory().setValue(1); }
                case "Penthouse" -> { if (resDeluxeRoom != null) resDeluxeRoom.getValueFactory().setValue(1); }
                default          -> { if (resSingleRoom != null) resSingleRoom.getValueFactory().setValue(1); }
            }
        }
    }

    // Initialize
    @FXML
    public void initialize() {
        setupSpinners();
        setDefaultDates();
    }

    private void setupSpinners() {
        if (resAdults   != null) resAdults.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        if (resChildren != null) resChildren.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));
        if (resSingleRoom != null) {
            resSingleRoom.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
            resSingleRoom.valueProperty().addListener((o, ov, nv) -> updatePricePreview());
        }
        if (resDoubleRoom != null) {
            resDoubleRoom.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
            resDoubleRoom.valueProperty().addListener((o, ov, nv) -> updatePricePreview());
        }
        if (resDeluxeRoom != null) {
            resDeluxeRoom.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
            resDeluxeRoom.valueProperty().addListener((o, ov, nv) -> updatePricePreview());
        }
        if (resCheckIn  != null) resCheckIn.valueProperty().addListener((o, ov, nv)  -> updatePricePreview());
        if (resCheckOut != null) resCheckOut.valueProperty().addListener((o, ov, nv) -> updatePricePreview());
    }

    private void setDefaultDates() {
        LocalDate today = LocalDate.now();
        if (resCheckIn  != null) resCheckIn.setValue(today);
        if (resCheckOut != null) resCheckOut.setValue(today.plusDays(1));
    }

    // Price Preview
    @FXML public void updatePricePreview() {
        LocalDate checkIn  = resCheckIn  != null ? resCheckIn.getValue()  : null;
        LocalDate checkOut = resCheckOut != null ? resCheckOut.getValue() : null;

        int single    = resSingleRoom != null ? resSingleRoom.getValue() : 0;
        int dbl       = resDoubleRoom != null ? resDoubleRoom.getValue() : 0;
        int penthouse = resDeluxeRoom != null ? resDeluxeRoom.getValue() : 0;

        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)
                || (single + dbl + penthouse) == 0) {
            setText(previewRooms,    "Rooms: —");
            setText(previewNights,   "Nights: —");
            setText(previewStrategy, "Rate: —");
            setText(previewAddOns,   "Add-ons: none");
            setText(previewSubtotal, "Subtotal: —");
            setText(previewTax,      "Tax (13%): —");
            setText(resTotalLabel,   "Total: —");
            return;
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double ratePerNight = (single * 150.0) + (dbl * 250.0) + (penthouse * 400.0);

        PriceStrategy strategy = PriceStrategy.forDates(checkIn, checkOut);
        double basePrice = strategy.calculate(ratePerNight, nights);
        String strategyLabel = strategy instanceof WeekendPricingStrategy ? "Weekend (+20%)" : "Standard";

        IBooking booking = new BaseBooking(basePrice, "Room");
        List<String> addOnLabels = new ArrayList<>();
        if (resWifi      != null && resWifi.isSelected())      { booking = ExtraServices.addWifi(booking, nights);      addOnLabels.add("Wi-Fi"); }
        if (resBreakfast != null && resBreakfast.isSelected()) { booking = ExtraServices.addBreakfast(booking, nights); addOnLabels.add("Breakfast"); }
        if (resParking   != null && resParking.isSelected())   { booking = ExtraServices.addParking(booking, nights);   addOnLabels.add("Parking"); }
        if (resSpa       != null && resSpa.isSelected())       { booking = ExtraServices.addSpa(booking, nights);       addOnLabels.add("Spa"); }

        double subtotal = booking.calculateTotal();
        double tax      = subtotal * 0.13;
        double total    = subtotal + tax;

        setText(previewRooms,    "Rooms: " + buildRoomSummary(single, dbl, penthouse));
        setText(previewNights,   "Nights: " + nights);
        setText(previewStrategy, "Rate: " + strategyLabel);
        setText(previewAddOns,   "Add-ons: " + (addOnLabels.isEmpty() ? "none" : String.join(", ", addOnLabels)));
        setText(previewSubtotal, String.format("Subtotal: $%.2f", subtotal));
        setText(previewTax,      String.format("Tax (13%%): $%.2f", tax));
        setText(resTotalLabel,   String.format("Total: $%.2f", total));
    }

    private String buildRoomSummary(int single, int dbl, int penthouse) {
        List<String> parts = new ArrayList<>();
        if (single    > 0) parts.add(single    + "x Single");
        if (dbl       > 0) parts.add(dbl       + "x Double");
        if (penthouse > 0) parts.add(penthouse  + "x Penthouse");
        return parts.isEmpty() ? "none" : String.join(", ", parts);
    }

    // FXML Handlers
    @FXML public void handleCreateReservation() {
        String firstName = txt(resFirstName);
        String lastName  = txt(resLastName);
        String phone     = txt(resPhone);
        String email     = txt(resEmail);

        if (firstName.isBlank() || lastName.isBlank()) {
            showResError("First and last name are required.");
            return;
        }
        if (phone.isBlank() || !phone.matches("\\d{10,15}")) {
            showResError("Phone must be 10–15 digits.");
            return;
        }
        if (!email.isBlank() && !email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showResError("Invalid email format.");
            return;
        }

        LocalDate checkIn  = resCheckIn  != null ? resCheckIn.getValue()  : null;
        LocalDate checkOut = resCheckOut != null ? resCheckOut.getValue() : null;
        if (checkIn == null || checkOut == null) {
            showResError("Check-in and check-out dates are required.");
            return;
        }
        if (!checkOut.isAfter(checkIn)) {
            showResError("Check-out must be after check-in.");
            return;
        }
        if (checkIn.isBefore(LocalDate.now())) {
            showResError("Check-in date cannot be in the past.");
            return;
        }

        int singleCount    = resSingleRoom != null ? resSingleRoom.getValue() : 0;
        int doubleCount    = resDoubleRoom != null ? resDoubleRoom.getValue() : 0;
        int penthouseCount = resDeluxeRoom != null ? resDeluxeRoom.getValue() : 0;

        List<String> selectedTypes = new ArrayList<>();
        for (int i = 0; i < singleCount;    i++) selectedTypes.add("Single");
        for (int i = 0; i < doubleCount;    i++) selectedTypes.add("Double");
        for (int i = 0; i < penthouseCount; i++) selectedTypes.add("Penthouse");

        if (selectedTypes.isEmpty()) {
            showResError("Please select at least one room.");
            return;
        }

        int adults   = resAdults   != null ? resAdults.getValue()   : 1;
        int children = resChildren != null ? resChildren.getValue() : 0;
        int total    = adults + children;
        int maxCap   = (singleCount * 2) + (doubleCount * 4) + (penthouseCount * 2);
        if (total > maxCap) {
            showResError("Occupancy of " + total + " exceeds room capacity of " + maxCap + ".");
            return;
        }

        List<Room> chosenRooms = new ArrayList<>();
        for (String type : selectedTypes) {
            List<Room> avail = bookingService.getAvailableRooms(type, checkIn, checkOut);
            if (avail.isEmpty()) {
                showResError("No available " + type + " rooms for the selected dates.");
                return;
            }
            chosenRooms.add(avail.get(0));
        }

        List<String> addOns = new ArrayList<>();
        if (resWifi      != null && resWifi.isSelected())      addOns.add("wifi");
        if (resBreakfast != null && resBreakfast.isSelected()) addOns.add("breakfast");
        if (resParking   != null && resParking.isSelected())   addOns.add("parking");
        if (resSpa       != null && resSpa.isSelected())       addOns.add("spa");

        String role = SessionManager.getCurrentUser() != null
                ? SessionManager.getCurrentUser().getRole() : "";
        double discountPct = askDiscount(role);
        if (discountPct < 0) return;

        Guest guest = adminService.searchGuests(phone).stream()
                .filter(g -> phone.equals(g.getPhone()))
                .findFirst()
                .orElseGet(() -> {
                    Guest g = new Guest();
                    g.setFirstName(firstName);
                    g.setLastName(lastName);
                    g.setPhone(phone);
                    g.setEmail(email.isBlank() ? null : email);
                    return g;
                });

        if (resLoyaltyStatus != null && resLoyaltyStatus.isSelected()) {
            String num = adminService.enrollLoyalty(guest);
            showInfo("Loyalty Enrolled", "Loyalty number: " + num);
        }

        try {
            bookingService.completeBooking(guest, chosenRooms, addOns,
                    checkIn, checkOut, discountPct, currentActor());

            logService.log(currentActor(), "RESERVATION", "Reservation", "",
                    "Created reservation for " + firstName + " " + lastName
                            + " | rooms: " + selectedTypes + " | discount: " + discountPct + "%",
                    SessionManager.getCurrentUser());

            clearForm();
            if (onReservationCreated != null) onReservationCreated.run();

            if (resSuccessLabel != null) {
                resSuccessLabel.setText("Reservation created for " + firstName + " " + lastName
                        + (discountPct > 0 ? " with " + (int) discountPct + "% discount." : "."));
                resSuccessLabel.setVisible(true);
            }
            if (resErrorLabel != null) resErrorLabel.setVisible(false);

        } catch (Exception ex) {
            showResError("Booking failed: " + ex.getMessage());
        }
    }

    // Private helpers
    private double askDiscount(String role) {
        double maxDiscount = adminService.getMaxDiscount(role);
        TextInputDialog dlg = new TextInputDialog("0");
        dlg.setTitle("Apply Discount");
        dlg.setHeaderText("Enter discount % (0–" + (int) maxDiscount + "% for " + role + ")");
        dlg.setContentText("Discount %:");
        Optional<String> result = dlg.showAndWait();
        if (result.isEmpty()) return 0.0;
        try {
            double pct = Double.parseDouble(result.get().trim());
            if (!adminService.validateDiscount(pct, role)) {
                showError("Discount " + pct + "% exceeds your role limit of " + maxDiscount + "%.");
                return -1;
            }
            if (pct > 0) {
                logService.log(currentActor(), "DISCOUNT", "Reservation", "",
                        "Applied " + pct + "% discount (role: " + role + ")",
                        SessionManager.getCurrentUser());
            }
            return pct;
        } catch (NumberFormatException e) {
            showError("Invalid discount value.");
            return -1;
        }
    }

    private void clearForm() {
        if (resFirstName    != null) resFirstName.clear();
        if (resLastName     != null) resLastName.clear();
        if (resPhone        != null) resPhone.clear();
        if (resEmail        != null) resEmail.clear();
        setDefaultDates();
        if (resAdults       != null) resAdults.getValueFactory().setValue(1);
        if (resChildren     != null) resChildren.getValueFactory().setValue(0);
        if (resSingleRoom   != null) resSingleRoom.getValueFactory().setValue(0);
        if (resDoubleRoom   != null) resDoubleRoom.getValueFactory().setValue(0);
        if (resDeluxeRoom   != null) resDeluxeRoom.getValueFactory().setValue(0);
        if (resWifi         != null) resWifi.setSelected(false);
        if (resBreakfast    != null) resBreakfast.setSelected(false);
        if (resParking      != null) resParking.setSelected(false);
        if (resSpa          != null) resSpa.setSelected(false);
        if (resLoyaltyStatus!= null) resLoyaltyStatus.setSelected(false);
        if (resTotalLabel   != null) resTotalLabel.setText(null);
        if (resErrorLabel   != null) resErrorLabel.setVisible(false);
        if (resSuccessLabel != null) resSuccessLabel.setVisible(false);
    }

    private void showResError(String msg) {
        if (resErrorLabel != null) {
            resErrorLabel.setText(msg);
            resErrorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 13px;");
            resErrorLabel.setVisible(true);
        }
        if (resSuccessLabel != null) resSuccessLabel.setVisible(false);
    }

    private String txt(TextField f)         { return f != null ? f.getText().trim() : ""; }
    private void   setText(Label l, String t) { if (l != null) l.setText(t); }
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
