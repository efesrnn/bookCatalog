package com.example.bookcatalog;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Book {
    private String cover;
    private String title;
    private String subtitle;
    private List<String> authors;
    private List<String> translators;
    private String isbn;
    private String publisher;
    private String date;
    private String edition;
    private String coverType; //Resim file path ile kullanılıyor o yüzden String kalsın.
    private String language;
    private double rating; //4.5 gibi noktalı bir değer olabileceği için int yerine double kullanıyoruz.
    private List<String> tags; //Birden fazla tag olabileceği için bir String List'i kullanıyoruz.
    private String numberOfPages;
    private int searchPriority = Integer.MAX_VALUE; // Yüksek bir başlangıç değeri
    //CONSTRUCTOR
    public Book(String cover, String title, String subtitle, List<String> authors, List<String> translators,
                String isbn, String publisher, String date, String edition,
                String coverType, String language, double rating, List<String> tags,String numberOfPages) {

        this.cover=cover;
        this.title = title;
        this.subtitle = subtitle;
        this.authors = authors;
        this.translators = translators;
        this.isbn = isbn;
        this.publisher = publisher;
        this.date = date;
        this.edition = edition;
        this.coverType = coverType;
        this.language = language;
        this.rating = rating;
        this.tags = tags;
        this.numberOfPages = numberOfPages;
    }

    public static Book fromJSON(JSONObject json) {
        String cover = json.optString("cover", "src/coverImages");
        String title = json.getString("title");
        String subtitle = json.optString("subtitle", "");
        List<String> authors = json.getJSONArray("authors").toList().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        List<String> translators = json.getJSONArray("translators").toList().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        String isbn = json.getString("isbn");
        String publisher = json.getString("publisher");
        String date = json.getString("date");
        String edition = json.optString("edition", "");
        String coverType = json.getString("coverType");
        String language = json.getString("language");
        double rating = json.optDouble("rating", 0.0);
        List<String> tags = json.getJSONArray("tags").toList().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        String numberOfPages = json.getString("numberOfPages");

        return new Book(cover,title, subtitle, authors, translators, isbn, publisher, date, edition, coverType, language, rating, tags,numberOfPages);
    }

    //GETTER & SETTER
    public String getCover() {return cover;}
    public void setCover (String cover) {this.cover = cover;}
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public List<String> getAuthors() { return authors; }
    public void setAuthors(List<String> authors) { this.authors = authors; }
    public List<String> getTranslators() { return translators; }
    public void setTranslators(List<String> translators) { this.translators = translators; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }
    public String getCoverType() { return coverType; }
    public void setCoverType(String coverType) { this.cover = coverType; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getNumberOfPages(){return numberOfPages;}
    public void  setNumberOfPages(String numberOfPages){this.numberOfPages=numberOfPages;}
    public int getSearchPriority() {return searchPriority;}
    public void setSearchPriority(int searchPriority) {this.searchPriority = searchPriority;}
}
