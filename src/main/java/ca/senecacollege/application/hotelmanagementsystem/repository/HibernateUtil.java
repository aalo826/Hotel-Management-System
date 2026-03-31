package ca.senecacollege.application.hotelmanagementsystem.repository;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class HibernateUtil {

    private static EntityManagerFactory sessionFactory;

    private HibernateUtil() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        if (sessionFactory == null) {
            try {
                sessionFactory = Persistence.createEntityManagerFactory("HotelPU");
            } catch (Exception e) {
                System.err.println("Initial EntityManagerFactory creation failed." + e);
                throw new ExceptionInInitializerError(e);
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}