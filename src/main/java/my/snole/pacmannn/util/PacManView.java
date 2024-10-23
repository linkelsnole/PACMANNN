package my.snole.pacmannn.util;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import my.snole.pacmannn.core.PacManModel;
import my.snole.pacmannn.model.ghost.Ghost;
import my.snole.pacmannn.model.pacman.BotPacMan;

/**
 * Класс для отображения игрового поля Pac-Man.
 * Управляет визуальным представлением игры.
 */
public class PacManView extends Group {
    public final static double CELL_WIDTH = 20.0; // Ширина одной клетки игрового поля

    @FXML private int rowCount; // Количество строк игрового поля
    @FXML private int columnCount; // Количество столбцов игрового поля
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
                    imageView.setX((double)column * CELL_WIDTH); // устанавливает X координату для изображения
                    imageView.setY((double)row * CELL_WIDTH); // устанавливает Y координату для изображения
                    imageView.setFitWidth(CELL_WIDTH); // устанавливает ширину изображения
                    imageView.setFitHeight(CELL_WIDTH); // устанавливает высоту изображения
                    this.cellViews[row][column] = imageView;
                    this.getChildren().add(imageView); // добавляет ImageView в группу
                }
            }
        }
    }

    /**
     * Обновление визуального представления игрового поля.
     * @param model текущая модель игры
     */
    public void update(PacManModel model) {
        assert model.getRowCount() == this.rowCount && model.getColumnCount() == this.columnCount;

        // очистка изображений клеток
        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {
                this.cellViews[row][column].setImage(null);
            }
        }

        // обновление изображений клеток в соответствии с моделью
        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {
                PacManModel.CellValue value = model.getCellValue(row, column);
                Image currentImage = this.cellViews[row][column].getImage();

                Image newImage = null;
                if (value == PacManModel.CellValue.WALL) {
                    newImage = this.wallImage;
                } else if (value == PacManModel.CellValue.BIGDOT) {
                    newImage = this.bigDotImage;
                } else if (value == PacManModel.CellValue.SMALLDOT) {
                    newImage = this.smallDotImage;
                }

                // Обновление изображения Pac-Man
                if (row == model.getPacmanLocation().getX() && column == model.getPacmanLocation().getY()) {
                    if (PacManModel.getLastDirection() == PacManModel.Direction.RIGHT || PacManModel.getLastDirection() == PacManModel.Direction.NONE) {
                        newImage = this.pacmanRightImage;
                    } else if (PacManModel.getLastDirection() == PacManModel.Direction.LEFT) {
                        newImage = this.pacmanLeftImage;
                    } else if (PacManModel.getLastDirection() == PacManModel.Direction.UP) {
                        newImage = this.pacmanUpImage;
                    } else if (PacManModel.getLastDirection() == PacManModel.Direction.DOWN) {
                        newImage = this.pacmanDownImage;
                    }
                }

                // Отображение всех привидений
                for (Ghost ghost : model.getGhosts()) {
                    if (row == ghost.getLocation().getX() && column == ghost.getLocation().getY()) {
                        newImage = ghost.getImage();
                    }
                }

                // Отображение всех ботов PacMan
                for (BotPacMan botPacMan : model.getBots()) {
                    if (row == botPacMan.getLocation().getX() && column == botPacMan.getLocation().getY()) {
                        if (botPacMan.getLastDirection() == PacManModel.Direction.RIGHT || botPacMan.getLastDirection() == PacManModel.Direction.NONE) {
                            newImage = this.botPacmanRightImage;
                        } else if (botPacMan.getLastDirection() == PacManModel.Direction.LEFT) {
                            newImage = this.botPacmanLeftImage;
                        } else if (botPacMan.getLastDirection() == PacManModel.Direction.UP) {
                            newImage = this.botPacmanUpImage;
                        } else if (botPacMan.getLastDirection() == PacManModel.Direction.DOWN) {
                            newImage = this.botPacmanDownImage;
                        }
                    }
                }

                // Устанавливаем изображение только если оно изменилось
                if (newImage != currentImage) {
                    this.cellViews[row][column].setImage(newImage);
                }
            }
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
        this.rowCount = rowCount;
        this.initializeGrid();
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
        this.columnCount = columnCount;
        this.initializeGrid();
    }
}
