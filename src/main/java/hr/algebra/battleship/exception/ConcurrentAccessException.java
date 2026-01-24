package hr.algebra.battleship.exception;

public class ConcurrentAccessException extends BattleshipException {
    public ConcurrentAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}