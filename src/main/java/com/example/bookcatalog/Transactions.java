package com.example.bookcatalog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;


import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;


public class Transactions {
    //COLORS FOR SAVE AND BACK BUTTON ANIMATION USING CSS
    private static String saveButtonBaseStyle = "-fx-font-weight: bold; -fx-background-color: #5cb85c; -fx-text-fill: white;";
    private static String saveButtonHoverStyle = "-fx-background-color: #4cae4c;"; // Lighter green when hovered

    private static String backButtonBaseStyle = "-fx-font-weight: bold; -fx-background-color: #f0ad4e; -fx-text-fill: white;";
    private static String backButtonHoverStyle = "-fx-background-color: #edb879;"; // Lighter orange when hovered


    private static double safeParseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0.0; // Default value or consider prompting user
        }
    }
    private static void setPromptTexts(Map<String, TextField> fieldMap) {
        Map<String, String> prompts = new HashMap<>();
        prompts.put("Authors", "Multiple authors must be entered using a comma (,) as a delimiter.");
        prompts.put("Translators", "Multiple translators must be entered using a comma (,) as a delimiter.");
        prompts.put("Tags", "Multiple tags must be entered using a comma (,) as a delimiter.");
        prompts.put("ISBN", "ISBN must be a unique 13-digit number.");
        prompts.put("Rating", "Rating must be an integer or float value.");

        fieldMap.forEach((key, textField) -> {
            if (prompts.containsKey(key)) {
                textField.setPromptText(prompts.get(key));
            }
        });
    }

    private static boolean checkIsbnExists(String directoryPath, String isbn) {
        try (Stream<Path> files = Files.list(Paths.get(directoryPath))) {
            return files.anyMatch(file -> file.getFileName().toString().equals(isbn + ".json"));
        } catch (IOException e) {
            System.err.println("Error checking ISBN existence: " + e.getMessage());
            return false;
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


        setPromptTexts(fieldMap); //Boş textFieldlarda gözükmesi gereken uyarılar.





        //SAVE BUTTON CREATION AND ANIMATION

        Button saveButton = new Button("Save");
        saveButton.setStyle(saveButtonBaseStyle);
        saveButton.setOnMouseEntered(e -> saveButton.setStyle(saveButtonBaseStyle + saveButtonHoverStyle));
        saveButton.setOnMouseExited(e -> saveButton.setStyle(saveButtonBaseStyle));


        //SAVE BUTTON ACTION

        saveButton.setOnAction(e -> {
            String isbn = fieldMap.get("ISBN").getText();
            String ratingStr = fieldMap.get("Rating").getText();
            String title = fieldMap.get("Title").getText();
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
            }else if(title.isEmpty()){
                Alert alert = new Alert(Alert.AlertType.WARNING, "Title field cannot be blank.");
                alert.showAndWait();
            }
            else if(!ratingStr.matches("\\d*(\\.\\d+)?|^$")){
                Alert alert = new Alert(Alert.AlertType.WARNING, "Rating must be an integer, float, or empty.");
                alert.showAndWait();
            }else if (checkIsbnExists("books", isbn)) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "This ISBN already exists. Please check the ISBN number.");
                alert.showAndWait();
            }

            else {
                try {
                    String directoryPath = "books";
                    String fileName = directoryPath + "/" + isbn + ".json";

                    java.nio.file.Path path = Paths.get(directoryPath);
                    if (!Files.exists(path)) {
                        Files.createDirectories(path);
                    }


                    //REACHING THE TEXTFIELDS THAT USER FILL BY USING MAP

                    //String title = fieldMap.get("Title").getText();
                    String subtitle = fieldMap.get("Subtitle").getText();
                    List<String> authors = Arrays.asList(fieldMap.get("Authors").getText().split(",\\s*"));
                    List<String> translators = Arrays.asList(fieldMap.get("Translators").getText().split(",\\s*"));
                    String publisher = fieldMap.get("Publisher").getText();
                    String date = fieldMap.get("Date").getText();
                    String edition = fieldMap.get("Edition").getText();
                    String cover = fieldMap.get("Cover").getText();
                    String language = fieldMap.get("Language").getText();
                    double rating = safeParseDouble(ratingStr);
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



    private static void updateBookDetails(JSONObject existingJson, Map<String, Object> userInput) {
        boolean isUpdated = false;
        for (String key : userInput.keySet()) {
            Object userValue = userInput.get(key);
            Object jsonValue = existingJson.opt(key);
            if ((userValue instanceof JSONArray && !jsonArraysEqual((JSONArray) userValue, existingJson.optJSONArray(key))) || !Objects.equals(userValue, jsonValue)) {
                existingJson.put(key, userValue);
                isUpdated = true;
            }
        }
    }

    private static void updateObservableList(Book selectedBook, JSONObject updatedJson) {

        selectedBook.setTitle(updatedJson.getString("title"));
        selectedBook.setSubtitle(updatedJson.getString("subtitle"));
        selectedBook.setAuthors(Arrays.asList(updatedJson.getJSONArray("authors").toList().toArray(new String[0])));
        selectedBook.setTranslators(Arrays.asList(updatedJson.getJSONArray("translators").toList().toArray(new String[0])));
        selectedBook.setIsbn(updatedJson.getString("isbn"));
        selectedBook.setPublisher(updatedJson.getString("publisher"));
        selectedBook.setDate(updatedJson.getString("date"));
        selectedBook.setEdition(updatedJson.getString("edition"));
        selectedBook.setCover(updatedJson.getString("cover"));
        selectedBook.setLanguage(updatedJson.getString("language"));
        selectedBook.setRating(updatedJson.getDouble("rating"));
        selectedBook.setTags(Arrays.asList(updatedJson.getJSONArray("tags").toList().toArray(new String[0])));

        Platform.runLater(() -> {
            System.out.println("Book (ISBN: " + selectedBook.getIsbn() + ") has changed.");
        });
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
                    textField.setText(String.valueOf(selectedBook.getRating()));
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


        setPromptTexts(fieldMap);



        //SAVE BUTTON FOR EDIT

        Button saveButton = new Button("Save");
        saveButton.setStyle(saveButtonBaseStyle);
        saveButton.setOnMouseEntered(e -> saveButton.setStyle(saveButtonBaseStyle + saveButtonHoverStyle));
        saveButton.setOnMouseExited(e -> saveButton.setStyle(saveButtonBaseStyle));


        saveButton.setOnAction(e->{
            // Kullanıcıdan alınan veriler
            Map<String, Object> userInput = new HashMap<>();
            userInput.put("title", fieldMap.get("Title").getText());
            userInput.put("subtitle", fieldMap.get("Subtitle").getText());
            userInput.put("authors", new JSONArray(Arrays.asList(fieldMap.get("Authors").getText().split(",\\s*"))));
            userInput.put("translators", new JSONArray(Arrays.asList(fieldMap.get("Translators").getText().split(",\\s*"))));
            userInput.put("isbn", fieldMap.get("ISBN").getText());
            userInput.put("publisher", fieldMap.get("Publisher").getText());
            userInput.put("date", fieldMap.get("Date").getText());
            userInput.put("edition", fieldMap.get("Edition").getText());
            userInput.put("cover", fieldMap.get("Cover").getText());
            userInput.put("language", fieldMap.get("Language").getText());
            if(userInput.get("rating")==null || userInput.get("rating")=="" || userInput.get("rating")==" "){
                userInput.put("rating","0.0");
            }else{userInput.put("rating", fieldMap.get("Rating").getText());
            }
            userInput.put("tags", new JSONArray(Arrays.asList(fieldMap.get("Tags").getText().split(",\\s*"))));

            String directoryPath = "books";
            String filePath = directoryPath + "/" + selectedBook.getIsbn() + ".json";

            try {
                // Mevcut JSON dosyasını okuma kısmı:
                Path path = Paths.get(filePath);
                String content = Files.readString(path);
                JSONObject existingJson = new JSONObject(content);

                String oldIsbn = existingJson.optString("isbn");
                String newIsbn = userInput.get("isbn").toString();

                updateBookDetails(existingJson, userInput);


                //If there is a String existing in rating part of json change it with default double value
                String RatingStr = userInput.get("rating").toString();


                // Process user input and compare it with existing JSON data
                boolean isUpdated = false;
                for (String key : userInput.keySet()) {
                    Object userValue = userInput.get(key);
                    Object jsonValue = existingJson.opt(key);

                    if (userValue instanceof JSONArray) {
                        JSONArray userArray = (JSONArray) userValue;
                        JSONArray jsonArray = existingJson.optJSONArray(key);

                        if (!jsonArraysEqual(userArray, jsonArray)) {
                            existingJson.put(key, userValue);
                            isUpdated = true;
                        }
                    } else if (!Objects.equals(userValue, jsonValue)) {
                        existingJson.put(key, userValue);
                        isUpdated = true;
                    }
                }



                // Eğer güncelleme yapıldıysa, dosyayı güncelleme kısmı:
                if (isUpdated) {
                    if (!oldIsbn.equals(newIsbn) && checkIsbnExists("books", newIsbn)) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "This ISBN already exists. Please check the ISBN number.");
                        alert.showAndWait();
                        return;
                    }
                    // Eğer ISBN güncellendiyse, dosyanın ismini değiştirme kısmı:
                    if (!oldIsbn.equals(newIsbn)) {
                        Path newPath = Paths.get("books", newIsbn + ".json");
                        Files.move(path, newPath, StandardCopyOption.REPLACE_EXISTING);
                        path = newPath; // Güncellenmiş path ile devam et
                    }
                    Files.writeString(path, existingJson.toString(), StandardOpenOption.TRUNCATE_EXISTING);
                    System.out.println("Successfully updated: " + filePath);
                }
                else if (oldIsbn.isEmpty() || newIsbn.isEmpty()) {
                    // ISBN alanı boşsa, kullanıcıyı uyar çünkü json ismi ona göre belirleniyor.
                    Alert alert = new Alert(Alert.AlertType.WARNING, "ISBN field cannot be left blank.");
                    alert.showAndWait();
                } else if (oldIsbn.length() > 13 || newIsbn.length() > 13) { Alert alert = new Alert(Alert.AlertType.WARNING,
                        //ISBN 13 rakamdan fazlaysa uyar.
                        "ISBN cannot be more than 13-digit value.");
                    alert.showAndWait();
                }else if(!oldIsbn.matches("\\d+") || !newIsbn.matches("\\d+")){
                    //ISBN string ifade içeriyorsa uyar.
                    Alert alert = new Alert(Alert.AlertType.WARNING, "ISBN must be a 13-digit numeric value.");
                    alert.showAndWait();
                }
                else if(!RatingStr.matches("\\d*(\\.\\d+)?|^$")){
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Rating must be an integer, float, or empty.");
                    alert.showAndWait();
                }else if(userInput.get("title").toString().isEmpty()){
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Title field cannot be blank.");
                    alert.showAndWait();
                }


                // Refresh the observable list and the table
                updateObservableList(selectedBook, existingJson);
                Platform.runLater(() -> GUI.bookTable.refresh());

            } catch (IOException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update the book: " + ex.getMessage());
                alert.showAndWait();
            }
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

    public static boolean jsonArraysEqual(JSONArray arr1, JSONArray arr2) {
        if (arr1 == null || arr2 == null) return true; // Null control
        if (arr1.length() != arr2.length()) return true;

        for (int i = 0; i < arr1.length(); i++) {
            if (!Objects.equals(arr1.opt(i), arr2.opt(i))) {
                return true;
            }
        }
        return false;
    }

    public static void deleteBooks(Stage stage, Scene mainScene, List<Book> selectedBooks) {
        selectedBooks.forEach(book -> {
            if (book.getIsbn() != null) {
                String directoryPath = "books";
                String filePath = directoryPath + "/" + book.getIsbn() + ".json";
                Path pathToDelete = Paths.get(filePath);

                System.out.println("Attempting to delete: " + pathToDelete.toAbsolutePath());

                try {
                    if (Files.exists(pathToDelete)) {
                        Files.delete(pathToDelete);  // Eğer dosya varsa sil
                        System.out.println("Successfully deleted: " + filePath);
                    } else {
                        System.out.println("File does not exist: " + filePath);
                    }
                } catch (IOException e) {
                    System.out.println("Failed to delete: " + filePath);
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "An error occurred while trying to delete the book: " + e.getMessage());
                        alert.showAndWait();
                    });
                }
            } else {
                System.out.println("No ISBN found for book with ISBN: " + book.getIsbn());
            }
        });
        Platform.runLater(() -> {
            GUI.booksData.removeAll(selectedBooks);  // Seçili kitapları veri listesinden kaldır
            stage.setScene(mainScene); // Ana sahneyi yeniden yükle
        });
    }








}
