package ca.senecacollege.application.hotelmanagementsystem.service;

import ca.senecacollege.application.hotelmanagementsystem.model.Guest;
import ca.senecacollege.application.hotelmanagementsystem.repository.GenericRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoyaltyService {

    private static final Logger logger = LogManager.getLogger(LoyaltyService.class);

    // 1 point earned per $1 paid
    private static final double EARN_RATE = 1.0;

    // 100 points = $1 discount
    private static final double REDEEM_RATE = 0.01;

    // Max points redeemable per reservation (caps at $50 discount)
    private static final int MAX_REDEEM_PER_RESERVATION = 5000;

    private final GenericRepository<Guest> guestRepo = new GenericRepository<>(Guest.class);

    // Earn points after a completed payment — call this after checkout
    public void earnPoints(Guest guest, double amountPaid) {
        if (guest == null || guest.getId() <= 0) return;

        int pointsEarned = (int) (amountPaid * EARN_RATE);
        guest.setLoyaltyPoints(guest.getLoyaltyPoints() + pointsEarned);
        guestRepo.update(guest);

        logger.info("Loyalty earned — Guest: {} {} | Points: +{} | Balance: {}",
                guest.getFirstName(), guest.getLastName(),
                pointsEarned, guest.getLoyaltyPoints());
    }

    // Calculate the dollar discount for a given points redemption amount
    public double calculateRedemptionDiscount(int pointsToRedeem) {
        int capped = Math.min(pointsToRedeem, MAX_REDEEM_PER_RESERVATION);
        return capped * REDEEM_RATE;
    }

    // Redeem points — deducts from balance and returns the dollar discount applied
    public double redeemPoints(Guest guest, int pointsToRedeem) throws Exception {
        if (guest == null || guest.getId() <= 0)
            throw new Exception("Guest must be a registered member to redeem points.");

        if (pointsToRedeem <= 0)
            throw new Exception("Points to redeem must be greater than zero.");

        if (pointsToRedeem > guest.getLoyaltyPoints())
            throw new Exception("Insufficient points. Available: " + guest.getLoyaltyPoints());

        int capped = Math.min(pointsToRedeem, MAX_REDEEM_PER_RESERVATION);
        double discount = capped * REDEEM_RATE;

        guest.setLoyaltyPoints(guest.getLoyaltyPoints() - capped);
        guestRepo.update(guest);

        logger.info("Loyalty redeemed — Guest: {} {} | Points: -{} | Discount: ${} | Balance: {}",
                guest.getFirstName(), guest.getLastName(),
                capped, discount, guest.getLoyaltyPoints());

        return discount;
    }

    // Preview how many points a guest would earn for a given total
    public int previewPointsEarned(double total) {
        return (int) (total * EARN_RATE);
    }

    public int getMaxRedeemPerReservation() { return MAX_REDEEM_PER_RESERVATION; }
    public double getEarnRate()   { return EARN_RATE; }
    public double getRedeemRate() { return REDEEM_RATE; }
}