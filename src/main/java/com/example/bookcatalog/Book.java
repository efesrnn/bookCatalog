package com.example.bookcatalog;

import java.util.List;
public class Book {
    private String title;
    private String subtitle;
    private List<String> authors;
    private List<String> translators;
    private String isbn;
    private String publisher;
    private String date; //Proje dosyasını tekrar oku gerekirse int yap.
    private String edition;
    private String cover; //Resim file path ile kullanılıyor o yüzden String kalsın.
    private String language;
    private double rating; //4.5 falan olabilir double kalsın.
    private List<String> tags; //Hoca birkaç tag girmiş olabilir taklaya gelmeyelim.

    //CONSTRUCTOR
    public Book(String title, String subtitle, List<String> authors, List<String> translators, String isbn,
                String publisher, String date, String edition, String cover, String language, double rating,
                List<String> tags) {
        this.title = title;
        this.subtitle = subtitle;
        this.authors = authors;
        this.translators = translators;
        this.isbn = isbn;
        this.publisher = publisher;
        this.date = date;
        this.edition = edition;
        this.cover = cover;
        this.language = language;
        this.rating = rating;
        this.tags = tags;
    }

    //GETTER & SETTER
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
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
