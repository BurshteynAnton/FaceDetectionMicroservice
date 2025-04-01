package antonBurshteyn.exception;

public class PhotoProcessingException extends RuntimeException {
    public PhotoProcessingException(String message) {
        super(message);
    }

    public PhotoProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
