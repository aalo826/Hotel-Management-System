package ca.senecacollege.application.hotelmanagementsystem.service;

import ca.senecacollege.application.hotelmanagementsystem.model.Guest;
import ca.senecacollege.application.hotelmanagementsystem.model.Reservation;
import ca.senecacollege.application.hotelmanagementsystem.repository.GenericRepository;
import ca.senecacollege.application.hotelmanagementsystem.repository.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AdminService {

    private final GenericRepository<Reservation> reservationRepo = new GenericRepository<>(Reservation.class);
    private final GenericRepository<Guest>       guestRepo       = new GenericRepository<>(Guest.class);

    // Search Reservations
    public List<Reservation> searchReservations(String name,
                                                String phone,
                                                LocalDate date,
                                                String status,
                                                String roomType) {
        return reservationRepo.findAll().stream()
                .filter(r -> {
                    if (name != null && !name.isBlank()) {
                        String full = (r.getGuest() != null)
                                ? r.getGuest().getFirstName() + " " + r.getGuest().getLastName()
                                : "";
                        if (!full.toLowerCase().contains(name.toLowerCase())) return false;
                    }
                    return true;
                })
                .filter(r -> {
                    if (phone != null && !phone.isBlank()) {
                        String p = (r.getGuest() != null) ? r.getGuest().getPhone() : "";
                        if (p == null || !p.contains(phone)) return false;
                    }
                    return true;
                })
                .filter(r -> {
                    if (date != null) {
                        return (r.getCheckInDate() != null && !r.getCheckInDate().isAfter(date))
                                && (r.getCheckOutDate() != null && !r.getCheckOutDate().isBefore(date));
                    }
                    return true;
                })
                .filter(r -> {
                    if (status != null && !status.equals("All")) {
                        return status.equalsIgnoreCase(r.getStatus());
                    }
                    return true;
                })
                .filter(r -> {
                    if (roomType != null && !roomType.equals("All")) {
                        return r.getRooms().stream()
                                .anyMatch(room -> roomType.equalsIgnoreCase(room.getType()));
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    // Discount Validations
    public boolean validateDiscount(double discountPct, String role) {
        if ("Manager".equalsIgnoreCase(role)) return discountPct >= 0 && discountPct <= 30.0;
        if ("Admin".equalsIgnoreCase(role))   return discountPct >= 0 && discountPct <= 15.0;
        return false;
    }

    public double getMaxDiscount(String role) {
        if ("Manager".equalsIgnoreCase(role)) return 30.0;
        if ("Admin".equalsIgnoreCase(role))   return 15.0;
        return 0.0;
    }

    public double applyDiscount(double originalAmount, double discountPct) {
        return originalAmount * (1.0 - discountPct / 100.0);
    }

    // Loyalty Enrollment
    public String enrollLoyalty(Guest guest) {
        if (guest.getLoyaltyNumber() != null && !guest.getLoyaltyNumber().isBlank())
            return guest.getLoyaltyNumber(); // already enrolled

        String loyaltyNumber = "LYL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        guest.setLoyaltyNumber(loyaltyNumber);
        if (guest.getLoyaltyPoints() < 0) guest.setLoyaltyPoints(0);
        guestRepo.update(guest);
        return loyaltyNumber;
    }

    // Guest Lookup
    public List<Guest> searchGuests(String nameOrPhone) {
        String q = nameOrPhone == null ? "" : nameOrPhone.toLowerCase();
        return guestRepo.findAll().stream()
                .filter(g -> {
                    String full = g.getFirstName() + " " + g.getLastName();
                    String phone = g.getPhone() != null ? g.getPhone() : "";
                    return full.toLowerCase().contains(q) || phone.contains(q);
                })
                .collect(Collectors.toList());
    }

    public List<Reservation> getAllReservations() {
        return reservationRepo.findAll();
    }

    public Reservation getReservationById(int id) {
        return reservationRepo.findById(id);
    }

    // NET BALANCE HELPER  (uses JPA sum rather than looping in controller)
    public double getNetPaid(int reservationId) {
        EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
        try {
            Double sum = em.createQuery(
                            "SELECT SUM(p.amount) FROM Payment p WHERE p.reservation.id = :rid",
                            Double.class)
                    .setParameter("rid", reservationId)
                    .getSingleResult();
            return sum != null ? sum : 0.0;
        } finally {
            em.close();
        }
    }
}