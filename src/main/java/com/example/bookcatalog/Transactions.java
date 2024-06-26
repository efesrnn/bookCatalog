package com.example.bookcatalog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;


import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.example.bookcatalog.GUI.booksData;


public class Transactions {
    //COLORS FOR SAVE AND BACK BUTTON ANIMATION USING CSS
    private static String saveButtonBaseStyle = "-fx-font-weight: bold; -fx-background-color: #5cb85c; -fx-text-fill: white;";
    private static String saveButtonHoverStyle = "-fx-background-color: #4cae4c;"; //yeşil renk kodu

    private static String backButtonBaseStyle = "-fx-font-weight: bold; -fx-background-color: #f0ad4e; -fx-text-fill: white;";
    private static String backButtonHoverStyle = "-fx-background-color: #edb879;"; //turuncu renk kodu


    private static String coverImagePath = null;




    private static Double validateRating(String ratingStr) {
        try {
            if(ratingStr.isEmpty()) return 0.0;
            double rating = Double.parseDouble(ratingStr);
            if (rating < 0.0 || rating > 10.0) {
                // Uyarı mesajı göster
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Rating");
                alert.setHeaderText(null);
                alert.setContentText("Rating must be a number between 0 and 10.");
                alert.showAndWait();
                return null; // Geçersiz değerler için null döner, bu da değerin güncellenmeyeceğini belirtir.
            }
            return rating; // Geçerli bir değerse, bu değeri döndür.
        } catch (NumberFormatException e) {
            // Sayısal olmayan bir giriş yapıldığında uyarı göster
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Rating");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a valid number for rating.");
            alert.showAndWait();
            return null; // Hatalı girişlerde null olarak ayarla.
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

        System.out.println("Prompt texts successfully loaded.");
    }
    //setPromptTexts sınıfı ile form alanlarına ipuçları ekleyerek kullanıcıların doğru ve düzenli bilgi girmelerini sağlıyoruz.
    //Örneğin, birden fazla yazar veya çevirmen girerken virgül kullanılmalıdır. Bu sayede olası hataları önlüyoruz.

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex); // Bu fonksiyonla, verilen dosya adından dosya uzantısını noktasıyla birlikte çıkarıp döndürdük.
        }
        return ""; // Default'ta uzantı yok.
    }

    private static boolean checkIsbnExists(String directoryPath, String isbn) {
        try (Stream<Path> files = Files.list(Paths.get(directoryPath))) {
            return files.anyMatch(file -> file.getFileName().toString().equals(isbn + ".json"));
        } catch (IOException e) {
            System.err.println("Error checking ISBN existence: " + e.getMessage());
            return false;
        }
    }
    //Verilen ISBN numarasını içeren dosyanın bir klasörde olup olmadığını kontrol edip ona göre true veya false dönüyor.

    private static String readJsonFile(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);
            System.out.println("Json successfully read.");
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Failed to read file: " + path);
            e.printStackTrace();
        }
        return "{}"; //Herhangi bir okuma hatasında jsonı boşa çevirip programın çalışmayı durdurmasını engelliyor.
    }



    public static void showAddBookSection(Stage stage, Scene mainScene) {

        System.out.println("Layout has changed as Edit Section.");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.TOP_CENTER);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(350);
        imageView.setFitWidth(350);
        imageView.setPreserveRatio(true);

        Button selectImageButton = new Button("Select Image");
        Button removeImageButton = new Button("Remove Image");

// Bu kod bloğuyla, başlangıçta varsayılan bir resmi yüklüyoruz.
        try {
            File file = new File("coverImages/default_image.jpg");
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
            } else {
                System.err.println("File does not exist: " + file.getAbsolutePath());
            }
        } catch (Exception ex) {
            System.err.println("Failed to load default image: " + ex.getMessage());
        }





