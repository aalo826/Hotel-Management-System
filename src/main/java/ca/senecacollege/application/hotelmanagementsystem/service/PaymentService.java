package ca.senecacollege.application.hotelmanagementsystem.service;

import ca.senecacollege.application.hotelmanagementsystem.model.Payment;
import ca.senecacollege.application.hotelmanagementsystem.model.Reservation;
import ca.senecacollege.application.hotelmanagementsystem.repository.GenericRepository;
import ca.senecacollege.application.hotelmanagementsystem.repository.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentService {

    private static final Logger logger = LogManager.getLogger(PaymentService.class);

    private final GenericRepository<Payment> paymentRepo = new GenericRepository<>(Payment.class);
    private final GenericRepository<Reservation> reservationRepo = new GenericRepository<>(Reservation.class);

    // Record a payment (cash, card, or loyalty)
    public Payment recordPayment(int reservationId, double amount, String method) throws Exception {
        if (amount <= 0) throw new Exception("Payment amount must be greater than zero.");

        Reservation reservation = reservationRepo.findById(reservationId);
        if (reservation == null) throw new Exception("Reservation #" + reservationId + " not found.");

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setStatus("Paid");
        payment.setPaymentDate(LocalDate.now());

        paymentRepo.save(payment);

        logger.info("Payment recorded — Reservation #{} | Amount: ${} | Method: {}",
                reservationId, amount, method);

        return payment;
    }

    // Record a refund as a negative payment entry
    public Payment recordRefund(int reservationId, double refundAmount, String reason) throws Exception {
        if (refundAmount <= 0) throw new Exception("Refund amount must be greater than zero.");

        Reservation reservation = reservationRepo.findById(reservationId);
        if (reservation == null) throw new Exception("Reservation #" + reservationId + " not found.");

        Payment refund = new Payment();
        refund.setReservation(reservation);
        refund.setAmount(-Math.abs(refundAmount)); // negative entry
        refund.setMethod("Refund");
        refund.setStatus("Refunded");
        refund.setPaymentDate(LocalDate.now());

        paymentRepo.save(refund);

        logger.info("Refund recorded — Reservation #{} | Amount: -${} | Reason: {}",
                reservationId, refundAmount, reason);

        return refund;
    }

    // Get the net balance paid for a reservation
    public double getNetPaid(int reservationId) {
        List<Payment> all = paymentRepo.findAll();
        return all.stream()
                .filter(p -> p.getReservation() != null
                        && p.getReservation().getId() == reservationId)
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    // Get remaining balance owed
    public double getRemainingBalance(int reservationId) {
        Reservation reservation = reservationRepo.findById(reservationId);
        if (reservation == null) return 0;
        double netPaid = getNetPaid(reservationId);
        return reservation.getTotalPrice() - netPaid;
    }

    // Update payment status
    public void updateStatus(int paymentId, String newStatus) {
        Payment payment = paymentRepo.findById(paymentId);
        if (payment != null) {
            payment.setStatus(newStatus);
            paymentRepo.update(payment);
            logger.info("Payment #{} status updated to: {}", paymentId, newStatus);
        }
    }

    public List<Payment> getAllPayments() {
        return paymentRepo.findAll();
    }
}