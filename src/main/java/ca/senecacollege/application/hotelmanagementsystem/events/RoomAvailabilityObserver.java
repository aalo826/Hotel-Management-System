package ca.senecacollege.application.hotelmanagementsystem.events;

public interface RoomAvailabilityObserver {
    void onRoomAvailabilityChanged(RoomAvailabilityEvent event);
}