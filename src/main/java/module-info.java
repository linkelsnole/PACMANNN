module my.snole.pacmannn {
    requires javafx.controls;
    requires javafx.fxml;


    opens my.snole.pacmannn to javafx.fxml;
    exports my.snole.pacmannn;
}