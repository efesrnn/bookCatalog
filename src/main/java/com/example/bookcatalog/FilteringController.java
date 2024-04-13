package com.example.bookcatalog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
    private Map<String, CheckBox> tagCheckboxes = new HashMap<>();
    private Stage stage; // Eğer controller'a stage geçirmeniz gerekirse diye eklendi

    public void setStage(Stage stage) {
        this.stage = stage; // Stage'i set etmek için bir method
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

    public void initialize() {
        loadUniqueTags();
    }

    private void loadUniqueTags() {
        Set<String> uniqueTags = new HashSet<>();
        for (Book book : GUI.booksData) {
            uniqueTags.addAll(book.getTags());
        }

        tagsContainer.getChildren().clear(); // Clear existing tags if any before loading
        for (String tag : uniqueTags) {
            CheckBox checkBox = new CheckBox(tag);
            // Removed action event that automatically applies filters
            tagsContainer.getChildren().add(checkBox);
            tagCheckboxes.put(tag, checkBox);
        }
    }

    @FXML
    protected void applyFilters() {
        Set<String> selectedTags = tagCheckboxes.values().stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toSet());

        GUI.filteredBooks.setPredicate(book -> selectedTags.isEmpty() || book.getTags().stream().anyMatch(selectedTags::contains));
        // Optionally, close the stage here if you still want to close it manually by button
    }

    // Modify clearFilters to not close the window or apply filters automatically
    @FXML
    protected void clearFilters() {
        tagCheckboxes.values().forEach(cb -> cb.setSelected(false));
        GUI.filteredBooks.setPredicate(p -> true);
        // Do not call applyFilters() here to avoid closing the window
    }
}
