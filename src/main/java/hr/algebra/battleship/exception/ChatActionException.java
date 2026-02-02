package hr.algebra.battleship.exception;

public class ChatActionException extends RuntimeException {
    public ChatActionException() {
    }

    public ChatActionException(String message) {
        super(message);
    }

    public ChatActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChatActionException(Throwable cause) {
        super(cause);
    }

    public ChatActionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
