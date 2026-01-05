package hr.algebra.battleship.services;

import hr.algebra.battleship.gameEngine.GameEngine;
import hr.algebra.battleship.model.enums.GameState;
import hr.algebra.battleship.model.game.GameData;

public class GameSetupService {

    private GameEngine gameEngine;
    private GameData gameData;

    public GameSetupService(GameEngine gameEngine, GameData gameData) {
        this.gameEngine = gameEngine;
        this.gameData = gameData;
    }

    // ✅ UKLONJEN TurnService parametar
    public void placeShipsForCurrentPlayer() {
        if (gameData == null) {
            throw new IllegalStateException("GameData nije inicijaliziran!");
        }
        gameEngine.placeRandomShipsForCurrentPlayer();
    }

    // ✅ UKLONJEN TurnService parametar
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
