package antonBurshteyn.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DatabaseException extends RuntimeException {
    private final String email;

    public DatabaseException(String email, Throwable cause) {
        super("Database error for user: " + email, cause);
        this.email = email;
    }
}

