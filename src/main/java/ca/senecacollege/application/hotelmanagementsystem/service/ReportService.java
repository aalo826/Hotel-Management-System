package ca.senecacollege.application.hotelmanagementsystem.service;

import ca.senecacollege.application.hotelmanagementsystem.model.Feedback;
import ca.senecacollege.application.hotelmanagementsystem.model.Log;
import ca.senecacollege.application.hotelmanagementsystem.model.Payment;
import ca.senecacollege.application.hotelmanagementsystem.model.Reservation;
import ca.senecacollege.application.hotelmanagementsystem.repository.GenericRepository;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {

    private static final double TAX_RATE = 0.13;

    private final GenericRepository<Payment>     paymentRepo     = new GenericRepository<>(Payment.class);
    private final GenericRepository<Reservation> reservationRepo = new GenericRepository<>(Reservation.class);
    private final GenericRepository<Feedback>    feedbackRepo    = new GenericRepository<>(Feedback.class);
    private final GenericRepository<Log>         logRepo         = new GenericRepository<>(Log.class);

    // Revenue row
    public static class RevenueRow {
        public final String period;
        public final int    reservationCount;
        public final double subtotal;
        public final double tax;
        public final double discount;
        public final double total;

        public RevenueRow(String period, int count, double subtotal, double discount) {
            this.period           = period;
            this.reservationCount = count;
            this.subtotal         = subtotal;
            this.tax              = subtotal * TAX_RATE;
            this.discount         = discount;
            this.total            = subtotal + this.tax - discount;
        }

        // Getters for TableView PropertyValueFactory
        public String getPeriod()           { return period; }
        public int    getReservationCount() { return reservationCount; }
        public double getSubtotal()         { return subtotal; }
        public double getTax()              { return tax; }
        public double getDiscount()         { return discount; }
        public double getTotal()            { return total; }
    }

    // Occupancy row
    public static class OccupancyRow {
        public final String date;
        public final int    roomsAvailable;
        public final int    roomsOccupied;
        public final double occupancyPct;

        public OccupancyRow(String date, int available, int occupied) {
            this.date           = date;
            this.roomsAvailable = available;
            this.roomsOccupied  = occupied;
            this.occupancyPct   = available > 0 ? (occupied * 100.0 / available) : 0;
        }

        public String getDate()           { return date; }
        public int    getRoomsAvailable() { return roomsAvailable; }
        public int    getRoomsOccupied()  { return roomsOccupied; }
        public double getOccupancyPct()   { return Math.round(occupancyPct * 10.0) / 10.0; }
    }

    // Feedback summary row
    public static class FeedbackRow {
        public final int    reservationId;
        public final String guestName;
        public final int    rating;
        public final String comment;
        public final String date;
        public final String sentimentTag;

        public FeedbackRow(Feedback f) {
            this.reservationId = f.getReservation() != null ? f.getReservation().getId() : 0;
            this.guestName     = f.getGuest() != null
                    ? f.getGuest().getFirstName() + " " + f.getGuest().getLastName() : "Anonymous";
            this.rating        = f.getRating();
            this.comment       = f.getComment() != null ? f.getComment() : "";
            this.date          = f.getSubmissionDate() != null ? f.getSubmissionDate().toString() : "";
            this.sentimentTag  = f.getSentimentTag() != null ? f.getSentimentTag() : "";
        }

        public int    getReservationId() { return reservationId; }
        public String getGuestName()     { return guestName; }
        public int    getRating()        { return rating; }
        public String getComment()       { return comment; }
        public String getDate()          { return date; }
        public String getSentimentTag()  { return sentimentTag; }
    }

    // Revenue by day
    public List<RevenueRow> getRevenueByDay(LocalDate from, LocalDate to) {
        List<Payment> payments = paymentRepo.findAll().stream()
                .filter(p -> p.getPaymentDate() != null
                        && !p.getPaymentDate().isBefore(from)
                        && !p.getPaymentDate().isAfter(to)
                        && p.getAmount() > 0)
                .collect(Collectors.toList());

        Map<LocalDate, List<Payment>> byDay = payments.stream()
                .collect(Collectors.groupingBy(Payment::getPaymentDate));

        return byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new RevenueRow(
                        e.getKey().toString(),
                        e.getValue().size(),
                        e.getValue().stream().mapToDouble(Payment::getAmount).sum(),
                        0.0))
                .collect(Collectors.toList());
    }

    // Revenue by week
    public List<RevenueRow> getRevenueByWeek(LocalDate from, LocalDate to) {
        WeekFields wf = WeekFields.of(Locale.getDefault());
        List<Payment> payments = paymentRepo.findAll().stream()
                .filter(p -> p.getPaymentDate() != null
                        && !p.getPaymentDate().isBefore(from)
                        && !p.getPaymentDate().isAfter(to)
                        && p.getAmount() > 0)
                .collect(Collectors.toList());

        Map<String, List<Payment>> byWeek = payments.stream()
                .collect(Collectors.groupingBy(p -> {
                    int year = p.getPaymentDate().getYear();
                    int week = p.getPaymentDate().get(wf.weekOfWeekBasedYear());
                    return year + "-W" + String.format("%02d", week);
                }));

        return byWeek.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new RevenueRow(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream().mapToDouble(Payment::getAmount).sum(),
                        0.0))
                .collect(Collectors.toList());
    }

    // Revenue by month
    public List<RevenueRow> getRevenueByMonth(LocalDate from, LocalDate to) {
        List<Payment> payments = paymentRepo.findAll().stream()
                .filter(p -> p.getPaymentDate() != null
                        && !p.getPaymentDate().isBefore(from)
                        && !p.getPaymentDate().isAfter(to)
                        && p.getAmount() > 0)
                .collect(Collectors.toList());

        Map<String, List<Payment>> byMonth = payments.stream()
                .collect(Collectors.groupingBy(p ->
                        p.getPaymentDate().getYear() + "-" +
                                String.format("%02d", p.getPaymentDate().getMonthValue())));

        return byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new RevenueRow(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream().mapToDouble(Payment::getAmount).sum(),
                        0.0))
                .collect(Collectors.toList());
    }

    // Occupancy by day (assumes total rooms = 20 for now — replace with DB query if rooms are seeded)
    public List<OccupancyRow> getOccupancyByDay(LocalDate from, LocalDate to, int totalRooms) {
        List<Reservation> reservations = reservationRepo.findAll().stream()
                .filter(r -> r.getStatus() != null && !r.getStatus().equalsIgnoreCase("Cancelled"))
                .collect(Collectors.toList());

        List<OccupancyRow> rows = new ArrayList<>();
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            final LocalDate day = cursor;
            long occupied = reservations.stream()
                    .filter(r -> r.getCheckInDate() != null && r.getCheckOutDate() != null
                            && !day.isBefore(r.getCheckInDate())
                            && day.isBefore(r.getCheckOutDate()))
                    .count();
            rows.add(new OccupancyRow(day.toString(), totalRooms, (int) occupied));
            cursor = cursor.plusDays(1);
        }
        return rows;
    }

    // Feedback summary
    public List<FeedbackRow> getFeedbackSummary() {
        return feedbackRepo.findAll().stream()
                .map(FeedbackRow::new)
                .collect(Collectors.toList());
    }

    public double getAverageRating() {
        List<Feedback> all = feedbackRepo.findAll();
        if (all.isEmpty()) return 0;
        return all.stream().mapToInt(Feedback::getRating).average().orElse(0);
    }

    public Map<String, Long> getSentimentCounts() {
        return feedbackRepo.findAll().stream()
                .collect(Collectors.groupingBy(
                        f -> f.getSentimentTag() != null ? f.getSentimentTag() : "Unknown",
                        Collectors.counting()));
    }

    // Activity logs
    public List<Log> getActivityLogs() {
        return logRepo.findAll();
    }
}