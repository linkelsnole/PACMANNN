package my.snole.pacmannn.model.pacman;

import javafx.geometry.Point2D;
import my.snole.pacmannn.core.PacManModel;
import my.snole.pacmannn.model.GameCharacter;

/**
 * Класс, представляющий Pac-Man.
 */
public class PacMan extends GameCharacter {

    /**
     * Конструктор PacMan.
     * @param location начальная позиция
     * @param velocity начальная скорость
     */
    public PacMan(Point2D location, Point2D velocity) {
        super(location, velocity); // Вызов конструктора суперкласса
    }


    /**
     * Метод для движения Pac-Man в указанном направлении.
     * @param direction направление движения
     * @param grid игровая сетка
     */
    public void move(PacManModel.Direction direction, PacManModel.CellValue[][] grid) {
        if (direction == null) {
            direction = PacManModel.Direction.NONE;
        }

        Point2D potentialVelocity = changeVelocity(direction); // Изменяем скорость в соответствии с направлением
        Point2D potentialLocation = location.add(potentialVelocity); // Вычисляем потенциальное новое местоположение
        potentialLocation = setGoingOffscreenNewLocation(potentialLocation, grid.length, grid[0].length); // Обрабатываем выход за границы

        if (grid[(int) potentialLocation.getX()][(int) potentialLocation.getY()] != PacManModel.CellValue.WALL) {
            this.velocity = potentialVelocity; // Обновляем скорость
            this.location = potentialLocation; // Обновляем местоположение
            PacManModel.setLastDirection(direction); // Устанавливаем последнее направление
        } else {
            // Проверяем возможность движения в текущем направлении
            Point2D currentVelocity = changeVelocity(PacManModel.getLastDirection()); // Текущее направление
            Point2D currentLocation = location.add(currentVelocity); // Текущее местоположение
            currentLocation = setGoingOffscreenNewLocation(currentLocation, grid.length, grid[0].length); // Обрабатываем выход за границы

            if (grid[(int) currentLocation.getX()][(int) currentLocation.getY()] != PacManModel.CellValue.WALL) {
                this.velocity = currentVelocity; // Обновляем скорость
                this.location = currentLocation; // Обновляем местоположение
            } else {
                this.velocity = new Point2D(0, 0); // Если ни новое, ни текущее направление не подходят, остановка
            }
        }
    }

    /**
     * Изменяет скорость Pac-Man в зависимости от направления.
     * @param direction направление движения
     * @return новая скорость
     */
    private Point2D changeVelocity(PacManModel.Direction direction) {
        if (direction == null) {
            direction = PacManModel.Direction.NONE;
        }
        switch (direction) {
            case LEFT:
                return new Point2D(0, -1);
            case RIGHT:
                return new Point2D(0, 1);
            case UP:
                return new Point2D(-1, 0);
            case DOWN:
                return new Point2D(1, 0);
            default:
                return new Point2D(0, 0);
        }
    }
}
