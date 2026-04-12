package ca.senecacollege.application.hotelmanagementsystem.controller;

import ca.senecacollege.application.hotelmanagementsystem.model.*;
import ca.senecacollege.application.hotelmanagementsystem.repository.GenericRepository;
import ca.senecacollege.application.hotelmanagementsystem.service.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ReportsController {

    // Services / Repos
    private final ReportService              reportService = new ReportService();
    private final LogService                 logService    = new LogService();
    private final GenericRepository<Room>    roomRepo      = new GenericRepository<>(Room.class);

    // FXML Fields — Revenue
    @FXML private ComboBox<String> revPeriodFilter;
    @FXML private DatePicker       revDateFrom;
    @FXML private DatePicker       revDateTo;
    @FXML private ComboBox<String> revRoomTypeFilter;
    @FXML private TableView<ReportService.RevenueRow>          revenueTable;
    @FXML private TableColumn<ReportService.RevenueRow,String> revColPeriod;
    @FXML private TableColumn<ReportService.RevenueRow,String> revColCount;
    @FXML private TableColumn<ReportService.RevenueRow,String> revColSubtotal;
    @FXML private TableColumn<ReportService.RevenueRow,String> revColTax;
    @FXML private TableColumn<ReportService.RevenueRow,String> revColDiscount;
    @FXML private TableColumn<ReportService.RevenueRow,String> revColTotal;

    // FXML Fields — Occupancy
    @FXML private ComboBox<String> occPeriodFilter;
    @FXML private DatePicker       occDateFrom;
    @FXML private DatePicker       occDateTo;
    @FXML private ComboBox<String> occRoomTypeFilter;
    @FXML private TableView<ReportService.OccupancyRow>          occupancyTable;
    @FXML private TableColumn<ReportService.OccupancyRow,String> occColDate;
    @FXML private TableColumn<ReportService.OccupancyRow,String> occColAvail;
    @FXML private TableColumn<ReportService.OccupancyRow,String> occColOcc;
    @FXML private TableColumn<ReportService.OccupancyRow,String> occColPct;

    // FXML Fields — Activity Log
    @FXML private DatePicker       actDateFrom;
    @FXML private DatePicker       actDateTo;
    @FXML private TextField        actActorFilter;
    @FXML private ComboBox<String> actActionFilter;
    @FXML private TableView<Log>          activityTable;
    @FXML private TableColumn<Log,String> actColTimestamp;
    @FXML private TableColumn<Log,String> actColActor;
    @FXML private TableColumn<Log,String> actColAction;
    @FXML private TableColumn<Log,String> actColEntity;
    @FXML private TableColumn<Log,String> actColEntityId;
    @FXML private TableColumn<Log,String> actColMessage;

    // Initialize
    @FXML
    public void initialize() {
        LocalDate today = LocalDate.now();

        set(revPeriodFilter,   "Daily", "Weekly", "Monthly");
        set(revRoomTypeFilter, "All", "Single", "Double", "Penthouse");
        set(occPeriodFilter,   "Daily", "Weekly", "Monthly");
        set(occRoomTypeFilter, "All", "Single", "Double", "Penthouse");
        set(actActionFilter,   "All Actions", "LOGIN", "LOGIN_FAILED", "RESERVATION",
                "PAYMENT", "REFUND", "CANCELLATION", "CHECKOUT", "CHECKIN",
                "DISCOUNT", "FEEDBACK", "WAITLIST", "DASHBOARD_VIEW");

        if (revPeriodFilter != null) revPeriodFilter.setValue("Monthly");
        if (occPeriodFilter != null) occPeriodFilter.setValue("Monthly");
        if (actActionFilter != null) actActionFilter.setValue("All Actions");

        if (revDateFrom != null) revDateFrom.setValue(today.minusDays(30));
        if (revDateTo   != null) revDateTo.setValue(today);
        if (occDateFrom != null) occDateFrom.setValue(today.minusDays(30));
        if (occDateTo   != null) occDateTo.setValue(today);
        if (actDateFrom != null) actDateFrom.setValue(today.minusDays(30));
        if (actDateTo   != null) actDateTo.setValue(today);

        setupReportTables();
    }

    private void setupReportTables() {
        if (revenueTable != null) {
            col(revColPeriod,   r -> r.getPeriod());
            col(revColCount,    r -> String.valueOf(r.getReservationCount()));
            col(revColSubtotal, r -> String.format("$%.2f", r.getSubtotal()));
            col(revColTax,      r -> String.format("$%.2f", r.getTax()));
            col(revColDiscount, r -> String.format("$%.2f", r.getDiscount()));
            col(revColTotal,    r -> String.format("$%.2f", r.getTotal()));
        }
        if (occupancyTable != null) {
            col(occColDate,  o -> o.getDate());
            col(occColAvail, o -> String.valueOf(o.getRoomsAvailable()));
            col(occColOcc,   o -> String.valueOf(o.getRoomsOccupied()));
            col(occColPct,   o -> o.getOccupancyPct() + "%");
        }
        if (activityTable != null) {
            col(actColTimestamp, l -> l.getTimestamp() != null
                    ? l.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "—");
            col(actColActor,    l -> safe(l.getActor()));
            col(actColAction,   l -> safe(l.getAction()));
            col(actColEntity,   l -> safe(l.getEntityType()));
            col(actColEntityId, l -> safe(l.getEntityId()));
            col(actColMessage,  l -> safe(l.getMessage()));
        }
    }

    // FXML Handlers
    @FXML public void handleGenerateRevenue() {
        LocalDate from = revDateFrom != null ? revDateFrom.getValue() : LocalDate.now().minusDays(30);
        LocalDate to   = revDateTo   != null ? revDateTo.getValue()   : LocalDate.now();
        if (from == null || to == null || from.isAfter(to)) {
            showError("Invalid date range for revenue report."); return;
        }
        String period = val(revPeriodFilter);
        List<ReportService.RevenueRow> rows = switch (period) {
            case "Daily"  -> reportService.getRevenueByDay(from, to);
            case "Weekly" -> reportService.getRevenueByWeek(from, to);
            default       -> reportService.getRevenueByMonth(from, to);
        };
        if (revenueTable != null) revenueTable.setItems(FXCollections.observableArrayList(rows));
        logService.log(currentActor(), "REPORT", "Revenue", "",
                "Generated " + period + " revenue report", SessionManager.getCurrentUser());
    }

    @FXML public void handleGenerateOccupancy() {
        LocalDate from = occDateFrom != null ? occDateFrom.getValue() : LocalDate.now().minusDays(30);
        LocalDate to   = occDateTo   != null ? occDateTo.getValue()   : LocalDate.now();
        if (from == null || to == null || from.isAfter(to)) {
            showError("Invalid date range for occupancy report."); return;
        }
        int totalRooms = roomRepo.findAll().size();
        if (totalRooms == 0) totalRooms = 20;
        List<ReportService.OccupancyRow> rows = reportService.getOccupancyByDay(from, to, totalRooms);
        if (occupancyTable != null) occupancyTable.setItems(FXCollections.observableArrayList(rows));
        logService.log(currentActor(), "REPORT", "Occupancy", "",
                "Generated occupancy report", SessionManager.getCurrentUser());
    }

    @FXML public void handleGenerateActivity() {
        LocalDate from      = actDateFrom != null ? actDateFrom.getValue() : null;
        LocalDate to        = actDateTo   != null ? actDateTo.getValue()   : null;
        String actorFilter  = txt(actActorFilter);
        String actionFilter = val(actActionFilter);

        List<Log> logs = logService.getAllLogs().stream()
                .filter(l -> from == null || (l.getTimestamp() != null
                        && !l.getTimestamp().toLocalDate().isBefore(from)))
                .filter(l -> to   == null || (l.getTimestamp() != null
                        && !l.getTimestamp().toLocalDate().isAfter(to)))
                .filter(l -> actorFilter.isBlank()
                        || (l.getActor() != null && l.getActor().toLowerCase()
                        .contains(actorFilter.toLowerCase())))
                .filter(l -> "All Actions".equals(actionFilter)
                        || actionFilter.equalsIgnoreCase(l.getAction()))
                .collect(Collectors.toList());

        if (activityTable != null)
            activityTable.setItems(FXCollections.observableArrayList(logs));
    }

    // Revenue Export
    @FXML public void handleExportRevenueCSV() {
        if (revenueTable == null || revenueTable.getItems().isEmpty()) {
            showError("Generate revenue report first."); return;
        }
        File file = chooseSaveFile("revenue_report", "CSV (*.csv)", "*.csv");
        if (file == null) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Period,Reservations,Subtotal,Tax,Discount,Total");
            for (ReportService.RevenueRow r : revenueTable.getItems())
                pw.printf("%s,%d,%.2f,%.2f,%.2f,%.2f%n",
                        r.getPeriod(), r.getReservationCount(),
                        r.getSubtotal(), r.getTax(), r.getDiscount(), r.getTotal());
            showInfo("Exported", "Revenue report saved to " + file.getName());
        } catch (IOException e) { showError("Export failed: " + e.getMessage()); }
    }

    @FXML public void handleExportRevenueTXT() {
        if (revenueTable == null || revenueTable.getItems().isEmpty()) {
            showError("Generate revenue report first."); return;
        }
        File file = chooseSaveFile("revenue_report", "Text File (*.txt)", "*.txt");
        if (file == null) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Period\tReservations\tSubtotal\tTax\tDiscount\tTotal");
            for (ReportService.RevenueRow r : revenueTable.getItems())
                pw.printf("%s\t%d\t%.2f\t%.2f\t%.2f\t%.2f%n",
                        r.getPeriod(), r.getReservationCount(),
                        r.getSubtotal(), r.getTax(), r.getDiscount(), r.getTotal());
            showInfo("Exported", "Revenue report saved to " + file.getName());
        } catch (IOException e) { showError("Export failed: " + e.getMessage()); }
    }

    // Occupancy Export
    @FXML public void handleExportOccupancyCSV() {
        if (occupancyTable == null || occupancyTable.getItems().isEmpty()) {
            showError("Generate occupancy report first."); return;
        }
        File file = chooseSaveFile("occupancy_report", "CSV (*.csv)", "*.csv");
        if (file == null) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Date,Rooms Available,Rooms Occupied,Occupancy %");
            for (ReportService.OccupancyRow r : occupancyTable.getItems())
                pw.printf("%s,%d,%d,%.1f%n",
                        r.getDate(), r.getRoomsAvailable(), r.getRoomsOccupied(), r.getOccupancyPct());
            showInfo("Exported", "Occupancy report saved to " + file.getName());
        } catch (IOException e) { showError("Export failed: " + e.getMessage()); }
    }

    @FXML public void handleExportOccupancyTXT() {
        if (occupancyTable == null || occupancyTable.getItems().isEmpty()) {
            showError("Generate occupancy report first."); return;
        }
        File file = chooseSaveFile("occupancy_report", "Text File (*.txt)", "*.txt");
        if (file == null) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Date\tRooms Available\tRooms Occupied\tOccupancy %");
            for (ReportService.OccupancyRow r : occupancyTable.getItems())
                pw.printf("%s\t%d\t%d\t%.1f%n",
                        r.getDate(), r.getRoomsAvailable(), r.getRoomsOccupied(), r.getOccupancyPct());
            showInfo("Exported", "Occupancy report saved to " + file.getName());
        } catch (IOException e) { showError("Export failed: " + e.getMessage()); }
    }

    // Activity Log Export
    @FXML public void handleExportActivityCSV() {
        if (activityTable == null || activityTable.getItems().isEmpty()) {
            showError("Generate activity log first."); return;
        }
        File file = chooseSaveFile("activity_log", "CSV (*.csv)", "*.csv");
        if (file == null) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Timestamp,Actor,Action,EntityType,EntityID,Message");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (Log l : activityTable.getItems())
                pw.printf("%s,%s,%s,%s,%s,%s%n",
                        l.getTimestamp() != null ? l.getTimestamp().format(fmt) : "",
                        safe(l.getActor()), safe(l.getAction()),
                        safe(l.getEntityType()), safe(l.getEntityId()), safe(l.getMessage()));
            showInfo("Exported", "Activity log saved to " + file.getName());
        } catch (IOException e) { showError("Export failed: " + e.getMessage()); }
    }

    @FXML public void handleExportActivityTXT() {
        if (activityTable == null || activityTable.getItems().isEmpty()) {
            showError("Generate activity log first."); return;
        }
        File file = chooseSaveFile("activity_log", "Text File (*.txt)", "*.txt");
        if (file == null) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("Timestamp\tActor\tAction\tEntityType\tEntityID\tMessage");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (Log l : activityTable.getItems())
                pw.printf("%s\t%s\t%s\t%s\t%s\t%s%n",
                        l.getTimestamp() != null ? l.getTimestamp().format(fmt) : "",
                        safe(l.getActor()), safe(l.getAction()),
                        safe(l.getEntityType()), safe(l.getEntityId()), safe(l.getMessage()));
            showInfo("Exported", "Activity log saved to " + file.getName());
        } catch (IOException e) { showError("Export failed: " + e.getMessage()); }
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

    private File chooseSaveFile(String defaultName, String description, String extension) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Report");
        fc.setInitialFileName(defaultName + "_" +
                LocalDate.now().toString().replace("-", "") + extension.replace("*", ""));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
        return fc.showSaveDialog(
                revenueTable != null ? revenueTable.getScene().getWindow() : null);
    }

    private String txt(TextField f)         { return f != null ? f.getText().trim() : ""; }
    private String val(ComboBox<String> cb) { return cb != null && cb.getValue() != null ? cb.getValue() : ""; }
    private String safe(String s)           { return s != null ? s : ""; }
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
