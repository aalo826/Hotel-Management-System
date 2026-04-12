package ca.senecacollege.application.hotelmanagementsystem;

import ca.senecacollege.application.hotelmanagementsystem.repository.HibernateUtil;
import ca.senecacollege.application.hotelmanagementsystem.service.AdminNotificationService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jakarta.persistence.EntityManager;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                    "/ca/senecacollege/application/hotelmanagementsystem/view/kiosk-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1400, 900);
            stage.setTitle("Aurora Grand Hotel");
            stage.setScene(scene);
            stage.setWidth(1400);
            stage.setHeight(900);
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) {
            System.err.println("Failed to load UI: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("Initializing Hotel Management System...");

        try {
            EntityManager em = HibernateUtil.getEntityManagerFactory().createEntityManager();
            System.out.println("Database connection: OK");
            em.close();
        } catch (Exception e) {
            System.err.println("Database connection: FAILED — " + e.getMessage());
        }


        AdminNotificationService.getInstance();
        System.out.println("Room availability observer: registered");

        launch(args);
    }
}