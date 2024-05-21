package my.snole.pacmannn;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.util.Timer;
import java.util.TimerTask;

public class GameTimer {

    private int secondsElapsed;
    private Timer timer;
    private Label timerLabel;

    public GameTimer(Label timerLabel) {
        this.timerLabel = timerLabel;
        this.secondsElapsed = 0;
    }

    public void start() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    secondsElapsed++;
                    updateLabel();
                });
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

    public void reset() {
        if (timer != null) {
            timer.cancel();
        }
        secondsElapsed = -1;
        updateLabel();
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void updateLabel() {
        int minutes = secondsElapsed / 60;
        int seconds = secondsElapsed % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    public void setSecondsElapsed(int secondsElapsed) {
        this.secondsElapsed = secondsElapsed;
    }
}
