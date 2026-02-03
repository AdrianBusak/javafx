package hr.algebra.battleship;

import hr.algebra.battleship.controller.BoardController;
import hr.algebra.battleship.jndi.ConfigurationKey;
import hr.algebra.battleship.jndi.ConfigurationReader;
import hr.algebra.battleship.model.enums.PlayerType;
import hr.algebra.battleship.model.game.GameStateMessage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BattleshipApplication extends Application {

    public static PlayerType playerType;
    public static BoardController boardController;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/hr/algebra/battleship/boardView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1400, 800);
        boardController = fxmlLoader.getController();

        stage.setTitle("BATTLESHIP - " + playerType.toString());
        stage.setScene(scene);
        stage.show();

        if (!PlayerType.SINGLE_PLAYER.name().equals(playerType.name())) {
            if (PlayerType.PLAYER_2.name().equals(playerType.name())) {
                Thread serverThread = new Thread(() -> acceptRequests(
                        ConfigurationReader.getIntegerValueForKey(
                                ConfigurationKey.PLAYER_2_SERVER_PORT)));
                serverThread.setDaemon(true);
                serverThread.start();
            } else {
                Thread serverThread = new Thread(() -> acceptRequests(
                        ConfigurationReader.getIntegerValueForKey(
                                ConfigurationKey.PLAYER_1_SERVER_PORT)));
                serverThread.setDaemon(true);
                serverThread.start();
            }
        }
    }

    public static void main(String[] args) {
        // ✅ Provjeri argument
        if (args.length == 0) {
            System.out.println("Usage: java BattleshipApplication [SINGLE_PLAYER|PLAYER_1|PLAYER_2]");
            JOptionPane.showMessageDialog(null,
                    "Koristi: java BattleshipApplication [SINGLE_PLAYER|PLAYER_1|PLAYER_2]");
            System.exit(0);
        }

        String firstCommandLineArg = args[0];
        boolean playerTypeExists = false;

        for (PlayerType pt : PlayerType.values()) {
            if (firstCommandLineArg.equals(pt.toString())) {
                playerTypeExists = true;
                break;
            }
        }

        if (!playerTypeExists) {
            System.out.println("Nepostojeća vrsta igrača: " + firstCommandLineArg);
            JOptionPane.showMessageDialog(null,
                    "Nepostojeća vrsta igrača: " + firstCommandLineArg);
            System.exit(0);
        } else {
            playerType = PlayerType.valueOf(firstCommandLineArg);
            launch();
        }
    }

    private static void acceptRequests(Integer port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.err.printf("Server sluša na portu: %d%n", serverSocket.getLocalPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.err.printf("Klijent spojen sa porta %s%n", clientSocket.getPort());
                new Thread(() -> processSerializableClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processSerializableClient(Socket clientSocket) {
        try (ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {

            GameStateMessage gameStateMessage = (GameStateMessage) ois.readObject();

            Platform.runLater(() -> {
                if (boardController != null) {
                    boardController.restoreGameState(gameStateMessage);
                }
            });

            System.out.println("GameState primljen od protivnika");
            oos.writeObject("Primljeno");
            oos.flush();

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Greška pri primanju: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendRequestToPlayer2(GameStateMessage gameStateMessage) {
        new Thread(() -> {
            try (Socket clientSocket = new Socket(
                    ConfigurationReader.getStringValueForKey(ConfigurationKey.HOSTNAME),
                    ConfigurationReader.getIntegerValueForKey(
                            ConfigurationKey.PLAYER_2_SERVER_PORT))) {

                System.err.printf("Player 1 se spaja na %s:%d%n",
                        clientSocket.getInetAddress(), clientSocket.getPort());

                sendSerializableRequest(clientSocket, gameStateMessage);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Greška pri slanju Player 2: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    // ✅ Plaer 2 šalje Player 1-u
    public static void sendRequestToPlayer1(GameStateMessage gameStateMessage) {
        new Thread(() -> {
            try (Socket clientSocket = new Socket(
                    ConfigurationReader.getStringValueForKey(ConfigurationKey.HOSTNAME),
                    ConfigurationReader.getIntegerValueForKey(
                            ConfigurationKey.PLAYER_1_SERVER_PORT))) {

                System.err.printf("Player 2 se spaja na %s:%d%n",
                        clientSocket.getInetAddress(), clientSocket.getPort());

                sendSerializableRequest(clientSocket, gameStateMessage);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Greška pri slanju Player 1: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private static void sendSerializableRequest(Socket client, GameStateMessage gameStateMessage)
            throws IOException, ClassNotFoundException {
        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
        oos.writeObject(gameStateMessage);
        oos.flush();
        System.out.println("GameState poslano protivniku");
        System.out.println("" + ois.readObject());
    }
}