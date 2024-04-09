module com.example.bookcatalog {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;


    opens com.example.bookcatalog to javafx.fxml;
    exports com.example.bookcatalog;
}