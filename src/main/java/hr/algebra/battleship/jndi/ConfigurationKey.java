package hr.algebra.battleship.jndi;

public enum ConfigurationKey {
    HOSTNAME("hostname"),
    PLAYER_1_SERVER_PORT("player1.server.port"),
    PLAYER_2_SERVER_PORT("player2.server.port"),
    RMI_PORT("rmi.port");

    private final String key;

    ConfigurationKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
