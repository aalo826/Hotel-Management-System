package ca.senecacollege.application.hotelmanagementsystem;

import ca.senecacollege.application.hotelmanagementsystem.model.Room;
import ca.senecacollege.application.hotelmanagementsystem.model.User;
import ca.senecacollege.application.hotelmanagementsystem.repository.GenericRepository;
import ca.senecacollege.application.hotelmanagementsystem.service.RoomFactory;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class DatabaseSeeder {

    private static final int SINGLE_COUNT    = 8;
    private static final int DOUBLE_COUNT    = 8;
    private static final int PENTHOUSE_COUNT = 4;

    public static void main(String[] args) {

        GenericRepository<User> userRepo = new GenericRepository<>(User.class);
        GenericRepository<Room> roomRepo = new GenericRepository<>(Room.class);

        System.out.println("=== Starting Database Seeding ===");

        // Users
        seedUser(userRepo, "manager", "manager123", "Manager");
        seedUser(userRepo, "admin",   "admin123",   "Admin");

        // Rooms
        List<Room> existing = roomRepo.findAll();

        int roomNum = 101;
        roomNum = seedRooms(roomRepo, existing, "Single",    SINGLE_COUNT,    roomNum);
        roomNum = seedRooms(roomRepo, existing, "Double",    DOUBLE_COUNT,    roomNum);
        seedRooms(roomRepo, existing, "Penthouse", PENTHOUSE_COUNT, roomNum);

        System.out.println("=== Seeding Complete ===");
    }

    // Helpers
    private static void seedUser(GenericRepository<User> repo,
                                 String username, String rawPassword, String role) {
        boolean exists = repo.findAll().stream()
                .anyMatch(u -> u.getUsername().equals(username));
        if (exists) {
            System.out.println("  SKIP user: " + username + " (already exists)");
            return;
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        u.setRole(role);
        repo.save(u);
        System.out.println("  SEEDED user: " + username + " / role: " + role);
    }

    private static int seedRooms(GenericRepository<Room> repo,
                                 List<Room> existing,
                                 String type,
                                 int count,
                                 int startRoomNum) {
        int num = startRoomNum;
        for (int i = 0; i < count; i++, num++) {
            String roomNumber = String.valueOf(num);
            boolean exists = existing.stream()
                    .anyMatch(r -> r.getRoomNumber().equals(roomNumber));
            if (exists) {
                System.out.println("  SKIP room: " + roomNumber + " (already exists)");
                continue;
            }
            // RoomFactory sets type as "Single" / "Double" / "Penthouse" — all capitalised
            Room room = RoomFactory.createRoom(type.toLowerCase());
            room.setRoomNumber(roomNumber);
            room.setAvailable(true);
            repo.save(room);
            System.out.println("  SEEDED room: " + roomNumber + " (" + type + ")");
        }
        return num;
    }
}