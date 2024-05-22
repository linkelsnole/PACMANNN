package my.snole.pacmannn.core;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import my.snole.pacmannn.core.Controller;

/**
 * Sets up the controller, loads the fxml file, and runs the application.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("pacman1.fxml"));
        Pane root = loader.load();
        primaryStage.setTitle("PacMan");
        Controller controller = loader.getController();
        root.setOnKeyPressed(controller);
        double sceneWidth = controller.getBoardWidth() + 200.0;
        double sceneHeight = controller.getBoardHeight() + 100.0;
        primaryStage.setScene(new Scene(root, sceneWidth, sceneHeight));
        primaryStage.show();
        root.requestFocus();

        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
