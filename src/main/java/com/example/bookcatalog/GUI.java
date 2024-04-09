package com.example.bookcatalog;

import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;



public class GUI extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    //COLUMN CREATION METHOD FOR STRING TYPE
    private <S, T> TableColumn<S, T> createColumn(String title, java.util.function.Function<S, T> propertyValueFactory) {
        TableColumn<S, T> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(propertyValueFactory.apply(cellData.getValue())));
        return column;
    }
    //COLUMN CREATION METHOD FOR LIST<> TYPE
    private <S, T> TableColumn<S, String> createColumnForList(String title, Function<S, List<T>> propertyValueFactory) {
        TableColumn<S, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> {
            List<T> list = propertyValueFactory.apply(cellData.getValue());
            // Convert the list to a comma-separated string for display
            String displayValue = list.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            return new SimpleObjectProperty<>(displayValue);
        });
        return column;
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

        //ADD BUTTON
        Button addButton = new Button("Add");
        addButton.setOnAction(e->{ /*Henüz işlev yok*/ } );

        //EDIT BUTTON
        Button editButton = new Button("Edit");
        editButton.setOnAction(e->{ /*Henüz işlev yok*/ });
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e->{ /*Henüz işlev yok*/ });
        HBox addEditBox = new HBox(10, addButton, editButton,deleteButton);
        addEditBox.setAlignment(Pos.CENTER_LEFT);

        //IMPORT & EXPORT JSON BUTTONS
        Button importButton = new Button("Import JSON");
        importButton.setOnAction(e->{ /*Henüz işlev yok*/ });
        Button exportButton = new Button("Export JSON");
        exportButton.setOnAction(e-> { /*Henüz işlev yok*/ });
        HBox jsonBox = new HBox(10, importButton, exportButton);
        jsonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox bottomLayout = new VBox(20, addEditBox, jsonBox);
        bottomLayout.setAlignment(Pos.CENTER);
        bottomLayout.setPadding(new Insets(15, 20, 15, 20));


        //TABLE & COLUMNS

        //TABLEVIEW WITH NO COLUMNS
        TableView<Book> bookTable = new TableView<>();
        bookTable.setPlaceholder(new Label("No books to display. Use 'Add' to insert new entries.")); //if no data

        //SPESIFIC COLUMNS FOR ALL THE DATA WE'VE BEEN TOLD IN DESCRIBTION
        TableColumn<Book, String> titleColumn = createColumn("Title", Book::getTitle);
        TableColumn<Book, String> subtitleColumn = createColumn("Subtitle", Book::getSubtitle);
        TableColumn<Book, String> authorsColumn = createColumnForList("Authors", Book::getAuthors);
        TableColumn<Book, String> translatorsColumn = createColumnForList("Translators", Book::getTranslators);
        TableColumn<Book, String> isbnColumn = createColumn("ISBN", Book::getIsbn);
        TableColumn<Book, String> publisherColumn = createColumn("Publisher", Book::getPublisher);
        TableColumn<Book, String> publicationDateColumn = createColumn("Publication Date", Book::getDate);
        TableColumn<Book, String> editionColumn = createColumn("Edition", Book::getEdition);
        TableColumn<Book, String> coverColumn = createColumn("Cover", Book::getCover);
        TableColumn<Book, String> languageColumn = createColumn("Language", Book::getLanguage);
        TableColumn<Book, String> ratingColumn = createColumn("Rating",
                book -> String.format("%.1f", book.getRating())); //double problem yarattığı için String formatına çevirdik.
        TableColumn<Book, String> tagsColumn = createColumnForList("Tags", Book::getTags);

        //THE CODE FOR ALL TABLE ELEMENTS FITS IN LAYOUT
        double titleColumnWidthPercentage = 0.083;
        titleColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(titleColumnWidthPercentage));
        subtitleColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(titleColumnWidthPercentage));
        authorsColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(titleColumnWidthPercentage));
        translatorsColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(titleColumnWidthPercentage));
        isbnColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(titleColumnWidthPercentage));
        publisherColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(titleColumnWidthPercentage));
        publicationDateColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(titleColumnWidthPercentage));
        editionColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(titleColumnWidthPercentage));
        coverColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(titleColumnWidthPercentage));
        languageColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(titleColumnWidthPercentage));
        ratingColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(titleColumnWidthPercentage));
        tagsColumn.prefWidthProperty().bind(bookTable.widthProperty().multiply(titleColumnWidthPercentage));

        //COLUMN CREATION
        bookTable.getColumns().addAll(
                titleColumn, subtitleColumn, authorsColumn, translatorsColumn,
                isbnColumn, publisherColumn, publicationDateColumn, editionColumn,
                coverColumn, languageColumn, ratingColumn, tagsColumn
        );



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
