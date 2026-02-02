package hr.algebra.battleship.utils;

import hr.algebra.battleship.exception.ChatActionException;
import hr.algebra.battleship.jndi.ConfigurationKey;
import hr.algebra.battleship.jndi.ConfigurationReader;
import hr.algebra.battleship.rmi.ChatRemoteService;
import hr.algebra.battleship.views.BattleshipApplication;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Optional;

public class ChatUtils {

    private ChatUtils() {}

    public static Timeline getChatRefreshTimeline(ChatRemoteService chatRemoteService,
                                                  TextArea chatMessageTextArea)
    {
        Timeline chatMessagesRefreshTimeLine = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            try {
                List<String> chatMessages =  chatRemoteService.getAllMessages();

                StringBuilder textMessagesBuilder = new StringBuilder();

                for(String message : chatMessages) {
                    textMessagesBuilder.append(message).append("\n");
                }

                chatMessageTextArea.setText(textMessagesBuilder.toString());

            } catch (RemoteException ex) {
                throw new ChatActionException("RMI Chat service failed!", ex);
            }
        }), new KeyFrame(Duration.seconds(1)));

        chatMessagesRefreshTimeLine.setCycleCount(Animation.INDEFINITE);
        return chatMessagesRefreshTimeLine;
    }

    public static void sendChatMessage(ChatRemoteService chatRemoteService,
                                       TextField chatMessageTextField)
    {
        String chatMessage = chatMessageTextField.getText();
        try {
            chatRemoteService.sendChatMessage(BattleshipApplication.playerType + ": " + chatMessage);
        } catch (RemoteException e) {
            throw new ChatActionException("Sending chat message failed!", e);
        }
    }

    public static Optional<ChatRemoteService> initializeChatRemoteService() {

        Optional<ChatRemoteService> chatRemoteServiceOptional
                = Optional.empty();

        try {
            Registry registry = LocateRegistry.getRegistry(
                    ConfigurationReader.getStringValueForKey(ConfigurationKey.HOSTNAME),
                    ConfigurationReader.getIntegerValueForKey(ConfigurationKey.RMI_PORT));
            chatRemoteServiceOptional = Optional.of((ChatRemoteService) registry.lookup(ChatRemoteService.REMOTE_OBJECT_NAME));
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        return chatRemoteServiceOptional;
    }
}
