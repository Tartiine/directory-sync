module com.directorysync.gui {
    requires transitive javafx.controls;
    requires javafx.fxml;

    opens com.directorysync.gui to javafx.fxml;
    exports com.directorysync.gui;
}