// Sonrasında, finalCoverImagePath değişkeni lambda ifadesi içinde kullanıyoruz.
        selectImageButton.setOnAction(e -> {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Cover Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files",  "*.jpg")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                Image image = new Image(selectedFile.toURI().toString());
                imageView.setImage(image);
                // Eğer gerekirse, lambda dışında coverImagePath'i güncelliyoruz.
                coverImagePath = selectedFile.getAbsolutePath();
            }
        });

        removeImageButton.setOnAction(e -> {
            try {
                File defaultImageFile = new File("coverImages/default_image.jpg");
                if (defaultImageFile.exists()) {
                    imageView.setImage(new Image(defaultImageFile.toURI().toString()));
                    coverImagePath = defaultImageFile.getAbsolutePath(); // coverImagePath değişkenini varsayılan resmin path'ine ayarladık.
                } else {
                    System.err.println("Default image file not found: " + defaultImageFile.getAbsolutePath());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Failed to reload default image: " + ex.getMessage());
            }
        });

        HBox imageControls = new HBox(10, selectImageButton, removeImageButton);
        imageControls.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(imageView, imageControls);


        //Body için yeni VBOX (Text Field ve Label'i dahil edebilmek için)

        VBox bookInfoEnteringField = new VBox(10);
        bookInfoEnteringField.setAlignment(Pos.TOP_CENTER);
        bookInfoEnteringField.setPadding(new Insets(20, 40, 20, 40));


        //Bütün kitap dataları için yeni Map kullanıyoruz.

        Map<String, TextField> fieldMap = new HashMap<>();
        String[] fieldNames = {"Title", "Subtitle", "Authors", "Translators", "ISBN", "Publisher", "Date", "Edition", "Language", "Rating", "Tags"};


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
            String isbn = fieldMap.get("ISBN").getText().trim();
            String ratingStr = fieldMap.get("Rating").getText();
            String title = fieldMap.get("Title").getText();
            Double rating = validateRating(ratingStr);
            if (rating == null) {
                return; //alertten sonra menüye geri dönme
            }


            if (!isbn.matches("^[0-9Xx-]+$")) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "ISBN must be a 10 or 13-digit numeric value.");
                alert.showAndWait();
                return;
            }

            if (coverImagePath != null) {
                try {
                    Path sourcePath = Paths.get(coverImagePath);
                    // Dosya uzantısını dinamik olarak belirledik.
                    String fileExtension = "";
                    int extensionIndex = coverImagePath.lastIndexOf('.');
                    if (extensionIndex != -1) {
                        fileExtension = coverImagePath.substring(extensionIndex); // Nokta dahil olarak uzantıyı (".jpg") tutuyoruz.
                    }

                    // Klasörün var olduğundan emin olup sonrasında doğru uzantıya sahip hedef yolunu belirliyoruz.
                    Path targetPath = Paths.get("coverImages", isbn + fileExtension);
                    Files.createDirectories(targetPath.getParent());

                    // Bu kod bloğunun amacı resim dosyasını hedef konuma kopyaladıktan sonra var olan dosyanın üzerine yazdırma işlemi."
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                    // coverImagePath'i yeni yolunu yansıtır şekilde güncelliyoruz.
                    coverImagePath = targetPath.toString();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save cover image.");
                    alert.showAndWait();
                    return;
                }
            }



            if(title.isEmpty()){
                Alert alert = new Alert(Alert.AlertType.WARNING, "Title field cannot be blank.");
                alert.showAndWait();
            }
            else if(!ratingStr.matches("\\d*(\\.\\d+)?|^$")){
                Alert alert = new Alert(Alert.AlertType.WARNING, "Rating must be an integer, float, or empty.");
                alert.showAndWait();
            }
            else if (checkIsbnExists("books", isbn)) {
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

                    String cover = coverImagePath;
                    //String title = fieldMap.get("Title").getText();
                    String subtitle = fieldMap.get("Subtitle").getText();
                    List<String> authors = Arrays.asList(fieldMap.get("Authors").getText().split(",\\s*"));
                    List<String> translators = Arrays.asList(fieldMap.get("Translators").getText().split(",\\s*"));
                    String publisher = fieldMap.get("Publisher").getText();
                    String date = fieldMap.get("Date").getText();
                    String edition = fieldMap.get("Edition").getText();
                    String language = fieldMap.get("Language").getText();
                    List<String> tags = Arrays.asList(fieldMap.get("Tags").getText().split(",\\s*"));



                    //CREATING THE JSON FILE VIA INFO WE GET FROM TEXT FIELDS


                    JSONObject bookJson = new JSONObject();

                    if(coverImagePath==null || coverImagePath=="coverImages/default_image.jpg"){
                        bookJson.put("cover","");
                    }
                    bookJson.put("cover", coverImagePath);
                    bookJson.put("title", title);
                    bookJson.put("subtitle", subtitle);
                    bookJson.put("authors", new JSONArray(authors));
                    bookJson.put("translators", new JSONArray(translators));
                    bookJson.put("isbn", isbn);
                    bookJson.put("publisher", publisher);
                    bookJson.put("date", date);
                    bookJson.put("edition", edition);
                    bookJson.put("language", language);
                    bookJson.put("rating", rating);
                    bookJson.put("tags", new JSONArray(tags));



                    //FILE OUTPUT

                    Files.write(Paths.get(fileName), bookJson.toString().getBytes(), StandardOpenOption.CREATE_NEW);
                    System.out.println("Successfully saved to " + fileName);


                    //UPDATING THE BOOK TABLE -TABLEVIEW-
                    Book newBook = new Book(coverImagePath,title, subtitle, authors, translators, isbn, publisher, date,
                            edition, language, rating, tags);

                    Platform.runLater(() -> booksData.add(newBook));

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
            System.out.println("Layout has been changed as Main Layout.");
            stage.setScene(mainScene);
        });




        //ADD SECTION LAYOUT SETTINGS

        HBox buttonBox = new HBox(20, saveButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(0, 0, 0, 0));


        VBox addLayoutALL = new VBox(20);
        addLayoutALL.getChildren().addAll(layout, bookInfoEnteringField, buttonBox);
        addLayoutALL.setAlignment(Pos.TOP_CENTER);
        addLayoutALL.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(addLayoutALL);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setStyle("-fx-background: #f4f4f4; -fx-border-color: #f4f4f4;");

        Scene addBookScene = new Scene(scrollPane, 1000, 800);
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
        System.out.println("Book details has been updated.");
    }
