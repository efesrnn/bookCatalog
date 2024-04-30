package com.example.bookcatalog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class FilteringController {
    @FXML
    private Button applyButton;
    @FXML
    private Button clearButton;
    @FXML
    private VBox tagsContainer;
    @FXML
    private Label noTagsLabel;
    @FXML
    private ComboBox<String> ratingComboBox;  // The ComboBox for rating filters

    private Map<String, CheckBox> tagCheckboxes = new HashMap<>();
    private Stage stage;
    private static Set<String> savedSelectedTags = new HashSet<>();
    private static String savedSelectedRating = "Any"; // To save the last selected rating

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    protected void onMouseEnteredApply(MouseEvent event) {
        applyButton.setStyle("-fx-font-weight: bold; -fx-background-color: #4cae4c; -fx-text-fill: white; -fx-font-size: 12pt;");
    }

    @FXML
    protected void onMouseExitedApply(MouseEvent event) {
        applyButton.setStyle("-fx-font-weight: bold; -fx-background-color: #5cb85c; -fx-text-fill: white; -fx-font-size: 12pt;");
    }

    @FXML
    protected void onMouseEnteredClear(MouseEvent event) {
        clearButton.setStyle("-fx-font-weight: bold; -fx-background-color: #edb879; -fx-text-fill: white; -fx-font-size: 12pt;");
    }

    @FXML
    protected void onMouseExitedClear(MouseEvent event) {
        clearButton.setStyle("-fx-font-weight: bold; -fx-background-color: #f0ad4e; -fx-text-fill: white; -fx-font-size: 12pt;");
    }

    @FXML
    public void initialize() {
        loadUniqueTags();
        restoreCheckedStates();
        initializeRatingComboBox();  // Initialize ComboBox items
    }
    private void initializeRatingComboBox() {
        ratingComboBox.getItems().add("Filter by Rating");
        ratingComboBox.getItems().addAll("Above 7.5", "5.0 - 7.5", "2.5 - 5.0", "Below 2.5", "Any");
        ratingComboBox.setValue("Filter by Rating"); // Default selection
    }

    private void loadUniqueTags() {
        Set<String> uniqueTags = new HashSet<>();
        for (Book book : GUI.booksData) {
            uniqueTags.addAll(book.getTags());
        }

        tagsContainer.getChildren().clear();
        noTagsLabel.setVisible(uniqueTags.isEmpty());
        for (String tag : uniqueTags) {
            CheckBox checkBox = new CheckBox(tag);
            tagsContainer.getChildren().add(checkBox);
            tagCheckboxes.put(tag, checkBox);
        }
    }

    private void restoreCheckedStates() {
        tagCheckboxes.forEach((tag, checkBox) -> checkBox.setSelected(savedSelectedTags.contains(tag)));
        ratingComboBox.setValue(savedSelectedRating); // Restore the selected rating
    }

    @FXML
    protected void applyFilters() {
        Set<String> selectedTags = tagCheckboxes.values().stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toSet());
        String selectedRating = ratingComboBox.getValue();

        savedSelectedTags = new HashSet<>(selectedTags);
        savedSelectedRating = selectedRating; // Save the selected rating

        GUI.filteredBooks.setPredicate(book -> matchTags(book.getTags(), selectedTags) && matchRating(book.getRating(), selectedRating));
        if (stage != null) {
            stage.close();
        }
    }

    private boolean matchTags(List<String> bookTags, Set<String> selectedTags) {
        return selectedTags.isEmpty() || bookTags.stream().anyMatch(selectedTags::contains);
    }

    private boolean matchRating(double rating, String ratingFilter) {
        switch (ratingFilter) {
            case "Above 7.5":
                return rating > 7.5;
            case "5.0 - 7.5":
                return rating >= 5.0 && rating <= 7.5;
            case "2.5 - 5.0":
                return rating >= 2.5 && rating <= 5.0;
            case "Below 2.5":
                return rating < 2.5;
            case "Any":
            default:
                return true;
        }
    }

    @FXML
    protected void clearFilters() {
        tagCheckboxes.values().forEach(cb -> cb.setSelected(false));
        savedSelectedTags.clear(); // Clear the saved tag selections
        ratingComboBox.setValue("Filter by Rating");
        savedSelectedRating = "Any"; // Reset the saved rating filter
        GUI.filteredBooks.setPredicate(p -> true);

        // Optionally, close the filter window if you have a separate stage for filters
        if (stage != null) {
            stage.close();
        }
    }
}