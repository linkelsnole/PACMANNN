package my.snole.pacmannn.model.ghost;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import my.snole.pacmannn.model.ghost.Ghost;

public class BlueGhost extends Ghost {
    public BlueGhost(Point2D location, Point2D velocity, GhostManager ghostManager) {
        super(location, velocity, ghostManager);
        this.image = new Image(getClass().getResourceAsStream("/image/blueghost1.gif"));
    }
}