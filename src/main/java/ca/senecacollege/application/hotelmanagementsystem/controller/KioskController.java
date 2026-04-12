package ca.senecacollege.application.hotelmanagementsystem.controller;

import ca.senecacollege.application.hotelmanagementsystem.model.Guest;
import ca.senecacollege.application.hotelmanagementsystem.model.Room;
import ca.senecacollege.application.hotelmanagementsystem.repository.GenericRepository;
import ca.senecacollege.application.hotelmanagementsystem.service.BookingService;
import ca.senecacollege.application.hotelmanagementsystem.service.IBooking;
import ca.senecacollege.application.hotelmanagementsystem.service.BaseBooking;
import ca.senecacollege.application.hotelmanagementsystem.service.ExtraServices;
import ca.senecacollege.application.hotelmanagementsystem.service.PriceStrategy;
import ca.senecacollege.application.hotelmanagementsystem.service.WeekendPricingStrategy;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class KioskController {

    @FXML private VBox welcomePane, guestsPane, datesPane, roomsPane, memberPromptPane, memberYesPane,
            guestInfoPane, addOnsPane, summaryPane, successPane;
    @FXML private Label adultsCountLabel, childrenCountLabel, datesErrorLabel, suggestedRoomType,
            suggestedRoomCapacity, suggestedRoomPrice, suggestedRoomCountLabel,
            singleRoomCount, doubleRoomCount, penthouseRoomCount,
            memberErrorLabel, guestInfoErrorLabel, addOnSubtotalLabel,
            summaryGuestName, summaryPhone, summaryGuests, summaryCheckIn, summaryCheckOut,
            summaryRoom, summaryAddOns, summarySubtotal, summaryTax, summaryTotal,
            summaryLoyaltyPoints, summaryPricingStrategy, summaryErrorLabel, successMessage;
    @FXML private DatePicker checkInDate, checkOutDate;
    @FXML private TextField memberPhoneField, firstNameField, lastNameField, emailField, phoneField;
    @FXML private CheckBox wifiCheckbox, breakfastCheckbox, parkingCheckbox, spaCheckbox;

    private int adultsCount = 1, childrenCount = 0, singleRooms = 0, doubleRooms = 1, penthouseRooms = 0;
    private boolean isMember = false;
    private Guest foundMember = null;

    private static final double SINGLE_PRICE = 150.0, DOUBLE_PRICE = 250.0, PENTHOUSE_PRICE = 400.0;
    private static final double TAX_RATE = 0.13;

    private final BookingService bookingService = new BookingService();
    private final GenericRepository<Guest> guestRepo = new GenericRepository<>(Guest.class);

    @FXML
    public void initialize() {
        showPane(welcomePane);
        updateCounters();
    }

    private void showPane(VBox paneToShow) {
        VBox[] allPanes = { welcomePane, guestsPane, datesPane, roomsPane, memberPromptPane,
                memberYesPane, guestInfoPane, addOnsPane, summaryPane, successPane };
        for (VBox pane : allPanes) if (pane != null) pane.setVisible(false);
        if (paneToShow != null) paneToShow.setVisible(true);
    }

    @FXML public void handleGoToLogin()    { switchScene("/ca/senecacollege/application/hotelmanagementsystem/view/login-view.fxml", "Login"); }
    @FXML public void handleGoToFeedback() { switchScene("/ca/senecacollege/application/hotelmanagementsystem/view/feedback-view.fxml", "Feedback"); }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) welcomePane.getScene().getWindow();
            stage.setTitle("Aurora Grand Hotel - " + title);
            stage.setScene(new Scene(root, 1400, 900));
            stage.show();
        } catch (IOException e) { System.err.println("Load error: " + e.getMessage()); }
    }

    @FXML public void handleBookReservation()  { showPane(guestsPane); }
    @FXML public void handleAdultsIncrease()   { adultsCount++; updateCounters(); }
    @FXML public void handleAdultsDecrease()   { if (adultsCount > 1) adultsCount--; updateCounters(); }
    @FXML public void handleChildrenIncrease() { childrenCount++; updateCounters(); }
    @FXML public void handleChildrenDecrease() { if (childrenCount > 0) childrenCount--; updateCounters(); }

    @FXML public void handleSingleIncrease()    { singleRooms++; updateCounters(); }
    @FXML public void handleSingleDecrease()    { if (singleRooms > 0) singleRooms--; updateCounters(); }
    @FXML public void handleDoubleIncrease()    { doubleRooms++; updateCounters(); }
    @FXML public void handleDoubleDecrease()    { if (doubleRooms > 0) doubleRooms--; updateCounters(); }
    @FXML public void handlePenthouseIncrease() { penthouseRooms++; updateCounters(); }
    @FXML public void handlePenthouseDecrease() { if (penthouseRooms > 0) penthouseRooms--; updateCounters(); }

    @FXML public void handleGuestsNext()  { showPane(datesPane); }
    @FXML public void handleGuestsBack()  { showPane(welcomePane); }

    @FXML public void handleDatesNext() {
        if (checkInDate.getValue() == null || checkOutDate.getValue() == null) {
            datesErrorLabel.setText("Please select both check-in and check-out dates.");
            datesErrorLabel.setVisible(true);
            return;
        }
        if (!checkOutDate.getValue().isAfter(checkInDate.getValue())) {
            datesErrorLabel.setText("Check-out must be after check-in.");
            datesErrorLabel.setVisible(true);
            return;
        }
        datesErrorLabel.setVisible(false);
        updateSuggestedRoom();
        showPane(roomsPane);
    }
    @FXML public void handleDatesBack() { showPane(guestsPane); }

    private void updateSuggestedRoom() {
        int total = adultsCount + childrenCount;
        if (total <= 2) { singleRooms = 1; doubleRooms = 0; }
        else            { singleRooms = 0; doubleRooms = 1; }
        updateCounters();
    }

    private void updateCounters() {
        if (adultsCountLabel   != null) adultsCountLabel.setText(String.valueOf(adultsCount));
        if (childrenCountLabel != null) childrenCountLabel.setText(String.valueOf(childrenCount));
        if (singleRoomCount    != null) singleRoomCount.setText(String.valueOf(singleRooms));
        if (doubleRoomCount    != null) doubleRoomCount.setText(String.valueOf(doubleRooms));
        if (penthouseRoomCount != null) penthouseRoomCount.setText(String.valueOf(penthouseRooms));
    }

    @FXML public void handleRoomsNext() {
        if ((singleRooms + doubleRooms + penthouseRooms) == 0) {
            // Reuse datesErrorLabel pattern — but roomsPane has no error label in the FXML,
            // so show an alert instead.
            new Alert(Alert.AlertType.WARNING, "Please select at least one room.", ButtonType.OK).showAndWait();
            return;
        }
        showPane(memberPromptPane);
    }
    @FXML public void handleRoomsBack()       { showPane(datesPane); }

    @FXML public void handleMemberYes()       { isMember = true;  showPane(memberYesPane); }
    @FXML public void handleMemberNo()        { isMember = false; showPane(guestInfoPane); }
    @FXML public void handleMemberPromptBack(){ showPane(roomsPane); }

    @FXML public void handleMemberLookupNext() {
        String phone = memberPhoneField.getText().trim();
        List<Guest> allGuests = guestRepo.findAll();
        foundMember = allGuests.stream()
                .filter(g -> phone.equals(g.getPhone()))
                .findFirst().orElse(null);
        if (foundMember != null) {
            showPane(addOnsPane);
        } else {
            memberErrorLabel.setText("Member not found. Please check your phone number.");
            memberErrorLabel.setVisible(true);
        }
    }

    @FXML public void handleMemberYesBack()  { showPane(memberPromptPane); }
    @FXML public void handleGuestInfoNext()  {
        if (firstNameField.getText().isBlank() || lastNameField.getText().isBlank()) {
            if (guestInfoErrorLabel != null) {
                guestInfoErrorLabel.setText("First and last name are required.");
                guestInfoErrorLabel.setVisible(true);
            }
            return;
        }
        if (guestInfoErrorLabel != null) guestInfoErrorLabel.setVisible(false);
        showPane(addOnsPane);
    }
    @FXML public void handleGuestInfoBack()  { showPane(memberPromptPane); }

    // Add-ons
    @FXML public void handleAddOnToggle() {
        long nights = nightsFromPickers();
        double preview = 0;
        if (wifiCheckbox      != null && wifiCheckbox.isSelected())      preview += 10.0;
        if (spaCheckbox       != null && spaCheckbox.isSelected())       preview += 50.0;
        if (breakfastCheckbox != null && breakfastCheckbox.isSelected()) preview += 20.0 * nights;
        if (parkingCheckbox   != null && parkingCheckbox.isSelected())   preview += 15.0 * nights;
        if (addOnSubtotalLabel != null)
            addOnSubtotalLabel.setText(String.format("Add-ons subtotal: $%.0f", preview));
    }

    @FXML public void handleAddOnsNext() { buildSummary(); showPane(summaryPane); }
    @FXML public void handleAddOnsBack() { if (isMember) showPane(memberYesPane); else showPane(guestInfoPane); }

    // Summary
    private void buildSummary() {
        String firstName, lastName, phone;
        if (isMember && foundMember != null) {
            firstName = foundMember.getFirstName();
            lastName  = foundMember.getLastName();
            phone     = foundMember.getPhone();
        } else {
            firstName = firstNameField.getText().trim();
            lastName  = lastNameField.getText().trim();
            phone     = phoneField.getText().trim();
        }

        if (summaryGuestName != null) summaryGuestName.setText(firstName + " " + lastName);
        if (summaryPhone     != null) summaryPhone.setText(phone);
        if (summaryGuests    != null) summaryGuests.setText(
                "Guests: " + adultsCount + " adult(s), " + childrenCount + " child(ren)");
        if (summaryCheckIn   != null) summaryCheckIn.setText("Check-in:  " + checkInDate.getValue());
        if (summaryCheckOut  != null) summaryCheckOut.setText("Check-out: " + checkOutDate.getValue());

        // Room summary
        List<String> roomParts = new ArrayList<>();
        if (singleRooms    > 0) roomParts.add(singleRooms    + "x Single");
        if (doubleRooms    > 0) roomParts.add(doubleRooms    + "x Double");
        if (penthouseRooms > 0) roomParts.add(penthouseRooms + "x Penthouse");
        if (summaryRoom != null) summaryRoom.setText("Rooms: " + String.join(", ", roomParts));

        long nights = nightsFromPickers();
        double roomRatePerNight = (singleRooms * SINGLE_PRICE)
                + (doubleRooms * DOUBLE_PRICE)
                + (penthouseRooms * PENTHOUSE_PRICE);

        // Pricing strategy
        PriceStrategy strategy = PriceStrategy.forDates(checkInDate.getValue(), checkOutDate.getValue());
        double basePrice = strategy.calculate(roomRatePerNight, nights);
        String strategyLabel = (strategy instanceof WeekendPricingStrategy)
                ? "Weekend rate (+20%)" : "Standard rate";
        if (summaryPricingStrategy != null)
            summaryPricingStrategy.setText("Pricing: " + strategyLabel);

        // Add-ons with nights-aware decorators
        IBooking booking = new BaseBooking(basePrice, "Room");
        List<String> addOnLabels = new ArrayList<>();
        if (wifiCheckbox      != null && wifiCheckbox.isSelected())      { booking = ExtraServices.addWifi(booking, nights);      addOnLabels.add("Wi-Fi ($10)"); }
        if (spaCheckbox       != null && spaCheckbox.isSelected())       { booking = ExtraServices.addSpa(booking, nights);       addOnLabels.add("Spa ($50)"); }
        if (breakfastCheckbox != null && breakfastCheckbox.isSelected()) { booking = ExtraServices.addBreakfast(booking, nights); addOnLabels.add("Breakfast ($" + (20 * nights) + ")"); }
        if (parkingCheckbox   != null && parkingCheckbox.isSelected())   { booking = ExtraServices.addParking(booking, nights);   addOnLabels.add("Parking ($" + (15 * nights) + ")"); }
        if (summaryAddOns != null)
            summaryAddOns.setText("Add-ons: " + (addOnLabels.isEmpty() ? "None" : String.join(", ", addOnLabels)));

        double subtotal = booking.calculateTotal();
        double tax      = subtotal * TAX_RATE;
        double total    = subtotal + tax;

        if (summarySubtotal != null) summarySubtotal.setText(String.format("Subtotal: $%.2f", subtotal));
        if (summaryTax      != null) summaryTax.setText(String.format("Tax (13%%): $%.2f", tax));
        if (summaryTotal    != null) summaryTotal.setText(String.format("Total: $%.2f", total));

        // Loyalty points
        if (summaryLoyaltyPoints != null) {
            int pointsEarned = (int) total;
            String loyaltyText = (isMember && foundMember != null)
                    ? String.format("Points after stay: %d (current: %d + earning %d)",
                    foundMember.getLoyaltyPoints() + pointsEarned,
                    foundMember.getLoyaltyPoints(), pointsEarned)
                    : String.format("You will earn %d loyalty points with this booking!", pointsEarned);
            summaryLoyaltyPoints.setText(loyaltyText);
        }
    }

    // Confirm
    @FXML public void handleConfirmReservation() {
        if (summaryErrorLabel != null) summaryErrorLabel.setVisible(false);

        Guest guest;
        if (isMember && foundMember != null) {
            guest = foundMember;
        } else {
            String phone = phoneField.getText().trim();
            guest = guestRepo.findAll().stream()
                    .filter(g -> phone.equals(g.getPhone()))
                    .findFirst()
                    .orElseGet(() -> {
                        Guest g = new Guest();
                        g.setFirstName(firstNameField.getText().trim());
                        g.setLastName(lastNameField.getText().trim());
                        g.setPhone(phone);
                        String email = emailField != null ? emailField.getText().trim() : "";
                        g.setEmail(email.isBlank() ? null : email);
                        return g;
                    });
        }

        List<Room> chosenRooms = new ArrayList<>();
        try {
            chosenRooms.addAll(resolveRooms("Single",    singleRooms));
            chosenRooms.addAll(resolveRooms("Double",    doubleRooms));
            chosenRooms.addAll(resolveRooms("Penthouse", penthouseRooms));
        } catch (Exception e) {
            showSummaryError(e.getMessage());
            return;
        }

        List<String> addOns = new ArrayList<>();
        if (wifiCheckbox      != null && wifiCheckbox.isSelected())      addOns.add("wifi");
        if (spaCheckbox       != null && spaCheckbox.isSelected())       addOns.add("spa");
        if (breakfastCheckbox != null && breakfastCheckbox.isSelected()) addOns.add("breakfast");
        if (parkingCheckbox   != null && parkingCheckbox.isSelected())   addOns.add("parking");

        try {
            bookingService.completeBooking(
                    guest,
                    chosenRooms,
                    addOns,
                    checkInDate.getValue(),
                    checkOutDate.getValue(),
                    0.0,       // kiosk guests don't get staff discounts
                    "kiosk");

            successMessage.setText("Booking confirmed! Enjoy your stay, " + guest.getFirstName() + ".");
            showPane(successPane);

        } catch (Exception e) {
            showSummaryError("Booking error: " + e.getMessage());
        }
    }

    // Private helpers
    private List<Room> resolveRooms(String type, int count) throws Exception {
        if (count <= 0) return new ArrayList<>();
        List<Room> available = bookingService.getAvailableRooms(
                type, checkInDate.getValue(), checkOutDate.getValue());
        if (available.size() < count) {
            throw new Exception("Only " + available.size() + " " + type
                    + " room(s) available for the selected dates (requested " + count + ").");
        }
        return available.subList(0, count);
    }

    private long nightsFromPickers() {
        if (checkInDate.getValue() == null || checkOutDate.getValue() == null) return 1;
        return Math.max(1, ChronoUnit.DAYS.between(checkInDate.getValue(), checkOutDate.getValue()));
    }

    private void showSummaryError(String msg) {
        if (summaryErrorLabel != null) {
            summaryErrorLabel.setText(msg);
            summaryErrorLabel.setVisible(true);
        }
    }

    @FXML public void handleSummaryBack()   { showPane(addOnsPane); }
    @FXML public void handleBackToWelcome() { initialize(); }
}