package hr.algebra.battleship.controller;
import hr.algebra.battleship.gameEngine.GameEngine;
import hr.algebra.battleship.model.enums.*;
import hr.algebra.battleship.model.game.*;
import hr.algebra.battleship.model.ships.*;
import hr.algebra.battleship.services.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.net.URL;
import java.util.*;

public class BoardController implements Initializable {

    @FXML private GridPane player1Board, player2Board;
    @FXML private Label gameStatusLabel, currentPlayerLabel, remainingShipsLabel;
    @FXML private Button resetButton, placeRandomShipsBtn, startGameBtn, saveGameBtn;
    @FXML private HBox setupPanel;

    @FXML private Button carrierBtn, battleshipBtn, cruiserBtn, submarineBtn, destroyerBtn;
    @FXML private ToggleButton horizontalBtn, verticalBtn;

    private GameEngine gameEngine;
    private TurnService turnService;
    private BoardUIService boardUIService;
    private GameSetupService gameSetupService;
    private CellVisualizationService visualizationService;

    private GameData gameData;
    private Player player1, player2;

    private Ship selectedShip;
    private Orientation selectedOrientation = Orientation.HORIZONTAL;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gameEngine = GameEngine.getInstance();
        player1 = new Player("Igrač 1", new Board());
        player2 = new Player("Igrač 2", new Board());
        gameEngine.initializeGame(player1, player2);
        gameData = gameEngine.getGameData();
        turnService = gameEngine.getTurnService();
        turnService.setGameData(gameData);
        gameSetupService = new GameSetupService(gameEngine, gameData);
        boardUIService = new BoardUIService();
        visualizationService = new CellVisualizationService(boardUIService);

