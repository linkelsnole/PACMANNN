package my.snole.pacmannn.model.ghost;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import my.snole.pacmannn.model.pacman.BotPacMan;
import my.snole.pacmannn.core.PacManModel;
import my.snole.pacmannn.model.GameCharacter;

import java.util.List;
import java.util.Random;

public class Ghost extends GameCharacter {
    protected Image image;
    private int stepCounter;
    private static final int SLOW_DOWN_FACTOR = 1;
    private boolean shouldMove;

    public Ghost(Point2D location, Point2D velocity) {
        super(location, velocity);
        this.stepCounter = 0;
        this.shouldMove = true;
    }

    @Override
    public void move(PacManModel.CellValue[][] grid) {
        if (shouldMove) {
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
            System.out.println("Ghost moved to: " + this.location);
        }
        stepCounter++;
        if (stepCounter % SLOW_DOWN_FACTOR == 0) {
            shouldMove = !shouldMove;
        }
    }

    public void moveTowardsCharacter(Point2D targetLocation, boolean ghostEatingMode, PacManModel.CellValue[][] grid) {
        if (shouldMove) {
            Random generator = new Random();
            Point2D potentialVelocity = this.velocity;
            Point2D potentialLocation = this.location.add(potentialVelocity);
            potentialLocation = setGoingOffscreenNewLocation(potentialLocation, grid.length, grid[0].length);

            if (!ghostEatingMode) {
                if (this.location.getY() == targetLocation.getY()) {
                    potentialVelocity = new Point2D(this.location.getX() > targetLocation.getX() ? -1 : 1, 0);
                } else if (this.location.getX() == targetLocation.getX()) {
                    potentialVelocity = new Point2D(0, this.location.getY() > targetLocation.getY() ? -1 : 1);
                }
            } else {
                if (this.location.getY() == targetLocation.getY()) {
                    potentialVelocity = new Point2D(this.location.getX() > targetLocation.getX() ? 1 : -1, 0);
                } else if (this.location.getX() == targetLocation.getX()) {
                    potentialVelocity = new Point2D(0, this.location.getY() > targetLocation.getY() ? 1 : -1);
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
            System.out.println("Ghost moved towards character to: " + this.location);
        }
        stepCounter++;
        if (stepCounter % SLOW_DOWN_FACTOR == 0) {
            shouldMove = !shouldMove;
        }
    }

    public void moveTowardsPacmanOrBots(Point2D pacmanLocation, List<BotPacMan> bots, boolean ghostEatingMode, PacManModel.CellValue[][] grid) {
        Point2D closestTarget = pacmanLocation;
        double closestDistance = this.location.distance(pacmanLocation);

        for (BotPacMan bot : bots) {
            double distance = this.location.distance(bot.getLocation());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestTarget = bot.getLocation();
            }
        }

        moveTowardsCharacter(closestTarget, ghostEatingMode, grid);
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

    public Image getImage() {
        return image;
    }
}
