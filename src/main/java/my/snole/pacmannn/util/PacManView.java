package my.snole.pacmannn.util;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import my.snole.pacmannn.model.PacManModel;
import my.snole.pacmannn.model.ghost.Ghost;
import my.snole.pacmannn.model.pacman.BotPacMan;

/**
 * Класс для отображения игрового поля Pac-Man.
 * Управляет визуальным представлением игры.
 */
public class PacManView extends Group {
    public final static double CELL_WIDTH = 20.0; // Ширина одной клетки игрового поля

    private int rowCount; // Количество строк игрового поля
    private int columnCount; // Количество столбцов игрового поля
    private ImageView[][] cellViews; // Массив для хранения изображений клеток

    private Image pacmanRightImage; // Изображение Pac-Man, смотрящего направо
    private Image pacmanUpImage; // Изображение Pac-Man, смотрящего вверх
    private Image pacmanDownImage; // Изображение Pac-Man, смотрящего вниз
    private Image pacmanLeftImage; // Изображение Pac-Man, смотрящего влево
    private Image blueGhostImage; // Изображение синего привидения
    private Image redGhostImage; // Изображение красного привидения
    private Image pinkGhostImage; // Изображение розового привидения
    private Image yellowGhostImage; // Изображение желтого привидения
    private Image wallImage; // Изображение стены
    private Image bigDotImage; // Изображение большого шарика
    private Image smallDotImage; // Изображение маленького шарика
    private Image botPacmanRightImage; // Изображение бота Pac-Man, смотрящего направо
    private Image botPacmanUpImage; // Изображение бота Pac-Man, смотрящего вверх
    private Image botPacmanDownImage; // Изображение бота Pac-Man, смотрящего вниз
    private Image botPacmanLeftImage; // Изображение бота Pac-Man, смотрящего влево

    /**
     * Конструктор PacManView.
     * Загружает изображения для различных игровых объектов.
     */
    public PacManView() {
        this.pacmanRightImage = new Image(getClass().getResourceAsStream("/image/pacmanRight.gif"));
        this.pacmanUpImage = new Image(getClass().getResourceAsStream("/image/pacmanUp.gif"));
        this.pacmanDownImage = new Image(getClass().getResourceAsStream("/image/pacmanDown.gif"));
        this.pacmanLeftImage = new Image(getClass().getResourceAsStream("/image/pacmanLeft.gif"));
        this.blueGhostImage = new Image(getClass().getResourceAsStream("/image/blueghost.gif"));
        this.redGhostImage = new Image(getClass().getResourceAsStream("/image/redghost.gif"));
        this.pinkGhostImage = new Image(getClass().getResourceAsStream("/image/pinkghost.gif"));
        this.yellowGhostImage = new Image(getClass().getResourceAsStream("/image/yellowghost.gif"));
        this.wallImage = new Image(getClass().getResourceAsStream("/image/wall.png"));
        this.bigDotImage = new Image(getClass().getResourceAsStream("/image/whitedot.png"));
        this.smallDotImage = new Image(getClass().getResourceAsStream("/image/smalldot.png"));
        this.botPacmanRightImage = new Image(getClass().getResourceAsStream("/image/pacmanRight.gif"));
        this.botPacmanUpImage = new Image(getClass().getResourceAsStream("/image/pacmanUp.gif"));
        this.botPacmanDownImage = new Image(getClass().getResourceAsStream("/image/pacmanDown.gif"));
        this.botPacmanLeftImage = new Image(getClass().getResourceAsStream("/image/pacmanLeft.gif"));
    }

    /**
     * Инициализация игрового поля.
     * Создает ImageView для каждой клетки.
     */
    private void initializeGrid() {
        if (this.rowCount > 0 && this.columnCount > 0) {
            this.cellViews = new ImageView[this.rowCount][this.columnCount];
            for (int row = 0; row < this.rowCount; row++) {
                for (int column = 0; column < this.columnCount; column++) {
                    ImageView imageView = new ImageView();
                    imageView.setX(column * CELL_WIDTH); // Устанавливает X координату для изображения
                    imageView.setY(row * CELL_WIDTH); // Устанавливает Y координату для изображения
                    imageView.setFitWidth(CELL_WIDTH); // Устанавливает ширину изображения
                    imageView.setFitHeight(CELL_WIDTH); // Устанавливает высоту изображения
                    this.cellViews[row][column] = imageView;
                    this.getChildren().add(imageView); // Добавляет ImageView в группу
                }
            }
        }
    }

