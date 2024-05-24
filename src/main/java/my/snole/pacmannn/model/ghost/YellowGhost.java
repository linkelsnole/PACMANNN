package my.snole.pacmannn.model.ghost;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;

public class YellowGhost extends Ghost {
    public YellowGhost(Point2D location, Point2D velocity, GhostManager ghostManager, Image defaultImage) {
        super(location, velocity, ghostManager, defaultImage);
        this.image = defaultImage;
    }
}