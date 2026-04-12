package ca.senecacollege.application.hotelmanagementsystem.service;

import ca.senecacollege.application.hotelmanagementsystem.model.Log;
import ca.senecacollege.application.hotelmanagementsystem.model.User;
import ca.senecacollege.application.hotelmanagementsystem.repository.GenericRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.List;

public class LogService {

    private static final Logger logger = LogManager.getLogger(LogService.class);

    private final GenericRepository<Log> logRepo = new GenericRepository<>(Log.class);

    public void log(String actor, String action, String entityType, String entityId,
                    String message, User user) {
        try {
            Log entry = new Log();
            entry.setTimestamp(LocalDateTime.now());
            entry.setActor(actor);
            entry.setAction(action);
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setMessage(message);
            entry.setUser(user);

            logRepo.save(entry);
        } catch (Exception e) {
            // Logging must never crash the main flow
            logger.error("Failed to write log entry — action: {} actor: {} error: {}",
                    action, actor, e.getMessage());
        }
    }

    // Convenience overload — no entity context needed
    public void log(String actor, String action, String message, User user) {
        log(actor, action, "", "", message, user);
    }

    public List<Log> getAllLogs() {
        return logRepo.findAll();
    }
}