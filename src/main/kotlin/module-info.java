module dev.pbt.casigma {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.reflect;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires koin.core.jvm;
    requires exposed.core;
    requires exposed.java.time;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires org.slf4j;
    requires flyway.core;
    requires spring.security.crypto;
    requires kotlinx.datetime;

    exports dev.pbt.casigma;
    opens dev.pbt.casigma.controllers to javafx.fxml;
}