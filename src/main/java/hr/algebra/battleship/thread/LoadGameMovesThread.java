package hr.algebra.battleship.thread;

import hr.algebra.battleship.exception.ConcurrentAccessException;
import hr.algebra.battleship.model.game.GameMove;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.util.List;

import static hr.algebra.battleship.utils.XmlUtils.loadGameMoves;

/**
 * Thread za učitavanje zadnjeg poteza i ažuriranje UI-ja
 */
public class LoadGameMovesThread extends AbstractGameMoveThread implements Runnable {

    private final Label label;

    public LoadGameMovesThread(Label label) {
        this.label = label;
    }

    @Override
    public void run() {
        try {
            List<GameMove> gameMoves = loadGameMoves();

            if (!gameMoves.isEmpty()) {
                GameMove lastMove = gameMoves.getLast();
                Platform.runLater(
                        () -> label.setText("Last move: (" + lastMove.getRow() + ", "
                                + lastMove.getColumn() + ") - " + lastMove.getResult())
                );
            } else {
                Platform.runLater(() -> label.setText("No moves yet"));
            }
        } catch (ConcurrentAccessException e) {
            System.err.println("Error loading game moves: " + e.getMessage());
            e.printStackTrace();
        }
    }
}