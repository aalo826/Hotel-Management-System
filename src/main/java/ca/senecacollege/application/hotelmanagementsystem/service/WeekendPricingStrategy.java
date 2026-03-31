package ca.senecacollege.application.hotelmanagementsystem.service;

public class WeekendPricingStrategy implements PriceStrategy {
    @Override
    public double calculate(double basePrice, long numberOfNights) {
        return (basePrice * 1.2) * numberOfNights;
    }
}
