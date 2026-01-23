package hr.algebra.battleship.model.game;

import hr.algebra.battleship.model.enums.GameState;

import java.io.Serializable;
import java.util.List;

public class GameData implements Serializable {
    private List<Player> players;
    private int currentPlayerIndex;
    private GameState gameState;

    public GameData(List<Player> players, int currentPlayerIndex, GameState gameState) {
        this.players = players;
        this.currentPlayerIndex = currentPlayerIndex;
        this.gameState = gameState;
    }

    public GameData() {
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}
