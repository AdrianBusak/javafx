package hr.algebra.battleship.thread;

import hr.algebra.battleship.exception.ConcurrentAccessException;
import hr.algebra.battleship.model.game.GameMove;

import java.io.FileNotFoundException;

/**
 * Thread za spravljanje novog poteza u game moves datoteku
 */
public class SaveGameMoveThread extends AbstractGameMoveThread implements Runnable {

    private final GameMove gameMove;

    public SaveGameMoveThread(GameMove gameMove) {
        this.gameMove = gameMove;
    }

    @Override
    public void run() {
        try {
            saveGameMove(gameMove);
        } catch (ConcurrentAccessException e) {
            System.err.println("Error saving game move: " + e.getMessage());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}