package my.snole.pacmannn.model.ghost;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;

public class RedGhost extends Ghost {
    public RedGhost(Point2D location, Point2D velocity, GhostManager ghostManager) {
        super(location, velocity, ghostManager);
        this.image = new Image(getClass().getResourceAsStream("/image/redghost.gif"));
    }
}
