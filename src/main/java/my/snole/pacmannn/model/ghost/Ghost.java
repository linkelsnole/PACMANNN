package my.snole.pacmannn.model.ghost;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import my.snole.pacmannn.core.PacManModel;
import my.snole.pacmannn.model.pacman.BotPacMan;
import my.snole.pacmannn.model.GameCharacter;

import java.util.List;
import java.util.Random;

/**
 * Класс для представления привидения в игре Pac-Man.
 * Наследуется от GameCharacter.
 */
public class Ghost extends GameCharacter {
    protected Image image; // изображение привидения
    private Image defaultImage; // изображение по умолчанию
    private int stepCounter; // счётчик шагов
    private static final int SLOW_DOWN_FACTOR = 100000000; // фактор замедления
    private boolean shouldMove; // флаг - нужно ли двигаться
    private GhostManager ghostManager; // менеджер привидений

    /**
     * Конструктор привидения.
     * @param location начальное местоположение
     * @param velocity начальная скорость
     * @param ghostManager менеджер привидений
     * @param defaultImage изображение по умолчанию
     */
    public Ghost(Point2D location, Point2D velocity, GhostManager ghostManager, Image defaultImage) {
        super(location, velocity);
        this.stepCounter = 0;
        this.shouldMove = true;
        this.ghostManager = ghostManager;
        this.defaultImage = defaultImage;
        this.image = defaultImage;
    }

    /**
     * Меняет изображение привидения на синее.
     */
    public void changeImageToBlue() {
        this.image = new Image(getClass().getResourceAsStream("/image/blueghost.gif"));
    }

    /**
     * Сбрасывает изображение привидения на изображение по умолчанию.
     */
    public void resetImage() {
        this.image = defaultImage;
    }

    /**
     * Движение привидения.
     * @param grid игровая сетка
     */


    /**
     * Движение привидения в сторону цели.
     * @param targetLocation местоположение цели
     * @param ghostEatingMode режим поедания привидений
     * @param grid игровая сетка
     */
    public void moveTowardsCharacter(Point2D targetLocation, boolean ghostEatingMode, PacManModel.CellValue[][] grid) {
        if (shouldMove) {
            Random generator = new Random();
            Point2D potentialVelocity = this.velocity;
            Point2D potentialLocation = this.location.add(potentialVelocity);
            potentialLocation = setGoingOffscreenNewLocation(potentialLocation, grid.length, grid[0].length);

            if (!ghostEatingMode) { // если привидение не в режиме поедания
                if (this.location.getY() == targetLocation.getY()) {
                    potentialVelocity = new Point2D(this.location.getX() > targetLocation.getX() ? -1 : 1, 0);
                } else if (this.location.getX() == targetLocation.getX()) {
                    potentialVelocity = new Point2D(0, this.location.getY() > targetLocation.getY() ? -1 : 1);
                }
            } else { // если привидение в режиме поедания
                if (this.location.getY() == targetLocation.getY()) {
                    potentialVelocity = new Point2D(this.location.getX() > targetLocation.getX() ? 1 : -1, 0);
                } else if (this.location.getX() == targetLocation.getX()) {
                    potentialVelocity = new Point2D(0, this.location.getY() > targetLocation.getY() ? 1 : -1);
                }
            }

            potentialLocation = this.location.add(potentialVelocity);
            potentialLocation = setGoingOffscreenNewLocation(potentialLocation, grid.length, grid[0].length);

            int attempts = 0;
            while (attempts < 4 && grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] == PacManModel.CellValue.WALL) {
                int randomNum = generator.nextInt(4);
                PacManModel.Direction direction = PacManModel.Direction.values()[randomNum];
                potentialVelocity = changeVelocity(direction);
                potentialLocation = this.location.add(potentialVelocity);
                potentialLocation = setGoingOffscreenNewLocation(potentialLocation, grid.length, grid[0].length);
                attempts++;
            }

            // Если не удалось найти направление без стены, привидение не двигается
            if (attempts < 4) {
                this.velocity = potentialVelocity;
                this.location = potentialLocation;
                System.out.println("Ghost moved towards character to: " + this.location);
            }
        }
        stepCounter++;
        if (stepCounter % SLOW_DOWN_FACTOR == 0) {
            shouldMove = !shouldMove;
        }
    }

    /**
     * Движение привидения в сторону Pac-Man или ботов.
     * @param pacmanLocation местоположение Pac-Man
     * @param bots список ботов Pac-Man
     * @param ghostEatingMode режим поедания привидений
     * @param grid игровая сетка
     */
    public void moveTowardsPacmanOrBots(Point2D pacmanLocation, List<BotPacMan> bots, boolean ghostEatingMode, PacManModel.CellValue[][] grid) {
        Point2D closestTarget = pacmanLocation; // ближайшая цель - пакман
        double closestDistance = this.location.distance(pacmanLocation); // расстояние до пакмана

        for (BotPacMan bot : bots) {
            double distance = this.location.distance(bot.getLocation());
            if (ghostEatingMode) { // если привидение в режиме поедания
                if (distance > closestDistance) { // выбираем максимальное расстояние
                    closestDistance = distance;
                    closestTarget = bot.getLocation();
                }
            } else { // если привидение не в режиме поедания
                if (distance < closestDistance) { // выбираем минимальное расстояние
                    closestDistance = distance;
                    closestTarget = bot.getLocation();
                }
            }
        }

        moveTowardsCharacter(closestTarget, ghostEatingMode, grid); // движение к ближайшей цели
    }

    /**
     * Разворот направления движения привидения.
     */
    public void reverseDirection() {
        this.velocity = new Point2D(-this.velocity.getX(), -this.velocity.getY());
    }

    /**
     * Изменение скорости привидения в зависимости от направления.
     * @param direction направление
     * @return новая скорость
     */
    private Point2D changeVelocity(PacManModel.Direction direction) {
        switch (direction) {
            case LEFT: return new Point2D(0, -1);
            case RIGHT: return new Point2D(0, 1);
            case UP: return new Point2D(-1, 0);
            case DOWN: return new Point2D(1, 0);
            default: return new Point2D(0, 0);
        }
    }

    /**
     * Возвращает изображение привидения.
     * @return изображение привидения
     */
    public Image getImage() {
        return image;
    }
}
