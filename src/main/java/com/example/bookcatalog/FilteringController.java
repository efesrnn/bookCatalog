package com.example.bookcatalog;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
    private Map<String, CheckBox> tagCheckboxes = new HashMap<>();
    private Stage stage;


    private static Set<String> savedSelectedTags = new HashSet<>();

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

    public void initialize() {
        loadUniqueTags();
        restoreCheckedStates();
    }

    private void loadUniqueTags() {
        Set<String> uniqueTags = new HashSet<>();
        for (Book book : GUI.booksData) {
            uniqueTags.addAll(book.getTags());
        }

        tagsContainer.getChildren().clear();
        if (uniqueTags.isEmpty()) {
            noTagsLabel.setVisible(true); // Eğer tag yoksa, uyarı mesajını göster
        } else {
            noTagsLabel.setVisible(false); // Eğer tag varsa, uyarı mesajını gizle
            for (String tag : uniqueTags) {
                CheckBox checkBox = new CheckBox(tag);
                tagsContainer.getChildren().add(checkBox);
                tagCheckboxes.put(tag, checkBox);
            }
        }
    }
    // Restores the check states of the checkboxes based on previously saved selected tags
    private void restoreCheckedStates() {
        tagCheckboxes.forEach((tag, checkBox) -> {
            checkBox.setSelected(savedSelectedTags.contains(tag));
        });
    }

    @FXML
    protected void applyFilters() {
        Set<String> selectedTags = tagCheckboxes.values().stream().filter(CheckBox::isSelected).map(CheckBox::getText).collect(Collectors.toSet());

        savedSelectedTags = new HashSet<>(selectedTags);

        GUI.filteredBooks.setPredicate(book -> selectedTags.isEmpty() || book.getTags().stream().anyMatch(selectedTags::contains));
// Optionally, close the stage here if you still want to close it manually by button
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    protected void clearFilters() {
        tagCheckboxes.values().forEach(cb -> cb.setSelected(false));
        GUI.filteredBooks.setPredicate(p -> true);
        // Do not call applyFilters() here to avoid closing the window
        savedSelectedTags.clear();
    }
}
