package ca.senecacollege.application.hotelmanagementsystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "room_number", unique = true, nullable = false)
    private String roomNumber;

    @Column(nullable = false)
    private String type;

    @Column(name = "max_occupancy")
    private int maxOccupancy;

    private double price;

    @Column(name = "is_available")
    private boolean isAvailable;

    public Room() {
        this.isAvailable = true;
    }

    public boolean validateCapacity(int totalGuests) {
        return totalGuests <= this.maxOccupancy;
    }

    public void markAsAvailable() {
        this.isAvailable = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getType() { return type; }
    public void setType(String type) {
        this.type = type;

        if ("Single".equalsIgnoreCase(type) || "Deluxe".equalsIgnoreCase(type) || "Penthouse".equalsIgnoreCase(type)) {
            this.maxOccupancy = 2;
        } else if ("Double".equalsIgnoreCase(type)) {
            this.maxOccupancy = 4;
        }
    }

    public int getMaxOccupancy() { return maxOccupancy; }
    public void setMaxOccupancy(int maxOccupancy) { this.maxOccupancy = maxOccupancy; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}