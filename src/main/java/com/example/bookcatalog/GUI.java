package com.example.bookcatalog;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUI extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        Label titleLabel = new Label("Book Catalog [v1.1] - Manage your book collection efficiently");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setPadding(new Insets(5));


        //HELP & ABOUT BUTTONS
        Button helpButton = new Button("Help");
        helpButton.setOnAction(e -> {/*Henüz işlev yok*/ });
        Button aboutButton = new Button("About");
        aboutButton.setOnAction(e -> { /*Henüz işlev yok*/});
        HBox helpAboutBox = new HBox(10, helpButton, aboutButton);
        helpAboutBox.setAlignment(Pos.CENTER_RIGHT);
        helpAboutBox.setPadding(new Insets(0, 20, 10, 0));

        //SEARCH BUTTON & TEXTFIELD
        Label searchLabel = new Label("Search a book:");
        TextField searchField = new TextField();
        searchField.setPromptText("Enter book title, author, or ISBN"); //Tıklayınca kaybolan yazı.
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e->{ /*Henüz işlev yok*/});

        VBox topLayout = new VBox(10, searchLabel, searchField, searchButton);
        topLayout.setAlignment(Pos.CENTER);
        topLayout.setPadding(new Insets(15, 20, 15, 20));

        // Combine title, help/about bar, and search controls in the top layout
        VBox combinedTopLayout = new VBox(5, titleLabel, helpAboutBox, topLayout);
        combinedTopLayout.setAlignment(Pos.CENTER);

        //ADD & EDIT BUTTONS
        Button addButton = new Button("Add");
        addButton.setOnAction(e->{ /*Henüz işlev yok*/ } );
        Button editButton = new Button("Edit");
        editButton.setOnAction(e->{ /*Henüz işlev yok*/ });
        HBox addEditBox = new HBox(10, addButton, editButton);
        addEditBox.setAlignment(Pos.CENTER);

        //IMPORT & EXPORT JSON BUTTONS
        Button importButton = new Button("Import JSON");
        importButton.setOnAction(e->{ /*Henüz işlev yok*/ });
        Button exportButton = new Button("Export JSON");
        exportButton.setOnAction(e-> { /*Henüz işlev yok*/ });
        HBox jsonBox = new HBox(10, importButton, exportButton);
        jsonBox.setAlignment(Pos.CENTER);

        VBox bottomLayout = new VBox(20, addEditBox, jsonBox);
        bottomLayout.setAlignment(Pos.CENTER);
        bottomLayout.setPadding(new Insets(15, 20, 15, 20));


        //Ortadaki her bilginin gözükeceği table
        TableView<Book> bookTable = new TableView<>();
        bookTable.setPlaceholder(new Label("No books to display. Use 'Add' to insert new entries.")); //if no data


        //GUI within order
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(combinedTopLayout);
        mainLayout.setCenter(bookTable);
        mainLayout.setBottom(bottomLayout);

        Scene scene = new Scene(mainLayout, 800, 600);
        stage.setTitle("Book Catalog");
        stage.setScene(scene);
        stage.show();

    }
}
