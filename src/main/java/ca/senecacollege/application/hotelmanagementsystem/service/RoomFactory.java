package ca.senecacollege.application.hotelmanagementsystem.service;

import ca.senecacollege.application.hotelmanagementsystem.model.Room;

public class RoomFactory {

    public static Room createRoom(String roomType) {
        Room room = new Room();

        switch (roomType.toLowerCase()) {
            case "single":
                room.setType("Single");
                room.setMaxOccupancy(2);
                room.setPrice(150.0);
                break;
            case "double":
                room.setType("Double");
                room.setMaxOccupancy(4);
                room.setPrice(250.0);
                break;
            case "penthouse":
                room.setType("Penthouse");
                room.setMaxOccupancy(4);
                room.setPrice(450.0);
                break;
            default:
                throw new IllegalArgumentException("Unknown room type: " + roomType);
        }
        return room;
    }
}