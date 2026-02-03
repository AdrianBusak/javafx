package hr.algebra.battleship.controller;

import hr.algebra.battleship.exception.ChatActionException;
import hr.algebra.battleship.gameEngine.GameEngine;
import hr.algebra.battleship.model.enums.*;
import hr.algebra.battleship.model.game.*;
import hr.algebra.battleship.model.game.Cell;
import hr.algebra.battleship.model.ships.*;
import hr.algebra.battleship.rmi.ChatRemoteService;
import hr.algebra.battleship.services.*;
import hr.algebra.battleship.utils.ChatUtils;
import hr.algebra.battleship.utils.GameUtils;
import hr.algebra.battleship.utils.DocumentationUtils;
import hr.algebra.battleship.BattleshipApplication;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
    @FXML private Label player1BoardLabel, player2BoardLabel;
    @FXML private Button resetButton, placeRandomShipsBtn, startGameBtn, saveGameBtn, readyButton;
    @FXML private Button loadGameBtn, historyBtn;
    @FXML private HBox setupPanel;

    @FXML private Button carrierBtn, battleshipBtn, cruiserBtn, submarineBtn, destroyerBtn;
    @FXML private ToggleButton horizontalBtn, verticalBtn;

    @FXML private TextField chatInputField;
    @FXML private TextArea chatDisplayArea;
    @FXML private Button sendChatBtn;

    private ChatRemoteService chatService;
    private String playerName;

    private GameEngine gameEngine;
    private TurnService turnService;
    private BoardUIService boardUIService;
    private GameSetupService gameSetupService;
    private CellVisualizationService visualizationService;

    private Timeline chatRefreshTimeline;


    private GameData gameData;
    private Player player1, player2;

    private Ship selectedShip;
    private Orientation selectedOrientation = Orientation.HORIZONTAL;

    private boolean player1Ready = false, player2Ready = false, boardLocked = false;

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
        initializeChat();
        initializeBoards();
        setupEventHandlers();
        updateBoardLabelsForCurrentPlayer();
        resetGame();


    }

    private void initializeChat() {
        try {
            Optional<ChatRemoteService> chatRemoteServiceOptional = ChatUtils.initializeChatRemoteService();

            if (chatRemoteServiceOptional.isPresent()) {
                chatService = chatRemoteServiceOptional.get();
                playerName = isPlayer1() ? "Igrač 1" : "Igrač 2";
                chatDisplayArea.appendText("Chat spojen!\n");

                chatRefreshTimeline = ChatUtils.getChatRefreshTimeline(chatService, chatDisplayArea);
                chatRefreshTimeline.play();

            } else {
                chatDisplayArea.appendText("RMI servis nije dostupan!\n");
            }

        } catch (ChatActionException e) {
            chatDisplayArea.appendText("Chat greška: " + e.getMessage() + "\n");
            System.err.println("Chat connection error: " + e.getMessage());
        }
    }


    private void initializeBoards() {
        boardUIService.createBoardGrid(player1Board, false, this::handleCellClick);
        boardUIService.createBoardGrid(player2Board, true, this::handleCellClick);
    }

    private boolean isSinglePlayer() {
        return BattleshipApplication.playerType == PlayerType.SINGLE_PLAYER;
    }

    private boolean isMultiplayer() {
        return BattleshipApplication.playerType == PlayerType.PLAYER_1 ||
                BattleshipApplication.playerType == PlayerType.PLAYER_2;
    }

    private boolean isPlayer1() {
        return BattleshipApplication.playerType == PlayerType.PLAYER_1;
    }

    //============== CHAT =========================
    @FXML
    private void handleSendChat() {
        try {
            ChatUtils.sendChatMessage(chatService, chatInputField);

            chatInputField.clear();
        } catch (Exception e) {
            chatDisplayArea.appendText("Greška slanja: " + e.getMessage() + "\n");
        }
    }

    // ============= PERSISTENCE HANDLERS =============

    @FXML private void handleSaveGame() {
        try {
            GameStateMessage gameStateMsg = new GameStateMessage();
            gameStateMsg.setPlayer1Board(player1.getBoard());
            gameStateMsg.setPlayer2Board(player2.getBoard());
            gameStateMsg.setCurrentPlayerIndex(gameData.getCurrentPlayerIndex());
            gameStateMsg.setGameState(gameData.getGameState());

            GameUtils.saveGame(gameStateMsg);
            gameStatusLabel.setText("Igra je spremljena u ./game/save.dat");
            System.out.println("Igra je uspješno spremljena!");

        } catch (Exception e) {
            gameStatusLabel.setText("Greška pri spravljanju igre: " + e.getMessage());
            System.err.println("Greška pri spravljanju igre: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleLoadGame() {
        try {
            System.out.println("Pokušavam učitati igru...");

            Object loadedObj = GameUtils.loadGame();

            if (loadedObj == null) {
                gameStatusLabel.setText("Nema spremljene igre!");
                return;
            }

            if (!(loadedObj instanceof GameStateMessage)) {
                gameStatusLabel.setText("Datoteka je oštećena!");
                return;
            }

            GameStateMessage loadedGame = (GameStateMessage) loadedObj;

            if (loadedGame.getPlayer1Board() != null) {
                player1.setBoard(loadedGame.getPlayer1Board());
            }
            if (loadedGame.getPlayer2Board() != null) {
                player2.setBoard(loadedGame.getPlayer2Board());
            }

            gameData.setCurrentPlayerIndex(loadedGame.getCurrentPlayerIndex());
            gameData.setGameState(loadedGame.getGameState());

            player1Board.getChildren().clear();
            player2Board.getChildren().clear();
            initializeBoards();

            boardUIService.updateBoardVisuals(player1Board, player1, false);
            boardUIService.updateBoardVisuals(player2Board, player2, true);

            gameStatusLabel.setText("Igra je učitana iz ./game/save.dat");
            currentPlayerLabel.setText("Igrač: " + player1.getName());

            System.out.println("Igra je uspješno učitana!");

        } catch (Exception e) {
            gameStatusLabel.setText("Nema spravljene igre ili datoteka je oštećena!");
            System.err.println("Greška pri učitavanju igre: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Dokumentacija
    @FXML private void handleGenerateDocumentation() {
        try {
            System.out.println("Generiram dokumentaciju...");
            DocumentationUtils.generateHtmlDocumentationFile();
            gameStatusLabel.setText("Dokumentacija generiirana: ./doc/documentation.html");
            System.out.println("Dokumentacija je uspješno generiirana!");

        } catch (Exception e) {
            gameStatusLabel.setText("Greška pri generiranju dokumentacije!");
            System.err.println("Greška pri generiranju dokumentacije: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============= SHIP SELECTION =============

    @FXML private void selectCarrier() { selectShip(new Carrier()); }
    @FXML private void selectBattleship() { selectShip(new Battleship()); }
    @FXML private void selectCruiser() { selectShip(new Cruiser()); }
    @FXML private void selectSubmarine() { selectShip(new Submarine()); }
    @FXML private void selectDestroyer() { selectShip(new Destroyer()); }

    private void selectShip(Ship ship) {
        int currentPlayerIndex = gameData.getCurrentPlayerIndex();
        Player currentPlayer = gameData.getPlayers().get(currentPlayerIndex);

        if (currentPlayer.getBoard().getShips().size() >= 5) {
            gameStatusLabel.setText("Već si postavio 5 brodova!");
            return;
        }

        selectedShip = ship;
        String playerName = currentPlayerIndex == 0 ? "Igrač 1" : "Igrač 2";
        gameStatusLabel.setText(ship.getType() + " (" + playerName + ") - Klikni na svoj board!");
    }

    @FXML private void handleOrientationToggle() {
        horizontalBtn.setSelected(!verticalBtn.isSelected());
        verticalBtn.setSelected(!horizontalBtn.isSelected());
        selectedOrientation = horizontalBtn.isSelected()
                ? Orientation.HORIZONTAL
                : Orientation.VERTICAL;
    }

    private void handleCellClick(Rectangle cell, int row, int col, boolean isOpponent) {
        if (gameData.getGameState() == GameState.SETUP) {
            handleSetupPhase(cell, row, col);
        } else if (gameData.getGameState() == GameState.PLAYING) {
            handlePlayingPhase(cell, row, col, isOpponent);
        }
    }

    private void handleSetupPhase(Rectangle cell, int row, int col) {
        if (!isSinglePlayer() && cell.getParent() != player1Board) {
            gameStatusLabel.setText("Klikni na svoj board!");
            return;
        }

        if (selectedShip != null) {
            handleManualShipPlacement(row, col);
        } else {
            gameStatusLabel.setText("Prvo odaberi brod!");
        }
    }

    private void handlePlayingPhase(Rectangle cell, int row, int col, boolean isOpponent) {
        if (isMultiplayer() && boardLocked) {
            gameStatusLabel.setText("Čekaj potez protivnika...");
            return;
        }

        boolean clickedOnPlayer2Board = cell.getParent() == player2Board;
        if (!clickedOnPlayer2Board) {
            gameStatusLabel.setText("Klikni na Protivnički Board!");
            return;
        }

        try {
            Player opponent;
            if (isSinglePlayer()) {
                int currentPlayerIndex = gameData.getCurrentPlayerIndex();
                opponent = gameData.getPlayers().get((currentPlayerIndex + 1) % 2);
            } else if (isPlayer1()) {
                opponent = player2;
            } else {
                opponent = player1;
            }

            Cell targetCell = opponent.getBoard().getCell(row, col);
            if (targetCell == null) return;

            AttackResult result = targetCell.attack();

            String resultStr = result == AttackResult.MISS ? "MISS" :
                    result == AttackResult.SUNK ? "SINK" : "HIT";
            String playerSymbol = isPlayer1() ? "PLAYER_1" : "PLAYER_2";
            GameUtils.createGameAndSaveWithThread(row, col, resultStr, playerSymbol);

            Rectangle cellRect = findCellInGrid(player2Board, row, col);
            if (cellRect != null) {
                boardUIService.updateCellStyle(cellRect,
                        result == AttackResult.MISS ? CellState.MISS : CellState.HIT);
                visualizationService.animateAttack(cellRect, result);
            }

            updateGameStatus(result);

            if (result == AttackResult.SUNK && opponent.getBoard().getShips().stream()
                    .allMatch(Ship::isSunk)) {
                result = AttackResult.WIN;
                gameData.setGameState(GameState.GAME_OVER);
            }

            if (result != AttackResult.WIN) {
                if (isSinglePlayer()) {
                    PauseTransition pause = new PauseTransition(Duration.millis(800));
                    pause.setOnFinished(e -> {
                        switchTurn();
                        swapBoardsForNextPlayer();
                        updateBoardLabelsForCurrentPlayer();
                    });
                    pause.play();
                } else {
                    GameStateMessage responseMessage = new GameStateMessage();
                    responseMessage.setGameState(GameState.PLAYING);
                    responseMessage.setAttackRow(row);
                    responseMessage.setAttackCol(col);
                    responseMessage.setAttackResult(resultStr);
                    responseMessage.setPlayer1Board(player1.getBoard());
                    responseMessage.setPlayer2Board(player2.getBoard());

                    if (isPlayer1()) {
                        BattleshipApplication.sendRequestToPlayer2(responseMessage);
                    } else {
                        BattleshipApplication.sendRequestToPlayer1(responseMessage);
                    }

                    boardLocked = true;
                    currentPlayerLabel.setText("Čekaš...");
                    gameStatusLabel.setText("Čekaj potez protivnika...");
                }
            } else {
                endGame();
            }
        } catch (Exception e) {
            System.err.println("Greška: " + e.getMessage());
            e.printStackTrace();
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
        Player currentPlayer = gameData.getPlayers().get(gameData.getCurrentPlayerIndex());

        if (currentPlayer.getBoard().getShips().size() >= 5) {
            gameStatusLabel.setText("Već si postavio 5 brodova!");
            return;
        }

        Player targetPlayer = isMultiplayer() ? (isPlayer1() ? player1 : player2) : currentPlayer;
        boolean placed = targetPlayer.getBoard().placeShip(selectedShip, row, col, selectedOrientation);

        if (placed) {
            GridPane currentBoard = isSinglePlayer() ?
                    (gameData.getCurrentPlayerIndex() == 0 ? player1Board : player2Board) :
                    player1Board;

            boardUIService.updateBoardVisuals(currentBoard, targetPlayer, false);
            gameStatusLabel.setText("Brod postavljen!");
            selectedShip = null;

            int shipsCount = targetPlayer.getBoard().getShips().size();
            remainingShipsLabel.setText("Preostali brodovi: " + (5 - shipsCount));

            if (shipsCount >= 5) {
                if (isSinglePlayer()) {
                    if (gameData.getCurrentPlayerIndex() == 0) {
                        gameData.setCurrentPlayerIndex(1);
                        updateBoardLabelsForCurrentPlayer();
                        currentPlayerLabel.setText("Trenutni igrač: Igrač 2");
                        gameStatusLabel.setText("Igrač 2 - Postavi svoje brodove!");
                        remainingShipsLabel.setText("Preostali brodovi: 5");
                    } else {
                        gameStatusLabel.setText("Svi brodovi postavljeni!");
                        startGameBtn.setDisable(false);
                    }
                } else {
                    gameStatusLabel.setText("Svi brodovi postavljeni! Klikni Spreman!");
                    readyButton.setDisable(false);
                }
            }
        } else {
            gameStatusLabel.setText("Nemoguće postaviti brod!");
        }
    }

    private void updateGameStatus(AttackResult result) {
        if (result == null) {
            gameStatusLabel.setText("Igra je spremna!");
            return;
        }

        String text = switch (result) {
            case HIT -> "Pogođeno!";
            case MISS -> "Promašaj!";
            case SUNK -> "Brod potopljen!";
            case WIN -> "Pobijedio!";
            default -> "?";
        };

        gameStatusLabel.setText(text);
    }

    private void switchTurn() {
        gameData.setCurrentPlayerIndex((gameData.getCurrentPlayerIndex() + 1) % 2);
    }

    private void updateBoardLabelsForCurrentPlayer() {
        if (player1BoardLabel == null || player2BoardLabel == null) return;

        int currentPlayerIndex = gameData.getCurrentPlayerIndex();

        if (isSinglePlayer()) {
            if (currentPlayerIndex == 0) {
                player1BoardLabel.setText("Moj Board (Igrač 1)");
                player2BoardLabel.setText("Protivnički Board (Igrač 2)");
            } else {
                player1BoardLabel.setText("Moj Board (Igrač 2)");
                player2BoardLabel.setText("Protivnički Board (Igrač 1)");
            }
        } else {
            player1BoardLabel.setText("Moji brodovi");
            player2BoardLabel.setText("Napadam ovdje");
        }
    }

    private void swapBoardsForNextPlayer() {
        int currentPlayerIndex = gameData.getCurrentPlayerIndex();
        Player currentPlayer = gameData.getPlayers().get(currentPlayerIndex);
        Player opponent = gameData.getPlayers().get((currentPlayerIndex + 1) % 2);

        boardUIService.updateBoardVisuals(player1Board, currentPlayer, false);
        hideShipsOnBoard(player2Board, opponent);

        currentPlayerLabel.setText("Igrač " + (currentPlayerIndex + 1) + " - TVOJ RED!");
    }

    @FXML private void handlePlaceRandomShips() {
        Player currentPlayer = gameData.getPlayers().get(gameData.getCurrentPlayerIndex());

        if (currentPlayer.getBoard().getShips().size() >= 5) {
            gameStatusLabel.setText("Već si postavio 5 brodova!");
            return;
        }

        Player targetPlayer = isMultiplayer() ? (isPlayer1() ? player1 : player2) : currentPlayer;
        gameSetupService.placeShipsForPlayer(targetPlayer);

        GridPane currentBoard = isSinglePlayer() ?
                (gameData.getCurrentPlayerIndex() == 0 ? player1Board : player2Board) :
                player1Board;

        boardUIService.updateBoardVisuals(currentBoard, targetPlayer, false);
        gameStatusLabel.setText("Nasumični brodovi postavljeni!");

        int shipsCount = targetPlayer.getBoard().getShips().size();

        if (shipsCount >= 5) {
            if (isSinglePlayer()) {
                if (gameData.getCurrentPlayerIndex() == 0) {
                    gameData.setCurrentPlayerIndex(1);
                    updateBoardLabelsForCurrentPlayer();
                    currentPlayerLabel.setText("Trenutni igrač: Igrač 2");
                    gameStatusLabel.setText("Igrač 2 - Postavi brodove!");
                    remainingShipsLabel.setText("Preostali brodovi: 5");
                    selectedShip = null;
                } else {
                    gameStatusLabel.setText("Svi brodovi postavljeni!");
                    startGameBtn.setDisable(false);
                }
            } else {
                gameStatusLabel.setText("Svi brodovi postavljeni! Klikni Spreman!");
                readyButton.setDisable(false);
            }
        }
    }

    @FXML private void handleReady() {
        Player currentPlayer = isPlayer1() ? player1 : player2;

        if (currentPlayer.getBoard().getShips().size() < 5) {
            gameStatusLabel.setText("Moraš postaviti 5 brodova!");
            return;
        }

        if (isPlayer1()) {
            player1Ready = true;
        } else {
            player2Ready = true;
        }

        readyButton.setDisable(true);
        gameStatusLabel.setText("Spreman/na! Čekam protivnika...");

        GameStateMessage message = new GameStateMessage();
        message.setGameState(GameState.SETUP);
        if (isPlayer1()) {
            message.setPlayer1Board(player1.getBoard());
            BattleshipApplication.sendRequestToPlayer2(message);
        } else {
            message.setPlayer2Board(player2.getBoard());
            BattleshipApplication.sendRequestToPlayer1(message);
        }
    }

    public void restoreGameState(GameStateMessage gameStateMessage) {
        try {
            if (gameStateMessage == null) {
                System.err.println("GameStateMessage je null!");
                return;
            }

            System.out.println("Primljen GameStateMessage");

            if (gameStateMessage.getPlayer1Board() != null) {
                player1.setBoard(gameStateMessage.getPlayer1Board());
                System.out.println("Igrač 1 board ažuriran");
            }
            if (gameStateMessage.getPlayer2Board() != null) {
                player2.setBoard(gameStateMessage.getPlayer2Board());
                System.out.println("Igrač 2 board ažuriran");
            }

            GameState newGameState = gameStateMessage.getGameState();

            if (newGameState == GameState.SETUP) {
                if (isPlayer1()) {
                    player2Ready = true;
                    System.out.println("Igrac 2 je spreman!");
                } else {
                    player1Ready = true;
                    System.out.println("Igrač 1 je spreman!");
                }

                gameStatusLabel.setText("Protivnik je spreman! Klikni POČNI!");
                startGameBtn.setDisable(false);

            } else if (newGameState == GameState.PLAYING) {
                gameData.setGameState(GameState.PLAYING);
                System.out.println("Igra je u tijeku...");

                if (gameStateMessage.getAttackRow() != -1 && gameStateMessage.getAttackCol() != -1) {
                    int attackRow = gameStateMessage.getAttackRow();
                    int attackCol = gameStateMessage.getAttackCol();
                    String result = gameStateMessage.getAttackResult();

                    System.out.println("Protivnik je napao: [" + attackRow + "," + attackCol + "] = " + result);

                    Rectangle cellRect = findCellInGrid(player1Board, attackRow, attackCol);

                    if (cellRect != null) {
                        if ("HIT".equals(result)) {
                            boardUIService.updateCellStyle(cellRect, CellState.HIT);
                            System.out.println("Prikazano: POGOĐENO");
                        } else if ("MISS".equals(result)) {
                            boardUIService.updateCellStyle(cellRect, CellState.MISS);
                            System.out.println("Prikazano: PROMAŠAJ");
                        }
                    }
                }

                boardLocked = false;
                currentPlayerLabel.setText("TVOJ RED!");
                gameStatusLabel.setText("Napad!");

                setupPanel.setVisible(false);
                setupPanel.setManaged(false);

                System.out.println("Spreman za napad!");
            }

        } catch (Exception e) {
            System.err.println("Greška pri obnavljanju stanja: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleStartGame() {
        if (isSinglePlayer()) {
            if (player1.getBoard().getShips().size() < 5 || player2.getBoard().getShips().size() < 5) {
                gameStatusLabel.setText("Svi trebaju postaviti brodove!");
                return;
            }
        } else {
            if (!player1Ready || !player2Ready) {
                gameStatusLabel.setText("Oba igrača trebaju biti spremna!");
                return;
            }
        }

        gameData.setCurrentPlayerIndex(0);
        gameData.setGameState(GameState.PLAYING);
        gameStatusLabel.setText("Igra je počela!");

        setupPanel.setVisible(false);
        setupPanel.setManaged(false);

        if (isSinglePlayer()) {
            updateBoardLabelsForCurrentPlayer();
            currentPlayerLabel.setText("Igrač 1 - TVOJ RED!");
            boardUIService.updateBoardVisuals(player1Board, player1, false);
            hideShipsOnBoard(player2Board, player2);
        } else {
            if (isPlayer1()) {
                boardUIService.updateBoardVisuals(player1Board, player1, false);
                boardUIService.updateBoardVisuals(player2Board, player2, true);
                currentPlayerLabel.setText("TVOJ RED!");
            } else {
                boardUIService.updateBoardVisuals(player1Board, player2, false);
                boardUIService.updateBoardVisuals(player2Board, player1, true);
                currentPlayerLabel.setText("Čekaš...");
                boardLocked = true;
            }

            updateBoardLabelsForCurrentPlayer();

            GameStateMessage message = new GameStateMessage();
            message.setGameState(GameState.PLAYING);
            message.setAttackRow(-1);
            message.setAttackCol(-1);
            message.setPlayer1Board(player1.getBoard());
            message.setPlayer2Board(player2.getBoard());

            if (isPlayer1()) {
                BattleshipApplication.sendRequestToPlayer2(message);
            } else {
                BattleshipApplication.sendRequestToPlayer1(message);
            }
        }

        disableSetupButtons();
    }

    @FXML private void handleReset() {
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

        if (isSinglePlayer()) {
            gameStatusLabel.setText("Igrač 1 - Postavi brodove!");
            currentPlayerLabel.setText("Trenutni igrač: Igrač 1");
        } else {
            String playerLabel = isPlayer1() ? "Igrač 1" : "Igrač 2";
            gameStatusLabel.setText(playerLabel + " - Postavi brodove!");
            currentPlayerLabel.setText("Postava: " + playerLabel);
        }


        enableSetupButtons();
        selectedShip = null;
        selectedOrientation = Orientation.HORIZONTAL;
        horizontalBtn.setSelected(true);
        verticalBtn.setSelected(false);

        player1Ready = false;
        player2Ready = false;
        boardLocked = false;
    }

    private void endGame() {
        gameData.setGameState(GameState.GAME_OVER);

        if (isSinglePlayer()) {
            String winner = gameData.getCurrentPlayerIndex() == 0 ? "Igrač 1" : "Igrač 2";
            gameStatusLabel.setText(winner + " JE POBIJEDIO!");
            currentPlayerLabel.setText("IGRA JE GOTOVA!");
        } else {
            boolean iWon = (isPlayer1() && player2.getBoard().getShips().stream().allMatch(Ship::isSunk)) ||
                    (!isPlayer1() && player1.getBoard().getShips().stream().allMatch(Ship::isSunk));

            gameStatusLabel.setText(iWon ? "TI SI POBIJEDIO!" : "IZGUBIO SI!");
            currentPlayerLabel.setText(iWon ? "POBJEDA!" : "PORAZ!");
        }
    }

    private void hideShipsOnBoard(GridPane grid, Player player) {
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

    private void disableSetupButtons() {
        placeRandomShipsBtn.setDisable(true);
        readyButton.setDisable(true);
        startGameBtn.setDisable(true);
        carrierBtn.setDisable(true);
        battleshipBtn.setDisable(true);
        cruiserBtn.setDisable(true);
        submarineBtn.setDisable(true);
        destroyerBtn.setDisable(true);
        horizontalBtn.setDisable(true);
        verticalBtn.setDisable(true);
    }

    private void enableSetupButtons() {
        if (isMultiplayer()) {
            saveGameBtn.setVisible(false);
            loadGameBtn.setVisible(false);
            System.out.println("Spremi/Učitaj su onemogućeni za multiplayer");
        }else{
            placeRandomShipsBtn.setDisable(false);
            readyButton.setDisable(true);
            startGameBtn.setDisable(true);
            carrierBtn.setDisable(false);
            battleshipBtn.setDisable(false);
            cruiserBtn.setDisable(false);
            submarineBtn.setDisable(false);
            destroyerBtn.setDisable(false);
            horizontalBtn.setDisable(false);
            verticalBtn.setDisable(false);
        }
    }

    private void setupEventHandlers() {
        resetButton.setOnAction(e -> handleReset());
        placeRandomShipsBtn.setOnAction(e -> handlePlaceRandomShips());
        readyButton.setOnAction(e -> handleReady());
        startGameBtn.setOnAction(e -> handleStartGame());
        saveGameBtn.setOnAction(e -> handleSaveGame());
        loadGameBtn.setOnAction(e -> handleLoadGame());
        historyBtn.setOnAction(e -> handleGenerateDocumentation());
        horizontalBtn.setOnAction(e -> handleOrientationToggle());
        verticalBtn.setOnAction(e -> handleOrientationToggle());
        sendChatBtn.setOnAction(e -> handleSendChat());
        chatInputField.setOnAction(e -> handleSendChat());
    }
}
