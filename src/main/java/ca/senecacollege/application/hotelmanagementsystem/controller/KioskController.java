package ca.senecacollege.application.hotelmanagementsystem.controller;

import ca.senecacollege.application.hotelmanagementsystem.model.Guest;
import ca.senecacollege.application.hotelmanagementsystem.repository.GenericRepository;
import ca.senecacollege.application.hotelmanagementsystem.service.BookingService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class KioskController {

    // Panes
    @FXML private VBox welcomePane;
    @FXML private VBox guestsPane;
    @FXML private VBox datesPane;
    @FXML private VBox roomsPane;
    @FXML private VBox memberPromptPane;
    @FXML private VBox memberYesPane;
    @FXML private VBox guestInfoPane;
    @FXML private VBox addOnsPane;
    @FXML private VBox summaryPane;
    @FXML private VBox successPane;

    // Guests Pane
    @FXML private Label adultsCountLabel;
    @FXML private Label childrenCountLabel;

    // Booking Dates Pane
    @FXML private DatePicker checkInDate;
    @FXML private DatePicker checkOutDate;
    @FXML private Label datesErrorLabel;

    // Rooms Pane
    @FXML private Label suggestedRoomType;
    @FXML private Label suggestedRoomCapacity;
    @FXML private Label suggestedRoomPrice;
    @FXML private Label suggestedRoomCountLabel;
    @FXML private Label singleRoomCount;
    @FXML private Label doubleRoomCount;
    @FXML private Label penthouseRoomCount;

    // Member Yes Pane
    @FXML private TextField memberPhoneField;
    @FXML private Label memberErrorLabel;

    // Guest Info Pane
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private CheckBox memberSignupCheckbox;
    @FXML private Label guestInfoErrorLabel;

    // Add-Ons Pane
    @FXML private CheckBox wifiCheckbox;
    @FXML private CheckBox breakfastCheckbox;
    @FXML private CheckBox parkingCheckbox;
    @FXML private CheckBox spaCheckbox;
    @FXML private Label addOnSubtotalLabel;

    // Summary Pane
    @FXML private Label summaryGuestName;
    @FXML private Label summaryPhone;
    @FXML private Label summaryGuests;
    @FXML private Label summaryCheckIn;
    @FXML private Label summaryCheckOut;
    @FXML private Label summaryRoom;
    @FXML private Label summaryAddOns;
    @FXML private Label summarySubtotal;
    @FXML private Label summaryTax;
    @FXML private Label summaryTotal;
    @FXML private Label summaryLoyaltyPoints;
    @FXML private Label summaryErrorLabel;

    // Success Pane
    @FXML private Label successMessage;

    // Booking Data
    private int adultsCount    = 1;
    private int childrenCount  = 0;
    private int singleRooms    = 0;
    private int doubleRooms    = 1;
    private int penthouseRooms = 0;
    private boolean isMember   = false;
    private Guest foundMember  = null; // Stores DB-looked-up member

    // Pricing Constants
    private static final double SINGLE_PRICE    = 150.0;
    private static final double DOUBLE_PRICE    = 250.0;
    private static final double PENTHOUSE_PRICE = 400.0;
    private static final double WIFI_PRICE      = 10.0;
    private static final double BREAKFAST_PRICE = 5.0;
    private static final double PARKING_PRICE   = 30.0;
    private static final double SPA_PRICE       = 25.0;
    private static final double TAX_RATE        = 0.13;

    private final BookingService bookingService = new BookingService();
    private final GenericRepository<Guest> guestRepo = new GenericRepository<>(Guest.class);

    // Initialize
    @FXML
    public void initialize() {
        showPane(welcomePane);
        adultsCountLabel.setText("1");
        childrenCountLabel.setText("0");
        singleRoomCount.setText("0");
        doubleRoomCount.setText("1");
        penthouseRoomCount.setText("0");
    }

    // Pane Switcher
    private void showPane(VBox paneToShow) {
        VBox[] allPanes = {
                welcomePane, guestsPane, datesPane, roomsPane,
                memberPromptPane, memberYesPane, guestInfoPane,
                addOnsPane, summaryPane, successPane
        };
        for (VBox pane : allPanes) {
            if (pane != null) pane.setVisible(false);
        }
        if (paneToShow != null) paneToShow.setVisible(true);
    }

    // Welcome Pane
    @FXML public void handleBookReservation() { showPane(guestsPane); }

    @FXML public void handleGoToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/ca/senecacollege/application/hotelmanagementsystem/view/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 900);
            Stage stage = (Stage) welcomePane.getScene().getWindow();
            stage.setTitle("Aurora Grand Hotel - Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load login: " + e.getMessage());
        }
    }

    @FXML public void handleGoToFeedback() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/ca/senecacollege/application/hotelmanagementsystem/view/feedback-view.fxml"));
            Scene scene = new Scene(loader.load(), 1400, 900);
            Stage stage = (Stage) welcomePane.getScene().getWindow();
            stage.setTitle("Aurora Grand Hotel - Feedback");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load feedback: " + e.getMessage());
        }
    }

    // Guests Pane
    @FXML public void handleAdultsIncrease() {
        adultsCount++;
        adultsCountLabel.setText(String.valueOf(adultsCount));
    }
    @FXML public void handleAdultsDecrease() {
        if (adultsCount > 1) { adultsCount--; adultsCountLabel.setText(String.valueOf(adultsCount)); }
    }
    @FXML public void handleChildrenIncrease() {
        childrenCount++;
        childrenCountLabel.setText(String.valueOf(childrenCount));
    }
    @FXML public void handleChildrenDecrease() {
        if (childrenCount > 0) { childrenCount--; childrenCountLabel.setText(String.valueOf(childrenCount)); }
    }
    @FXML public void handleGuestsNext() { showPane(datesPane); }
    @FXML public void handleGuestsBack() { showPane(welcomePane); }

    // Booking Dates Pane
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

    // Rooms Pane
    private void updateSuggestedRoom() {
        int total = adultsCount + childrenCount;

        // Reset all custom counts
        singleRooms    = 0;
        doubleRooms    = 0;
        penthouseRooms = 0;

        if (total <= 2) {
            // 1-2 guests → suggest single rooms
            int count = (int) Math.ceil(total / 2.0);
            suggestedRoomType.setText("Single Room");
            suggestedRoomCapacity.setText("Capacity: 2 per room");
            suggestedRoomPrice.setText(" $150/night");
            suggestedRoomCountLabel.setText(count + " room" + (count > 1 ? "s" : ""));
            singleRooms = count;
        } else {
            // 3+ guests → always suggest double rooms
            int count = (int) Math.ceil(total / 4.0);
            suggestedRoomType.setText("Double Room");
            suggestedRoomCapacity.setText("Capacity: 4 per room");
            suggestedRoomPrice.setText(" $250/night");
            suggestedRoomCountLabel.setText(count + " room" + (count > 1 ? "s" : ""));
            doubleRooms = count;
        }

        // Reflect in custom room count labels below
        singleRoomCount.setText(String.valueOf(singleRooms));
        doubleRoomCount.setText(String.valueOf(doubleRooms));
        penthouseRoomCount.setText(String.valueOf(penthouseRooms));
    }

    // Custom room adjustments
    @FXML public void handleSingleDecrease() {
        if (singleRooms > 0) { singleRooms--; singleRoomCount.setText(String.valueOf(singleRooms)); }
    }
    @FXML public void handleSingleIncrease() {
        singleRooms++; singleRoomCount.setText(String.valueOf(singleRooms));
    }
    @FXML public void handleDoubleDecrease() {
        if (doubleRooms > 0) { doubleRooms--; doubleRoomCount.setText(String.valueOf(doubleRooms)); }
    }
    @FXML public void handleDoubleIncrease() {
        doubleRooms++; doubleRoomCount.setText(String.valueOf(doubleRooms));
    }
    @FXML public void handlePenthouseDecrease() {
        if (penthouseRooms > 0) { penthouseRooms--; penthouseRoomCount.setText(String.valueOf(penthouseRooms)); }
    }
    @FXML public void handlePenthouseIncrease() {
        penthouseRooms++; penthouseRoomCount.setText(String.valueOf(penthouseRooms));
    }

    @FXML public void handleRoomsNext() { showPane(memberPromptPane); }
    @FXML public void handleRoomsBack() { showPane(datesPane); }

    // Member Prompt Pane
    @FXML public void handleMemberYes() {
        isMember = true;
        foundMember = null;
        memberPhoneField.clear();
        memberErrorLabel.setVisible(false);
        showPane(memberYesPane);
    }
    @FXML public void handleMemberNo() {
        isMember = false;
        foundMember = null;
        showPane(guestInfoPane);
    }
    @FXML public void handleMemberPromptBack() { showPane(roomsPane); }

    // Member Yes Pane
    @FXML public void handleMemberLookupNext() {
        String phone = memberPhoneField.getText().trim();

        if (phone.isEmpty()) {
            memberErrorLabel.setText("Please enter your phone number.");
            memberErrorLabel.setVisible(true);
            return;
        }

        try {
            // Query all guests and find one matching the phone number
            List<Guest> allGuests = guestRepo.findAll();
            Guest match = allGuests.stream()
                    .filter(g -> phone.equals(g.getPhone()))
                    .findFirst()
                    .orElse(null);

            if (match != null) {
                // Member found — store their data and proceed
                foundMember = match;
                memberErrorLabel.setVisible(false);
                showPane(addOnsPane);
            } else {
                // Not found — show error, stay on this screen
                memberErrorLabel.setText("No member found with that phone number. Please try again.");
                memberErrorLabel.setVisible(true);
            }
        } catch (Exception e) {
            memberErrorLabel.setText("Error looking up member: " + e.getMessage());
            memberErrorLabel.setVisible(true);
        }
    }

    @FXML public void handleMemberYesBack() { showPane(memberPromptPane); }

    // Guest Info Pane
    @FXML public void handleGuestInfoNext() {
        if (firstNameField.getText().trim().isEmpty() ||
                lastNameField.getText().trim().isEmpty()) {
            guestInfoErrorLabel.setText("First and last name are required.");
            guestInfoErrorLabel.setVisible(true);
            return;
        }
        guestInfoErrorLabel.setVisible(false);
        showPane(addOnsPane);
    }
    @FXML public void handleGuestInfoBack() { showPane(memberPromptPane); }

    // Add-Ons Pane
    @FXML public void handleAddOnToggle() {
        double subtotal = 0;
        if (wifiCheckbox.isSelected())      subtotal += WIFI_PRICE;
        if (breakfastCheckbox.isSelected()) subtotal += BREAKFAST_PRICE;
        if (parkingCheckbox.isSelected())   subtotal += PARKING_PRICE;
        if (spaCheckbox.isSelected())       subtotal += SPA_PRICE;
        addOnSubtotalLabel.setText(String.format("Subtotal: $%.0f", subtotal));
    }
    @FXML public void handleAddOnsNext() {
        buildSummary();
        showPane(summaryPane);
    }
    @FXML public void handleAddOnsBack() {
        if (isMember) showPane(memberYesPane);
        else showPane(guestInfoPane);
    }

    // Summary Pane
    private void buildSummary() {
        String firstName, lastName, phone;

        if (isMember && foundMember != null) {
            // Use real data from DB lookup
            firstName = foundMember.getFirstName();
            lastName  = foundMember.getLastName();
            phone     = foundMember.getPhone();
        } else {
            // Use manually entered data
            firstName = firstNameField.getText().trim();
            lastName  = lastNameField.getText().trim();
            phone     = phoneField.getText().trim();
        }

        summaryGuestName.setText(firstName + " " + lastName);
        summaryPhone.setText(phone);
        summaryGuests.setText("Guests: " + adultsCount + " Adults, " + childrenCount +
                " Child" + (childrenCount != 1 ? "ren" : ""));
        summaryCheckIn.setText("Check-in: " +
                (checkInDate.getValue() != null ? checkInDate.getValue().toString() : "—"));
        summaryCheckOut.setText("Check-out: " +
                (checkOutDate.getValue() != null ? checkOutDate.getValue().toString() : "—"));

        // Room summary
        StringBuilder roomStr = new StringBuilder();
        if (singleRooms    > 0) roomStr.append("Single x").append(singleRooms).append(" ");
        if (doubleRooms    > 0) roomStr.append("Double x").append(doubleRooms).append(" ");
        if (penthouseRooms > 0) roomStr.append("Penthouse x").append(penthouseRooms);
        if (roomStr.length() == 0) roomStr.append("Double x1");
        summaryRoom.setText("Room: " + roomStr.toString().trim());

        // Add-ons
        StringBuilder addOnStr = new StringBuilder();
        double addOnTotal = 0;
        if (wifiCheckbox.isSelected())      { addOnStr.append("Wi-Fi, ");     addOnTotal += WIFI_PRICE; }
        if (breakfastCheckbox.isSelected()) { addOnStr.append("Breakfast, "); addOnTotal += BREAKFAST_PRICE; }
        if (parkingCheckbox.isSelected())   { addOnStr.append("Parking, ");   addOnTotal += PARKING_PRICE; }
        if (spaCheckbox.isSelected())       { addOnStr.append("Spa, ");       addOnTotal += SPA_PRICE; }
        String addOns = addOnStr.length() > 2 ?
                addOnStr.substring(0, addOnStr.length() - 2) : "None";
        summaryAddOns.setText("Add-Ons: " + addOns);

        // Pricing
        long nights = 1;
        if (checkInDate.getValue() != null && checkOutDate.getValue() != null) {
            nights = ChronoUnit.DAYS.between(checkInDate.getValue(), checkOutDate.getValue());
            if (nights < 1) nights = 1;
        }
        double roomTotal = (singleRooms * SINGLE_PRICE +
                doubleRooms * DOUBLE_PRICE +
                penthouseRooms * PENTHOUSE_PRICE) * nights;
        if (roomTotal == 0) roomTotal = DOUBLE_PRICE * nights;

        double subtotal = roomTotal + addOnTotal;
        double tax      = subtotal * TAX_RATE;
        double total    = subtotal + tax;
        int loyaltyPts  = (int)(total / 10);

        summarySubtotal.setText(String.format("Subtotal: $%.2f", subtotal));
        summaryTax.setText(String.format("Tax: $%.2f", tax));
        summaryTotal.setText(String.format("Total: $%.2f", total));
        summaryLoyaltyPoints.setText("Loyalty points earned: " + loyaltyPts);
    }

    @FXML public void handleConfirmReservation() {
        try {
            Guest guest;

            if (isMember && foundMember != null) {
                // Use existing member from DB
                guest = foundMember;
            } else if (isMember && foundMember == null) {
                // Member path but no valid member found — block confirmation
                summaryErrorLabel.setText("No member found. Please go back and verify your phone number.");
                summaryErrorLabel.setVisible(true);
                return;
            } else {
                // New guest — create from form data
                guest = new Guest();
                guest.setFirstName(firstNameField.getText().trim());
                guest.setLastName(lastNameField.getText().trim());
                guest.setEmail(emailField.getText().trim());
                guest.setPhone(phoneField.getText().trim());
            }

            bookingService.completeBooking(guest, checkInDate.getValue(), checkOutDate.getValue());

            successMessage.setText("Your booking has been successful!\nEnjoy your stay " +
                    guest.getFirstName() + "!");
            showPane(successPane);

        } catch (Exception e) {
            summaryErrorLabel.setText("Booking failed: " + e.getMessage());
            summaryErrorLabel.setVisible(true);
        }
    }

    @FXML public void handleSummaryBack() { showPane(addOnsPane); }

    // Successful Booking Pane
    @FXML public void handleBackToWelcome() {
        adultsCount    = 1;
        childrenCount  = 0;
        singleRooms    = 0;
        doubleRooms    = 1;
        penthouseRooms = 0;
        isMember       = false;
        foundMember    = null;
        initialize();
    }
}