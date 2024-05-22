module my.snole.pacmannn {
    requires javafx.controls;
    requires javafx.fxml;


    opens my.snole.pacmannn to javafx.fxml;
    exports my.snole.pacmannn;
    exports my.snole.pacmannn.model;
    opens my.snole.pacmannn.model to javafx.fxml;
    exports my.snole.pacmannn.model.ghost;
    opens my.snole.pacmannn.model.ghost to javafx.fxml;
    exports my.snole.pacmannn.core;
    opens my.snole.pacmannn.core to javafx.fxml;
    exports my.snole.pacmannn.model.pacman;
    opens my.snole.pacmannn.model.pacman to javafx.fxml;
    exports my.snole.pacmannn.util;
    opens my.snole.pacmannn.util to javafx.fxml;
}