// Kullanıcının girdiği bilgilerle var olan kitap detaylarını güncelliyoruz.
// Eğer yeni girdi önceki bilgilerden farklıysa, gerekli bilgiyi güncelliyoruz ve güncelleme yapıldığını belirten bir mesaj gösteriyoruz.

    private static void updateObservableList(Book selectedBook, JSONObject updatedJson) {

        selectedBook.setTitle(updatedJson.getString("title"));
        selectedBook.setSubtitle(updatedJson.getString("subtitle"));
        selectedBook.setAuthors(Arrays.asList(updatedJson.getJSONArray("authors").toList().toArray(new String[0])));
        selectedBook.setTranslators(Arrays.asList(updatedJson.getJSONArray("translators").toList().toArray(new String[0])));
        selectedBook.setIsbn(updatedJson.getString("isbn"));
        selectedBook.setPublisher(updatedJson.getString("publisher"));
        selectedBook.setDate(updatedJson.getString("date"));
        selectedBook.setEdition(updatedJson.getString("edition"));
        selectedBook.setLanguage(updatedJson.getString("language"));
        selectedBook.setRating(updatedJson.getDouble("rating"));
        selectedBook.setTags(Arrays.asList(updatedJson.getJSONArray("tags").toList().toArray(new String[0])));

        Platform.runLater(() -> {
            System.out.println(selectedBook.getTitle()+ " (ISBN: " + selectedBook.getIsbn() + ") has changed.");
        });
    }



    public static void showEditBookSection(Stage stage, Scene mainScene, Book selectedBook) {
        System.out.println("Layout has changed as Edit Section.");


        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.TOP_CENTER);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(350);
        imageView.setFitWidth(350);
        imageView.setPreserveRatio(true);

        final String[] coverImagePath = {"coverImages/" + selectedBook.getIsbn() + ".jpg"};
        File coverImageFile = new File(coverImagePath[0]);

        if (!coverImageFile.exists()) {
            coverImagePath[0] = "coverImages/default_image.jpg";
        }

        imageView.setImage(new Image(new File(coverImagePath[0]).toURI().toString()));



        VBox bookInfoEnteringField = new VBox(10);
        bookInfoEnteringField.setAlignment(Pos.TOP_CENTER);
        bookInfoEnteringField.setPadding(new Insets(20, 40, 20, 40));

        Map<String, TextField> fieldMap = new HashMap<>();
        String[] fieldNames = {"Title", "Subtitle", "Authors", "Translators", "ISBN", "Publisher", "Date", "Edition", "Language", "Rating", "Tags"};

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


        saveButton.setOnAction(e -> {
            // Gerekli alanları doğrulama işlemi.
            String isbn = fieldMap.get("ISBN").getText();
            String title = fieldMap.get("Title").getText();
            String ratingStr = fieldMap.get("Rating").getText();
            Double rating = validateRating(ratingStr);
            if (rating == null) {
                return;
            }

            String oldIsbn = selectedBook.getIsbn(); // Mevcut ISBN
            String newIsbn = fieldMap.get("ISBN").getText().trim();

            if (title.isEmpty() || isbn.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "ISBN and Title fields cannot be left blank.");
                alert.showAndWait();
                return;
            }

            if (!isbn.matches("^[0-9Xx-]+$")) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "ISBN must be a numeric value with either 10 or 13 digits.");
                alert.showAndWait();
                return;
            }


              if(checkIsbnExists("books", isbn)&& !newIsbn.equals(oldIsbn)) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "This ISBN already exists. Please check the ISBN number.");
                alert.showAndWait();
                return;
            }

            if (!ratingStr.matches("\\d*(\\.\\d+)?|^$")) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Rating must be an integer, float, or empty.");
                alert.showAndWait();
                return;
            }



            Map<String, Object> userInput = new HashMap<>();
            userInput.put("title", title);
            userInput.put("subtitle", fieldMap.get("Subtitle").getText());
            userInput.put("authors", new JSONArray(Arrays.asList(fieldMap.get("Authors").getText().split(",\\s*"))));
            userInput.put("translators", new JSONArray(Arrays.asList(fieldMap.get("Translators").getText().split(",\\s*"))));
            userInput.put("isbn", isbn);
            userInput.put("publisher", fieldMap.get("Publisher").getText());
            userInput.put("date", fieldMap.get("Date").getText());
            userInput.put("edition", fieldMap.get("Edition").getText());
            userInput.put("language", fieldMap.get("Language").getText());
            userInput.put("rating", rating);
            userInput.put("tags", new JSONArray(Arrays.asList(fieldMap.get("Tags").getText().split(",\\s*"))));

            // Lambda dışında yolları tanımlayıp hesapladık.



            // Dosya yolu oluşturma ve kontrol etme
            String directoryPath = "books";
            String imageDirectory = "coverImages";
            String newFilePath = directoryPath + "/" + newIsbn + ".json";
            String oldFilePath = directoryPath + "/" + oldIsbn + ".json";
            String oldImagePath = imageDirectory + "/" + oldIsbn + ".jpg";
            String newImagePath = imageDirectory + "/" + newIsbn + ".jpg";

            Path newPath = Paths.get(newFilePath);
            Path oldPath = Paths.get(oldFilePath);
            Path oldImgPath = Paths.get(oldImagePath);
            Path newImgPath = Paths.get(newImagePath);

            boolean isNewIsbnDifferent = !oldIsbn.equals(newIsbn);

            try {
                Files.createDirectories(Paths.get(directoryPath)); // Directory'nin var olduğundan emin oluyoruz.
                Files.createDirectories(Paths.get(imageDirectory)); // Resim directory'sini kontrol et

                JSONObject existingJson = new JSONObject(isNewIsbnDifferent ? "{}" : readJsonFile(oldPath));

                // cover keyini koruma
                String existingCover = existingJson.optString("cover", null);

                updateBookDetails(existingJson, userInput);
                if (existingCover != null && !existingCover.isEmpty()) {
                    existingJson.put("cover", existingCover);  // Cover değerini koru
                }

                Files.writeString(newPath, existingJson.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                if (!oldIsbn.equals(newIsbn)) {
                    Files.deleteIfExists(oldPath); // Eski dosyayı sil

                    // Kapak resmi ismini güncelle
                    if (Files.exists(oldImgPath)) {
                        Files.move(oldImgPath, newImgPath, StandardCopyOption.REPLACE_EXISTING);
                        existingJson.put("cover", newImagePath.toString());  // Cover keyini yeni dosya yoluna güncelle
                        Files.writeString(newPath, existingJson.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    }
                }

                System.out.println("Successfully updated: " + newPath);
                updateObservableList(selectedBook, existingJson);
                Platform.runLater(() -> GUI.bookTable.refresh());
            } catch (IOException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update the book: " + ex.getMessage());
                alert.showAndWait();
            }

            stage.setScene(mainScene);
        });

        Button selectImageButton = new Button("Select Image");
        Button removeImageButton = new Button("Remove Image");

        selectImageButton.setOnAction(e -> {
            if (!selectedBook.getIsbn().equals(fieldMap.get("ISBN").getText().trim())) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "ISBN and coverImage change actions cannot be applied at the same time. Please do it separately");
                alert.showAndWait();
                return;
            }

            String isbn = fieldMap.get("ISBN").getText();
            Map<String, Object> userInput = new HashMap<>();

            final String localCoverImagePath = coverImagePath[0]; // coverImagePath'in güncel halini tutuyoruz.
            final String currentISBNPath = "coverImages/" + selectedBook.getIsbn() + ".jpg";
            final String newISBNPath = "coverImages/" + isbn + ".jpg";

