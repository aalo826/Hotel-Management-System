package ca.senecacollege.application.hotelmanagementsystem.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Reservations_Rooms")
public class ReservationRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "res_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    private String specialInstructions;

    public ReservationRoom() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
}