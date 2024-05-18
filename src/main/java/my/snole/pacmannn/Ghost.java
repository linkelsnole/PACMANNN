package my.snole.pacmannn;

import javafx.geometry.Point2D;
import java.util.Random;

public class Ghost extends GameCharacter {
    public Ghost(Point2D location, Point2D velocity) {
        super(location, velocity);
    }

    @Override
    public void move(PacManModel.CellValue[][] grid) {
        Random generator = new Random();
        Point2D potentialVelocity = this.velocity;
        Point2D potentialLocation = this.location.add(potentialVelocity);
        potentialLocation = setGoingOffscreenNewLocation(potentialLocation, grid.length, grid[0].length);

        while (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == PacManModel.CellValue.WALL) {
            int randomNum = generator.nextInt(4);
            PacManModel.Direction direction = PacManModel.Direction.values()[randomNum];
            potentialVelocity = changeVelocity(direction);
            potentialLocation = this.location.add(potentialVelocity);
            potentialLocation = setGoingOffscreenNewLocation(potentialLocation, grid.length, grid[0].length);
        }

        this.velocity = potentialVelocity;
        this.location = potentialLocation;
    }

    public void moveTowardsPacman(Point2D pacmanLocation, boolean ghostEatingMode, PacManModel.CellValue[][] grid) {
        Random generator = new Random();
        Point2D potentialVelocity = this.velocity;
        Point2D potentialLocation = this.location.add(potentialVelocity);
        potentialLocation = setGoingOffscreenNewLocation(potentialLocation, grid.length, grid[0].length);

        if (!ghostEatingMode) {
            if (this.location.getY() == pacmanLocation.getY()) {
                potentialVelocity = new Point2D(this.location.getX() > pacmanLocation.getX() ? -1 : 1, 0);
            } else if (this.location.getX() == pacmanLocation.getX()) {
                potentialVelocity = new Point2D(0, this.location.getY() > pacmanLocation.getY() ? -1 : 1);
            }
        } else {
            if (this.location.getY() == pacmanLocation.getY()) {
                potentialVelocity = new Point2D(this.location.getX() > pacmanLocation.getX() ? 1 : -1, 0);
            } else if (this.location.getX() == pacmanLocation.getX()) {
                potentialVelocity = new Point2D(0, this.location.getY() > pacmanLocation.getY() ? 1 : -1);
            }
        }

        potentialLocation = this.location.add(potentialVelocity);
        potentialLocation = setGoingOffscreenNewLocation(potentialLocation, grid.length, grid[0].length);

        while (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == PacManModel.CellValue.WALL) {
            int randomNum = generator.nextInt(4);
            PacManModel.Direction direction = PacManModel.Direction.values()[randomNum];
            potentialVelocity = changeVelocity(direction);
            potentialLocation = this.location.add(potentialVelocity);
            potentialLocation = setGoingOffscreenNewLocation(potentialLocation, grid.length, grid[0].length);
        }

        this.velocity = potentialVelocity;
        this.location = potentialLocation;
    }

    private Point2D changeVelocity(PacManModel.Direction direction) {
        switch (direction) {
            case LEFT: return new Point2D(0, -1);
            case RIGHT: return new Point2D(0, 1);
            case UP: return new Point2D(-1, 0);
            case DOWN: return new Point2D(1, 0);
            default: return new Point2D(0, 0);
        }
    }
}
