package com.example.bookcatalog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Transactions {

    public static void showAddBookForm(Stage stage, Scene mainScene) {


        Label infoLabel = new Label("Please enter all the information about the book you want to add.");
        infoLabel.setWrapText(true); //layout değişikliğinde yazının satır atlaması ve resize işlemleri
        infoLabel.setMaxWidth(700); // yazının max genişliği
        infoLabel.setAlignment(Pos.TOP_CENTER);
        infoLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10;"); //css ile görünüm iyileştirme



        //NEW VBOX FOR BODY (TO INCLUDE TEXT FIELD AND LABELS)

        VBox bookInfoEnteringField = new VBox(10);
        bookInfoEnteringField.setAlignment(Pos.TOP_CENTER);
        bookInfoEnteringField.setPadding(new Insets(20, 40, 20, 40));



        //A LOOP TO CREATE ALL LABELS AND TEXTFIELDS IN ORDER

        String[] labels = {"Title", "Subtitle", "Authors", "Translators", "ISBN", "Publisher", "Date", "Edition", "Cover", "Language", "Rating", "Tags"};
        for (String label : labels) {
            Label lbl = new Label(label + ":");
            lbl.setMinWidth(60);                       // Labellarin genişliğini biraz daha artırılabilir
            lbl.setAlignment(Pos.CENTER);
            TextField txt = new TextField();
            txt.setPrefWidth(600);                     // Text fieldın genişliği Hgrow ile güzel gözükmedi manuel
            HBox loopBox = new HBox(10, lbl, txt); //  ayarlayıp Pos'u ortaya çektim.
            loopBox.setAlignment(Pos.CENTER);

            bookInfoEnteringField.getChildren().add(loopBox);
        }



        //COLORS FOR SAVE AND BACK BUTTON ANIMATION USING CSS

        String saveButtonBaseStyle = "-fx-font-weight: bold; -fx-background-color: #5cb85c; -fx-text-fill: white;";
        String saveButtonHoverStyle = "-fx-background-color: #4cae4c;"; // Mouse üzerine gelince

        String backButtonBaseStyle = "-fx-font-weight: bold; -fx-background-color: #f0ad4e; -fx-text-fill: white;";
        String backButtonHoverStyle = "-fx-background-color: #edb879;"; // Mouse üzerine gelince



        //SAVE BUTTON CREATION AND ANIMATION

        Button saveButton = new Button("Save");
        saveButton.setStyle(saveButtonBaseStyle);
        saveButton.setOnMouseEntered(e -> saveButton.setStyle(saveButtonBaseStyle + saveButtonHoverStyle));
        saveButton.setOnMouseExited(e -> saveButton.setStyle(saveButtonBaseStyle));



        //SAVE BUTTON ACTION

        saveButton.setOnAction(e -> {
            System.out.println("Successfully saved!");

            stage.setScene(mainScene);
        });



        //BACK BUTTON CREATION AND ANIMATION

        Button backButton = new Button("Back");
        backButton.setStyle(backButtonBaseStyle);
        backButton.setOnMouseEntered(e -> backButton.setStyle(backButtonBaseStyle + backButtonHoverStyle));
        backButton.setOnMouseExited(e -> backButton.setStyle(backButtonBaseStyle));



        //BACK BUTTON ACTION

        backButton.setOnAction(e -> {
            stage.setScene(mainScene);
        });



        //ADD SECTION LAYOUT SETTINGS

        HBox buttonBox = new HBox(20, saveButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(20, 0, 0, 0));

        VBox addLayoutALL = new VBox(20);
        addLayoutALL.getChildren().addAll(infoLabel, bookInfoEnteringField, buttonBox);
        addLayoutALL.setAlignment(Pos.TOP_CENTER);
        addLayoutALL.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(addLayoutALL);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setStyle("-fx-background: #f4f4f4; -fx-border-color: #f4f4f4;");

        Scene addBookScene = new Scene(scrollPane, 800, 600);
        stage.setScene(addBookScene);

    }


}
