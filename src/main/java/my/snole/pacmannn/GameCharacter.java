package my.snole.pacmannn;

import javafx.geometry.Point2D;

public abstract class GameCharacter {
    protected Point2D location;
    protected Point2D velocity;

    public GameCharacter(Point2D location, Point2D velocity) {
        this.location = location;
        this.velocity = velocity;
    }

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public Point2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Point2D velocity) {
        this.velocity = velocity;
    }

    public abstract void move(PacManModel.CellValue[][] grid);

    protected Point2D setGoingOffscreenNewLocation(Point2D objectLocation, int rowCount, int columnCount) {
        if (objectLocation.getY() >= columnCount) {
            objectLocation = new Point2D(objectLocation.getX(), 0);
        }
        if (objectLocation.getY() < 0) {
            objectLocation = new Point2D(objectLocation.getX(), columnCount - 1);
        }
        if (objectLocation.getX() >= rowCount) {
            objectLocation = new Point2D(0, objectLocation.getY());
        }
        if (objectLocation.getX() < 0) {
            objectLocation = new Point2D(rowCount - 1, objectLocation.getY());
        }
        return objectLocation;
    }
}
