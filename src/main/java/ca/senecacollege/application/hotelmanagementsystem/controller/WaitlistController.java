package ca.senecacollege.application.hotelmanagementsystem.controller;

import ca.senecacollege.application.hotelmanagementsystem.model.*;
import ca.senecacollege.application.hotelmanagementsystem.repository.GenericRepository;
import ca.senecacollege.application.hotelmanagementsystem.service.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WaitlistController {

    // Services / Repos
    private final LogService                   logService   = new LogService();
    private final GenericRepository<Waitlist>  waitlistRepo = new GenericRepository<>(Waitlist.class);

    private java.util.function.Consumer<Waitlist> onConvertToReservation;

    public void setOnConvertToReservation(java.util.function.Consumer<Waitlist> callback) {
        this.onConvertToReservation = callback;
    }

    // FXML Fields
    @FXML private ComboBox<String> waitlistRoomTypeFilter;
    @FXML private DatePicker       waitlistDateFrom;
    @FXML private DatePicker       waitlistDateTo;

    @FXML private TableView<Waitlist>          waitlistTable;
    @FXML private TableColumn<Waitlist,String> waitColId;
    @FXML private TableColumn<Waitlist,String> waitColName;
    @FXML private TableColumn<Waitlist,String> waitColPhone;
    @FXML private TableColumn<Waitlist,String> waitColRoomType;
    @FXML private TableColumn<Waitlist,String> waitColCheckIn;
    @FXML private TableColumn<Waitlist,String> waitColCheckOut;
    @FXML private TableColumn<Waitlist,String> waitColAddedOn;
    @FXML private TableColumn<Waitlist,String> waitColStatus;
    @FXML private TableColumn<Waitlist,Void>   waitColActions;

    // Initialize
    @FXML
    public void initialize() {
        set(waitlistRoomTypeFilter, "All Types", "Single", "Double", "Penthouse");
        setupWaitlistTable();
    }

    private void setupWaitlistTable() {
        if (waitlistTable == null) return;

        col(waitColId,       w -> String.valueOf(w.getId()));
        col(waitColName,     w -> w.getGuestName());
        col(waitColPhone,    w -> w.getPhone() != null ? w.getPhone() : "—");
        col(waitColRoomType, w -> w.getDesiredRoomType());
        col(waitColCheckIn,  w -> w.getDesiredCheckIn()  != null ? w.getDesiredCheckIn().toString()  : "—");
        col(waitColCheckOut, w -> w.getDesiredCheckOut() != null ? w.getDesiredCheckOut().toString() : "—");
        col(waitColAddedOn,  w -> w.getAddedOn() != null ? w.getAddedOn().toString() : "—");
        col(waitColStatus,   w -> w.getStatus() != null ? w.getStatus() : "—");

        if (waitColActions != null) {
            waitColActions.setCellFactory(col -> new TableCell<>() {
                private final Button btnConvert = smallButton("→ Reserve", "#2e7d32", "#fff");
                private final Button btnRemove  = smallButton("Remove",    "#c62828", "#fff");
                private final HBox   box        = new HBox(4, btnConvert, btnRemove);

                {
                    btnConvert.setOnAction(e -> convertWaitlistEntry(getTableView().getItems().get(getIndex())));
                    btnRemove.setOnAction(e  -> removeWaitlistEntry(getTableView().getItems().get(getIndex())));
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : box);
                }
            });
        }

        loadWaitlistData();
    }

    // Data
    private void loadWaitlistData() {
        if (waitlistTable == null) return;
        List<Waitlist> all = waitlistRepo.findAll();
        String typeFilter = val(waitlistRoomTypeFilter);
        LocalDate from    = waitlistDateFrom != null ? waitlistDateFrom.getValue() : null;
        LocalDate to      = waitlistDateTo   != null ? waitlistDateTo.getValue()   : null;

        List<Waitlist> filtered = all.stream()
                .filter(w -> "All Types".equals(typeFilter)
                        || w.getDesiredRoomType().toLowerCase().contains(typeFilter.toLowerCase()))
                .filter(w -> from == null || (w.getDesiredCheckIn() != null
                        && !w.getDesiredCheckIn().isBefore(from)))
                .filter(w -> to   == null || (w.getDesiredCheckOut() != null
                        && !w.getDesiredCheckOut().isAfter(to)))
                .collect(Collectors.toList());

        waitlistTable.setItems(FXCollections.observableArrayList(filtered));
    }

    // FXML Handlers
    @FXML public void handleWaitlistFilter() {
        loadWaitlistData();
    }

    @FXML public void handleAddToWaitlist() {
        // Step 1: Guest name
        TextInputDialog nameDlg = new TextInputDialog();
        nameDlg.setTitle("Add to Waitlist");
        nameDlg.setHeaderText("Guest full name:");
        Optional<String> nameOpt = nameDlg.showAndWait();
        if (nameOpt.isEmpty() || nameOpt.get().isBlank()) return;

        // Step 2: Guest phone
        TextInputDialog phoneDlg = new TextInputDialog();
        phoneDlg.setTitle("Add to Waitlist");
        phoneDlg.setHeaderText("Guest phone (optional):");
        Optional<String> phoneOpt = phoneDlg.showAndWait();

        // Step 3: Room type + quantity picker
        String roomSummary = showRoomPickerDialog();
        if (roomSummary == null) return; // user cancelled or selected nothing

        // Step 4: Resolve dates (use filter pickers or sensible defaults)
        LocalDate today    = LocalDate.now();
        LocalDate checkIn  = waitlistDateFrom != null && waitlistDateFrom.getValue() != null
                ? waitlistDateFrom.getValue() : today;
        LocalDate checkOut = waitlistDateTo   != null && waitlistDateTo.getValue()   != null
                ? waitlistDateTo.getValue()   : today.plusDays(1);

        // Step 5: Persist
        Waitlist entry = new Waitlist();
        entry.setGuestName(nameOpt.get().trim());
        entry.setPhone(phoneOpt.map(String::trim).orElse(""));
        entry.setDesiredRoomType(roomSummary);          // e.g. "Single x2, Double x1"
        entry.setDesiredCheckIn(checkIn);
        entry.setDesiredCheckOut(checkOut);
        waitlistRepo.save(entry);

        logService.log(currentActor(), "WAITLIST", "Waitlist", "",
                "Added " + entry.getGuestName() + " to waitlist for " + roomSummary,
                SessionManager.getCurrentUser());

        loadWaitlistData();
        showInfo("Added", entry.getGuestName() + " added to waitlist.\nRooms: " + roomSummary);
    }

    @FXML public void handleConvertWaitlist() {
        Waitlist selected = waitlistTable != null ? waitlistTable.getSelectionModel().getSelectedItem() : null;
        if (selected != null) convertWaitlistEntry(selected);
    }

    @FXML public void handleRemoveWaitlist() {
        Waitlist selected = waitlistTable != null ? waitlistTable.getSelectionModel().getSelectedItem() : null;
        if (selected != null) removeWaitlistEntry(selected);
    }

    // Room Picker Dialog
    private String showRoomPickerDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add to Waitlist");
        dialog.setHeaderText("Select desired room types and quantities:");

        ButtonType confirmBtn = new ButtonType("Add to Waitlist", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);

        // Build the grid
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 24, 16, 24));

        // Header labels
        Label typeHeader = new Label("Room Type");
        typeHeader.setStyle("-fx-font-weight: bold;");
        Label qtyHeader = new Label("Quantity");
        qtyHeader.setStyle("-fx-font-weight: bold;");
        grid.add(typeHeader, 0, 0);
        grid.add(qtyHeader,  1, 0);

        // Spinners for each type
        Spinner<Integer> singleSpinner    = makeSpinner();
        Spinner<Integer> doubleSpinner    = makeSpinner();
        Spinner<Integer> penthouseSpinner = makeSpinner();

        grid.add(new Label("Single"),    0, 1);  grid.add(singleSpinner,    1, 1);
        grid.add(new Label("Double"),    0, 2);  grid.add(doubleSpinner,    1, 2);
        grid.add(new Label("Penthouse"), 0, 3);  grid.add(penthouseSpinner, 1, 3);

        // Live summary label
        Label summaryLabel = new Label("No rooms selected.");
        summaryLabel.setStyle("-fx-text-fill: #555; -fx-font-style: italic;");
        grid.add(new Label("Summary:"), 0, 4);
        grid.add(summaryLabel, 1, 4);

        // Update summary whenever any spinner changes
        Runnable updateSummary = () -> {
            String s = buildRoomSummary(
                    singleSpinner.getValue(),
                    doubleSpinner.getValue(),
                    penthouseSpinner.getValue());
            summaryLabel.setText(s.isEmpty() ? "No rooms selected." : s);
        };
        singleSpinner.valueProperty().addListener((o, ov, nv)    -> updateSummary.run());
        doubleSpinner.valueProperty().addListener((o, ov, nv)    -> updateSummary.run());
        penthouseSpinner.valueProperty().addListener((o, ov, nv) -> updateSummary.run());

        dialog.getDialogPane().setContent(grid);

        // Convert result only when OK is pressed
        dialog.setResultConverter(btn -> {
            if (btn == confirmBtn) {
                return buildRoomSummary(
                        singleSpinner.getValue(),
                        doubleSpinner.getValue(),
                        penthouseSpinner.getValue());
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) {
            if (result.isPresent()) {
                // OK was pressed but nothing selected
                showInfo("No rooms selected", "Please select at least one room type and quantity.");
            }
            return null;
        }
        return result.get();
    }

    private String buildRoomSummary(int single, int dbl, int penthouse) {
        List<String> parts = new ArrayList<>();
        if (single    > 0) parts.add("Single x"    + single);
        if (dbl       > 0) parts.add("Double x"    + dbl);
        if (penthouse > 0) parts.add("Penthouse x" + penthouse);
        return String.join(", ", parts);
    }

    private Spinner<Integer> makeSpinner() {
        Spinner<Integer> s = new Spinner<>(0, 10, 0);
        s.setEditable(true);
        s.setPrefWidth(80);
        return s;
    }

    // Private helpers

    private void convertWaitlistEntry(Waitlist w) {
        w.setStatus("Converted");
        waitlistRepo.update(w);
        logService.log(currentActor(), "WAITLIST", "Waitlist",
                String.valueOf(w.getId()),
                "Converted waitlist entry #" + w.getId() + " to reservation form",
                SessionManager.getCurrentUser());
        loadWaitlistData();

        if (onConvertToReservation != null) onConvertToReservation.accept(w);
    }

    private void removeWaitlistEntry(Waitlist w) {
        w.setStatus("Removed");
        waitlistRepo.update(w);
        logService.log(currentActor(), "WAITLIST", "Waitlist",
                String.valueOf(w.getId()),
                "Removed waitlist entry #" + w.getId(),
                SessionManager.getCurrentUser());
        loadWaitlistData();
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

    private String val(ComboBox<String> cb) { return cb != null && cb.getValue() != null ? cb.getValue() : ""; }
    private String currentActor() {
        User u = SessionManager.getCurrentUser();
        return u != null ? u.getUsername() : "unknown";
    }
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }
}