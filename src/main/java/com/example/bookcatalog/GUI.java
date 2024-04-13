package com.example.bookcatalog;

import javafx.application.Application;
import javafx.application.Platform; //Layoutlar arası geçişin kusursuz olması için ekledim.
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.stream.Stream;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.json.JSONObject;


public class GUI extends Application {



    public static ObservableList<Book> booksData = FXCollections.observableArrayList();
    public static FilteredList<Book> filteredBooks;

    public static void main(String[] args) {
        launch(args);
    }

    private void showFilterWindow(Stage mainStage) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Filtering.fxml"));
            Parent root = loader.load();

            Stage filterStage = new Stage();
            filterStage.setTitle("Filter Tags");
            filterStage.initModality(Modality.WINDOW_MODAL);
            filterStage.initOwner(mainStage);

            FilteringController controller = loader.getController();
            controller.setStage(filterStage);

            filterStage.setScene(new Scene(root));
            filterStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void loadExistingBooks(String directoryPath) {
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                            JSONObject json = new JSONObject(content);
                            Book book = Book.fromJSON(json);
                            booksData.add(book);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            String displayValue = list.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            return new SimpleObjectProperty<>(displayValue);
        });
        return column;
    }

    //COLUMN SIZE FITS 800x600
    private void editColumnWidths(TableView<?> table, TableColumn<?, ?>[] columns, double widthPercentage) {
        for (TableColumn<?, ?> column : columns) {
            column.prefWidthProperty().bind(table.widthProperty().multiply(widthPercentage));
        }
    }

    @Override
    public void start(Stage stage) {
        loadExistingBooks("books");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println("--------------------------------------------------------------------");
        System.out.println("             WELCOME TO BOOK CATALOG'S COMMAND LINE!");
        System.out.println("                    Logs will be shown here.");
        System.out.println("--------------------------------------------------------------------");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println("                           TEAM-5");
        System.out.println(" ");

        filteredBooks = new FilteredList<>(booksData, p -> true);
        Label titleLabel = new Label("Book Catalog [v1.3] - Manage your book collection efficiently");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setPadding(new Insets(5));


        //HELP & ABOUT BUTTONS
        Button helpButton = new Button("Help");
        Button aboutButton = new Button("About");
        HBox helpAboutBox = new HBox(10, helpButton, aboutButton);
        helpAboutBox.setAlignment(Pos.CENTER_RIGHT);
        helpAboutBox.setPadding(new Insets(0, 20, 10, 0));

        //SEARCH BUTTON, FILTER BUTTON & SEARCHING TEXT FIELD
        Label searchLabel = new Label("Search a book:");
        TextField searchField = new TextField();
        searchField.setPromptText("Enter book title, author, or ISBN"); //Tıklayınca kaybolan yazı.
        Button searchButton = new Button("Search");
        Button filtersButton = new Button("Filters");
        HBox searchAndFiltersBox = new HBox(10, searchLabel, searchField, searchButton, filtersButton);
        searchAndFiltersBox.setAlignment(Pos.CENTER);
        searchAndFiltersBox.setPadding(new Insets(15, 20, 15, 20));
        searchField.setMaxWidth(400);



        // Combine title, help/about bar, and search controls in the top layout
        VBox topLayout = new VBox(5, titleLabel, helpAboutBox, searchAndFiltersBox);
        topLayout.setAlignment(Pos.CENTER);


        //ADD & EDIT BUTTONS

        //ADD BUTTON
        Button addButton = new Button("Add");


        //EDIT BUTTON
        Button editButton = new Button("Edit");



        // DELETE BUTTON
        String deleteButtonBaseStyle = "-fx-font-weight: bold; -fx-background-color: #dc3545; -fx-text-fill: white;";
        String deleteButtonHoverStyle = "-fx-background-color: #d9534f;"; // Mouse üzerine gelince
        String deleteButtonArmedStyle = "-fx-background-color: #c82333;"; // Basıldığında

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle(deleteButtonBaseStyle);



        //DELETE BUTTON ANIMATION

        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle(deleteButtonBaseStyle + deleteButtonHoverStyle));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle(deleteButtonBaseStyle));
        deleteButton.setOnMousePressed(e -> deleteButton.setStyle(deleteButtonBaseStyle + deleteButtonArmedStyle));
        deleteButton.setOnMouseReleased(e -> deleteButton.setStyle(deleteButtonBaseStyle + (deleteButton.isHover() ? deleteButtonHoverStyle : "")));



        //BOTTOM-UPPER BUTTONS HBOX (ADD-EDIT-DELETE)

        HBox addEditBox = new HBox(10, addButton, editButton,deleteButton);
        addEditBox.setAlignment(Pos.CENTER_LEFT);

        //IMPORT & EXPORT JSON BUTTONS
        Button importButton = new Button("Import JSON");
        importButton.setOnAction(e->{ /*Henüz işlev yok*/ });
        Button exportButton = new Button("Export JSON");
        exportButton.setOnAction(e-> { /*Henüz işlev yok*/ });



        //BOTTOM-DEEPER BUTTONS HBOX (IMPORT-EXPORT)

        HBox jsonBox = new HBox(10, importButton, exportButton);
        jsonBox.setAlignment(Pos.CENTER_RIGHT);



        //BOTTOM VBOX

        VBox bottomLayout = new VBox(20, addEditBox, jsonBox);
        bottomLayout.setAlignment(Pos.CENTER);
        bottomLayout.setPadding(new Insets(15, 20, 15, 20));



        //TABLE & COLUMNS

        //TABLEVIEW WITH NO COLUMNS

        TableView<Book> bookTable = new TableView<>();
        bookTable.setPlaceholder(new Label("No books to display. Use 'Add' to insert new entries.")); //if no data
        bookTable.setItems(GUI.filteredBooks);
        bookTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);



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
        TableColumn<?, ?>[] columns = {
                titleColumn, subtitleColumn, authorsColumn, translatorsColumn, isbnColumn, publisherColumn,
                publicationDateColumn, editionColumn, coverColumn, languageColumn, ratingColumn, tagsColumn
        };
        editColumnWidths(bookTable, columns, 0.083);


        //COLUMN CREATION
        bookTable.getColumns().addAll(
                titleColumn, subtitleColumn, authorsColumn, translatorsColumn,
                isbnColumn, publisherColumn, publicationDateColumn, editionColumn,
                coverColumn, languageColumn, ratingColumn, tagsColumn
        );



        //GUI within order
        VBox tableContainer = new VBox(bookTable);
        tableContainer.setAlignment(Pos.CENTER); // Center alignment inside the VBox
        tableContainer.setPadding(new Insets(15)); // 10 pixels padding on all sides
        VBox.setVgrow(bookTable, Priority.ALWAYS);

        // Use this VBox as the center of the main layout

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(topLayout);
        mainLayout.setCenter(tableContainer);
        mainLayout.setBottom(bottomLayout);

        Scene mainScene = new Scene(mainLayout, 800, 600);



        //HELP BUTTON ACTION
        helpButton.setOnAction(e -> {/*Henüz işlev yok*/ });



        //ABOUT BUTTON ACTION
        aboutButton.setOnAction(e -> { /*Henüz işlev yok*/});



        //SEARCH BUTTON ACTION
        searchButton.setOnAction(e->{ /*Henüz işlev yok*/});



        //FILTERS BUTTON ACTION
        filtersButton.setOnAction(e->{showFilterWindow(stage);});



        //ADD BUTTON ACTION

        addButton.setOnAction(e -> {
            //  2. bir layout oluşturmak yerine var olanı layout arasında geçiş yapabilmek için Add butonu
            //  layoutu oluşturduktan sonra kullanılabilir hale geldi add butonun burda olmasının sebebi bu muhtemelen
            //  edit butonu da daha sonra buraya gelecek
            boolean isFullScreen = stage.isFullScreen();
            double width = stage.getWidth();
            double height = stage.getHeight();

            Transactions.showAddBookSection(stage, mainScene);

            // layoutlar arası geçişte pencere boyutunu koruyo
            Platform.runLater(() -> {
                stage.setFullScreen(isFullScreen);
                if (!isFullScreen) {
                    // Tam ekran modu dışında, önceki boyutları geri yükle
                    stage.setWidth(width);
                    stage.setHeight(height);
                }
            });
        });
        addButton.setStyle("-fx-font-weight: bold; ");


        stage.setTitle("Book Catalog");
        stage.setScene(mainScene);
        stage.show();


        // EDIT BUTTON ACTION
        editButton.setOnAction(e -> {
            // Get all selected books
            List<Book> selectedBooks = new ArrayList<>(bookTable.getSelectionModel().getSelectedItems());

            // Check if exactly one book is selected
            if (selectedBooks.size() == 1) {
                Book selectedBook = selectedBooks.get(0); // Get the single selected book
                Transactions.showEditBookSection(stage, mainScene, selectedBook);
                bookTable.refresh(); // Refresh the TableView after editing
            } else if (selectedBooks.isEmpty()) {
                // No book selected
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a book from the table to edit.");
                alert.showAndWait();
            } else {
                // More than one book selected
                Alert alert = new Alert(Alert.AlertType.WARNING, "Editing is only applicable to one selected book at a time.");
                alert.showAndWait();
            }
        });




        // DELETE BUTTON ACTION

        deleteButton.setOnAction(e -> {
            List<Book> selectedBooks = new ArrayList<>(bookTable.getSelectionModel().getSelectedItems());
            if (!selectedBooks.isEmpty()) {
                // Build a string with all selected book titles
                String bookListString = selectedBooks.stream()
                        .map(Book::getTitle)
                        .collect(Collectors.joining(", "));

                // Create and show confirmation alert with book titles
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Confirm Deletion");
                confirmationAlert.setHeaderText("Are you sure you want to delete the selected books?");
                confirmationAlert.setContentText("You are about to delete the following books: " + bookListString);

                // Customize the button labels if desired
                ButtonType delete2Button = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                confirmationAlert.getButtonTypes().setAll(delete2Button, cancelButton);

                Optional<ButtonType> response = confirmationAlert.showAndWait();
                if (response.isPresent() && response.get() == delete2Button) {
                    Transactions.deleteBooks(stage, mainScene, selectedBooks);
                    bookTable.refresh();  // görünümü refreshler
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select at least one book to delete.");
                alert.showAndWait();
            }
        });










        editButton.setStyle("-fx-font-weight: bold; ");
    }

}