// Bu yolları işlemlerimizde kullandık ve yakalandıktan sonra üzerlerinde değişiklik yapılmadığından emin olduk.
            if (localCoverImagePath != null || localCoverImagePath !="coverImages/default_image.jpg") {
                try {
                    Path sourcePath = Paths.get(localCoverImagePath);
                    Path targetPath = Paths.get(currentISBNPath);
                    Files.createDirectories(targetPath.getParent()); //Directory'nin var olduğundan emin olduk ki herhangi bir hatayla karşılaşılmasın.
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                    String jsonFilePath = "books/" + selectedBook.getIsbn() + ".json";
                    Path jsonPath = Paths.get(jsonFilePath);
                    JSONObject bookJson = new JSONObject(Files.readString(jsonPath, StandardCharsets.UTF_8));
                    bookJson.put("cover", targetPath.toString());  // Cover değerini boş olarak ayarla
                    Files.writeString(jsonPath, bookJson.toString(), StandardOpenOption.TRUNCATE_EXISTING); // Güncellenmiş JSON'ı yaz
                    System.out.println("Cover key in JSON updated to isbn after file edition.");
                } catch (NoSuchFileException ex2){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Image Successfully removed. " + ex2.getMessage());
                    alert.showAndWait();
                    return;
                } catch (IOException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update cover image: " + ex.getMessage());
                    alert.showAndWait();
                    return;
                }
            }

            if (!selectedBook.getIsbn().equals(isbn)) {  // ISBN'in değişip değişmediğini kontrol ediyoruz.
                try {
                    // JSON dosyaları için eski ve yeni yolları belirle
                    Path oldJsonPath = Paths.get("books", selectedBook.getIsbn() + ".json");
                    Path newJsonPath = Paths.get("books", isbn + ".json");
                    if (!Files.exists(newJsonPath)) { // Yeni ISBN ile dosya yoksa, taşı
                        Files.move(oldJsonPath, newJsonPath, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "A file with the new ISBN already exists. Please check the ISBN.");
                        alert.showAndWait();
                        return;
                    }

                    // Resim dosyaları için eski ve yeni yolları belirle
                    Path oldImagePath = Paths.get("coverImages", selectedBook.getIsbn() + ".jpg");
                    Path newImagePath = Paths.get("coverImages", isbn + ".jpg");
                    if (Files.exists(oldImagePath)) {
                        if (!Files.exists(newImagePath)) { // Yeni ISBN ile resim yoksa, taşı
                            Files.move(oldImagePath, newImagePath, StandardCopyOption.REPLACE_EXISTING);
                        } else {
                            // Yeni ISBN ile bir resim zaten varsa, eski resmi sil veya isim çakışmasını önlemek adına işlem yapma
                            Files.deleteIfExists(oldImagePath);
                        }
                    }
                    // Dosya taşındıktan sonra yeni oluşacak duruma göre coverImagePath'i güncelledik.
                    coverImagePath[0] = newImagePath.toString();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update ISBN and image file names: " + ex.getMessage());
                    alert.showAndWait();
                    return;
                }
            }



            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Cover Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.jpg")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                try {
                    Path sourcePath = selectedFile.toPath();
                    String fileName = selectedBook.getIsbn() + getFileExtension(selectedFile.getName());
                    Path targetPath = Paths.get("coverImages", fileName);
                    if (Files.notExists(targetPath)) {
                        Files.createFile(targetPath);  // Yeni dosya oluştur
                    }
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                    // Güncellenmiş dosya yolunu kitap nesnesinde ve ImageView'da ayarla
                    selectedBook.setCover(targetPath.toString());
                    imageView.setImage(new Image(targetPath.toUri().toString()));

                    System.out.println("Cover image updated successfully to " + targetPath);

                } catch (IOException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update cover image: " + ex.getMessage());
                    alert.showAndWait();
                }
            }
        });






        removeImageButton.setOnAction(e -> {
            try {
                // Varsayılan resmin path'ini tanımlama
                File defaultImageFile = new File("coverImages/default_image.jpg");
                if (defaultImageFile.exists()) {
                    imageView.setImage(new Image(defaultImageFile.toURI().toString()));
                } else {
                    System.err.println("Default image file not found.");
                }

                // Çeşitli uzantılara sahip kapak görseli dosyasının silinmesini deneme.
                String[] possibleExtensions = {".jpg",};
                boolean fileDeleted = false;
                for (String extension : possibleExtensions) {
                    File currentCoverImageFile = new File("coverImages/" + selectedBook.getIsbn() + extension);
                    if (currentCoverImageFile.exists()) {
                        Files.delete(currentCoverImageFile.toPath());  // Geçerli kapak resmi dosyasını sildik.
                        System.out.println("Cover image file deleted successfully: " + currentCoverImageFile.getName());
                        fileDeleted = true;
                        break;
                    }
                }
                if (!fileDeleted) {
                    System.out.println("No cover image file found to delete.");
                    JSONObject json =new JSONObject();
                }
                else {
                    // Eğer dosya silindi ise, JSON dosyasındaki cover bilgisini güncelle
                    String jsonFilePath = "books/" + selectedBook.getIsbn() + ".json";
                    Path jsonPath = Paths.get(jsonFilePath);
                    JSONObject bookJson = new JSONObject(Files.readString(jsonPath, StandardCharsets.UTF_8));
                    bookJson.put("cover", "");  // Cover değerini boş olarak ayarla
                    Files.writeString(jsonPath, bookJson.toString(), StandardOpenOption.TRUNCATE_EXISTING); // Güncellenmiş JSON'ı yaz
                    System.out.println("Cover key in JSON updated to empty after file deletion.");
                }
            } catch (IOException ex) {
                System.err.println("Failed to delete the cover image file: " + ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Failed to load default image: " + ex.getMessage());
            }

            // Kitabın kapak resmi path'ini 'null' olarak güncelledik.
            selectedBook.setCover(null);
        });



        // LAYOUT FOR IMAGE CONTROLS
        HBox imageControls = new HBox(10, selectImageButton, removeImageButton);
        imageControls.setAlignment(Pos.CENTER);

        // ADDING THE NEW IMAGEVIEW AND ITS CONTROLS TO THE MAIN LAYOUT
        layout.getChildren().addAll(imageView, imageControls);




        //BACK BUTTON CREATION AND ANIMATION

        Button backButton = new Button("Back");
        backButton.setStyle(backButtonBaseStyle);
        backButton.setOnMouseEntered(e -> backButton.setStyle(backButtonBaseStyle + backButtonHoverStyle));
        backButton.setOnMouseExited(e -> backButton.setStyle(backButtonBaseStyle));


        //BACK BUTTON ACTION

        backButton.setOnAction(e -> {
            System.out.println("Layout has been changed as Main Layout.");
            stage.setScene(mainScene);
        });


        // EDIT WINDOW LAYOUT
        HBox buttons = new HBox(20,saveButton,backButton);
        buttons.setAlignment(Pos.BASELINE_CENTER);
        VBox mainLayout = new VBox(20, layout, bookInfoEnteringField,buttons);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setStyle("-fx-background: #f4f4f4; -fx-border-color: #f4f4f4;");

        Scene editBookScene = new Scene(scrollPane, 1000, 800);
        stage.setScene(editBookScene);



    }

    public static boolean jsonArraysEqual(JSONArray arr1, JSONArray arr2) {
        if (arr1 == null || arr2 == null) return true; // Null kontrolü.
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
            System.out.println("Layout has been refreshed due to change of files.");
            booksData.removeAll(selectedBooks);  // Seçili kitapları veri listesinden kaldırmak.
            stage.setScene(mainScene); // Refresh için Main Layout'u yeniden yükleme.
        });
    }








}
