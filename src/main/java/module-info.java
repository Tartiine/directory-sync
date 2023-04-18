module com.directorysync.gui {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.directorysync.gui to javafx.fxml;
    exports com.directorysync.gui;
}