        initializeBoards();
        setupEventHandlers();
        resetGame();
    }

    private void initializeBoards() {
        boardUIService.createBoardGrid(player1Board, false, this::handleCellClick);
        boardUIService.createBoardGrid(player2Board, true, this::handleCellClick);
    }

    // ✅ RUČNO POSTAVLJANJE - Izbor brodova
    @FXML private void selectCarrier() { selectShip(new Carrier()); }
    @FXML private void selectBattleship() { selectShip(new Battleship()); }
    @FXML private void selectCruiser() { selectShip(new Cruiser()); }
    @FXML private void selectSubmarine() { selectShip(new Submarine()); }
    @FXML private void selectDestroyer() { selectShip(new Destroyer()); }

    private void selectShip(Ship ship) {
        int currentPlayerIndex = gameData.getCurrentPlayerIndex();
        Player currentPlayer = gameData.getPlayers().get(currentPlayerIndex);

        if (currentPlayer.getBoard().getShips().size() >= 5) {
            gameStatusLabel.setText("❌ Već si postavio 5 brodova!");
            return;
        }

        selectedShip = ship;
        String playerName = currentPlayerIndex == 0 ? "Igrač 1" : "Igrač 2";
        gameStatusLabel.setText("📍 " + ship.getType() + " (" + playerName + ") - Klikni na svoj board!");
    }

    @FXML
    private void handleOrientationToggle() {
        horizontalBtn.setSelected(!verticalBtn.isSelected());
        verticalBtn.setSelected(!horizontalBtn.isSelected());
        selectedOrientation = horizontalBtn.isSelected()
                ? Orientation.HORIZONTAL
                : Orientation.VERTICAL;
    }

    private void handleCellClick(Rectangle cell, int row, int col, boolean isOpponent) {
// ✅ Tijekom SETUP-a - postavi brodove
        if (gameData.getGameState() == GameState.SETUP) {
            boolean currentIsPlayer1 = gameData.getCurrentPlayerIndex() == 0;

            if (currentIsPlayer1 && cell.getParent() != player1Board) {
                gameStatusLabel.setText("⚠️ Klikni na svoj board (Igrač 1)!");
                return;
            }

            if (!currentIsPlayer1 && cell.getParent() != player2Board) {
                gameStatusLabel.setText("⚠️ Klikni na svoj board (Igrač 2)!");
                return;
            }

            if (selectedShip != null) {
                handleManualShipPlacement(row, col);
            } else {
                gameStatusLabel.setText("⚠️ Prvo odaberi brod!");
            }
            return;
        }

// ✅ Tijekom igre - napad
        if (gameData.getGameState() == GameState.PLAYING) {
            boolean currentIsPlayer1 = gameData.getCurrentPlayerIndex() == 0;

// Provjeri koji board koristi trenutni igrač
// Ako je Player 1: lijevo je njegov board, desno je protivnički
// Ako je Player 2: lijevo je njegove board, desno je protivnički
// ALI: player1Board sadrži Player 1, player2Board sadrži Player 2

// Tijekom PLAYING:
// - player1Board prikazuje trenutni igrač (može biti P1 ili P2 ovisno o logici)
// - player2Board prikazuje protivnika

// Trebam kliknuti na DESNI board (protivnički)
            boolean clickedOnLeftBoard = (cell.getParent() == player1Board);
            boolean clickedOnRightBoard = (cell.getParent() == player2Board);

            if (!clickedOnRightBoard) {
                gameStatusLabel.setText("⚠️ Klikni na Protivnički Board (desno)!");
                return;
            }

            try {
                AttackResult result = gameEngine.attack(row, col);

                Rectangle targetCell = findCellInGrid(player2Board, row, col);

                if (targetCell != null) {
                    boardUIService.updateCellStyle(targetCell,
                            result == AttackResult.MISS ? CellState.MISS : CellState.HIT);
                    visualizationService.animateAttack(targetCell, result);
                }

                updateGameStatus(result);

                if (result != AttackResult.WIN) {
                    PauseTransition pause = new PauseTransition(Duration.millis(800));
                    pause.setOnFinished(e -> {
                        switchTurn();
// ✅ PREBACI BOARDOVE ZA SLJEDEĆEG IGRAČA
                        swapBoardsForNextPlayer();
                    });
                    pause.play();
                } else {
                    endGame();
                }
            } catch (Exception e) {
                System.err.println("Greška pri napadu: " + e.getMessage());
            }
        }
    }

    private Rectangle findCellInGrid(GridPane grid, int row, int col) {
        for (var child : grid.getChildren()) {
            Integer nodeRow = GridPane.getRowIndex(child);
            Integer nodeCol = GridPane.getColumnIndex(child);

            if (nodeRow != null && nodeCol != null && nodeRow == row && nodeCol == col) {
                return (Rectangle) child;
            }
        }
        return null;
    }

    private void handleManualShipPlacement(int row, int col) {
        Player currentPlayer = gameData.getPlayers()
                .get(gameData.getCurrentPlayerIndex());

        if (currentPlayer.getBoard().getShips().size() >= 5) {
            gameStatusLabel.setText("❌ Već si postavio 5 brodova!");
            return;
        }

        GridPane currentBoard = gameData.getCurrentPlayerIndex() == 0
                ? player1Board : player2Board;

        boolean placed = currentPlayer.getBoard()
                .placeShip(selectedShip, row, col, selectedOrientation);

        if (placed) {
            boardUIService.updateBoardVisuals(currentBoard, currentPlayer, false);
            gameStatusLabel.setText("✅ Brod postavljen!");

            selectedShip = null;

            int shipsCount = currentPlayer.getBoard().getShips().size();
            remainingShipsLabel.setText("Preostali brodovi: " + (5 - shipsCount));

            if (shipsCount >= 5) {
                if (gameData.getCurrentPlayerIndex() == 0) {
                    gameData.setCurrentPlayerIndex(1);
                    currentPlayerLabel.setText("Trenutni igrač: Igrač 2");
                    gameStatusLabel.setText("🚢 Igrač 2 - Postavi svoje brodove!");
                    remainingShipsLabel.setText("Preostali brodovi: 5");
                } else {
                    gameStatusLabel.setText("✅ Svi brodovi postavljeni!");
                    startGameBtn.setDisable(false);
                }
            } else {
                gameStatusLabel.setText("📍 Postavi sljedeći brod!");
            }
        } else {
            gameStatusLabel.setText("❌ Nemoguće postaviti brod!");
        }
    }

    private void updateGameStatus(AttackResult result) {
        if (result == null) {
            gameStatusLabel.setText("Igra je spremna!");
            return;
        }

        String text = switch (result) {
            case HIT -> "💥 Pogođeno!";
            case MISS -> "💧 Promašaj!";
            case SUNK -> "⚠️ Brod potopljen!";
            case WIN -> "🎉 Pobijedio!";
            default -> "?";
        };

        gameStatusLabel.setText(text);
    }

    private void switchTurn() {
        turnService.switchTurn();
        Player currentPlayer = turnService.getCurrentPlayer();
        currentPlayerLabel.setText("Red je: " + currentPlayer.getName());
        gameStatusLabel.setText(currentPlayer.getName() + " je na potezu!");
    }

    // ✅ NOVO - Prebaci boardove za sljedećeg igrača
    private void swapBoardsForNextPlayer() {
        int currentPlayerIndex = gameData.getCurrentPlayerIndex();
        Player currentPlayer = gameData.getPlayers().get(currentPlayerIndex);
        Player opponentPlayer = gameData.getPlayers().get((currentPlayerIndex + 1) % 2);

// ✅ Lijevo: Tvoj board (sa brodovima)
// ✅ Desno: Protivnički board (samo napadi vidljivi)

// Ovisno o tome koja je igrač na potezu:
        if (currentPlayerIndex == 0) {
// Player 1 je na potezu
// Lijevo (player1Board) = Player 1 board sa brodovima
// Desno (player2Board) = Player 2 board sa napadima
            boardUIService.updateBoardVisuals(player1Board, player1, false);
            hideBrodsOnBoard(player2Board, player2);
        } else {
// Player 2 je na potezu
// Lijevo (player1Board) = Player 2 board sa brodovima
// Desno (player2Board) = Player 1 board sa napadima

// Prebaci prikaz: player1Board postaje Player 2 board
            boardUIService.updateBoardVisuals(player1Board, player2, false);
// player2Board postaje Player 1 board (sa napadima)
            hideBrodsOnBoard(player2Board, player1);
        }
    }

    // ✅ Sakrij brodove na boardu ali prikaži napade
    private void hideBrodsOnBoard(GridPane grid, Player player) {
        for (var child : grid.getChildren()) {
            if (child instanceof Rectangle rect) {
                Integer row = GridPane.getRowIndex(rect);
                Integer col = GridPane.getColumnIndex(rect);

                if (row == null || col == null) continue;

                Cell cell = player.getBoard().getCell(row, col);
                rect.getStyleClass().clear();
                rect.getStyleClass().add("cell-rectangle");

                if (cell.getState() == CellState.HIT) {
                    rect.getStyleClass().add("cell-hit");
                } else if (cell.getState() == CellState.MISS) {
                    rect.getStyleClass().add("cell-miss");
                }
            }
        }
    }

    @FXML
    private void handlePlaceRandomShips() {
        Player currentPlayer = gameData.getPlayers()
                .get(gameData.getCurrentPlayerIndex());

        if (currentPlayer.getBoard().getShips().size() >= 5) {
            gameStatusLabel.setText("❌ Već si postavio 5 brodova!");
            return;
        }

        gameSetupService.placeShipsForCurrentPlayer();

        GridPane currentBoard = gameData.getCurrentPlayerIndex() == 0
                ? player1Board : player2Board;

        boardUIService.updateBoardVisuals(currentBoard, currentPlayer, false);
        gameStatusLabel.setText("🎲 Random brodovi postavljeni!");

        int shipsCount = currentPlayer.getBoard().getShips().size();

        if (shipsCount >= 5) {
            if (gameData.getCurrentPlayerIndex() == 0) {
                gameData.setCurrentPlayerIndex(1);
                currentPlayerLabel.setText("Trenutni igrač: Igrač 2");
                gameStatusLabel.setText("🚢 Igrač 2 - Postavi brodove!");
                remainingShipsLabel.setText("Preostali brodovi: 5");
                selectedShip = null;
            } else {
                gameStatusLabel.setText("✅ Svi brodovi postavljeni!");
                startGameBtn.setDisable(false);
            }
        }
    }

    @FXML
    private void handleStartGame() {
        if (!gameSetupService.isSetupComplete()) {
            gameStatusLabel.setText("❌ Svi trebaju postaviti brodove!");
            return;
        }

        gameData.setCurrentPlayerIndex(0);
        gameData.setGameState(GameState.PLAYING);
        gameStatusLabel.setText("▶️ Igra je počela!");
        currentPlayerLabel.setText("Red je: Igrač 1");

        setupPanel.setVisible(false);
        setupPanel.setManaged(false);

// ✅ Prikaži Player 1 board sa brodovima i Player 2 sa napadima
        boardUIService.updateBoardVisuals(player1Board, player1, false);
        hideBrodsOnBoard(player2Board, player2);

        placeRandomShipsBtn.setDisable(true);
        carrierBtn.setDisable(true);
        battleshipBtn.setDisable(true);
        cruiserBtn.setDisable(true);
        submarineBtn.setDisable(true);
        destroyerBtn.setDisable(true);
        horizontalBtn.setDisable(true);
        verticalBtn.setDisable(true);
    }

    @FXML
    private void handleSaveGame() {
        try {
            saveGameState();
            gameStatusLabel.setText("💾 Igra je spremljena!");
        } catch (Exception e) {
            gameStatusLabel.setText("❌ Greška pri spremanju!");
            e.printStackTrace();
        }
    }

    private void saveGameState() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append(" \"gameState\": \"").append(gameData.getGameState()).append("\",\n");
        sb.append(" \"currentPlayer\": ").append(gameData.getCurrentPlayerIndex()).append(",\n");
        sb.append(" \"player1Ships\": ").append(player1.getBoard().getShips().size()).append(",\n");
        sb.append(" \"player2Ships\": ").append(player2.getBoard().getShips().size()).append("\n");
        sb.append("}\n");

        System.out.println("Spremi igru:\n" + sb.toString());
    }

    @FXML
    private void handleReset() {
        resetGame();
    }

    private void resetGame() {
        GameEngine.resetInstance();
        gameEngine = GameEngine.getInstance();

        player1 = new Player("Igrač 1", new Board());
        player2 = new Player("Igrač 2", new Board());
        gameEngine.initializeGame(player1, player2);
        gameData = gameEngine.getGameData();
        turnService = gameEngine.getTurnService();
        turnService.setGameData(gameData);
        gameSetupService = new GameSetupService(gameEngine, gameData);

        boardUIService.clearBoardStyles(player1Board);
        boardUIService.clearBoardStyles(player2Board);

        player1Board.setVisible(true);
        player1Board.setManaged(true);
        player2Board.setVisible(true);
        player2Board.setManaged(true);

        setupPanel.setVisible(true);
        setupPanel.setManaged(true);

        gameStatusLabel.setText("🚢 Igrač 1 - Postavi brodove!");
        currentPlayerLabel.setText("Trenutni igrač: Igrač 1");
        remainingShipsLabel.setText("Preostali brodovi: 5");

        placeRandomShipsBtn.setDisable(false);
        startGameBtn.setDisable(true);

        carrierBtn.setDisable(false);
        battleshipBtn.setDisable(false);
        cruiserBtn.setDisable(false);
        submarineBtn.setDisable(false);
        destroyerBtn.setDisable(false);
        horizontalBtn.setDisable(false);
        verticalBtn.setDisable(false);

        selectedShip = null;
        selectedOrientation = Orientation.HORIZONTAL;
        horizontalBtn.setSelected(true);
        verticalBtn.setSelected(false);
    }

    private void endGame() {
        gameData.setGameState(GameState.GAME_OVER);
        String winner = turnService.getCurrentPlayer().getName();
        gameStatusLabel.setText("🎉 " + winner + " POBIJEDIO!");
        currentPlayerLabel.setText("IGRA JE GOTOVA!");
    }

    private void setupEventHandlers() {
        resetButton.setOnAction(e -> handleReset());
        placeRandomShipsBtn.setOnAction(e -> handlePlaceRandomShips());
        startGameBtn.setOnAction(e -> handleStartGame());
        saveGameBtn.setOnAction(e -> handleSaveGame());
        horizontalBtn.setOnAction(e -> handleOrientationToggle());
        verticalBtn.setOnAction(e -> handleOrientationToggle());
    }

    public void restoreGameState(GameStateMessage gameStateMessage) {
    }
}