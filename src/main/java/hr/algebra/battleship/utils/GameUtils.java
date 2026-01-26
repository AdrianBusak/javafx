package hr.algebra.battleship.utils;

import hr.algebra.battleship.model.game.GameMove;
import hr.algebra.battleship.thread.SaveGameMoveThread;
import javafx.scene.control.Button;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Utility klasa za game logiku - spravljanje, učitavanje, replay igara
 */
public class GameUtils {

    public static final Integer BOARD_SIZE = 10;
    private static final String SAVE_GAME_FILE_PATH = "./game/save.dat";

    private GameUtils() {}

    /**
     * Sprema igru u datoteku
     */
    public static void saveGame(Object gameState) {
        try {
            // ✅ Kreiraj game direktorij ako ne postoji
            Path gameDir = Path.of("./game");
            if (!Files.exists(gameDir)) {
                Files.createDirectories(gameDir);
                System.out.println("📁 Created game directory");
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_GAME_FILE_PATH))) {
                oos.writeObject(gameState);
                System.out.println("💾 Game saved successfully!");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving game!", e);
        }
    }


    /**
     * Učitava igru iz datoteke
     */
    public static Object loadGame() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_GAME_FILE_PATH))) {
            return ois.readObject();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Game save file not found!", e);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error loading game!", e);
        }
    }

    /**
     * Kreira GameMove i sprema ga s thread-om
     */
    public static void createGameAndSaveWithThread(int row, int column, String result, String playerSymbol) {
        GameMove gameMove = new GameMove(
                row,
                column,
                result,
                playerSymbol,
                System.currentTimeMillis()
        );

        // Spremi u XML
        XmlUtils.saveNewMove(gameMove);

        // Spremi u objektni format s thread-om
        SaveGameMoveThread thread = new SaveGameMoveThread(gameMove);
        new Thread(thread).start();
    }

}