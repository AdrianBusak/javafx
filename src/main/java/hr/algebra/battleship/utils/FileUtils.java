package hr.algebra.battleship.utils;

import hr.algebra.battleship.thread.LoadGameMovesThread;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility klasa za file operacije i button mapping
 */
public class FileUtils {

    private FileUtils() {}

    public static final String GAME_MOVES_FILE_NAME = "./dat/gameMoves.dat";
    public static final String GAME_MOVES_XML_FILE_NAME = "./xml/gameMoves.xml";
    public static final String DTD_FILE_PATH = "./xml/dtd/gameMoves.dtd";
    public static final AtomicBoolean FILE_ACCESS_IN_PROGRESS = new AtomicBoolean(false);

    /**
     * Pronalazi poziciju kliknutog buttona na board-u
     */
    public static Optional<Integer[]> determineButtonPosition(Button button, Button[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == button) {
                    return Optional.of(new Integer[]{i, j});
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Kreira Timeline za osvježavanje zadnjeg poteza svakih 5 sekundi
     */
    public static Timeline getTheLastGameMoveRefreshTimeline(Label label) {
        Timeline showTheLastGameMoveTimeline = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            Thread thread = new Thread(new LoadGameMovesThread(label));
            thread.start();
        }), new KeyFrame(Duration.seconds(5)));

        showTheLastGameMoveTimeline.setCycleCount(Animation.INDEFINITE);
        return showTheLastGameMoveTimeline;
    }
}