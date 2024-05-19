package my.snole.pacmannn;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;

import java.util.Timer;
import java.util.TimerTask;

public class Controller implements EventHandler<KeyEvent> {
    final private static double FRAMES_PER_SECOND = 5.0;
    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label gameOverLabel;
    @FXML private BorderPane borderPane;
    @FXML private Label timerLabel;
    private static final int MOVE_INTERVAL = 6;
    private GameTimer gameTimer;
    private PacManView pacManView;
    private PacManModel pacManModel;
    private static final String[] levelFiles = {
            "level1.txt",
            "level2.txt",
            "level3.txt"
    };

    private Timer timer;
    private static int ghostEatingModeCounter;
    private boolean paused;
    private boolean gameStarted;

    public Controller() {
        this.paused = false;
        this.gameStarted = false;
    }

    public void initialize() {
        gameTimer = new GameTimer(timerLabel);
        this.pacManModel = new PacManModel();
        this.pacManView = new PacManView();
        this.pacManView.setRowCount(pacManModel.getRowCount());
        this.pacManView.setColumnCount(pacManModel.getColumnCount());
        this.borderPane.setCenter(this.pacManView);
        this.update(PacManModel.Direction.NONE);
        ghostEatingModeCounter = 25;
        gameTimer.start();
        startTimer();
    }

    private void startTimer() {
        this.timer = new java.util.Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                Platform.runLater(new Runnable() {
                    public void run() {
                        if (gameStarted) {
                            update(pacManModel.getCurrentDirection());
                        }
                    }
                });
            }
        };

        long frameTimeInMilliseconds = (long)(1000.0 / FRAMES_PER_SECOND);
        this.timer.schedule(timerTask, 0, frameTimeInMilliseconds);
    }

    private void update(PacManModel.Direction direction) {
        if (!paused && gameStarted) {
            this.pacManModel.step(direction);
            this.pacManView.update(pacManModel);
            this.scoreLabel.setText(String.format("Score: %d", this.pacManModel.getScore()));
            this.levelLabel.setText(String.format("Level: %d", this.pacManModel.getLevel()));
            if (pacManModel.isGameOver()) {
                this.gameOverLabel.setText("GAME OVER");
                pause();
            }
            if (pacManModel.isYouWon()) {
                this.gameOverLabel.setText("YOU WON!");
            }
            if (pacManModel.isGhostEatingMode()) {
                ghostEatingModeCounter--;
            }
            if (ghostEatingModeCounter == 0 && pacManModel.isGhostEatingMode()) {
                pacManModel.setGhostEatingMode(false);
            }
        }
    }

    @Override
    public void handle(KeyEvent keyEvent) {
        boolean keyRecognized = true;
        KeyCode code = keyEvent.getCode();
        PacManModel.Direction direction = PacManModel.Direction.NONE;
        if (code == KeyCode.LEFT) {
            direction = PacManModel.Direction.LEFT;
        } else if (code == KeyCode.RIGHT) {
            direction = PacManModel.Direction.RIGHT;
        } else if (code == KeyCode.UP) {
            direction = PacManModel.Direction.UP;
        } else if (code == KeyCode.DOWN) {
            direction = PacManModel.Direction.DOWN;
        } else if (code == KeyCode.G) {
            restartGame();
        } else {
            keyRecognized = false;
        }
        if (keyRecognized) {
            keyEvent.consume();
            pacManModel.setCurrentDirection(direction);
        }
    }

    private void restartGame() {
        pause();
        this.pacManModel.startNewGame();
        this.gameOverLabel.setText("");
        paused = false;
        gameStarted = true;
        ghostEatingModeCounter = 25;
        gameTimer.start();
        borderPane.requestFocus();
        startTimer();
        update(PacManModel.Direction.NONE);
    }

    public void pause() {
        if (this.timer != null) {
            this.timer.cancel();
        }
        this.paused = true;
    }

    public double getBoardWidth() {
        return PacManView.CELL_WIDTH * this.pacManView.getColumnCount();
    }

    public double getBoardHeight() {
        return PacManView.CELL_WIDTH * this.pacManView.getRowCount();
    }

    public static void setGhostEatingModeCounter() {
        ghostEatingModeCounter = 25;
    }

    public static int getGhostEatingModeCounter() {
        return ghostEatingModeCounter;
    }

    public static String getLevelFile(int x) {
        return levelFiles[x];
    }

    public boolean getPaused() {
        return paused;
    }

    @FXML
    private void handleStartButtonAction() {
//        int botCount = Integer.parseInt(botCountField.getText());
//        pacManModel.addBots(botCount);
        gameStarted = true;
        borderPane.requestFocus();
    }

    @FXML
    private void handleStopButtonAction() {
        gameTimer.reset();
        gameStarted = false;
        borderPane.requestFocus();
    }

    @FXML
    private void handleAddBotsButton () {
        pacManModel.addBots(1);
        gameStarted = true;
        borderPane.requestFocus();
    }
}
