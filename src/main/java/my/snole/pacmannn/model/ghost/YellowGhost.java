package my.snole.pacmannn.model.ghost;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import my.snole.pacmannn.model.ghost.Ghost;

public class YellowGhost extends Ghost {
    public YellowGhost(Point2D location, Point2D velocity) {
        super(location, velocity);
        this.image = new Image(getClass().getResourceAsStream("/image/yellowghost.gif"));
    }
}
