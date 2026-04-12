package ca.senecacollege.application.hotelmanagementsystem.service;

import ca.senecacollege.application.hotelmanagementsystem.events.RoomAvailabilityEvent;
import ca.senecacollege.application.hotelmanagementsystem.events.RoomAvailabilityObserver;
import ca.senecacollege.application.hotelmanagementsystem.events.RoomAvailabilityNotifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AdminNotificationService implements RoomAvailabilityObserver {

    private static final Logger logger = LogManager.getLogger(AdminNotificationService.class);
    private static final int MAX_NOTIFICATIONS = 50;

    private static AdminNotificationService instance;

    private AdminNotificationService() {
        // Auto-register with the notifier on creation
        RoomAvailabilityNotifier.getInstance().subscribe(this);
        logger.info("AdminNotificationService registered as observer.");
    }

    public static synchronized AdminNotificationService getInstance() {
        if (instance == null) instance = new AdminNotificationService();
        return instance;
    }

    // Inbox
    private final LinkedList<RoomAvailabilityEvent> notifications = new LinkedList<>();
    private int unreadCount = 0;

    @Override
    public void onRoomAvailabilityChanged(RoomAvailabilityEvent event) {
        if (event.getType() == RoomAvailabilityEvent.Type.ROOM_BECAME_AVAILABLE) {
            synchronized (notifications) {
                notifications.addFirst(event);         // newest first
                if (notifications.size() > MAX_NOTIFICATIONS)
                    notifications.removeLast();
                unreadCount++;
            }
            logger.info("Admin notified: Room {} ({}) is now available. Triggered by: {}",
                    event.getRoomNumber(), event.getRoomType(), event.getTriggeredBy());
        }
    }

    public List<RoomAvailabilityEvent> getNotifications() {
        synchronized (notifications) {
            return Collections.unmodifiableList(new LinkedList<>(notifications));
        }
    }

    public int getAndClearUnreadCount() {
        synchronized (notifications) {
            int count = unreadCount;
            unreadCount = 0;
            return count;
        }
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void clearAll() {
        synchronized (notifications) {
            notifications.clear();
            unreadCount = 0;
        }
    }
}