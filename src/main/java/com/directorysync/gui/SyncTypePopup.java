package com.directorysync.gui;

import javafx.stage.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class SyncTypePopup {

    //-1: not set
    //0: hard synchronization
    //1: soft synchronization
    static int syncType;

    public static int display() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Selection initialization mode");
        window.setMinWidth(250);
        window.setResizable(false);

        Label label = new Label();
        label.setText("How do you want to initialize the synchronization?");

        syncType = -1;
        Button hardSyncButton = new Button("Hard synchronization:\ndelete all then copy all");
        hardSyncButton.setOnAction(e -> { syncType = 0; window.close(); });
        hardSyncButton.setMinWidth(300); hardSyncButton.setMaxWidth(300);
        hardSyncButton.setTextAlignment(TextAlignment.CENTER);
        Button softSyncButton = new Button("Soft synchronization:\nonly copy missing files and solve conflicts");
        softSyncButton.setOnAction(e -> { syncType = 1; window.close(); });
        softSyncButton.setMinWidth(300); softSyncButton.setMaxWidth(300);
        softSyncButton.setTextAlignment(TextAlignment.CENTER);

        VBox buttons = new VBox(10);
        buttons.getChildren().addAll(hardSyncButton, softSyncButton);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, buttons);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER); 
        Scene scene = new Scene(layout);
        scene.getStylesheets().add("styleForm.css");
        window.setScene(scene);
        window.showAndWait();

        return syncType;
    }
}