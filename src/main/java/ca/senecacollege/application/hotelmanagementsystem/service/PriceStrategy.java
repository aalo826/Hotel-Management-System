package ca.senecacollege.application.hotelmanagementsystem.service;

import java.time.LocalDate;

public interface PriceStrategy {
    double calculate(double basePrice, long numberOfNights);
}