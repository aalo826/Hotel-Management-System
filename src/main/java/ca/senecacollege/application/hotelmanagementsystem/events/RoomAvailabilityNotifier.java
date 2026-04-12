package ca.senecacollege.application.hotelmanagementsystem.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoomAvailabilityNotifier {

    private static final Logger logger = LogManager.getLogger(RoomAvailabilityNotifier.class);

    private final List<RoomAvailabilityObserver> observers = new CopyOnWriteArrayList<>();

    private static RoomAvailabilityNotifier instance;

    private RoomAvailabilityNotifier() {}

    public static synchronized RoomAvailabilityNotifier getInstance() {
        if (instance == null) instance = new RoomAvailabilityNotifier();
        return instance;
    }

    // Subscription management
    public void subscribe(RoomAvailabilityObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            logger.debug("Observer registered: {}", observer.getClass().getSimpleName());
        }
    }

    public void unsubscribe(RoomAvailabilityObserver observer) {
        observers.remove(observer);
        logger.debug("Observer unregistered: {}", observer.getClass().getSimpleName());
    }

    // Event dispatch
    public void notifyObservers(RoomAvailabilityEvent event) {
        logger.info("Dispatching event to {} observer(s): {}", observers.size(), event);
        for (RoomAvailabilityObserver observer : observers) {
            try {
                observer.onRoomAvailabilityChanged(event);
            } catch (Exception e) {
                // One bad observer must never stop the others from receiving the event
                logger.error("Observer {} threw an exception: {}",
                        observer.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }
}