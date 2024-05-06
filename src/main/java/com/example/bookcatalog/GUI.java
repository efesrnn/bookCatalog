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
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

// Dosya işlemleri için Java IO importları:
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

// JSON işleme importları:
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

// Veri yönetimi için yardımcı importlar:
import java.util.*;
import java.util.function.Function;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Uyarı ve hata mesajlarını yönetme:
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.bookcatalog.FilteringController.isFiltered;
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
                    .forEach(this::processFile);
        } catch (IOException e) {
            System.out.println("Directory for books does not exists. Add a book using 'Book Catalog'to create directory");
        }
    }
    private boolean isValidISBN(String isbn) {
        // Sayısal karakterler, kısa çizgiler ve ISBN-10 için sonunda 'X' olabilir
        return Pattern.matches("^[0-9Xx-]+$", isbn);
    }


    private void processFile(Path path) {
        if (Files.exists(path)) {
            try {
                String content = new String(Files.readAllBytes(path));
                JSONTokener tokener = new JSONTokener(content);
                Object json = tokener.nextValue();
                if (json instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) json;
                    String isbn = jsonObject.optString("isbn", "unknown");
                    // ISBN geçerli mi kontrol et
                    if (isValidISBN(isbn)) {
                        Path booksDirectory = Paths.get("books");
                        Files.createDirectories(booksDirectory); // Eğer dizin yoksa oluştur
                        Path newPath = booksDirectory.resolve(isbn + ".json");

                        // Yeni ISBN dosyasını yaz veya üzerine yaz
                        Files.writeString(newPath, jsonObject.toString());
                        System.out.println("Saved or updated JSON file as: " + newPath.getFileName());

                        // İşlemi tamamladıktan sonra yeni dosyayla işleme devam et
                        processBookJson(jsonObject, newPath);
                    } else {
                        System.out.println("Invalid ISBN found in file: " + path.getFileName() + ", skipping processing.");
                    }
                } else if (json instanceof JSONArray) {
                    System.out.println("Found JSON Array at: " + path);
                    System.out.println("Default save process can only handle JSON Objects.");
                    System.out.println("Converting all book data at JSON Array into separate JSON Objects...");
                    splitJsonArrayIntoFiles(path);
                } else {
                    System.out.println("Unexpected JSON format at: " + path);
                }
            } catch (IOException e) {
                System.out.println("Error reading or writing file: " + path);
                e.printStackTrace();
            }
        } else {
            System.out.println("File does not exist: " + path);
        }
    }




    private void splitJsonArrayIntoFiles(Path jsonArrayPath) {
        String uniqueTempFileName = "temp_" + UUID.randomUUID().toString() + ".json";
        Path tempPath = jsonArrayPath.resolveSibling(uniqueTempFileName);

        try {
            Files.move(jsonArrayPath, tempPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Renamed original JSON file to temporary file: " + tempPath);

            JSONArray jsonArray = new JSONArray(Files.readString(tempPath));
            Path directoryPath = Paths.get("books");
            Files.createDirectories(directoryPath);

            jsonArray.forEach(item -> {
                JSONObject jsonObject = (JSONObject) item;
                String isbn = jsonObject.optString("isbn", "unknown");
                Path newFilePath = directoryPath.resolve(isbn + ".json");

                copyImageIfAvailable(isbn, jsonArrayPath.getParent());
                try {
                    if (!Files.exists(newFilePath)) {
                        Files.writeString(newFilePath, jsonObject.toString());
                        System.out.println("Created new JSON file for ISBN: " + isbn + " at: " + newFilePath);
                    } else {
                        System.out.println("File already exists for ISBN: " + isbn);
                    }
                } catch (IOException e) {
                    System.out.println("Failed to write JSON file for ISBN: " + isbn);
                }
            });
            try {
            Files.deleteIfExists(directoryPath);
            System.out.println("Deleted JSON Array file: " + directoryPath);
            System.out.println("Conversion process successfully!");}
            catch (IOException ee){
                System.out.println("Not deleting json array due to importing...");
            }

            //Oluşturulan yeni json objeleri table a eklemek için table ı sıfırlayıp tekrar loadlıyoz.
            Platform.runLater(() -> {
                booksData.clear();
                loadExistingBooks("books");
                System.out.println("Reloading books after separating JSON Array...");
            });
        } catch (IOException e) {
            System.out.println("Error processing JSON array file: " + jsonArrayPath);
            e.printStackTrace();
        }

    }



    private void processBookJson(JSONObject json, Path path) throws IOException {

        boolean needsUpdate = false;
        String content = Files.readString(path);
        JSONObject jsonObject = new JSONObject(content);
        String isbn = jsonObject.optString("isbn", "unknown");
        Path destinationPath = Paths.get("books/" + isbn + ".json");
        if (!Files.exists(destinationPath)) {
            Files.copy(path, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Book file copied to 'books' directory with ISBN as filename: " + destinationPath);
        }

        String coverImagePath = json.optString("cover", "src/coverImages/default_image.jpg");
        Path coverImagePathFile = Paths.get(coverImagePath);
        if (!Files.exists(coverImagePathFile)) {
            System.out.println("Cover image file not found at: " + coverImagePath + ", using default image.");
            json.put("cover", "src/coverImages/default_image.jpg");
            needsUpdate = true;
        }


        // Anahtarlar listesi ve varsayılan değerler
        if (!json.has("title")) {
            System.out.println("Couldn't find the 'title' key at: "+path);
            System.out.println("Creating as blank... ");
            json.put("title", "");
            needsUpdate = true;
        }
        if (!json.has("subtitle")) {
            System.out.println("Couldn't find the 'subtitle' key at: "+path);
            System.out.println("Creating as blank... ");
            json.put("subtitle", "");
            needsUpdate = true;
        }
        if (!json.has("authors") || !(json.opt("authors") instanceof JSONArray)) {
            json.put("authors", new JSONArray());
                System.out.println("Couldn't find any 'authors' key or it might not be a JSON Array at: "+path);
                System.out.println("Creating as blank... ");
            needsUpdate = true;
        }
        if (!json.has("translators") || !(json.opt("translators") instanceof JSONArray)) {
            json.put("translators", new JSONArray());
                System.out.println("Couldn't find any 'translators' key or it might not be a JSON Array at: "+path);
                System.out.println("Creating as blank... ");
            needsUpdate = true;
        }
        if (!json.has("isbn")) {
            System.out.println("Couldn't find the 'isbn' key at: "+path);
            System.out.println("Creating as blank... ");
            json.put("isbn", "");
            needsUpdate = true;
        }
        if (!json.has("publisher")) {
            System.out.println("Couldn't find the 'publisher' key at: "+path);
            System.out.println("Creating as blank... ");
            json.put("publisher", "");
            needsUpdate = true;
        }
        if (!json.has("date")) {
            System.out.println("Couldn't find the 'date' key at: "+path);
            System.out.println("Creating as blank... ");
            json.put("date", "");
            needsUpdate = true;
        }
        if (!json.has("edition")) {
            System.out.println("Couldn't find the 'edition' key at: "+path);
            System.out.println("Creating as blank... ");
            json.put("edition", "");
            needsUpdate = true;
        }
        if (!json.has("cover")) {
            System.out.println("Couldn't find the 'cover' key at: "+path);
            System.out.println("Creating as blank... ");
            json.put("cover", "src/coverImages");
            needsUpdate = true;
        }
        if (!json.has("language")) {
            System.out.println("Couldn't find the 'language' key at: "+path);
            System.out.println("Creating as blank... ");
            json.put("language", "");
            needsUpdate = true;
        }
        if (!json.has("tags") || !(json.opt("tags") instanceof JSONArray)) {
            json.put("tags", new JSONArray());
                System.out.println("Couldn't find any 'tags' key or it might not be a JSON Array at: "+path);
                System.out.println("Creating as blank... ");
            needsUpdate = true;
        }
        if(!json.has("rating")){
            System.out.println("Couldn't find the 'rating' key at: "+path);
            System.out.println("Creating as blank... ");
            json.put("rating","");
            needsUpdate=true;
        }

        // Rating çevrim kontrolü ve varsayılan değer atama
        if (json.has("rating")) {
            try {
                json.getDouble("rating"); // Eğer bu başarısız olursa JSONException fırlatır
            } catch (JSONException ex) {
                try {
                    double rating = Double.parseDouble(json.optString("rating", "0.0"));
                    json.put("rating", rating);
                } catch (NumberFormatException e) {
                    json.put("rating", 0.0);
                    needsUpdate = true;
                }
            }
        } else {
            json.put("rating", 0.0);
            needsUpdate = true;
        }

        // Eğer JSON dosyası güncellenirse, dosyayı yeniden yaz
        if (needsUpdate) {
            try {
                Files.writeString(path, json.toString(), StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("Updated JSON file due to missing fields at: " + path);
            } catch (IOException e) {
                System.out.println("Error writing JSON file: " + path);
                e.printStackTrace();
            }
        }

        // Kitap nesnesi oluştur ve listeye ekle
        try {
            Book book = Book.fromJSON(json);
            booksData.add(book);
        } catch (Exception e) {
            System.out.println("Error creating book from JSON object: " + path);
            e.printStackTrace();
        }
    }


    private void exportSelectedItems(Stage stage, TableView<Book> tableView) {
        if (tableView.getSelectionModel().getSelectedItems().isEmpty()) {
            System.out.println("No items selected.");
            Alert alert= new Alert(Alert.AlertType.WARNING,"No line selected.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("ExportedData.json");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter fileWriter = new FileWriter(file)) {
                JSONArray jsonArray = new JSONArray();
                tableView.getSelectionModel().getSelectedItems().forEach(book -> {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("isbn", book.getIsbn());
                    jsonObj.put("title", book.getTitle());
                    jsonObj.put("date", book.getDate());
                    jsonObj.put("rating", book.getRating());
                    jsonObj.put("edition", book.getEdition());
                    jsonObj.put("language", book.getLanguage());
                    jsonObj.put("tags", new JSONArray(book.getTags()));
                    jsonObj.put("translators", new JSONArray(book.getTranslators()));
                    jsonObj.put("subtitle", book.getSubtitle());
                    jsonObj.put("cover", book.getCover());
                    jsonObj.put("publisher", book.getPublisher());
                    jsonObj.put("authors", new JSONArray(book.getAuthors()));

                    jsonArray.put(jsonObj);

                    // Check for cover image and copy it
                    copyCoverImage(book.getIsbn(), file.getParent());
                });

                fileWriter.write(jsonArray.toString(4)); // Pretty print with 4-space indentation
                fileWriter.flush();
                System.out.println("Selected items exported successfully to " + file.getPath());
            } catch (IOException e) {
                System.err.println("Failed to save the file: " + e.getMessage());
            }
        }
    }

    private void copyImageIfAvailable(String isbn, Path sourceDir) {
        Path imageFilePath = sourceDir.resolve(isbn + ".jpg");
        Path destinationImagePath = Paths.get("src/coverImages", isbn + ".jpg");

        try {
            if (Files.exists(imageFilePath) && !Files.exists(destinationImagePath)) {
                Files.copy(imageFilePath, destinationImagePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Cover image for ISBN " + isbn + " copied successfully to 'src/coverImages'.");
            }
        } catch (IOException e) {
            System.out.println("Failed to copy cover image for ISBN: " + isbn);
            e.printStackTrace();
        }
    }


    private void copyCoverImage(String isbn, String destinationDir) {
        Path sourcePath = Paths.get("src/coverImages/" + isbn + ".jpg");
        Path destinationPath = Paths.get(destinationDir, isbn + ".jpg");

        try {
            if (Files.exists(sourcePath)) {
                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Cover image copied successfully to " + destinationPath);
            } else {
                System.out.println("No cover image found for ISBN: " + isbn);
            }
        } catch (IOException e) {
            System.err.println("Failed to copy cover image for ISBN: " + isbn);
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


    // Mevcut kitap verilerini yüklerken rating değerlerini aralığın dışındaysa güncelleme:
    public static void updateAllRatingsToValidRange() {
        try {
            Path booksPath = Paths.get("books");
            Files.list(booksPath).forEach(path -> {
                try {
                    String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                    JSONTokener tokener = new JSONTokener(content);
                    Object json = tokener.nextValue();
                    if (json instanceof JSONObject) {
                        JSONObject bookJson = (JSONObject) json;
                        double rating = bookJson.optDouble("rating", 0.0);
                        if (rating < 0.0 || rating > 10.0) {
                            bookJson.put("rating", 0.0);
                            Files.write(path, bookJson.toString().getBytes(StandardCharsets.UTF_8));
                            System.out.println("Updated invalid rating to 0.0 for ISBN: " + bookJson.optString("isbn", "unknown"));
                        }
                    } else if (json instanceof JSONArray) {
                        // JSONArray için bir işlem tanımlanmadı, gerekiyorsa buraya ekleme yapılabilir
                        System.out.println("Skipping JSONArray file for the Rating conversion process at: " + path);
                    }
                } catch (IOException e) {
                    System.err.println("Error updating rating: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            System.err.println("Failed to access books directory: " + e.getMessage());
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

        updateAllRatingsToValidRange();
        loadExistingBooks("books");

        filteredBooks = new FilteredList<>(booksData, p -> true);
        Label titleLabel = new Label("Book Catalog [v1.9]");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setPadding(new Insets(5));


        //HELP & ABOUT BUTTONS
        MenuBar menuBar = new MenuBar();
        Menu helpMenu = new Menu("Help");

        MenuItem helpMenuItemAdd = new MenuItem("Add Help");
        MenuItem helpMenuItemEdit = new MenuItem("Edit Help");
        MenuItem helpMenuItemDelete = new MenuItem("Delete Help");
        MenuItem helpMenuItemSearch = new MenuItem("Search Help");
        MenuItem helpMenuItemFilters = new MenuItem("Filters Help");
        MenuItem helpMenuItemImport = new MenuItem("Import Help");
        MenuItem helpMenuItemExport = new MenuItem("Export Help");

        Menu aboutMenu = new Menu("About");
        MenuItem aboutMenuItem = new MenuItem("About");

        helpMenu.getItems().add(helpMenuItemAdd);
        helpMenu.getItems().add(helpMenuItemEdit);
        helpMenu.getItems().add(helpMenuItemDelete);
        helpMenu.getItems().add(helpMenuItemSearch);
        helpMenu.getItems().add(helpMenuItemFilters);
        helpMenu.getItems().add(helpMenuItemImport);
        helpMenu.getItems().add(helpMenuItemExport);

        aboutMenu.getItems().add(aboutMenuItem);
        menuBar.getMenus().addAll(helpMenu,aboutMenu);


        //SEARCH BUTTON, FILTER BUTTON & SEARCHING TEXT FIELD
        Label searchLabel = new Label("Search a book:");
        TextField searchField = new TextField();
        searchField.setPromptText("Enter book title, author, or ISBN"); //Tıklayınca kaybolan yazı.
        Button searchButton = new Button("Search");
        Button clearButton = new Button("Clear");
        clearButton.setVisible(false);
        Button filtersButton = new Button("Filters");
        HBox searchAndFiltersBox = new HBox(10, searchLabel, searchField, searchButton,clearButton, filtersButton);
        searchAndFiltersBox.setAlignment(Pos.CENTER);
        searchAndFiltersBox.setPadding(new Insets(15, 20, 15, 20));
        searchField.setMaxWidth(400);



        //title, help/about bar, ve search controls için toplayout vboxunda birleştirme işlemi.
        VBox topLayout = new VBox(5,menuBar, titleLabel, searchAndFiltersBox);
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
        importButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select JSON Files");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

            if (selectedFiles != null) {
                selectedFiles.forEach(file -> {


                    Path path = file.toPath();
                    processFile(path);


                    // Resim dosyasını kontrol et ve varsa kopyala
                    try {
                        String jsonContent = Files.readString(path);
                        JSONObject jsonObject = new JSONObject(jsonContent);
                        String isbn = jsonObject.optString("isbn", "unknown");

                        Path imageFilePath = path.getParent().resolve(path.getFileName().toString().replaceAll(".json$", ".jpg"));
                        Path destinationImagePath = Paths.get("src/coverImages/" + isbn + ".jpg");

                        if (Files.exists(imageFilePath) && !Files.exists(destinationImagePath)) {
                            Files.copy(imageFilePath, destinationImagePath, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Cover image file has found! Copied to 'src/coverImages' directory with ISBN as filename: " + isbn + ".jpg\n");
                        }
                    } catch (IOException | JSONException ex) {
                        System.err.println("Couldn't find any cover image file for ISBN: " + ex.getMessage()+"\n");
                    }
                    Platform.runLater(() -> {
                        booksData.clear();
                        loadExistingBooks("books");
                        System.out.println("Reloading books after separating JSON Array...");
                    });
                });
            }
        });

        Button exportButton = new Button("Export JSON");
        exportButton.setOnAction(e-> {
            exportSelectedItems(stage,bookTable);

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
        TableColumn<Book, String> languageColumn = createColumn("Language", Book::getLanguage);
        TableColumn<Book, String> ratingColumn = createColumn("Rating",
                book -> String.format("%.1f", book.getRating())); //double problem yarattığı için String formatına çevirdik.
        TableColumn<Book, String> tagsColumn = createColumnForList("Tags", Book::getTags);



        //THE CODE FOR ALL TABLE ELEMENTS FITS IN LAYOUT
        TableColumn<?, ?>[] columns = {
                titleColumn, subtitleColumn, authorsColumn, translatorsColumn, isbnColumn, publisherColumn,
                publicationDateColumn, editionColumn, languageColumn, ratingColumn, tagsColumn
        };
        editColumnWidths(bookTable, columns, 0.0769);


        //COLUMN CREATION
        bookTable.getColumns().addAll(
                titleColumn, subtitleColumn, authorsColumn, translatorsColumn,
                isbnColumn, publisherColumn, publicationDateColumn, editionColumn,
                languageColumn, ratingColumn, tagsColumn
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

        helpMenuItemAdd.setOnAction(e -> {
            // Create a new stage and layout
            Stage helpStage = new Stage();
            StackPane rootPane = new StackPane();
            rootPane.setStyle("-fx-background-color: lightgrey");

            // Load the image, ensure the path is correct
            Image helpImage = new Image("file:src/coverImages/forHelpAddUpdated.png");
            ImageView imageView = new ImageView(helpImage);

            // Check if the image is loaded
            if (helpImage.isError()) {
                System.out.println("Image cannot be loaded. Check the file path.");
            }

            imageView.setPreserveRatio(true);

            // Bind image view size to stack pane size
            imageView.fitWidthProperty().bind(rootPane.widthProperty());
            imageView.fitHeightProperty().bind(rootPane.heightProperty());

            // Add the image to the layout and set the stage
            rootPane.getChildren().add(imageView);
            Scene scene = new Scene(rootPane, 700, 600);

            helpStage.setTitle("Add Help");
            helpStage.setScene(scene);
            helpStage.show();
        });

        helpMenuItemEdit.setOnAction(e -> {
            // Create a new stage and layout
            Stage helpStage = new Stage();
            StackPane rootPane = new StackPane();
            rootPane.setStyle("-fx-background-color: lightgrey");

            // Load the image, make sure the path is correct
            Image helpImage = new Image("file:src/coverImages/forHelpEditUpdated.png");
            ImageView imageView = new ImageView(helpImage);

            // Check if the image has loaded correctly
            if (helpImage.isError()) {
                System.out.println("Image cannot be loaded. Check the file path.");
            }

            imageView.setPreserveRatio(true); // Preserve aspect ratio

            // Bind the image view size to the stack pane size
            imageView.fitWidthProperty().bind(rootPane.widthProperty());
            imageView.fitHeightProperty().bind(rootPane.heightProperty());

            // Add the image to the layout and set the stage
            rootPane.getChildren().add(imageView);
            Scene scene = new Scene(rootPane, 700, 600); // Initial size, but can be resized

            helpStage.setTitle("Edit Help");
            helpStage.setScene(scene);
            helpStage.show();
        });


        helpMenuItemDelete.setOnAction(e -> {
            // Yeni sahneyi (stage) ve düzeni (pane) oluştur
            Stage helpStage = new Stage();
            StackPane rootPane = new StackPane();
            rootPane.setStyle("-fx-background-color: lightgrey");

            // Resmi yükle, yol düzeltildi
            Image helpImage = new Image("file:src/coverImages/forHelpDeleteUpdated.png");
            ImageView imageView = new ImageView(helpImage);

            // Resmin yüklenip yüklenmediğini kontrol et
            if (helpImage.isError()) {
                System.out.println("Image cannot be loaded. Check the file path.");
            }

            imageView.setPreserveRatio(true); // Oranı koru
            imageView.setFitHeight(450); // Gösterilecek resmin boyutunu ayarla
            imageView.setFitWidth(700);

            imageView.fitWidthProperty().bind(rootPane.widthProperty());
            imageView.fitHeightProperty().bind(rootPane.heightProperty());
            // Resmi düzene ekle ve sahneye ata
            rootPane.getChildren().add(imageView);
            Scene scene = new Scene(rootPane, 700, 600);


            helpStage.setTitle("Delete Help");
            helpStage.setScene(scene);
            helpStage.show();

        });

        helpMenuItemSearch.setOnAction(e -> {
            // Yeni sahneyi (stage) ve düzeni (pane) oluştur
            Stage helpStage = new Stage();
            StackPane rootPane = new StackPane();
            rootPane.setStyle("-fx-background-color: lightgrey");

            // Resmi yükle, yol düzeltildi
            Image helpImage = new Image("file:src/coverImages/forHelpSearchUpdated.png");
            ImageView imageView = new ImageView(helpImage);

            // Resmin yüklenip yüklenmediğini kontrol et
            if (helpImage.isError()) {
                System.out.println("Image cannot be loaded. Check the file path.");
            }

            imageView.setPreserveRatio(true); // Oranı koru
            imageView.setFitHeight(450); // Gösterilecek resmin boyutunu ayarla
            imageView.setFitWidth(700);

            imageView.fitWidthProperty().bind(rootPane.widthProperty());
            imageView.fitHeightProperty().bind(rootPane.heightProperty());

            // Resmi düzene ekle ve sahneye ata
            rootPane.getChildren().add(imageView);
            Scene scene = new Scene(rootPane, 700, 600);


            helpStage.setTitle("Search Help");
            helpStage.setScene(scene);
            helpStage.show();

        });

        helpMenuItemFilters.setOnAction(e -> {
            // Yeni sahneyi (stage) ve düzeni (pane) oluştur
            Stage helpStage = new Stage();
            StackPane rootPane = new StackPane();
            rootPane.setStyle("-fx-background-color: lightgrey");

            // Resmi yükle, yol düzeltildi
            Image helpImage = new Image("file:src/coverImages/forHelpFiltersUpdated.png");
            ImageView imageView = new ImageView(helpImage);

            // Resmin yüklenip yüklenmediğini kontrol et
            if (helpImage.isError()) {
                System.out.println("Image cannot be loaded. Check the file path.");
            }

            imageView.setPreserveRatio(true); // Oranı koru
            imageView.setFitHeight(450); // Gösterilecek resmin boyutunu ayarla
            imageView.setFitWidth(700);

            imageView.fitWidthProperty().bind(rootPane.widthProperty());
            imageView.fitHeightProperty().bind(rootPane.heightProperty());

            // Resmi düzene ekle ve sahneye ata
            rootPane.getChildren().add(imageView);
            Scene scene = new Scene(rootPane, 700, 600);


            helpStage.setTitle("Filters Help");
            helpStage.setScene(scene);
            helpStage.show();

        });

        helpMenuItemImport.setOnAction(e -> {
            // Yeni sahneyi (stage) ve düzeni (pane) oluştur
            Stage helpStage = new Stage();
            StackPane rootPane = new StackPane();
            rootPane.setStyle("-fx-background-color: lightgrey");

            // Resmi yükle, yol düzeltildi
            Image helpImage = new Image("file:src/coverImages/forHelpImportUpdated.png");
            ImageView imageView = new ImageView(helpImage);

            // Resmin yüklenip yüklenmediğini kontrol et
            if (helpImage.isError()) {
                System.out.println("Image cannot be loaded. Check the file path.");
            }

            imageView.setPreserveRatio(true); // Oranı koru
            imageView.setFitHeight(450); // Gösterilecek resmin boyutunu ayarla
            imageView.setFitWidth(700);

            imageView.fitWidthProperty().bind(rootPane.widthProperty());
            imageView.fitHeightProperty().bind(rootPane.heightProperty());

            // Resmi düzene ekle ve sahneye ata
            rootPane.getChildren().add(imageView);
            Scene scene = new Scene(rootPane, 700, 600);


            helpStage.setTitle("Import Help");
            helpStage.setScene(scene);
            helpStage.show();

        });

        helpMenuItemExport.setOnAction(e -> {
            // Yeni sahneyi (stage) ve düzeni (pane) oluştur
            Stage helpStage = new Stage();
            StackPane rootPane = new StackPane();
            rootPane.setStyle("-fx-background-color: lightgrey");

            // Resmi yükle, yol düzeltildi
            Image helpImage = new Image("file:src/coverImages/forHelpExportUpdated.png");
            ImageView imageView = new ImageView(helpImage);

            // Resmin yüklenip yüklenmediğini kontrol et
            if (helpImage.isError()) {
                System.out.println("Image cannot be loaded. Check the file path.");
            }

            imageView.setPreserveRatio(true); // Oranı koru
            imageView.setFitHeight(450); // Gösterilecek resmin boyutunu ayarla
            imageView.setFitWidth(700);

            imageView.fitWidthProperty().bind(rootPane.widthProperty());
            imageView.fitHeightProperty().bind(rootPane.heightProperty());

            // Resmi düzene ekle ve sahneye ata
            rootPane.getChildren().add(imageView);
            Scene scene = new Scene(rootPane, 700, 600);


            helpStage.setTitle("Export Help");
            helpStage.setScene(scene);
            helpStage.show();

        });






        //ABOUT BUTTON ACTION
        aboutMenuItem.setOnAction(e -> {
            // Yeni sahneyi (stage) ve düzeni (pane) oluştur
            Stage helpStage = new Stage();
            StackPane rootPane = new StackPane();

            // Resmi yükle, yol düzeltildi
            Image helpImage = new Image("file:src/coverImages/aboutButton.png");
            ImageView imageView = new ImageView(helpImage);

            // Resmin yüklenip yüklenmediğini kontrol et
            if (helpImage.isError()) {
                System.out.println("Image cannot be loaded. Check the file path.");
            }

            imageView.setPreserveRatio(true); // Oranı koru
            imageView.setFitHeight(450); // Gösterilecek resmin boyutunu ayarla
            imageView.setFitWidth(700);

            // Resmi düzene ekle ve sahneye ata
            rootPane.getChildren().add(imageView);
            Scene scene = new Scene(rootPane, 700, 600);

            helpStage.setTitle("About");
            helpStage.setScene(scene);
            helpStage.show();
        });



        searchButton.setOnAction(e -> {
            try {
                isFiltered=true;
                if(isFiltered){
                    bookTable.setPlaceholder(new Label("No books to display for entered key word(s)."));}
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
                    }
                    else if (matchesLanguage) {
                        book.setSearchPriority(9);
                    }
                    else if (matchesRating) {
                        book.setSearchPriority(10);
                    }
                    else if (matchesTags) {
                        book.setSearchPriority(11);
                    }
                    else{
                        book.setSearchPriority(Integer.MAX_VALUE); // No match
                        clearButton.setVisible(true);
                        return false;
                    }
                    clearButton.setVisible(true);
                    return true;
                });
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "An error occurred during search: " + ex.getMessage());
                alert.showAndWait();
                isFiltered=false;
            }
            // Sıralı listeyi oluşturma
            SortedList<Book> sortedBooks = new SortedList<>(filteredBooks);
            sortedBooks.setComparator(Comparator.comparingInt(Book::getSearchPriority));
            // TableView'ı güncelleme
            isFiltered=true;

            bookTable.setItems(sortedBooks);
            bookTable.refresh();
        });


        clearButton.setOnAction(e -> {
            searchField.setText("");  // Clear the search field
            /*
            bookTable.setItems(booksData);  // Reset to original book list or an appropriate list
             */
            filteredBooks.setPredicate(book -> { book.setSearchPriority(Integer.MAX_VALUE); return true;});
            bookTable.refresh();
            clearButton.setVisible(false);  // Hide the clear button after clearing
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