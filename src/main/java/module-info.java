module com.example.bookcatalog {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires java.logging; //css uyarılarını gizlemek için


    opens com.example.bookcatalog to javafx.fxml;
    exports com.example.bookcatalog;
}
