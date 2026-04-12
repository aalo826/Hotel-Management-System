package ca.senecacollege.application.hotelmanagementsystem.service;

public class BaseBooking implements IBooking {
    private double price;
    private String type;

    public BaseBooking(double price, String type) {
        this.price = price;
        this.type = type;
    }

    @Override
    public double calculateTotal() {
        return price;
    }

    @Override
    public String getDescription() {
        return "Room (" + type + ")";
    }
}