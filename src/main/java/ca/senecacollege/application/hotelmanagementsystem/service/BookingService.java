package ca.senecacollege.application.hotelmanagementsystem.service;

import ca.senecacollege.application.hotelmanagementsystem.model.Guest;
import ca.senecacollege.application.hotelmanagementsystem.model.Reservation;
import ca.senecacollege.application.hotelmanagementsystem.repository.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;

public class BookingService {

    public void completeBooking(Guest guest, LocalDate checkIn, LocalDate checkOut) throws Exception {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            Reservation reservation = new Reservation();
            reservation.setCheckInDate(checkIn);
            reservation.setCheckOutDate(checkOut);
            reservation.setStatus("Confirmed");

            double totalPrice = calculatePrice(checkIn, checkOut);
            reservation.setTotalPrice(totalPrice);

            if (guest.getId() > 0) {
                // Existing guest (member) — merge instead of persist
                Guest managedGuest = em.merge(guest);
                reservation.setGuest(managedGuest);
            } else {
                // New guest — persist
                em.persist(guest);
                reservation.setGuest(guest);
            }

            em.persist(reservation);
            transaction.commit();
            System.out.println("Booking successfully persisted to database.");

        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new Exception("Database error: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    private double calculatePrice(LocalDate start, LocalDate end) {
        return 200.00;
    }

    public boolean isRoomAvailable(LocalDate date) {
        return true;
    }
}