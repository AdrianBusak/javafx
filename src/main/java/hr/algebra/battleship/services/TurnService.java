package hr.algebra.battleship.services;

import hr.algebra.battleship.model.game.GameData;
import hr.algebra.battleship.model.game.Player;

public class TurnService {

    private GameData gameData;

    public TurnService(GameData gameData) {
        this.gameData = gameData;
    }

    // ✅ DODAJ OVO - setter za ažuriranje gameData nakon resetiranja
    public void setGameData(GameData gameData) {
        this.gameData = gameData;
    }

    public Player getCurrentPlayer() {
        if (gameData == null) {
            throw new IllegalStateException("GameData nije inicijaliziran!");
        }
        return gameData.getPlayers().get(gameData.getCurrentPlayerIndex());
    }

    public Player getOpponent() {
        if (gameData == null) {
            throw new IllegalStateException("GameData nije inicijaliziran!");
        }
        int opponentIndex = (gameData.getCurrentPlayerIndex() + 1) % 2;
        return gameData.getPlayers().get(opponentIndex);
    }

    public void switchTurn() {
        if (gameData == null) {
            throw new IllegalStateException("GameData nije inicijaliziran!");
        }
        int currentIndex = gameData.getCurrentPlayerIndex();
        gameData.setCurrentPlayerIndex((currentIndex + 1) % 2);
    }
}
