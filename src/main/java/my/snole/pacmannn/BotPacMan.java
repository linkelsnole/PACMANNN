package my.snole.pacmannn;

import javafx.geometry.Point2D;
import java.util.*;

public class BotPacMan extends PacMan {
    private PacManModel.Direction lastDirection;

    public BotPacMan(Point2D location, Point2D velocity) {
        super(location, velocity);
        this.lastDirection = PacManModel.Direction.NONE;
    }

    @Override
    public void move(PacManModel.CellValue[][] grid) {
        Point2D closestFood = findClosestFood(location, grid);
        if (closestFood != null) {
            Queue<Point2D> queue = new LinkedList<>();
            Map<Point2D, Point2D> cameFrom = new HashMap<>();
            queue.add(location);
            cameFrom.put(location, null);

            while (!queue.isEmpty()) {
                Point2D current = queue.poll();
                if (current.equals(closestFood)) {
                    break;
                }

                for (Point2D dir : new Point2D[]{new Point2D(-1, 0), new Point2D(1, 0), new Point2D(0, -1), new Point2D(0, 1)}) {
                    Point2D neighbor = current.add(dir);
                    neighbor = setGoingOffscreenNewLocation(neighbor, grid.length, grid[0].length);

                    if (!cameFrom.containsKey(neighbor) && neighbor.getX() >= 0 && neighbor.getX() < grid.length && neighbor.getY() >= 0 && neighbor.getY() < grid[0].length && grid[(int) neighbor.getX()][(int) neighbor.getY()] != PacManModel.CellValue.WALL) {
                        queue.add(neighbor);
                        cameFrom.put(neighbor, current);
                    }
                }
            }

            Point2D nextStep = closestFood;
            while (cameFrom.get(nextStep) != null && !cameFrom.get(nextStep).equals(location)) {
                nextStep = cameFrom.get(nextStep);
            }

            this.velocity = nextStep.subtract(location);
            this.location = nextStep;

            // Устанавливаем последнее направление
            if (velocity.getX() == 1) {
                this.lastDirection = PacManModel.Direction.DOWN;
            } else if (velocity.getX() == -1) {
                this.lastDirection = PacManModel.Direction.UP;
            } else if (velocity.getY() == 1) {
                this.lastDirection = PacManModel.Direction.RIGHT;
            } else if (velocity.getY() == -1) {
                this.lastDirection = PacManModel.Direction.LEFT;
            }
        } else {
            this.velocity = new Point2D(0, 0);
        }
        this.location = setGoingOffscreenNewLocation(this.location, grid.length, grid[0].length);
    }

    public PacManModel.Direction getLastDirection() {
        return lastDirection;
    }

    private Point2D findClosestFood(Point2D start, PacManModel.CellValue[][] grid) {
        boolean[][] visited = new boolean[grid.length][grid[0].length];
        Queue<Point2D> queue = new LinkedList<>();
        queue.add(start);
        visited[(int) start.getX()][(int) start.getY()] = true;

        while (!queue.isEmpty()) {
            Point2D current = queue.poll();
            for (Point2D dir : new Point2D[]{new Point2D(-1, 0), new Point2D(1, 0), new Point2D(0, -1), new Point2D(0, 1)}) {
                Point2D neighbor = current.add(dir);
                if (neighbor.getX() >= 0 && neighbor.getX() < grid.length && neighbor.getY() >= 0 && neighbor.getY() < grid[0].length) {
                    if (!visited[(int) neighbor.getX()][(int) neighbor.getY()]) {
                        visited[(int) neighbor.getX()][(int) neighbor.getY()] = true;
                        if (grid[(int) neighbor.getX()][(int) neighbor.getY()] == PacManModel.CellValue.SMALLDOT ||
                                grid[(int) neighbor.getX()][(int) neighbor.getY()] == PacManModel.CellValue.BIGDOT) {
                            return neighbor;
                        }
                        queue.add(neighbor);
                    }
                }
            }
        }
        return null;
    }
}
