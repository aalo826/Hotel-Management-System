package ca.senecacollege.application.hotelmanagementsystem.service;

public class StandardPricingStrategy implements PriceStrategy {
    @Override
    public double calculate(double basePrice, long numberOfNights) {
        return basePrice * numberOfNights;
    }
}