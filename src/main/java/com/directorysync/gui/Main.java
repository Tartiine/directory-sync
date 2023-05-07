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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage window;
    private Scene sceneHome;
    List<Directory> directories;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setOnCloseRequest(e -> { e.consume(); closeProgram(); });
        window.setResizable(false);
        window.setTitle("Directory synchronization");

        directories = new LinkedList<>();

        Label titleLabel = new Label("Select folders to synchronize");
        titleLabel.getStyleClass().add("titleLabel");

        Button hardSyncButton = new Button("Hard synchronization");
        /*hardSyncButton.setOnAction(e -> {
            try {
                List<Path> targetPaths = new LinkedList<Path>();
                targetPaths.add(Path.of(secondDirChooser.getTextField().getText()));
                DirectorySync.hardSynchronization(
                    targetPaths,
                    Path.of(firstDirChooser.getTextField().getText()),
                    true
                );
            } catch (IOException exception) {
                AlertBox.display("Error", exception.getMessage());
            }
        });*/

        Button softSyncButton = new Button("Soft synchronization");
        /*softSyncButton.setOnAction(e -> {
            try {
                List<Path> targetPaths = new LinkedList<Path>();
                targetPaths.add(Path.of(firstDirChooser.getTextField().getText()));
                targetPaths.add(Path.of(secondDirChooser.getTextField().getText()));
                DirectorySync.softSynchronization(targetPaths);
            } catch (IOException exception) {
                AlertBox.display("Error", exception.getMessage());
            }
        });*/

        VBox dirsLayout = new VBox(10);
        dirsLayout.setAlignment(Pos.TOP_CENTER);
        refreshDirsList(dirsLayout);

        Button addButton = new Button("Add directory");
        addButton.setOnAction(e -> {
            Directory newDir = new Directory("New directory", Path.of(""));
            directories.add(newDir);
            newDir = DirConfigGUI.display(newDir);
            if (newDir.getName() == "%%/Remove/%%")
                directories.remove(newDir);
            refreshDirsList(dirsLayout);
        });

        Button startSyncButton = new Button("Start synchronization");

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
        for (int i = 0; i < directories.size(); i++) {
            final int currentIndex = i;
            Button btn = new Button(directories.get(i).getName());
            btn.setMinWidth(250);
            btn.setMaxWidth(250);
            btn.setOnAction(e -> {
                directories.set(currentIndex, DirConfigGUI.display(directories.get(currentIndex)));
                if (directories.get(currentIndex).getName() == "%%/Remove/%%")
                    directories.remove(currentIndex);
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
