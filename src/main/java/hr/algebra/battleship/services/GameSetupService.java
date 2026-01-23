package hr.algebra.battleship.services;

import hr.algebra.battleship.gameEngine.GameEngine;
import hr.algebra.battleship.model.enums.GameState;
import hr.algebra.battleship.model.enums.Orientation;
import hr.algebra.battleship.model.game.GameData;
import hr.algebra.battleship.model.game.Player;
import hr.algebra.battleship.model.ships.*;

import java.util.List;
import java.util.Random;

public class GameSetupService {

    private GameEngine gameEngine;
    private GameData gameData;

    public GameSetupService(GameEngine gameEngine, GameData gameData) {
        this.gameEngine = gameEngine;
        this.gameData = gameData;
    }

    public void placeShipsForCurrentPlayer() {
        if (gameData == null) {
            throw new IllegalStateException("GameData nije inicijaliziran!");
        }
        gameEngine.placeRandomShipsForCurrentPlayer();
    }

    // ✅ NOVO - Postavi brodove za specifičnog igrača
    public void placeShipsForPlayer(Player player) {
        if (player == null || player.getBoard() == null) {
            throw new IllegalStateException("Player nije inicijaliziran!");
        }

        List<Ship> ships = List.of(
                new Carrier(),
                new Battleship(),
                new Cruiser(),
                new Submarine(),
                new Destroyer()
        );

        Random rand = new Random();

        for (Ship ship : ships) {
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < 200) {
                int row = rand.nextInt(10);
                int col = rand.nextInt(10);
                Orientation orientation = rand.nextBoolean() ? Orientation.HORIZONTAL : Orientation.VERTICAL;

                try {
                    placed = player.getBoard().placeShip(ship, row, col, orientation);
                } catch (Exception e) {
                    System.err.println("Greška: " + e.getMessage());
                }
                attempts++;
            }
        }
    }

    public boolean switchToNextSetupPlayer() {
        if (gameData.getCurrentPlayerIndex() == 0) {
            gameData.setCurrentPlayerIndex(1);
            return true;
        } else {
            gameData.setCurrentPlayerIndex(0);
            gameData.setGameState(GameState.PLAYING);
            return false;
        }
    }

    public boolean isSetupComplete() {
        return gameData.getPlayers().stream()
                .allMatch(p -> p.getBoard().getShips().size() >= 5);
    }
}
