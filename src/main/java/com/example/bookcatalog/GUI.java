package com.example.bookcatalog;

import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class GUI extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage stage) throws IOException {

        Label searchLabel = new Label("Search a book");
        TextField searchFiled = new TextField();

        Button searchButton = new Button("Search Book");
        Button addButton = new Button("Add Book");
        Button editButton = new Button("Edit Book");
        HBox addEditBox = new HBox(10,addButton,editButton);
        Button importButton = new Button("Import JSON");
        Button exportButton = new Button("Export JSON");
        HBox jsonBox = new HBox(10, importButton, exportButton);

        TableView<Book> bookTable = new TableView<>();

        VBox mainLayout = new VBox(20,searchLabel,searchFiled,searchButton,addEditBox,bookTable, jsonBox);
        VBox.setVgrow(bookTable, Priority.ALWAYS);
        Scene scene = new Scene(mainLayout, 800, 600);
        stage.setTitle("Book Catalog [v1.0]");
        stage.setScene(scene);
        stage.show();

    }
}
