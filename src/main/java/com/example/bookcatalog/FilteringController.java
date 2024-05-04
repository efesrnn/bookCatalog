package com.example.bookcatalog;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.scene.control.ScrollPane;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.bookcatalog.GUI.bookTable;
import static com.example.bookcatalog.GUI.filteredBooks;

public class FilteringController {
    @FXML
    private Button applyButton;
    @FXML
    private Button clearButton;

    @FXML
    private Label noTagsLabel;
    @FXML
    private CheckBox ratingAbove75, rating5to75, rating25to5, ratingBelow25;
    @FXML
    private Accordion accordion;

    public static boolean isFiltered = false;
    private Map<String, CheckBox> tagCheckboxes = new HashMap<>();
    private Stage stage;
    private static Set<String> savedSelectedTags = new HashSet<>();
    private static Set<String> savedSelectedRatings = new HashSet<>();

    public void setStage(Stage stage) {
        this.stage = stage;
        if (this.stage != null) {
            this.stage.setMinWidth(600); // Set minimum width
            this.stage.setMinHeight(400); // Set minimum height
        }
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
        System.out.println("Initializing...");
        loadUniqueTags();
        restoreCheckedStates();
        System.out.println("Initialization completed successfully.");
    }

    private void loadUniqueTags() {
        Set<String> uniqueTags = new HashSet<>();
        for (Book book : GUI.filteredBooks) {
            uniqueTags.addAll(book.getTags());
        }

        VBox tagsBox = new VBox();
        tagsBox.getChildren().clear();
        noTagsLabel.setVisible(uniqueTags.isEmpty());
        tagsBox.getChildren().add(noTagsLabel);

        for (String tag : uniqueTags) {
            CheckBox checkBox = new CheckBox(tag);
            tagsBox.getChildren().add(checkBox);
            tagCheckboxes.put(tag, checkBox);
        }

        if (accordion != null) {
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(tagsBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            accordion.getPanes().get(0).setContent(scrollPane);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
        } else {
            System.out.println("Accordion is not initialized.");
        }
    }

    private void restoreCheckedStates() {
        tagCheckboxes.forEach((tag, checkBox) -> checkBox.setSelected(savedSelectedTags.contains(tag)));

        ratingAbove75.setSelected(savedSelectedRatings.contains("Above 7.5"));
        rating5to75.setSelected(savedSelectedRatings.contains("5.0 - 7.5"));
        rating25to5.setSelected(savedSelectedRatings.contains("2.5 - 5.0"));
        ratingBelow25.setSelected(savedSelectedRatings.contains("Below 2.5"));
    }

    @FXML
    protected void applyFilters() {
        isFiltered = true;
        if (isFiltered) {
            bookTable.setPlaceholder(new Label("No books to display for the selected filters."));
        }

        Set<String> selectedTags = tagCheckboxes.values().stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toSet());
        savedSelectedTags = new HashSet<>(selectedTags);

        Set<String> selectedRatings = new HashSet<>();
        if (ratingAbove75.isSelected()) selectedRatings.add("Above 7.5");
        if (rating5to75.isSelected()) selectedRatings.add("5.0 - 7.5");
        if (rating25to5.isSelected()) selectedRatings.add("2.5 - 5.0");
        if (ratingBelow25.isSelected()) selectedRatings.add("Below 2.5");
        savedSelectedRatings = new HashSet<>(selectedRatings);

        bookTable.refresh();

        GUI.filteredBooks.setPredicate(book ->
                (selectedTags.isEmpty() || matchTags(book.getTags(), selectedTags)) &&
                        (selectedRatings.isEmpty() || matchRating(book.getRating(), selectedRatings))
        );
        if (stage != null) {
            stage.close();
        }
    }

    private boolean matchTags(List<String> bookTags, Set<String> selectedTags) {
        return selectedTags.isEmpty() || bookTags.stream().anyMatch(selectedTags::contains);
    }

    private boolean matchRating(double rating, Set<String> ratingFilters) {
        return ratingFilters.contains("Any") || ratingFilters.stream().anyMatch(filter -> {
            switch (filter) {
                case "Above 7.5": return rating > 7.5;
                case "5.0 - 7.5": return rating >= 5.0 && rating <= 7.5;
                case "2.5 - 5.0": return rating >= 2.5 && rating <= 5.0;
                case "Below 2.5": return rating < 2.5;
                default: return true;
            }
        });
    }

    @FXML
    protected void clearFilters() {
        isFiltered = false;
        bookTable.setPlaceholder(new Label("No books to display. Use 'Add' to insert new entries."));
        tagCheckboxes.values().forEach(cb -> cb.setSelected(false));
        savedSelectedTags.clear();
        ratingAbove75.setSelected(false);
        rating5to75.setSelected(false);
        rating25to5.setSelected(false);
        ratingBelow25.setSelected(false);
        savedSelectedRatings.clear();

        bookTable.refresh();

        GUI.filteredBooks.setPredicate(p -> true);

        if (stage != null) {
            stage.close();
        }
    }
}
