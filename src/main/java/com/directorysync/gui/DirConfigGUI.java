package com.directorysync.gui;

import javafx.stage.*;

import java.io.IOException;
import java.nio.file.Path;

import com.directorysync.Directory;
import com.directorysync.DirectorySync;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DirConfigGUI {

    private static int fieldSize = 500;

    public static Directory display(Directory dir) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Configure '" + dir.getName() + "'");
        window.setMinWidth(250);
        window.setResizable(false);

        VBox nameFieldBox = new VBox(4);
        Label nameFieldLabel = new Label("Display name");
        TextField nameField = new TextField();
        nameField.setPromptText("Directory name");
        nameField.setPrefWidth(fieldSize);
        nameField.setText(dir.getName());
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            dir.setName(newValue);
        });
        nameFieldBox.getChildren().addAll(nameFieldLabel, nameField);

        VBox folderSelectionBox = new VBox(4);
        Label folderSelectionLabel = new Label("Directory path");
        DirChooserElement dirChooser = new DirChooserElement();
        HBox folderSelection = dirChooser.dirChooserButtonWithField(
            fieldSize,
            "Folder path",
            dir.getPath().toString(),
            10,
            "Select a folder to synchronize",
            window
        );
        dirChooser.getTextField().setId("folderTextField");
        folderSelectionBox.getChildren().addAll(folderSelectionLabel, folderSelection);
        
        CheckBox localCheckBox = new CheckBox("Local directory");
        localCheckBox.getStyleClass().add("white_text");
        localCheckBox.setSelected(dir.isLocal());
        localCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            dir.setLocal(newValue);
        });

        VBox ipFieldBox = new VBox(4);
        Label ipFieldLabel = new Label("IP address (is directory is not local)");
        TextField ipField = new TextField();
        ipField.setPromptText("IP address");
        ipField.setPrefWidth(fieldSize);
        ipField.setText(dir.getIpAddress());
        ipField.textProperty().addListener((observable, oldValue, newValue) -> {
            dir.setIpAddress(newValue);
        });
        ipFieldBox.getChildren().addAll(ipFieldLabel, ipField);

        HBox buttons = new HBox(10);
        Button confirmButton = new Button("Confirm");
        confirmButton.setOnAction(e -> {
            closeDirConfig(window, dir, dirChooser.getTextField().getText());
        });
        Button removeButton = new Button("Remove directory");
        removeButton.setOnAction(e -> {
            dir.setName("%%/Remove/%%");
            window.close();
        });
        buttons.getChildren().addAll(confirmButton, removeButton);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(nameFieldBox, folderSelectionBox, localCheckBox, ipFieldBox, buttons);
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setPadding(new Insets(10)); 
        Scene scene = new Scene(layout);
        scene.getStylesheets().add("styleForm.css");
        window.setScene(scene);
        window.setOnCloseRequest(e -> { e.consume(); closeDirConfig(window, dir, dirChooser.getTextField().getText()); });
        window.showAndWait();
        
        return dir;
    }

    private static void closeDirConfig(Stage window, Directory dir, String newPath) {
        if (dir.isLocal()) {
            try {
                switch (DirectorySync.getFolderValidity(newPath)) {
                    case 0: throw new IOException("Please enter a source directory.");
                    case 2: throw new IOException("Source directory (" + newPath + ") does not exist.");
                    case 3: throw new IOException("Unable to write or read in source directory (" + newPath + ").");
                }
            } catch (IOException exception) {
                AlertBox.display("Error", exception.getMessage());
                return;
            }
        }
        dir.setPath(Path.of(newPath));

        if (!dir.isLocal() && dir.getIpAddress().isEmpty()) {
            AlertBox.display("Error", "As your directory is not local, please sepcify an IP address.");
            return;
        }

        if (dir.getName().isEmpty()) dir.setName(dir.getPath().toString());
        window.close();
    }
}