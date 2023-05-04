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
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage window;
    private Scene sceneHome, scene2, sceneForm;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setOnCloseRequest(e -> { e.consume(); closeProgram(); });
        window.setResizable(false);
        window.setTitle("Directory synchronization");

        List<Directory> directories = new LinkedList<>();
        directories.add(new Directory("Directory 1", Path.of("testPath"), "ip"));
        directories.add(new Directory("Test 2", Path.of("test2")));
        directories.add(new Directory("The third one", Path.of("test3")));

        HBox topMenu = new HBox();
        /*Button buttonMenuA = new Button("File");
        Button buttonMenuB = new Button("Edit");
        Button buttonMenuC = new Button("View");
        topMenu.getChildren().addAll(buttonMenuA, buttonMenuB, buttonMenuC);*/

        Label titleLabel = new Label("Select folders to synchronize");
        titleLabel.getStyleClass().add("titleLabel");
        titleLabel.setPadding(new Insets(0, 0, 10, 0));

        VBox folderSelection = new VBox(10);

        DirChooserElement firstDirChooser = new DirChooserElement();
        HBox firstFolder = firstDirChooser.dirChooserButtonWithField(
            500,
            "First folder path",
            10,
            "Select a folder to synchronize",
            primaryStage
        );
        firstDirChooser.getTextField().setId("folderTextField");

        DirChooserElement secondDirChooser = new DirChooserElement();
        HBox secondFolder = secondDirChooser.dirChooserButtonWithField(
            500,
            "Second folder path",
            10,
            "Select a folder to synchronize",
            primaryStage
        );
        secondDirChooser.getTextField().setId("folderTextField");

        folderSelection.getChildren().addAll(
            firstFolder,
            secondFolder
        );

        Button hardSyncButton = new Button("Hard synchronization");
        hardSyncButton.setOnAction(e -> {
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
        });

        Button softSyncButton = new Button("Soft synchronization");
        softSyncButton.setOnAction(e -> {
            try {
                List<Path> targetPaths = new LinkedList<Path>();
                targetPaths.add(Path.of(firstDirChooser.getTextField().getText()));
                targetPaths.add(Path.of(secondDirChooser.getTextField().getText()));
                DirectorySync.softSynchronization(targetPaths);
            } catch (IOException exception) {
                AlertBox.display("Error", exception.getMessage());
            }
        });

        HBox actionButtons = new HBox(10);
        actionButtons.getChildren().addAll(hardSyncButton, softSyncButton);

        VBox layout1 = new VBox(10);
        layout1.getChildren().addAll(titleLabel, folderSelection, actionButtons);
        layout1.setAlignment(Pos.CENTER);
        layout1.setPadding(new Insets(20));
        
        /*VBox dirsLayout = new VBox(10);
        for (Directory dir : directories) {
            Button btn = new Button(dir.getName());
            btn.setOnAction(e -> {
                AlertBox.display(
                    "Infos about '"+dir.getName()+"'",
                    dir.getPath() +"\n" + dir.isLocal().toString() + "\n" + dir.getIpAddress());
            });
            dirsLayout.getChildren().add(btn);
        }*/

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topMenu);
        borderPane.setCenter(layout1);
        //borderPane.setBottom(dirsLayout);
        
        sceneHome = new Scene(borderPane);
        sceneHome.getStylesheets().add("styleForm.css");

        window.setScene(sceneHome);
        window.show();
    }

    private void closeProgram() {
        //Boolean answer = ConfirmBox.display("Confirmation", "Are you sure you want to close the program?");
        //if (answer) {
            System.out.println("Program closed");
            window.close();
        //}
    }
}
