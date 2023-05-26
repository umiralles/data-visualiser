module visualiser.datavisualiser {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires java.sql;
    requires javafx.web;
    requires json;

    opens visualiser.datavisualiser to javafx.fxml;
    exports visualiser.datavisualiser;
    exports visualiser.datavisualiser.controllers;
    opens visualiser.datavisualiser.controllers to javafx.fxml;
}