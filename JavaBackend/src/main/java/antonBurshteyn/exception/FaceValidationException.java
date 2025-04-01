package antonBurshteyn.exception;

public class FaceValidationException extends RuntimeException {
    public FaceValidationException(String message) {
        super(message);
    }

    public FaceValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
