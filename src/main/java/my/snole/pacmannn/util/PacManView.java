package my.snole.pacmannn.util;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import my.snole.pacmannn.core.PacManModel;
import my.snole.pacmannn.model.ghost.Ghost;
import my.snole.pacmannn.model.pacman.BotPacMan;

public class PacManView extends Group {
    public final static double CELL_WIDTH = 20.0;

    @FXML private int rowCount;
    @FXML private int columnCount;
    private ImageView[][] cellViews;
    private Image pacmanRightImage;
    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image blueGhostImage;
    private Image redGhostImage;
    private Image pinkGhostImage;
    private Image yellowGhostImage;
    private Image wallImage;
    private Image bigDotImage;
    private Image smallDotImage;
    private Image botPacmanRightImage;
    private Image botPacmanUpImage;
    private Image botPacmanDownImage;
    private Image botPacmanLeftImage;

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

    private void initializeGrid() {
        if (this.rowCount > 0 && this.columnCount > 0) {
            this.cellViews = new ImageView[this.rowCount][this.columnCount];
            for (int row = 0; row < this.rowCount; row++) {
                for (int column = 0; column < this.columnCount; column++) {
                    ImageView imageView = new ImageView();
                    imageView.setX((double)column * CELL_WIDTH);
                    imageView.setY((double)row * CELL_WIDTH);
                    imageView.setFitWidth(CELL_WIDTH);
                    imageView.setFitHeight(CELL_WIDTH);
                    this.cellViews[row][column] = imageView;
                    this.getChildren().add(imageView);
                }
            }
        }
    }

    public void update(PacManModel model) {
        assert model.getRowCount() == this.rowCount && model.getColumnCount() == this.columnCount;

        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {
                this.cellViews[row][column].setImage(null);
            }
        }

        for (int row = 0; row < this.rowCount; row++) {
            for (int column = 0; column < this.columnCount; column++) {
                PacManModel.CellValue value = model.getCellValue(row, column);
                if (value == PacManModel.CellValue.WALL) {
                    this.cellViews[row][column].setImage(this.wallImage);
                } else if (value == PacManModel.CellValue.BIGDOT) {
                    this.cellViews[row][column].setImage(this.bigDotImage);
                } else if (value == PacManModel.CellValue.SMALLDOT) {
                    this.cellViews[row][column].setImage(this.smallDotImage);
                }

                if (row == model.getPacmanLocation().getX() && column == model.getPacmanLocation().getY()) {
                    if (PacManModel.getLastDirection() == PacManModel.Direction.RIGHT || PacManModel.getLastDirection() == PacManModel.Direction.NONE) {
                        this.cellViews[row][column].setImage(this.pacmanRightImage);
                    } else if (PacManModel.getLastDirection() == PacManModel.Direction.LEFT) {
                        this.cellViews[row][column].setImage(this.pacmanLeftImage);
                    } else if (PacManModel.getLastDirection() == PacManModel.Direction.UP) {
                        this.cellViews[row][column].setImage(this.pacmanUpImage);
                    } else if (PacManModel.getLastDirection() == PacManModel.Direction.DOWN) {
                        this.cellViews[row][column].setImage(this.pacmanDownImage);
                    }
                }

                // Отображение всех привидений
                for (Ghost ghost : model.getGhosts()) {
                    if (row == ghost.getLocation().getX() && column == ghost.getLocation().getY()) {
                        this.cellViews[row][column].setImage(ghost.getImage());
                    }
                }

                // Отображение всех ботов PacMan
                for (BotPacMan botPacMan : model.getBots()) {
                    if (row == botPacMan.getLocation().getX() && column == botPacMan.getLocation().getY()) {
                        if (botPacMan.getLastDirection() == PacManModel.Direction.RIGHT || botPacMan.getLastDirection() == PacManModel.Direction.NONE) {
                            this.cellViews[row][column].setImage(this.botPacmanRightImage);
                        } else if (botPacMan.getLastDirection() == PacManModel.Direction.LEFT) {
                            this.cellViews[row][column].setImage(this.botPacmanLeftImage);
                        } else if (botPacMan.getLastDirection() == PacManModel.Direction.UP) {
                            this.cellViews[row][column].setImage(this.botPacmanUpImage);
                        } else if (botPacMan.getLastDirection() == PacManModel.Direction.DOWN) {
                            this.cellViews[row][column].setImage(this.botPacmanDownImage);
                        }
                    }
                }
            }
        }
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
        this.initializeGrid();
    }

    public int getColumnCount() {
        return this.columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
        this.initializeGrid();
    }
}
