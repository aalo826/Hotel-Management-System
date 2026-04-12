package ca.senecacollege.application.hotelmanagementsystem.service;

import ca.senecacollege.application.hotelmanagementsystem.events.RoomAvailabilityEvent;
import ca.senecacollege.application.hotelmanagementsystem.events.RoomAvailabilityNotifier;
import ca.senecacollege.application.hotelmanagementsystem.model.*;
import ca.senecacollege.application.hotelmanagementsystem.repository.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookingService {

    private static final Logger logger = LogManager.getLogger(BookingService.class);

    private final LoyaltyService loyaltyService = new LoyaltyService();

    // Convenience overload — single default room, no discount
    public void completeBooking(Guest guest, LocalDate checkIn, LocalDate checkOut) throws Exception {
        Room defaultRoom = new Room();
        defaultRoom.setType("Standard");
        defaultRoom.setPrice(200.0);
        completeBooking(guest, List.of(defaultRoom), new ArrayList<>(), checkIn, checkOut, 0.0, "system");
    }

    // 5-arg overload — no discount, no actor
    public void completeBooking(Guest guest, List<Room> rooms, List<String> serviceOptions,
                                LocalDate checkIn, LocalDate checkOut) throws Exception {
        completeBooking(guest, rooms, serviceOptions, checkIn, checkOut, 0.0, "system");
    }

    // Full booking — 7 args, used by AdminController
    public void completeBooking(Guest guest, List<Room> rooms, List<String> serviceOptions,
                                LocalDate checkIn, LocalDate checkOut,
                                double discountPct, String actorName) throws Exception {

        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            long nights = Math.max(1, ChronoUnit.DAYS.between(checkIn, checkOut));

            // Fix: re-attach rooms inside THIS EntityManager to avoid detached entity error
            List<Room> managedRooms = new ArrayList<>();
            for (Room r : rooms) {
                if (r.getId() > 0) {
                    Room managed = em.find(Room.class, r.getId());
                    if (managed == null) throw new Exception("Room #" + r.getId() + " not found.");
                    managedRooms.add(managed);
                } else {
                    // Transient room (e.g. default room) — persist it
                    em.persist(r);
                    managedRooms.add(r);
                }
            }

            double roomRatePerNight = managedRooms.stream().mapToDouble(Room::getPrice).sum();
            String roomType   = managedRooms.isEmpty() ? "Standard" : managedRooms.get(0).getType();
            String roomNumber = managedRooms.isEmpty() ? "N/A"
                    : (managedRooms.get(0).getRoomNumber() != null
                    ? managedRooms.get(0).getRoomNumber() : "N/A");

            // Strategy pattern
            PriceStrategy strategy = PriceStrategy.forDates(checkIn, checkOut);
            double basePrice = strategy.calculate(roomRatePerNight, nights);
            String strategyName = strategy instanceof WeekendPricingStrategy ? "Weekend (+20%)" : "Standard";

            // Decorator pattern — per-night aware
            IBooking bookingProcess = new BaseBooking(basePrice, roomType);
            if (serviceOptions != null) {
                if (serviceOptions.contains("wifi"))      bookingProcess = ExtraServices.addWifi(bookingProcess, nights);
                if (serviceOptions.contains("spa"))       bookingProcess = ExtraServices.addSpa(bookingProcess, nights);
                if (serviceOptions.contains("breakfast")) bookingProcess = ExtraServices.addBreakfast(bookingProcess, nights);
                if (serviceOptions.contains("parking"))   bookingProcess = ExtraServices.addParking(bookingProcess, nights);
            }

            double subtotal    = bookingProcess.calculateTotal();
            double discountAmt = subtotal * (discountPct / 100.0);
            double finalTotal  = subtotal - discountAmt;

            // Persist reservation
            Reservation reservation = new Reservation();
            reservation.setCheckInDate(checkIn);
            reservation.setCheckOutDate(checkOut);
            reservation.setStatus("Confirmed");
            reservation.setTotalPrice(finalTotal);
            reservation.setRooms(managedRooms);

            if (guest.getId() > 0) {
                reservation.setGuest(em.merge(guest));
            } else {
                em.persist(guest);
                reservation.setGuest(guest);
            }
            em.persist(reservation);

            Payment payment = new Payment();
            payment.setReservation(reservation);
            payment.setAmount(finalTotal);
            payment.setMethod("Pending");
            payment.setStatus("Pending");
            em.persist(payment);

            transaction.commit();

            // Observer: room occupied
            String guestName = guest.getFirstName() + " " + guest.getLastName();
            RoomAvailabilityNotifier.getInstance().notifyObservers(
                    new RoomAvailabilityEvent(
                            RoomAvailabilityEvent.Type.ROOM_BECAME_UNAVAILABLE,
                            roomNumber, roomType, guestName));

            logger.info("Booking confirmed — Guest: {} | Strategy: {} | Nights: {} | Discount: {}% | Total: ${}",
                    guestName, strategyName, nights, discountPct, finalTotal);

        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            logger.error("Booking FAILED for guest {}: {}", guest.getLastName(), e.getMessage(), e);
            throw e;
        } finally {
            em.close();
        }
    }

    // Returns available rooms of a given type not overlapping the requested dates
    public List<Room> getAvailableRooms(String type, LocalDate checkIn, LocalDate checkOut) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            // Query from the owning side (Reservation has the rooms collection)
            List<Integer> occupiedIds = em.createQuery(
                            "SELECT r.id FROM Reservation res JOIN res.rooms r " +
                                    "WHERE res.status IN ('Confirmed', 'Checked In') " +
                                    "AND res.checkInDate < :checkOut AND res.checkOutDate > :checkIn",
                            Integer.class)
                    .setParameter("checkIn", checkIn)
                    .setParameter("checkOut", checkOut)
                    .getResultList();

            return em.createQuery("FROM Room r WHERE r.type = :type AND r.isAvailable = true", Room.class)
                    .setParameter("type", type)
                    .getResultList()
                    .stream()
                    .filter(r -> !occupiedIds.contains(r.getId()))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    // Check-in — transitions Confirmed → Checked In
    public void checkInReservation(int reservationId, String actorName) throws Exception {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation == null) throw new Exception("Reservation #" + reservationId + " not found.");
            reservation.setStatus("Checked In");
            em.merge(reservation);
            transaction.commit();
            logger.info("Check-in complete — Reservation #{} by {}", reservationId, actorName);
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // Checkout — transitions to Checked Out, earns loyalty, fires observer
    public void checkoutReservation(int reservationId, String actorName) throws Exception {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation == null) throw new Exception("Reservation #" + reservationId + " not found.");
            reservation.setStatus("Checked Out");
            em.merge(reservation);
            transaction.commit();

            Guest guest = reservation.getGuest();
            if (guest != null && guest.getId() > 0)
                loyaltyService.earnPoints(guest, reservation.getTotalPrice());

            String roomNumber = (reservation.getRooms() != null && !reservation.getRooms().isEmpty())
                    ? reservation.getRooms().get(0).getRoomNumber() : "N/A";
            String roomType = (reservation.getRooms() != null && !reservation.getRooms().isEmpty())
                    ? reservation.getRooms().get(0).getType() : "Unknown";

            RoomAvailabilityNotifier.getInstance().notifyObservers(
                    new RoomAvailabilityEvent(
                            RoomAvailabilityEvent.Type.ROOM_BECAME_AVAILABLE,
                            roomNumber, roomType, actorName));

            logger.info("Checkout complete — Reservation #{} by {}", reservationId, actorName);
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // Cancellation — fires observer
    public void cancelReservation(int reservationId, String actorName) throws Exception {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation == null) throw new Exception("Reservation #" + reservationId + " not found.");
            reservation.setStatus("Cancelled");
            em.merge(reservation);
            transaction.commit();

            String roomNumber = (reservation.getRooms() != null && !reservation.getRooms().isEmpty())
                    ? reservation.getRooms().get(0).getRoomNumber() : "N/A";
            String roomType = (reservation.getRooms() != null && !reservation.getRooms().isEmpty())
                    ? reservation.getRooms().get(0).getType() : "Unknown";

            RoomAvailabilityNotifier.getInstance().notifyObservers(
                    new RoomAvailabilityEvent(
                            RoomAvailabilityEvent.Type.ROOM_BECAME_AVAILABLE,
                            roomNumber, roomType, actorName));

            logger.info("Cancellation complete — Reservation #{} by {}", reservationId, actorName);
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) transaction.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}