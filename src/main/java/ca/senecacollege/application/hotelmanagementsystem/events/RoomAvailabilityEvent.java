package ca.senecacollege.application.hotelmanagementsystem.events;

import java.time.LocalDateTime;

public class RoomAvailabilityEvent {

    public enum Type {
        ROOM_BECAME_AVAILABLE,   // checkout/cancellation freed a room
        ROOM_BECAME_UNAVAILABLE  // new booking occupied a room
    }

    private final Type       type;
    private final String     roomNumber;
    private final String     roomType;
    private final String     triggeredBy;
    private final LocalDateTime timestamp;

    public RoomAvailabilityEvent(Type type, String roomNumber, String roomType, String triggeredBy) {
        this.type        = type;
        this.roomNumber  = roomNumber;
        this.roomType    = roomType;
        this.triggeredBy = triggeredBy;
        this.timestamp   = LocalDateTime.now();
    }

    public Type          getType()        { return type; }
    public String        getRoomNumber()  { return roomNumber; }
    public String        getRoomType()    { return roomType; }
    public String        getTriggeredBy() { return triggeredBy; }
    public LocalDateTime getTimestamp()   { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s — Room %s (%s) triggered by %s",
                timestamp, type, roomNumber, roomType, triggeredBy);
    }
}