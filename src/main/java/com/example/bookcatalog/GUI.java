package com.example.bookcatalog;

//JavaFX arayüz tasarımları için gerekli importlar:

import javafx.application.Application;
import javafx.application.Platform; // Layoutlar arası geçişlerin güzel olması için
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

// Dosya işlemleri için Java IO importları:
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

// JSON işleme importları:
import org.json.JSONException;
import org.json.JSONObject;

// Veri yönetimi için yardımcı importlar:
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.stream.Stream;

// Uyarı ve hata mesajlarını yönetme:
import java.util.logging.Level;
import java.util.logging.Logger;
/* CSS Edit Kısmında düzgün bir şekilde çalışmasına rağmen her save veya back butonuna
basıldığında sürekli uyarı mesajı alıyorduk aşağıdaki import bu uyarı mesajını göstermemek için.*/




public class GUI extends Application {



    public static TableView<Book> bookTable = new TableView<>();
    public static ObservableList<Book> booksData = FXCollections.observableArrayList();
    public static FilteredList<Book> filteredBooks;

    public static void main(String[] args) {
        //CSS Edit Kısmında düzgün bir şekilde çalışmasına rağmen her save veya back butonuna
//basıldığında sürekli uyarı mesajı alıyorduk aşağıdaki logger bu uyarı mesajını göstermemek için.
        Logger.getLogger("javafx").setLevel(Level.SEVERE);
        launch(args);
    }

    private void showFilterWindow(Stage mainStage) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Filtering.fxml"));
            Parent root = loader.load();

            Stage filterStage = new Stage();
            filterStage.setTitle("Filters");
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

                            boolean needsUpdate = false;

                            // Rating double çevrim kontrolü
                            if (json.has("rating")) {
                                try {
                                    json.getDouble("rating"); // Eğer bu başarısız olursa JSONException fırlatır
                                } catch (JSONException ex) {
                                    try {
                                        double rating = Double.parseDouble(json.getString("rating"));
                                        json.put("rating", rating);
                                    } catch (NumberFormatException e) {
                                        json.put("rating", 0.0);
                                        needsUpdate = true;
                                        System.out.println("Failed to parse 'rating' as a double. Defaulting to 0.0 at: " + path);
                                    }
                                }
                            }

                            // JSON dosyası güncellenirse
                            if (needsUpdate) {
                                Files.writeString(path, json.toString(), StandardOpenOption.TRUNCATE_EXISTING);
                                System.out.println("Updated JSON file at: " + path);
                            }

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





