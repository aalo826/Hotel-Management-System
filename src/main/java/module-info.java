module ca.senecacollege.application.hotelmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;

    requires jbcrypt;
    requires org.apache.logging.log4j;

    opens ca.senecacollege.application.hotelmanagementsystem to javafx.fxml;
    opens ca.senecacollege.application.hotelmanagementsystem.controller to javafx.fxml;

    opens ca.senecacollege.application.hotelmanagementsystem.model to org.hibernate.orm.core, javafx.base;

    exports ca.senecacollege.application.hotelmanagementsystem;
    exports ca.senecacollege.application.hotelmanagementsystem.service;
    exports ca.senecacollege.application.hotelmanagementsystem.events;
}