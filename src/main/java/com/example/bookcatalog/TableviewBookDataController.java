package com.example.bookcatalog;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TableviewBookDataController {
    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private Label authorsLabel;
    @FXML
    private Label translatorsLabel;
    @FXML
    private Label isbnLabel;
    @FXML
    private Label publisherLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label editionLabel;
    @FXML
    private Label coverLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label tagsLabel;

    private Stage stage;
    private Book book;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setBook(Book book) {
        this.book = book;
        updateDetails();
    }

    private void updateDetails() {
        titleLabel.setText(book.getTitle());
        subtitleLabel.setText(book.getSubtitle());
        authorsLabel.setText(String.join(", ", book.getAuthors()));
        translatorsLabel.setText(String.join(", ", book.getTranslators()));
        isbnLabel.setText(book.getIsbn());
        publisherLabel.setText(book.getPublisher());
        dateLabel.setText(book.getDate());
        editionLabel.setText(book.getEdition());
        coverLabel.setText(book.getCover());
        languageLabel.setText(book.getLanguage());
        ratingLabel.setText(String.format("%.2f", book.getRating()));
        tagsLabel.setText(String.join(", ", book.getTags()));
        System.out.println("Detailed book information layout has opened.");
    }
}
