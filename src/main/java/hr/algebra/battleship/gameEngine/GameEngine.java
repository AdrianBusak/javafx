package hr.algebra.battleship.gameEngine;

import hr.algebra.battleship.model.enums.AttackResult;
import hr.algebra.battleship.model.enums.CellState;
import hr.algebra.battleship.model.enums.GameState;
import hr.algebra.battleship.model.enums.Orientation;
import hr.algebra.battleship.model.game.Board;
import hr.algebra.battleship.model.game.Cell;
import hr.algebra.battleship.model.game.GameData;
import hr.algebra.battleship.model.game.Player;
import hr.algebra.battleship.model.ships.*;
import hr.algebra.battleship.services.TurnService;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private static GameEngine instance;
    private GameData gameData;
    private TurnService turnService;

    private GameEngine() {}

    public static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    public void initializeGame(Player player1, Player player2) {
        gameData = new GameData();
        gameData.setPlayers(Arrays.asList(player1, player2));
        gameData.setGameState(GameState.SETUP);
        gameData.setCurrentPlayerIndex(0);
        turnService = new TurnService(gameData);
    }

    public void placeRandomShipsForCurrentPlayer() {
        if (gameData == null || gameData.getPlayers().isEmpty()) {
            throw new IllegalStateException("GameData nije inicijaliziran!");
        }

        Player currentPlayer = gameData.getPlayers()
                .get(gameData.getCurrentPlayerIndex());
        placeRandomShips(currentPlayer);
    }

    private void placeRandomShips(Player player) {
        if (player == null || player.getBoard() == null) {
            throw new IllegalArgumentException("Player ili Board je null!");
        }

        if (player.getBoard().getShips().size() >= 5) {
            System.out.println("Brodovi su već postavljeni za " + player.getName());
            return;
        }

        Board board = player.getBoard();
        List<Ship> ships = createStandardFleet();

        for (Ship ship : ships) {
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < 100) {
                int x = new Random().nextInt(10);
                int y = new Random().nextInt(10);
                Orientation orientation = Orientation.values()[new Random().nextInt(2)];

                placed = board.placeShip(ship, x, y, orientation);
                attempts++;
            }
        }
    }

    private List<Ship> createStandardFleet() {
        return Arrays.asList(
                new Carrier(),
                new Battleship(),
                new Cruiser(),
                new Submarine(),
                new Destroyer()
        );
    }

    public GameData getGameData() {
        return gameData;
    }

    public TurnService getTurnService() {
        return turnService;
    }
}
