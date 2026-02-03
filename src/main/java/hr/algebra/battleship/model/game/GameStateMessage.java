package hr.algebra.battleship.model.game;

import hr.algebra.battleship.model.enums.GameState;

import java.io.Serializable;

public class GameStateMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private GameState gameState;
    private int currentPlayerIndex;
    private int attackRow = -1;
    private int attackCol = -1;
    private String attackResult;
    private Board player1Board;
    private Board player2Board;

    public GameStateMessage() {}

    public GameState getGameState() { return gameState; }
    public void setGameState(GameState gameState) { this.gameState = gameState; }

    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public void setCurrentPlayerIndex(int currentPlayerIndex) { this.currentPlayerIndex = currentPlayerIndex; }

    public int getAttackRow() { return attackRow; }
    public void setAttackRow(int attackRow) { this.attackRow = attackRow; }

    public int getAttackCol() { return attackCol; }
    public void setAttackCol(int attackCol) { this.attackCol = attackCol; }

    public String getAttackResult() { return attackResult; }
    public void setAttackResult(String attackResult) { this.attackResult = attackResult; }

    public Board getPlayer1Board() { return player1Board; }
    public void setPlayer1Board(Board player1Board) { this.player1Board = player1Board; }

    public Board getPlayer2Board() { return player2Board; }
    public void setPlayer2Board(Board player2Board) { this.player2Board = player2Board; }

    @Override
    public String toString() {
        return "GameStateMessage{" +
                "gameState=" + gameState +
                ", currentPlayerIndex=" + currentPlayerIndex +
                ", attackRow=" + attackRow +
                ", attackCol=" + attackCol +
                ", attackResult=" + attackResult +
                ", player1Board=" + (player1Board != null ? "ok" : "nocando") +
                ", player2Board=" + (player2Board != null ? "ok" : "nocando") +
                '}';
    }
}
