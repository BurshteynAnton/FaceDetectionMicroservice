package antonBurshteyn.exception;

import antonBurshteyn.dto.PhotoValidationResponseDto;
import antonBurshteyn.enums.PhotoValidationStatus;
import antonBurshteyn.login.auth.AuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Object> buildErrorResponse(HttpServletRequest request, String message, HttpStatus status) {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/auth")) {
            return ResponseEntity.status(status)
                    .body(new AuthenticationResponse(message, status.value()));
        } else {
            return ResponseEntity.status(status)
                    .body(new PhotoValidationResponseDto("", PhotoValidationStatus.ERROR, message));
        }
    }

    // 204
    @ExceptionHandler(NoPhotosFoundException.class)
    public ResponseEntity<Void> handleNoPhotosFoundException(NoPhotosFoundException ex) {
        logger.warn("No photos found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("Invalid argument: {}", ex.getMessage());
        return buildErrorResponse(request, "Invalid request parameter: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 400
    @ExceptionHandler(InvalidFaceCountException.class)
    public ResponseEntity<Object> handleInvalidFaceCountException(InvalidFaceCountException ex, HttpServletRequest request) {
        logger.warn("Face detection failed: {}", ex.getMessage());
        return buildErrorResponse(request, "Face detection failed: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 400
    @ExceptionHandler(InvalidPhotoIdException.class)
    public ResponseEntity<Object> handleInvalidPhotoIdException(InvalidPhotoIdException ex, HttpServletRequest request) {
        logger.error("Invalid photo ID: {}", ex.getMessage());
        return buildErrorResponse(request, "Invalid photo ID provided: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        String errorMessage = String.join(", ", errors);
        logger.error("Validation error: {}", errorMessage);
        return buildErrorResponse(request, "Validation failed: " + errorMessage, HttpStatus.BAD_REQUEST);
    }

    // 400
    @ExceptionHandler(UserValidationExceptions.class)
    public ResponseEntity<Object> handleValidationException(UserValidationExceptions ex, HttpServletRequest request) {
        logger.error("Validation error for field {}: {}", ex.getField(), ex.getMessage());
        return buildErrorResponse(request, "Invalid input for field: " + ex.getField() + ". " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 400
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<PhotoValidationResponseDto> handleDataAccessException(DataAccessException ex) {
        logger.error("Database access error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new PhotoValidationResponseDto("", PhotoValidationStatus.FAILED, "Database constraint violation occurred."));
    }

    // 401
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        logger.error("Authentication failed: {}", ex.getMessage());
        return buildErrorResponse(request, "Incorrect username or password.", HttpStatus.UNAUTHORIZED);
    }

    // 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + ex.getMessage());
    }

    // 404
    @ExceptionHandler(PhotoNotFoundException.class)
    public ResponseEntity<Object> handlePhotoNotFoundException(PhotoNotFoundException ex, HttpServletRequest request) {
        logger.error("Photo not found: {}", ex.getMessage());
        return buildErrorResponse(request, "Photo not found: " + ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 404
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException ex, HttpServletRequest request) {
        logger.error("User not found: {}", ex.getMessage());
        return buildErrorResponse(request, "User not found in the system.", HttpStatus.NOT_FOUND);
    }

    // 409
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExistsException(UserAlreadyExistsException ex, HttpServletRequest request) {
        logger.error("User already exists: {}", ex.getMessage());
        return buildErrorResponse(request, "User already registered: " + ex.getMessage(), HttpStatus.CONFLICT);
    }

    // 409
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<Object> handleDatabaseException(DatabaseException ex, HttpServletRequest request) {
        logger.error("Database constraint violation: {}", ex.getMessage(), ex);
        return buildErrorResponse(request, "Database error: " + ex.getMessage(), HttpStatus.CONFLICT);
    }

    // 500
    @ExceptionHandler(PhotoProcessingException.class)
    public ResponseEntity<Object> handlePhotoProcessingException(PhotoProcessingException ex, HttpServletRequest request) {
        logger.error("Photo processing error: {}", ex.getMessage());
        return buildErrorResponse(request, "Photo processing failed: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 500
    @ExceptionHandler(StackOverflowError.class)
    public ResponseEntity<String> handleStackOverflowError(StackOverflowError ex) {
        logger.error("StackOverflowError detected!", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal server error: stack overflow detected.");
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error: ", ex);
        return buildErrorResponse(request, "An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 500
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(IOException ex, HttpServletRequest request) {
        logger.error("IO error while reading photo file: {}", ex.getMessage());
        return buildErrorResponse(request, "Error reading photo file: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 500
    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<Object> handleInternalServerError(InternalServerErrorException ex, HttpServletRequest request) {
        logger.error("Internal server error: {}", ex.getMessage());
        return buildErrorResponse(request, "Internal server error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 500
    @ExceptionHandler(FaceValidationException.class)
    public ResponseEntity<Object> handleFaceValidation(FaceValidationException ex, HttpServletRequest request) {
        logger.error("Face validation error: {}", ex.getMessage(), ex);
        return buildErrorResponse(request, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    // 503
    @ExceptionHandler(DatabaseConnectionException.class)
    public ResponseEntity<Object> handleDatabaseConnectionException(DatabaseConnectionException ex, HttpServletRequest request) {
        logger.error("Database connection error: {}", ex.getMessage(), ex);
        return buildErrorResponse(request, "Database is currently unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
