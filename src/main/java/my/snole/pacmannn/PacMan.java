package my.snole.pacmannn;

import javafx.geometry.Point2D;

public class PacMan extends GameCharacter {
    public PacMan(Point2D location, Point2D velocity) {
        super(location, velocity);
    }

    @Override
    public void move(PacManModel.CellValue[][] grid) {
        // Пакман двигается только в соответствии с направлением, установленным контроллером
    }

    public void move(PacManModel.Direction direction, PacManModel.CellValue[][] grid) {
        if (direction == null) {
            direction = PacManModel.Direction.NONE;
        }
        Point2D potentialVelocity = changeVelocity(direction);
        Point2D potentialLocation = location.add(potentialVelocity);
        potentialLocation = setGoingOffscreenNewLocation(potentialLocation, grid.length, grid[0].length);

        if (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] != PacManModel.CellValue.WALL) {
            this.velocity = potentialVelocity;
            this.location = potentialLocation;
            PacManModel.setLastDirection(direction);
        }
    }

    private Point2D changeVelocity(PacManModel.Direction direction) {
        if (direction == null) {
            direction = PacManModel.Direction.NONE;
        }
        switch (direction) {
            case LEFT: return new Point2D(0, -1);
            case RIGHT: return new Point2D(0, 1);
            case UP: return new Point2D(-1, 0);
            case DOWN: return new Point2D(1, 0);
            default: return new Point2D(0, 0);
        }
    }
}
