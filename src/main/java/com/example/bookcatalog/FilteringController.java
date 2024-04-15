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
        System.out.println("Tags being added to a map to prevent duplicate tags.");
        Set<String> uniqueTags = new HashSet<>();
        for (Book book : GUI.booksData) {
            uniqueTags.addAll(book.getTags());
        }

        tagsContainer.getChildren().clear();
        if (uniqueTags.isEmpty()) {
            noTagsLabel.setVisible(true); // Eğer tag yoksa, uyarı labelını gösterme
            //Burada bir sıkıntı var 1 tane labelsız checkbox gösteriyo düzelt.
        } else {
            noTagsLabel.setVisible(false); // Eğer tag varsa, uyarı mesajını gizle
            for (String tag : uniqueTags) {
                CheckBox checkBox = new CheckBox(tag);
                tagsContainer.getChildren().add(checkBox);
                tagCheckboxes.put(tag, checkBox);
            }
        }
        System.out.println("All tags have been shown.");
    }
    private void restoreCheckedStates() {
        tagCheckboxes.forEach((tag, checkBox) -> {
            checkBox.setSelected(savedSelectedTags.contains(tag));
        });
    }

    @FXML
    protected void applyFilters() {
        System.out.println("Initıalizing String set to keep selected tags.");
        Set<String> selectedTags = tagCheckboxes.values().stream().filter(CheckBox::isSelected).map(CheckBox::getText).collect(Collectors.toSet());

        savedSelectedTags = new HashSet<>(selectedTags);
        //Kullanıcı belirli tagleri girdikten sonra pencereyi kapatıp geri girdiğinde önceki seçili taglerin hala seçili kalması için kullandığımız kod.

        GUI.filteredBooks.setPredicate(book -> selectedTags.isEmpty() || book.getTags().stream().anyMatch(selectedTags::contains));
        System.out.println("Selected tags applied.");
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    protected void clearFilters() {
        tagCheckboxes.values().forEach(cb -> cb.setSelected(false));
        GUI.filteredBooks.setPredicate(p -> true);
        savedSelectedTags.clear();
        System.out.println("All selected tags removed.");
    }
}
