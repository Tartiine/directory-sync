package com.directorysync.gui;

import javafx.stage.*;

import java.util.List;

import com.directorysync.Directory;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;

public class DirSelectGUI {

    static int selectedDirectoryId;

    public static int display(String title, String headerText, List<Directory> directories) {        
        selectedDirectoryId = -1;
        
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(400);
        window.setResizable(false);

        Label label = new Label(headerText);
        label.setWrapText(true);

        ListView<String> dirListView = new ListView<>();
        for (Directory dir : directories) {
            dirListView.getItems().add(dir.getName());
        }
        dirListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        dirListView.getSelectionModel().selectFirst();;

        Button submitButton = new Button("Choose");
        submitButton.setOnAction(e -> {
            selectedDirectoryId = dirListView.getSelectionModel().getSelectedIndex();
            window.close();
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, dirListView, submitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout);
        scene.getStylesheets().add("styleForm.css");
        window.setScene(scene);
        window.showAndWait();

        return selectedDirectoryId;
    }
}