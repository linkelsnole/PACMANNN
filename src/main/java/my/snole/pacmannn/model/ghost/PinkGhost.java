package my.snole.pacmannn.model.ghost;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;

public class PinkGhost extends Ghost {
    public PinkGhost(Point2D location, Point2D velocity, GhostManager ghostManager, Image defaultImage) {
        super(location, velocity, ghostManager, defaultImage);
        this.image = defaultImage;
    }
}