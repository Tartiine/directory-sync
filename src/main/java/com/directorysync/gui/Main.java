package com.directorysync.gui;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import com.directorysync.Directory;
import com.directorysync.DirectorySync;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage window;
    private Scene sceneHome;
    public static List<Directory> directoryList;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setOnCloseRequest(e -> { e.consume(); closeProgram(); });
        window.setResizable(false);
        window.setTitle("Directory synchronization");

        directoryList = new LinkedList<>();

        directoryList.add(new Directory("folder 1", Path.of("/home/isaac/Bureau/folder 1")));
        directoryList.add(new Directory("folder 2", Path.of("/home/isaac/Bureau/folder 2")));

        Label titleLabel = new Label("Select folders to synchronize");
        titleLabel.getStyleClass().add("titleLabel");

        VBox dirsLayout = new VBox(10);
        dirsLayout.setAlignment(Pos.TOP_CENTER);
        refreshDirsList(dirsLayout);

        Button addButton = new Button("Add directory");
        addButton.setOnAction(e -> {
            Directory newDir = new Directory("New directory", Path.of(""));
            directoryList.add(newDir);
            newDir = DirConfigGUI.display(newDir);
            if (newDir.getName() == "%%/Remove/%%")
                directoryList.remove(newDir);
            refreshDirsList(dirsLayout);
        });

        Button startSyncButton = new Button("Start synchronization");
        startSyncButton.setOnAction(e -> {
            if (directoryList.size() < 2) {
                AlertBox.display("Not enough directories", "Please select at least two directories to synchronize.");
                return;
            }
            int syncType = SyncTypePopup.display();

            if (syncType == 0) {
                //Hard synchronization
                try {
                    List<Path> targetPaths = new LinkedList<Path>();
                    for(Directory dir:directoryList)
                        targetPaths.add(dir.getPath());
                    Path srcPath = targetPaths.get(0);
                    targetPaths.remove(0);
                    DirectorySync.hardSynchronization(targetPaths, srcPath, true);
                } catch (IOException exception) {
                    AlertBox.display("Error", exception.getMessage());
                }
                DirectorySync.watchEvents();
            } else if (syncType == 1) {
                //Soft synchronization
                try {
                    List<Path> targetPaths = new LinkedList<Path>();
                    for(Directory dir:directoryList)
                        targetPaths.add(dir.getPath());
                    DirectorySync.softSynchronization(targetPaths);
                } catch (IOException exception) {
                    AlertBox.display("Error", exception.getMessage());
                }
                DirectorySync.watchEvents();
            }
        });

        HBox actionButtons = new HBox(10);
        //actionButtons.getChildren().addAll(hardSyncButton, softSyncButton);
        actionButtons.getChildren().addAll(addButton, startSyncButton);

        VBox mainLayout = new VBox(30);
        mainLayout.getChildren().addAll(titleLabel, dirsLayout, actionButtons);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(20));
        
        sceneHome = new Scene(mainLayout);
        sceneHome.getStylesheets().add("styleForm.css");

        window.setScene(sceneHome);
        window.show();
    }

    private void refreshDirsList (VBox dirsLayout) {
        dirsLayout.getChildren().clear();
        for (int i = 0; i < directoryList.size(); i++) {
            final int currentIndex = i;
            Button btn = new Button(directoryList.get(i).getName());
            btn.setMinWidth(250);
            btn.setMaxWidth(250);
            btn.setOnAction(e -> {
                directoryList.set(currentIndex, DirConfigGUI.display(directoryList.get(currentIndex)));
                if (directoryList.get(currentIndex).getName() == "%%/Remove/%%")
                    directoryList.remove(currentIndex);
                refreshDirsList(dirsLayout);
            });
            dirsLayout.getChildren().add(btn);
        }
        window.sizeToScene();
    }

    private void closeProgram() {
        //if (communication is active)
        Boolean answer = ConfirmBox.display("Confirmation", "By exiting the program, the synchronization will end.\nAre you sure you want to exit the program?");
        if (answer) {
            System.out.println("Program closed");
            window.close();
        }
    }
}
