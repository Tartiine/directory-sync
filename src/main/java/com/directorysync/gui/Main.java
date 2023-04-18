package com.directorysync.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {

    Stage window;
    Scene sceneHome, scene2, sceneForm;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setOnCloseRequest(e -> { e.consume(); closeProgram(); });
        window.setResizable(false);
        window.setTitle("Directory synchronization");

        HBox topMenu = new HBox();
        /*Button buttonMenuA = new Button("File");
        Button buttonMenuB = new Button("Edit");
        Button buttonMenuC = new Button("View");
        topMenu.getChildren().addAll(buttonMenuA, buttonMenuB, buttonMenuC);*/

        Label titleLabel = new Label("Select folders to synchronize");

        GridPane folderSelectionGrid = new GridPane();
        folderSelectionGrid.setPadding(new Insets(10));
        folderSelectionGrid.setVgap(8);
        folderSelectionGrid.setHgap(10);

        Button firstFolderSelectButton = new Button("F");
        firstFolderSelectButton.setOnAction(e -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select a folder to synchronize");
            AnchorPane anchorPane;
            //Stage popupStage = (Stage)anchorPane.getScene().getWindow();
            //File folder = dirChooser.showDialog(popupStage);
        });
        GridPane.setConstraints(firstFolderSelectButton, 0, 0);

        TextField firstFolderPathField = new TextField();
        firstFolderPathField.setPromptText("First folder path");
        firstFolderPathField.setId("italic-label");
        GridPane.setConstraints(firstFolderPathField, 1, 0);

        Button secondFolderSelectButton = new Button("F");
        GridPane.setConstraints(secondFolderSelectButton, 0, 1);

        TextField secondFolderPathField = new TextField();
        secondFolderPathField.setPromptText("Second folder path");
        secondFolderPathField.setId("italic-label");
        GridPane.setConstraints(secondFolderPathField, 1, 1);

        Button hardSyncButton = new Button("Hard synchronization");
        GridPane.setConstraints(hardSyncButton, 1, 2);
        hardSyncButton.setOnAction(e -> AlertBox.display("Directory Sync", "Hard synchronization started!"));

        folderSelectionGrid.getChildren().addAll(
            firstFolderSelectButton,
            firstFolderPathField,
            secondFolderSelectButton,
            secondFolderPathField,
            hardSyncButton
        );

        VBox layout1 = new VBox(10);
        layout1.getChildren().addAll(titleLabel, folderSelectionGrid);
        layout1.setAlignment(Pos.CENTER);
        layout1.setPadding(new Insets(20, 10, 20, 10));
        
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topMenu);
        borderPane.setCenter(layout1);
        
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
