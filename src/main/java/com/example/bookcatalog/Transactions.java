package com.example.bookcatalog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;


import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;


public class Transactions {

    public static Map<String, String> isbnToUuidMap = new HashMap<>();//IN ADD METHOD , WE CREATE AN RANDOM UUID NUMBER
                                                                      //TO CREATE A UNIQUE JSON FILE NAME
                                                                      //FOR EDITING,WE MUST LOCATE THIS UUID NUMBER
                                                                      //SO WE CREATE A MAP WHICH ITS KEY IS THE ADDED BOOKS ISBN NUMBER AND VALUE IS UUID NUMBER

    private static double safeParseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0.0; // Default value or consider prompting user
        }
    }

    public static void showAddBookSection(Stage stage, Scene mainScene) {



        Label infoLabel = new Label("Please enter all the information about the book you want to add.");
        infoLabel.setWrapText(true); //layout değişikliğinde yazının satır atlaması ve resize işlemleri
        infoLabel.setMaxWidth(700); // yazının max genişliği
        infoLabel.setAlignment(Pos.TOP_CENTER);
        infoLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10px;");
        //css ile görünüm iyileştirme


        //NEW VBOX FOR BODY (TO INCLUDE TEXT FIELD AND LABELS)

        VBox bookInfoEnteringField = new VBox(10);
        bookInfoEnteringField.setAlignment(Pos.TOP_CENTER);
        bookInfoEnteringField.setPadding(new Insets(20, 40, 20, 40));


        //NEW MAP FOR ALL THE BOOK DATA

        Map<String, TextField> fieldMap = new HashMap<>();
        String[] fieldNames = {"Title", "Subtitle", "Authors", "Translators", "ISBN", "Publisher", "Date", "Edition", "Cover", "Language", "Rating", "Tags"};


        //A LOOP TO CREATE ALL LABELS AND TEXTFIELDS IN ORDER

        for (String fieldName : fieldNames) {
            TextField textField = new TextField();
            textField.setPrefWidth(600);
            fieldMap.put(fieldName, textField);
            Label label = new Label(fieldName + ":");
            label.setMinWidth(60);
            HBox hbox = new HBox(10, label, textField);
            hbox.setAlignment(Pos.CENTER);
            bookInfoEnteringField.getChildren().add(hbox);
        }


        //COLORS FOR SAVE AND BACK BUTTON ANIMATION USING CSS

        String saveButtonBaseStyle = "-fx-font-weight: bold; -fx-background-color: #5cb85c; -fx-text-fill: white;";
        String saveButtonHoverStyle = "-fx-background-color: #4cae4c;"; // Lighter green when hovered

        String backButtonBaseStyle = "-fx-font-weight: bold; -fx-background-color: #f0ad4e; -fx-text-fill: white;";
        String backButtonHoverStyle = "-fx-background-color: #edb879;"; // Lighter orange when hovered



        //SAVE BUTTON CREATION AND ANIMATION

        Button saveButton = new Button("Save");
        saveButton.setStyle(saveButtonBaseStyle);
        saveButton.setOnMouseEntered(e -> saveButton.setStyle(saveButtonBaseStyle + saveButtonHoverStyle));
        saveButton.setOnMouseExited(e -> saveButton.setStyle(saveButtonBaseStyle));


        //SAVE BUTTON ACTION

        saveButton.setOnAction(e -> {
            String isbn = fieldMap.get("ISBN").getText();
            if (isbn.isEmpty()) {
                // ISBN alanı boşsa, kullanıcıyı uyar çünkü json ismi ona göre belirleniyor.
                Alert alert = new Alert(Alert.AlertType.WARNING, "ISBN field cannot be left blank.");
                alert.showAndWait();
            } else if (isbn.length() > 13) { Alert alert = new Alert(Alert.AlertType.WARNING,
                    //ISBN 13 rakamdan fazlaysa uyar.
                    "ISBN cannot be more than 13-digit value.");
                alert.showAndWait();
            }else if(!isbn.matches("\\d+")){
                //ISBN string ifade içeriyorsa uyar.
                Alert alert = new Alert(Alert.AlertType.WARNING, "ISBN must be a 13-digit numeric value.");
                alert.showAndWait();
            }
            else {
                try {
                    String directoryPath = "books";
                    String randomUUID = UUID.randomUUID().toString();//INITIALIZING RANDOM UUID FOR "ISBN TO UUID" MAP
                    String fileName = directoryPath + "/" + randomUUID + ".json";

                    java.nio.file.Path path = Paths.get(directoryPath);
                    if (!Files.exists(path)) {
                        Files.createDirectories(path);
                    }


                    //REACHING THE TEXTFIELDS THAT USER FILL BY USING MAP

                    String title = fieldMap.get("Title").getText();
                    String subtitle = fieldMap.get("Subtitle").getText();
                    List<String> authors = Arrays.asList(fieldMap.get("Authors").getText().split(",\\s*"));
                    List<String> translators = Arrays.asList(fieldMap.get("Translators").getText().split(",\\s*"));
                    String publisher = fieldMap.get("Publisher").getText();
                    String date = fieldMap.get("Date").getText();
                    String edition = fieldMap.get("Edition").getText();
                    String cover = fieldMap.get("Cover").getText();
                    String language = fieldMap.get("Language").getText();
                    double rating = safeParseDouble(fieldMap.get("Rating").getText());
                    List<String> tags = Arrays.asList(fieldMap.get("Tags").getText().split(",\\s*"));


                    //CREATING THE JSON FILE VIA INFO WE GET FROM TEXT FIELDS

                    Book newBook = new Book(title, subtitle, authors, translators, isbn, publisher, date, edition, cover, language, rating, tags);

                    JSONObject bookJson = new JSONObject();
                    bookJson.put("title", title);
                    bookJson.put("subtitle", subtitle);
                    bookJson.put("authors", new JSONArray(authors));
                    bookJson.put("translators", new JSONArray(translators));
                    bookJson.put("isbn", isbn);
                    bookJson.put("publisher", publisher);
                    bookJson.put("date", date);
                    bookJson.put("edition", edition);
                    bookJson.put("cover", cover);
                    bookJson.put("language", language);
                    bookJson.put("rating", rating);
                    bookJson.put("tags", new JSONArray(tags));


                    //PUT THE NEW BOOK'S ISBN AS KEY AND ITS JSON UUID NUMBER AS VALUE IN "ISBN TO VALUE"
                    isbnToUuidMap.put(isbn, randomUUID);
                    //FILE OUTPUT

                    Files.write(Paths.get(fileName), bookJson.toString().getBytes(), StandardOpenOption.CREATE_NEW);
                    System.out.println("Successfully saved to " + fileName);


                    //UPDATING THE BOOK TABLE -TABLEVIEW-

                    Platform.runLater(() -> GUI.booksData.add(newBook));

                    stage.setScene(mainScene);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }}
        );


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
        VBox.setMargin(buttonBox, new Insets(0, 0, 0, 0));


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

    public static void showEditBookSection(Stage stage, Scene mainScene, Book selectedBook) {

        //EDIT PAGE LAYOUT INSTALLATIONS
        Label infoLabel = new Label("Please edit the information about the book");
        infoLabel.setWrapText(true);
        infoLabel.setMaxWidth(700);
        infoLabel.setAlignment(Pos.TOP_CENTER);
        infoLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10;");

        VBox bookInfoEnteringField = new VBox(10);
        bookInfoEnteringField.setAlignment(Pos.TOP_CENTER);
        bookInfoEnteringField.setPadding(new Insets(20, 40, 20, 40));

        Map<String, TextField> fieldMap = new HashMap<>();
        String[] fieldNames = {"Title", "Subtitle", "Authors", "Translators", "ISBN", "Publisher", "Date", "Edition", "Cover", "Language", "Rating", "Tags"};

        // CREATE EACH ATTRIBUTE'S LABEL AND TEXT FIELD BY LOOP
        for (String fieldName : fieldNames) {
            TextField textField = new TextField();
            textField.setPrefWidth(600);

            // FILL THE TEXT FIELDS BY SELECTED BOOK'S INFORMATION'S
            switch (fieldName) {
                case "Title":
                    textField.setText(selectedBook.getTitle());
                    break;
                case "Subtitle":
                    textField.setText(selectedBook.getSubtitle());
                    break;
                case "Authors":
                    textField.setText(String.join(", ", selectedBook.getAuthors()));
                    break;
                case "Translators":
                    textField.setText(String.join(", ", selectedBook.getTranslators()));
                    break;
                case "ISBN":
                    textField.setText(selectedBook.getIsbn());
                    break;
                case "Publisher":
                    textField.setText(selectedBook.getPublisher());
                    break;
                case "Date":
                    textField.setText(selectedBook.getDate());
                    break;
                case "Edition":
                    textField.setText(selectedBook.getEdition());
                    break;
                case "Cover":
                    textField.setText(selectedBook.getCover());
                    break;
                case "Language":
                    textField.setText(selectedBook.getLanguage());
                    break;
                case "Rating":
                    textField.setText(Double.toString(selectedBook.getRating()));
                    break;
                case "Tags":
                    textField.setText(String.join(", ", selectedBook.getTags()));
                    break;
            }

            //TEXT FIELDS LAYOUT
            fieldMap.put(fieldName, textField);
            Label label = new Label(fieldName + ":");
            label.setMinWidth(60);
            HBox hbox = new HBox(10, label, textField);
            hbox.setAlignment(Pos.CENTER);
            bookInfoEnteringField.getChildren().add(hbox);
        }

        //COLORS FOR SAVE AND BACK BUTTON ANIMATION USING CSS
        String saveButtonBaseStyle = "-fx-font-weight: bold; -fx-background-color: #5cb85c; -fx-text-fill: white;";
        String saveButtonHoverStyle = "-fx-background-color: #4cae4c;"; // Mouse üzerine gelince

        String backButtonBaseStyle = "-fx-font-weight: bold; -fx-background-color: #f0ad4e; -fx-text-fill: white;";
        String backButtonHoverStyle = "-fx-background-color: #edb879;"; // Mouse üzerine gelince

        //SAVE BUTTON FOR EDIT

        Button saveButton = new Button("Save");
        saveButton.setStyle(saveButtonBaseStyle);
        saveButton.setOnMouseEntered(e -> saveButton.setStyle(saveButtonBaseStyle + saveButtonHoverStyle));
        saveButton.setOnMouseExited(e -> saveButton.setStyle(saveButtonBaseStyle));


        saveButton.setOnAction(e->{

            //REACHING THE TEXT FIELDS THAT USER FILL BY USING MAP(SAME AS ADD METHOD)

            selectedBook.setTitle(fieldMap.get("Title").getText());
            selectedBook.setSubtitle(fieldMap.get("Subtitle").getText());
            selectedBook.setAuthors(Arrays.asList(fieldMap.get("Authors").getText().split(",\\s*")));
            selectedBook.setTranslators(Arrays.asList(fieldMap.get("Translators").getText().split(",\\s*")));
            selectedBook.setIsbn(fieldMap.get("ISBN").getText());
            selectedBook.setPublisher(fieldMap.get("Publisher").getText());
            selectedBook.setDate(fieldMap.get("Date").getText());
            selectedBook.setEdition(fieldMap.get("Edition").getText());
            selectedBook.setCover(fieldMap.get("Cover").getText());
            selectedBook.setLanguage(fieldMap.get("Language").getText());
            selectedBook.setRating(safeParseDouble(fieldMap.get("Rating").getText()));
            selectedBook.setTags(Arrays.asList(fieldMap.get("Tags").getText().split(",\\s*")));


            try {
               //CREATING A JSON OBJECT TO HOLD NEW INPUTS
                JSONObject bookJson = new JSONObject();
                bookJson.put("title",selectedBook.getTitle());
                bookJson.put("subtitle",selectedBook.getSubtitle());
                bookJson.put("authors",new JSONArray(selectedBook.getAuthors()));
                bookJson.put("translators",new JSONArray(selectedBook.getTranslators()));
                bookJson.put("isbn", selectedBook.getIsbn());
                bookJson.put("publisher", selectedBook.getPublisher());
                bookJson.put("date", selectedBook.getDate());
                bookJson.put("edition", selectedBook.getEdition());
                bookJson.put("cover", selectedBook.getCover());
                bookJson.put("language", selectedBook.getLanguage());
                bookJson.put("rating",selectedBook.getRating());
                bookJson.put("tags", new JSONArray(selectedBook.getTags()));


                //BY BOOK'S ISBN NUMBER, THE SOFTWARE FINDS THE JSON FILE NAME(BY "ISBN TO UUID" MAP)
                String directoryPath = "books";
                String filePath = directoryPath+ "/"+ isbnToUuidMap.get(selectedBook.getIsbn()) + ".json";
                Files.write(Paths.get(filePath), bookJson.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);


            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            stage.setScene(mainScene);

        });

        //BACK BUTTON FOR EDIT

        Button backButton = new Button("Back");
        backButton.setStyle(backButtonBaseStyle);
        backButton.setOnMouseEntered(e -> backButton.setStyle(backButtonBaseStyle + backButtonHoverStyle));
        backButton.setOnMouseExited(e -> backButton.setStyle(backButtonBaseStyle));

        backButton.setOnAction(e -> {
            stage.setScene(mainScene);
        });


        // EDIT WINDOW LAYOUT
        HBox buttons = new HBox(20,saveButton,backButton);
        buttons.setAlignment(Pos.BASELINE_CENTER);
        VBox mainLayout = new VBox(20, infoLabel, bookInfoEnteringField,buttons);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setStyle("-fx-background: #f4f4f4; -fx-border-color: #f4f4f4;");

        Scene editBookScene = new Scene(scrollPane, 800, 600);
        stage.setScene(editBookScene);


    }
    public static void deleteBooks(Stage stage, Scene mainScene, List<Book> selectedBooks) {
        selectedBooks.forEach(book -> {
            String uuid = isbnToUuidMap.get(book.getIsbn()); // Kitabın ISBN numarasına göre UUID'yi alıyo
            if (uuid != null) {
                try {
                    String directoryPath = "books";
                    String filePath = directoryPath + "/" + uuid + ".json";
                    Files.deleteIfExists(Paths.get(filePath)); // UUID'ye ait dosyayı siliyo
                    System.out.println("Successfully deleted: " + filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "An error occurred while trying to delete the book.");
                        alert.showAndWait();
                    });
                }
            }
        });
        Platform.runLater(() -> {
            GUI.booksData.removeAll(selectedBooks);// GUI'den kitapları kaldırma burda table view refresh actionda
            stage.setScene(mainScene);
        });
    }





}
