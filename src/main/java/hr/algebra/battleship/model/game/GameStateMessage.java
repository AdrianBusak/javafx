package hr.algebra.battleship.model.game;

import hr.algebra.battleship.model.enums.GameState;
import java.io.Serializable;

public class GameStateMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private GameState gameState;
    private int currentPlayerIndex;
    private int[][] player1Board;
    private int[][] player2Board;
    private int player1ShipsRemaining;
    private int player2ShipsRemaining;

    public GameStateMessage() {}

    public GameStateMessage(GameState gameState, int currentPlayerIndex,
                            int[][] player1Board, int[][] player2Board,
                            int player1ShipsRemaining, int player2ShipsRemaining) {
        this.gameState = gameState;
        this.currentPlayerIndex = currentPlayerIndex;
        this.player1Board = player1Board;
        this.player2Board = player2Board;
        this.player1ShipsRemaining = player1ShipsRemaining;
        this.player2ShipsRemaining = player2ShipsRemaining;
    }

    // Getters & Setters
    public GameState getGameState() { return gameState; }
    public void setGameState(GameState gameState) { this.gameState = gameState; }

    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public void setCurrentPlayerIndex(int currentPlayerIndex) { this.currentPlayerIndex = currentPlayerIndex; }

    public int[][] getPlayer1Board() { return player1Board; }
    public void setPlayer1Board(int[][] player1Board) { this.player1Board = player1Board; }

    public int[][] getPlayer2Board() { return player2Board; }
    public void setPlayer2Board(int[][] player2Board) { this.player2Board = player2Board; }

    public int getPlayer1ShipsRemaining() { return player1ShipsRemaining; }
    public void setPlayer1ShipsRemaining(int player1ShipsRemaining) { this.player1ShipsRemaining = player1ShipsRemaining; }

    public int getPlayer2ShipsRemaining() { return player2ShipsRemaining; }
    public void setPlayer2ShipsRemaining(int player2ShipsRemaining) { this.player2ShipsRemaining = player2ShipsRemaining; }
}
