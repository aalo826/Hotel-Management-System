module ca.senecacollege.application.hotelmanagementsystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;

    opens ca.senecacollege.application.hotelmanagementsystem to javafx.fxml;
    opens ca.senecacollege.application.hotelmanagementsystem.controller to javafx.fxml;

    opens ca.senecacollege.application.hotelmanagementsystem.model to org.hibernate.orm.core;

    exports ca.senecacollege.application.hotelmanagementsystem;
}