    //Tableview kitap bilgisine çift tıklayınca detaylı bilgi penceresini açmak için:
    private void showBookDetails(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bookcatalog/BookData.fxml"));
            Parent root = loader.load();
            TableviewBookDataController controller = loader.getController();
            Stage stage = new Stage();
            controller.setStage(stage);
            controller.setBook(book);

            // Resim dosyasına olan path
            String imagePath = "src/coverImages/" + book.getIsbn() + ".jpg";
            File imageFile = new File(imagePath);
            Image image;

            if (imageFile.exists()) {
                // Dosya yolunu URI'ye, sonra da URL'ye dönüştürüp bir Image nesnesine yükleme işlemi.
                image = new Image(imageFile.toURI().toString());
            } else {
                // Belirli bir kitap resmi yoksa varsayılan resmi kullanıyoruz.
                File defaultImageFile = new File("src/coverImages/default_image.jpg");
                image = new Image(defaultImageFile.toURI().toString());
            }

            // ImageView'da resmi ayarlama.
            controller.getCoverImageView().setImage(image);

            stage.setScene(new Scene(root));
            stage.setTitle(book.getTitle() + " Information Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred while trying to open book information window");
        }
    }








    @Override
    public void start(Stage stage) {
        System.out.println(" "); System.out.println(" ");
        System.out.println("--------------------------------------------------------------------");
        System.out.println("             WELCOME TO BOOK CATALOG'S COMMAND LINE!");
        System.out.println("                    Logs will be shown here.");
        System.out.println("--------------------------------------------------------------------");
        System.out.println(" "); System.out.println("                           TEAM-5"); System.out.println(" ");
        System.out.println("--------------------------------------------------------------------");

        loadExistingBooks("books");

        filteredBooks = new FilteredList<>(booksData, p -> true);
        Label titleLabel = new Label("Book Catalog [v1.7]");
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



        //title, help/about bar, ve search controls için toplayout vboxunda birleştirme işlemi.
        VBox topLayout = new VBox(5, titleLabel, helpAboutBox, searchAndFiltersBox);
        topLayout.setAlignment(Pos.CENTER);


        //ADD & EDIT BUTTONS

        //ADD BUTTON
        Button addButton = new Button("Add");


        //EDIT BUTTON
        Button editButton = new Button("Edit");



        // DELETE BUTTON AND CSS DECLARATIONS
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
        importButton.setOnAction(e->{
            /*Henüz işlev yok*/
            System.out.println("Import JSON button is not included for Milestone-2");
            Alert alert = new Alert(Alert.AlertType.WARNING, "Import JSON button is not included for Milestone-2");
            alert.showAndWait();
        });
        Button exportButton = new Button("Export JSON");
        exportButton.setOnAction(e-> {
            /*Henüz işlev yok*/
            System.out.println("Export JSON button is not included for Milestone-2");
            Alert alert = new Alert(Alert.AlertType.WARNING, "Export JSON button is not included for Milestone-2");
            alert.showAndWait();
        });



        //BOTTOM-DEEPER BUTTONS HBOX (IMPORT-EXPORT)

        HBox jsonBox = new HBox(10, importButton, exportButton);
        jsonBox.setAlignment(Pos.CENTER_RIGHT);



        //BOTTOM VBOX

        VBox bottomLayout = new VBox(20, addEditBox, jsonBox);
        bottomLayout.setAlignment(Pos.CENTER);
        bottomLayout.setPadding(new Insets(15, 20, 15, 20));



        //TABLE & COLUMNS

        bookTable.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Book rowData = row.getItem();
                    showBookDetails(rowData);
                }
            });
            return row;
        });


        bookTable.setPlaceholder(new Label("No books to display. Use 'Add' to insert new entries."));
        //bu listviewda hiç kitap yoksa bu yazıyı gösteriyor.
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



        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(topLayout);
        mainLayout.setCenter(tableContainer);
        mainLayout.setBottom(bottomLayout);

        Scene mainScene = new Scene(mainLayout, 1200, 1000);
        stage.setTitle("Book Catalog");
        stage.setScene(mainScene);
        stage.show();



        //HELP BUTTON ACTION
        helpButton.setOnAction(e -> {
            /*Henüz işlev yok*/
            System.out.println("Help button is not included for Milestone-.");
            Alert alert = new Alert(Alert.AlertType.WARNING, "Help button is not included for Milestone-2.");
            alert.showAndWait();
        });



        //ABOUT BUTTON ACTION
        aboutButton.setOnAction(e -> {
            /*Henüz işlev yok*/
            System.out.println("About button is not included for Milestone-.");
            Alert alert = new Alert(Alert.AlertType.WARNING, "About button is not included for Milestone-2.");
            alert.showAndWait();
        });
        searchButton.setOnAction(e -> {
            try {
                String searchText = searchField.getText().toLowerCase();

                filteredBooks.setPredicate(book -> {
                    if (searchText.isEmpty()) {
                        book.setSearchPriority(Integer.MAX_VALUE); // Reset priority
                        return true;
                    }

                    if (book == null) {
                        return false;
                    }

                    boolean matchesTitle = book.getTitle() != null && book.getTitle().toLowerCase().contains(searchText);
                    boolean matchesSubtitle = book.getSubtitle() != null && book.getSubtitle().toLowerCase().contains(searchText);
                    boolean matchesISBN = book.getIsbn() != null && book.getIsbn().toLowerCase().contains(searchText);
                    boolean matchesPublisher = book.getPublisher() != null && book.getPublisher().toLowerCase().contains(searchText);
                    boolean matchesAuthors = book.getAuthors() != null && book.getAuthors().stream().anyMatch(author -> author.toLowerCase().contains(searchText));
                    boolean matchesTranslators = book.getTranslators() != null && book.getTranslators().stream().anyMatch(translator -> translator.toLowerCase().contains(searchText));
                    boolean matchesRating = String.valueOf(book.getRating()).contains(searchText);
                    boolean matchesDate = book.getDate() != null && book.getDate().toLowerCase().contains(searchText);
                    boolean matchesLanguage = book.getLanguage() != null && book.getLanguage().toLowerCase().contains(searchText);
                    boolean matchesCover = book.getCover() != null && book.getCover().toLowerCase().contains(searchText);
                    boolean matchesEdition = book.getEdition() != null && book.getEdition().toLowerCase().contains(searchText);
                    boolean matchesTags = book.getTags() != null && book.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(searchText));

                    //Tablomuzda soldan sağa doğru olan sıralamayı öncelikte de aynı şekilde kullandık.
                    if (matchesTitle) {
                        book.setSearchPriority(1); //Highest Priority
                    } else if (matchesSubtitle) {
                        book.setSearchPriority(2);
                    } else if (matchesAuthors) {
                        book.setSearchPriority(3);
                    }
                    else if (matchesTranslators) {
                        book.setSearchPriority(4); }
                        else if (matchesISBN) {
                        book.setSearchPriority(5);
                    } else if (matchesPublisher) {
                        book.setSearchPriority(6);
                    }
                    else if (matchesDate) {
                        book.setSearchPriority(7);
                    }
                else if (matchesEdition) {
                        book.setSearchPriority(8);
                    }  else if (matchesCover) {
                        book.setSearchPriority(9);
                    }
                    else if (matchesLanguage) {
                        book.setSearchPriority(10);
                    }
                    else if (matchesRating) {
                        book.setSearchPriority(11);
                    }
                    else if (matchesTags) {
                        book.setSearchPriority(12);
                    }else {
                        book.setSearchPriority(Integer.MAX_VALUE); // No match
                        return false;
                    }

                    return true;
                });
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "An error occurred during search: " + ex.getMessage());
                alert.showAndWait();
            }
            // Sıralı listeyi oluşturma
            SortedList<Book> sortedBooks = new SortedList<>(filteredBooks);
            sortedBooks.setComparator(Comparator.comparingInt(Book::getSearchPriority));
            // TableView'ı güncelleme
            bookTable.setItems(sortedBooks);
            bookTable.refresh();
        });


        //FILTERS BUTTON ACTION
        filtersButton.setOnAction(e->{
            System.out.println("Filters succesfully loaded.");
            showFilterWindow(stage);
        });



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



        // EDIT BUTTON ACTION
        editButton.setOnAction(e -> {


            List<Book> selectedBooks = new ArrayList<>(bookTable.getSelectionModel().getSelectedItems());

            //1 kitap seçiliyse if statementı:
            if (selectedBooks.size() == 1) {
                Book selectedBook = selectedBooks.get(0); // Seçilen kitabı tutuyoruz.
                bookTable.refresh(); // Kitabı düzenledikten sonra TableView'ı yeniliyoruz.
                Transactions.showEditBookSection(stage, mainScene, selectedBook);
            } else if (selectedBooks.isEmpty()) {
                //kitap seçili olmama durumu:
                System.out.println("No selection for edit is not valid");
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a book from the table to edit.");
                alert.showAndWait();
            } else {
                //Multiple kitap seçilimi yapılırsa:
                System.out.println("Multiple selection for edit is not valid");
                Alert alert = new Alert(Alert.AlertType.WARNING, "Editing is only applicable to one selected book at a time.");
                alert.showAndWait();
            }
        });




        // DELETE BUTTON ACTION

        deleteButton.setOnAction(e -> {
            List<Book> selectedBooks = new ArrayList<>(bookTable.getSelectionModel().getSelectedItems());
            if (!selectedBooks.isEmpty()) {
                //Stream, koleksiyondaki öğeler üzerinde ardışık işlemler yapmamızı sağlayan bir araçtır.
                //bu sayede kitap verilerimizi daha hızlı ve kolay işliyoruz.
                String bookListString = selectedBooks.stream()
                        .map(Book::getTitle)
                        .collect(Collectors.joining(", "));

                //Yanlışlıkla delete tuşuna basmayı engellemek için:
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Confirm Deletion");
                System.out.println("Confirm Deletion?");
                confirmationAlert.setHeaderText("Are you sure you want to delete the selected books?");
                confirmationAlert.setContentText("You are about to delete the following books: " + bookListString);

                //Uyarı ekranı için confirm ve iptal butonları:
                ButtonType delete2Button = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                confirmationAlert.getButtonTypes().setAll(delete2Button, cancelButton);


                //Olası butona basma/basmama durumları için sonuçlar:
                Optional<ButtonType> response = confirmationAlert.showAndWait();
                if (response.isPresent() && response.get() == delete2Button) {
                    Transactions.deleteBooks(stage, mainScene, selectedBooks);
                    bookTable.refresh();
                } else if (response.isPresent() && response.get() == cancelButton) {
                    System.out.println("Deletion cancelled by user.");
                }
            } else {
                System.out.println("No selection has been made!");
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select at least one book to delete.");
                alert.showAndWait();
            }
        });










        editButton.setStyle("-fx-font-weight: bold; ");
    }

}