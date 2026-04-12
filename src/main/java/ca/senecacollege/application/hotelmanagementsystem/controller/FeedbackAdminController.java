package ca.senecacollege.application.hotelmanagementsystem.controller;

import ca.senecacollege.application.hotelmanagementsystem.model.*;
import ca.senecacollege.application.hotelmanagementsystem.service.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class FeedbackAdminController {

    // Services
    private final ReportService reportService = new ReportService();
    private final LogService    logService    = new LogService();

    // FXML Fields
    @FXML private ComboBox<String> feedbackRatingFilter;
    @FXML private ComboBox<String> feedbackSentimentFilter;
    @FXML private TextField        feedbackGuestFilter;
    @FXML private TextField        feedbackDateFilter;

    @FXML private TableView<ReportService.FeedbackRow>          feedbackTable;
    @FXML private TableColumn<ReportService.FeedbackRow,String> fbColRes;
    @FXML private TableColumn<ReportService.FeedbackRow,String> fbColGuest;
    @FXML private TableColumn<ReportService.FeedbackRow,String> fbColRating;
    @FXML private TableColumn<ReportService.FeedbackRow,String> fbColComment;
    @FXML private TableColumn<ReportService.FeedbackRow,String> fbColDate;

    // Initialize

    @FXML
    public void initialize() {
        set(feedbackRatingFilter,    "All", "1", "2", "3", "4", "5");
        set(feedbackSentimentFilter, "All Tags", "Positive", "Neutral", "Negative");
        setupFeedbackTable();
    }

    // Setup

    private void setupFeedbackTable() {
        if (feedbackTable == null) return;
        col(fbColRes,     f -> String.valueOf(f.getReservationId()));
        col(fbColGuest,   f -> f.getGuestName());
        col(fbColRating,  f -> "★".repeat(f.getRating()) + "☆".repeat(5 - f.getRating()));
        col(fbColComment, f -> f.getComment());
        col(fbColDate,    f -> f.getDate());
        loadFeedbackData();
    }

    // Data

    private void loadFeedbackData() {
        if (feedbackTable == null) return;
        List<ReportService.FeedbackRow> all = reportService.getFeedbackSummary();

        String ratingFilter    = val(feedbackRatingFilter);
        String sentimentFilter = val(feedbackSentimentFilter);
        String guestFilter     = txt(feedbackGuestFilter);
        String dateFilter      = txt(feedbackDateFilter);

        List<ReportService.FeedbackRow> filtered = all.stream()
                .filter(f -> "All".equals(ratingFilter)
                        || String.valueOf(f.getRating()).equals(ratingFilter))
                .filter(f -> "All Tags".equals(sentimentFilter)
                        || sentimentFilter.equalsIgnoreCase(f.getSentimentTag()))
                .filter(f -> guestFilter.isBlank()
                        || f.getGuestName().toLowerCase().contains(guestFilter.toLowerCase()))
                .filter(f -> dateFilter.isBlank()
                        || f.getDate().startsWith(dateFilter))
                .collect(Collectors.toList());

        feedbackTable.setItems(FXCollections.observableArrayList(filtered));

        logService.log(currentActor(), "FEEDBACK", "Feedback", "",
                "Viewed feedback list (" + filtered.size() + " entries)",
                SessionManager.getCurrentUser());
    }

    // FXML Handlers

    @FXML public void handleFeedbackFilter() {
        loadFeedbackData();
    }

    @FXML
    public void handleExportCSV() {
        exportFeedback("CSV", "CSV (*.csv)", "*.csv");
    }

    @FXML
    public void handleExportTXT() {
        exportFeedback("TXT", "Text Files (*.txt)", "*.txt");
    }

    private void exportFeedback(String type, String desc, String ext) {
        if (feedbackTable == null || feedbackTable.getItems().isEmpty()) {
            showError("No feedback data to export.");
            return;
        }

        File file = chooseSaveFile("feedback_report", desc, ext);
        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            if (type.equals("CSV")) {
                pw.println("Reservation,Guest,Rating,Comment,Date,Sentiment"); // Header
                for (ReportService.FeedbackRow f : feedbackTable.getItems()) {
                    pw.printf("%d,%s,%d,\"%s\",%s,%s%n",
                            f.getReservationId(), f.getGuestName(), f.getRating(),
                            f.getComment().replace("\"", "\"\""),
                            f.getDate(), f.getSentimentTag());
                }
            } else {
                pw.println("--- FEEDBACK REPORT ---");
                pw.println("Generated on: " + LocalDate.now());
                pw.println("-----------------------");
                for (ReportService.FeedbackRow f : feedbackTable.getItems()) {
                    pw.printf("Date: %s | Guest: %s | Rating: %d/5%n", f.getDate(), f.getGuestName(), f.getRating());
                    pw.printf("Comment: %s%n", f.getComment());
                    pw.printf("Sentiment: %s%n", f.getSentimentTag());
                    pw.println("-----------------------");
                }
            }
            showInfo("Exported", type + " report saved to " + file.getName());
        } catch (IOException e) {
            showError("Export failed: " + e.getMessage());
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

    private File chooseSaveFile(String defaultName, String description, String extension) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Report");
        fc.setInitialFileName(defaultName + "_" +
                LocalDate.now().toString().replace("-", "") + extension.replace("*", ""));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
        return fc.showSaveDialog(feedbackTable != null ? feedbackTable.getScene().getWindow() : null);
    }

    private String txt(TextField f)         { return f != null ? f.getText().trim() : ""; }
    private String val(ComboBox<String> cb) { return cb != null && cb.getValue() != null ? cb.getValue() : ""; }
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