package hr.algebra.battleship.thread;

import hr.algebra.battleship.exception.ConcurrentAccessException;
import hr.algebra.battleship.model.game.GameMove;
import hr.algebra.battleship.utils.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Apstraktna klasa za thread-safe pristup game move datotekama
 */
public abstract class AbstractGameMoveThread {

    /**
     * Thread-safe spravljanje novog poteza u datoteku
     */
    protected synchronized void saveGameMove(GameMove gameMove)
            throws FileNotFoundException, ConcurrentAccessException {

        // Čekaj da file access završi
        while (Boolean.TRUE.equals(FileUtils.FILE_ACCESS_IN_PROGRESS.get())) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ConcurrentAccessException("Waiting during file access attempt failed!", e);
            }
        }

        // Označi da je file access u toku
        FileUtils.FILE_ACCESS_IN_PROGRESS.set(true);

        List<GameMove> gameMoves = new ArrayList<>();

        // Učitaj postojeće poteze ako datoteka postoji
        if (Files.exists(Path.of(FileUtils.GAME_MOVES_FILE_NAME))) {
            List<GameMove> existingMoves = loadGameMoves();
            gameMoves.addAll(existingMoves);
        }

        // Dodaj novi potez
        gameMoves.add(gameMove);

        // Spremi sve poteze u datoteku
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(FileUtils.GAME_MOVES_FILE_NAME))) {
            oos.writeObject(gameMoves);
        } catch (IOException e) {
            throw new ConcurrentAccessException("Error while saving game move!", e);
        } finally {
            FileUtils.FILE_ACCESS_IN_PROGRESS.set(false);
            notifyAll();
        }
    }

    /**
     * Thread-safe učitavanje svih poteza iz datoteke
     */
    protected synchronized List<GameMove> loadGameMoves() throws ConcurrentAccessException {

        // Čekaj da file access završi
        while (Boolean.TRUE.equals(FileUtils.FILE_ACCESS_IN_PROGRESS.get())) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ConcurrentAccessException("Waiting during file access attempt failed!", e);
            }
        }

        // Označi da je file access u toku
        FileUtils.FILE_ACCESS_IN_PROGRESS.set(true);

        List<GameMove> gameMoves = new ArrayList<>();

        if (Files.exists(Path.of(FileUtils.GAME_MOVES_FILE_NAME))) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(FileUtils.GAME_MOVES_FILE_NAME))) {
                @SuppressWarnings("unchecked")
                List<GameMove> loadedMoves = (List<GameMove>) ois.readObject();
                gameMoves.addAll(loadedMoves);
            } catch (IOException | ClassNotFoundException e) {
                throw new ConcurrentAccessException("Error while loading game moves!", e);
            }
        }

        FileUtils.FILE_ACCESS_IN_PROGRESS.set(false);
        notifyAll();

        return gameMoves;
    }
}