package ca.senecacollege.application.hotelmanagementsystem.service;

import ca.senecacollege.application.hotelmanagementsystem.model.Feedback;
import ca.senecacollege.application.hotelmanagementsystem.model.Guest;
import ca.senecacollege.application.hotelmanagementsystem.model.Reservation;
import ca.senecacollege.application.hotelmanagementsystem.repository.GenericRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;

public class FeedbackService {

    private static final Logger logger = LogManager.getLogger(FeedbackService.class);
    private static final int MAX_COMMENT_LENGTH = 500;

    private final GenericRepository<Feedback> feedbackRepo = new GenericRepository<>(Feedback.class);

    public void submitFeedback(int rating, String comment, Guest guest, Reservation reservation)
            throws Exception {

        if (rating < 1 || rating > 5) {
            throw new Exception("Rating must be between 1 and 5.");
        }
        if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
            throw new Exception("Comment must be " + MAX_COMMENT_LENGTH + " characters or fewer.");
        }

        if (reservation != null && !"Checked Out".equalsIgnoreCase(reservation.getStatus())) {
            throw new Exception("Feedback can only be submitted after checkout.");
        }

        Feedback feedback = new Feedback();
        feedback.setRating(rating);
        feedback.setComment(comment != null ? comment.trim() : "");
        feedback.setSubmissionDate(LocalDate.now());
        feedback.setGuest(guest);
        feedback.setReservation(reservation);

        feedback.setSentimentTag(resolveSentimentTag(rating));

        feedbackRepo.save(feedback);

        logger.info("Feedback saved — Rating: {} | Sentiment: {} | Guest: {}",
                rating, feedback.getSentimentTag(),
                guest != null ? guest.getFirstName() + " " + guest.getLastName() : "Anonymous");
    }

    private String resolveSentimentTag(int rating) {
        if (rating >= 4) return "Positive";
        if (rating == 3) return "Neutral";
        return "Negative";
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepo.findAll();
    }
}