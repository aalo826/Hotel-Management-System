package ca.senecacollege.application.hotelmanagementsystem.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Waitlist")
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "guest_name", nullable = false)
    private String guestName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "desired_room_type", nullable = false)
    private String desiredRoomType;

    @Column(name = "desired_check_in", nullable = false)
    private LocalDate desiredCheckIn;

    @Column(name = "desired_check_out", nullable = false)
    private LocalDate desiredCheckOut;

    @Column(name = "added_on", nullable = false)
    private LocalDate addedOn;

    @Column(name = "status", length = 30)
    private String status; // "Waiting", "Converted", "Removed"

    public Waitlist() {
        this.addedOn = LocalDate.now();
        this.status  = "Waiting";
    }

    // Getters & setters
    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }

    public String getGuestName()              { return guestName; }
    public void setGuestName(String n)        { this.guestName = n; }

    public String getPhone()                  { return phone; }
    public void setPhone(String p)            { this.phone = p; }

    public String getDesiredRoomType()        { return desiredRoomType; }
    public void setDesiredRoomType(String t)  { this.desiredRoomType = t; }

    public LocalDate getDesiredCheckIn()      { return desiredCheckIn; }
    public void setDesiredCheckIn(LocalDate d){ this.desiredCheckIn = d; }

    public LocalDate getDesiredCheckOut()     { return desiredCheckOut; }
    public void setDesiredCheckOut(LocalDate d){ this.desiredCheckOut = d; }

    public LocalDate getAddedOn()             { return addedOn; }
    public void setAddedOn(LocalDate d)       { this.addedOn = d; }

    public String getStatus()                 { return status; }
    public void setStatus(String s)           { this.status = s; }
}