    /**
     * Обновление визуального представления игрового поля.
     * @param model текущая модель игры
     */
    public void update(PacManModel model) {
        // Проверка соответствия размеров модели и представления
        if (model.getRowCount() != this.rowCount || model.getColumnCount() != this.columnCount) {
            this.rowCount = model.getRowCount();
            this.columnCount = model.getColumnCount();
            this.getChildren().clear();
            initializeGrid();
        }

        // Очистка изображений клеток
        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {
                this.cellViews[row][column].setImage(null);
            }
        }

        // Обновление изображений клеток в соответствии с моделью
        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {
                PacManModel.CellValue value = model.getCellValue(row, column);
                Image newImage = null;

                switch (value) {
                    case WALL:
                        newImage = this.wallImage;
                        break;
                    case BIGDOT:
                        newImage = this.bigDotImage;
                        break;
                    case SMALLDOT:
                        newImage = this.smallDotImage;
                        break;
                    default:
                        break;
                }

                // Установка изображения стены или точки
                if (newImage != null) {
                    this.cellViews[row][column].setImage(newImage);
                }
            }
        }

        // Обновление изображения Pac-Man
        Point2D pacmanLocation = model.getPacman().getLocation();
        int pacRow = (int) pacmanLocation.getX();
        int pacCol = (int) pacmanLocation.getY();
        PacManModel.Direction pacDirection = model.getCurrentDirection();

        Image pacImage = switch (pacDirection) {
            case RIGHT, NONE -> this.pacmanRightImage;
            case LEFT -> this.pacmanLeftImage;
            case UP -> this.pacmanUpImage;
            case DOWN -> this.pacmanDownImage;
        };

        this.cellViews[pacRow][pacCol].setImage(pacImage);

        // Обновление изображений привидений
        for (Ghost ghost : model.getGhosts()) {
            Point2D ghostLocation = ghost.getLocation();
            int ghostRow = (int) ghostLocation.getX();
            int ghostCol = (int) ghostLocation.getY();
            Image ghostImage = ghost.getImage();
            this.cellViews[ghostRow][ghostCol].setImage(ghostImage);
        }

        // Обновление изображений ботов Pac-Man
        for (BotPacMan botPacMan : model.getBotPacMen()) {
            Point2D botLocation = botPacMan.getLocation();
            int botRow = (int) botLocation.getX();
            int botCol = (int) botLocation.getY();
            PacManModel.Direction botDirection = botPacMan.getLastDirection();

            Image botImage = switch (botDirection) {
                case RIGHT, NONE -> this.botPacmanRightImage;
                case LEFT -> this.botPacmanLeftImage;
                case UP -> this.botPacmanUpImage;
                case DOWN -> this.botPacmanDownImage;
            };

            this.cellViews[botRow][botCol].setImage(botImage);
        }
    }

    /**
     * Возвращает количество строк игрового поля.
     * @return количество строк
     */
    public int getRowCount() {
        return this.rowCount;
    }

    /**
     * Устанавливает количество строк игрового поля.
     * @param rowCount количество строк
     */
    public void setRowCount(int rowCount) {
        if (this.rowCount != rowCount) {
            this.rowCount = rowCount;
            initializeGrid();
        }
    }

    /**
     * Возвращает количество столбцов игрового поля.
     * @return количество столбцов
     */
    public int getColumnCount() {
        return this.columnCount;
    }

    /**
     * Устанавливает количество столбцов игрового поля.
     * @param columnCount количество столбцов
     */
    public void setColumnCount(int columnCount) {
        if (this.columnCount != columnCount) {
            this.columnCount = columnCount;
            initializeGrid();
        }
    }
}
