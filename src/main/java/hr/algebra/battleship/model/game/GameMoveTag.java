package hr.algebra.battleship.model.game;


public enum GameMoveTag {
    GAME_MOVE("GameMove"),
    ROW("Row"),
    COLUMN("Column"),
    RESULT("Result"),
    PLAYER_SYMBOL("PlayerSymbol"),
    TIMESTAMP("Timestamp");

    private final String tagName;

    GameMoveTag(String tagName) {
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }
}