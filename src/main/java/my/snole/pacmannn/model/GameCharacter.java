package my.snole.pacmannn.model;

import javafx.geometry.Point2D;
import my.snole.pacmannn.core.PacManModel;

/**
 * Абстрактный класс, представляющий игрового персонажа.
 * Содержит общие свойства и методы, которые могут быть использованы различными персонажами.
 */
public abstract class GameCharacter {
    protected Point2D location;
    protected Point2D velocity;

    /**
     * Конструктор для создания игрового персонажа с заданным местоположением и скоростью.
     * @param location начальное местоположение
     * @param velocity начальная скорость
     */
    public GameCharacter(Point2D location, Point2D velocity) {
        this.location = location;
        this.velocity = velocity;
    }

    /**
     * Возвращает текущее местоположение персонажа.
     * @return текущее местоположение
     */
    public Point2D getLocation() {
        return location;
    }

    /**
     * Устанавливает новое местоположение персонажа.
     * @param location новое местоположение
     */
    public void setLocation(Point2D location) {
        this.location = location;
    }

    /**
     * Возвращает текущую скорость персонажа.
     * @return текущая скорость
     */
    public Point2D getVelocity() {
        return velocity;
    }

    /**
     * Устанавливает новую скорость персонажа.
     * @param velocity новая скорость
     */
    public void setVelocity(Point2D velocity) {
        this.velocity = velocity;
    }

    /**
     * Абстрактный метод для перемещения персонажа.
     * Должен быть реализован в подклассах
     * @param grid игровое поле
     */
    public abstract void move(PacManModel.CellValue[][] grid);

    /**
     * Метод для обновления местоположения персонажа, если он выходит за границы экрана.
     * Перемещает персонажа на противоположную сторону экрана
     * @param objectLocation текущее местоположение персонажа
     * @param rowCount количество строк в игровом поле
     * @param columnCount количество столбцов в игровом поле
     * @return новое местоположение персонажа, скорректированное с учетом выхода за границы экрана
     */
    protected Point2D setGoingOffscreenNewLocation(Point2D objectLocation, int rowCount, int columnCount) {
        if (objectLocation.getY() >= columnCount) {
            objectLocation = new Point2D(objectLocation.getX(), 0); // Перемещение влево, если выходит за правую границу
        }
        if (objectLocation.getY() < 0) {
            objectLocation = new Point2D(objectLocation.getX(), columnCount - 1); // Перемещение вправо, если выходит за левую границу
        }
        if (objectLocation.getX() >= rowCount) {
            objectLocation = new Point2D(0, objectLocation.getY()); // Перемещение вверх, если выходит за нижнюю границу
        }
        if (objectLocation.getX() < 0) {
            objectLocation = new Point2D(rowCount - 1, objectLocation.getY()); // Перемещение вниз, если выходит за верхнюю границу
        }
        return objectLocation;
    }
}
