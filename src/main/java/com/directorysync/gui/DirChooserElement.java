package com.directorysync.gui;

import java.io.File;

import com.directorysync.DirectorySync;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class DirChooserElement {

    private TextField folderPathField = new TextField();

    public HBox dirChooserButtonWithField (int fieldSize, String fieldPromptText, int padding, String popupTitle, Stage currentStage) {
        HBox box = new HBox(padding);
        Button folderSelectButton = new Button();
        Image icon = new Image(getClass().getResourceAsStream("/imgs/folder.png"), 18, 18, false, false);
        folderSelectButton.getStyleClass().add("icon-button");
        folderSelectButton.setGraphic(new ImageView(icon));
        folderPathField.setPromptText(fieldPromptText);
        folderPathField.setPrefWidth(fieldSize);
        folderPathField.textProperty().addListener((observable, oldValue, newValue) -> {
            switch (DirectorySync.getFolderValidity(newValue)) {
                case 1: folderPathField.setStyle("-fx-border-color: #42ab42; -fx-background-color: #d1e4d1;");
                    break;
                case 2: folderPathField.setStyle("-fx-border-color: #af3a36; -fx-background-color: #ecd8d7;");
                    break;
                case 3: folderPathField.setStyle("-fx-border-color: #c0af13; -fx-background-color: #e4e3d3;");
                    break;
                case 4: folderPathField.setStyle("-fx-border-color: #aa2ca0; -fx-background-color: #e6cadf;");
                    break;
                default: folderPathField.setStyle("-fx-border-color: transparent; -fx-background-color: #ccc;");
                    break;
            }
        });
        folderSelectButton.setOnAction(e -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle(popupTitle);
            File folder = dirChooser.showDialog(currentStage);
            if (folder != null && folder.isDirectory())
                folderPathField.setText(folder.toString());
        });

        box.getChildren().addAll(folderSelectButton, folderPathField);
        return box;
    }

    public TextField getTextField() {
        return folderPathField;
    }
}
