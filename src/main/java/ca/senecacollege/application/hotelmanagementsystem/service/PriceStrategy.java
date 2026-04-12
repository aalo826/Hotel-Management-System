package ca.senecacollege.application.hotelmanagementsystem.service;

import java.time.LocalDate;
import java.time.DayOfWeek;

public interface PriceStrategy {

    double calculate(double basePrice, long numberOfNights);

    static PriceStrategy forDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) return new StandardPricingStrategy();

        long totalNights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        long weekendNights = 0;
        for (long i = 0; i < totalNights; i++) {
            DayOfWeek day = checkIn.plusDays(i).getDayOfWeek();
            if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
                weekendNights++;
            }
        }

        return (weekendNights * 2 >= totalNights) ? new WeekendPricingStrategy() : new StandardPricingStrategy();
    }
}