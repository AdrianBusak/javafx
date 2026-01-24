package hr.algebra.battleship.exception;

public abstract class BattleshipException extends RuntimeException {
    public BattleshipException(String message) {
        super(message);
    }
    public BattleshipException(String message, Throwable cause) {
        super(message, cause);
    }
}