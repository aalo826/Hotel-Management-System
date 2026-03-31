package ca.senecacollege.application.hotelmanagementsystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Add_On")
public class AddOn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private double price;
    private String pricingModel;

    @ManyToOne
    @JoinColumn(name = "res_id")
    private Reservation reservation;

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }
}