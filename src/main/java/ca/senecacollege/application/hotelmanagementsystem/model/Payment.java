package ca.senecacollege.application.hotelmanagementsystem.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "res_id")
    private Reservation reservation;

    private double amount;
    private LocalDate paymentDate;
    private String method;
    private String status;

    public Payment() {
        this.paymentDate = LocalDate.now();
    }

    public String getGuestName() {
        if (reservation != null && reservation.getGuest() != null) {
            String firstName = reservation.getGuest().getFirstName();
            String lastName = reservation.getGuest().getLastName();
            return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        }
        return "N/A";
    }

    public String getPhone() {
        if (reservation != null && reservation.getGuest() != null) {
            return reservation.getGuest().getPhone();
        }
        return "N/A";
    }

    public String getRoomType() {
        if (reservation != null && reservation.getRooms() != null && !reservation.getRooms().isEmpty()) {
            return reservation.getRooms().get(0).getType();
        }
        return "Standard";
    }

    public LocalDate getCheckInDate() {
        return reservation != null ? reservation.getCheckInDate() : null;
    }

    public LocalDate getCheckOutDate() {
        return reservation != null ? reservation.getCheckOutDate() : null;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}