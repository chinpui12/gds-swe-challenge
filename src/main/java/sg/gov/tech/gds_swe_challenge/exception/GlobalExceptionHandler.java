package sg.gov.tech.gds_swe_challenge.exception;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

@ControllerAdvice
@NullMarked
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles method argument validation failures from @Valid annotations and Bean Validation.
     * <p>
     * Extracts field validation errors from BindingResult, filters out null/empty messages,
     * and returns a standardized {@link ApiError} response with HTTP 400 Bad Request status.
     * </p>
     *
     * @param ex      the {@link MethodArgumentNotValidException} containing validation errors
     * @param headers HTTP response headers
     * @param status  the {@link HttpStatusCode} for the response (BAD_REQUEST)
     * @param request the current {@link WebRequest} containing path information
     * @return {@link ResponseEntity} with 400 status and {@link ApiError} body containing:
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        LOGGER.warn("Validation failed: {}", ex.getMessage());

        var errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> "[field: %s, error: %s]".formatted(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                "Invalid request data",
                request.getDescription(false).replace("uri=", ""),
                errors
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    /**
     * Handles business logic violations thrown by service layer.
     * <p>
     * Catches {@link IllegalArgumentException} and {@link IllegalStateException} for common business rule violations
     * such as invalid session state, duplicate submissions, or constraint violations. Returns HTTP 400 Bad Request
     * with standardized {@link ApiError} response containing the exception message.
     * </p>
     *
     * @param ex      the {@link RuntimeException} containing business logic violation details
     * @param request the current {@link WebRequest} for path extraction
     * @return {@link ResponseEntity} with HTTP 400 Bad Request status and {@link ApiError} body:
     */
    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class})
    public ResponseEntity<ApiError> handleBusinessLogicExceptions(
            RuntimeException ex,
            WebRequest request) {
        LOGGER.warn("Business logic error: {}", ex.getMessage());

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Business Rule Violation",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                List.of()
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> handleMissingRequestHeader(
            MissingRequestHeaderException ex,
            WebRequest request) {
        LOGGER.warn("Missing required header '{}': {}", ex.getHeaderName(), ex.getMessage());

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Missing Header",
                String.format("Required header '%s' is missing", ex.getHeaderName()),
                request.getDescription(false).replace("uri=", ""),
                List.of("Please provide the " + ex.getHeaderName() + " header")
        );
        return ResponseEntity.badRequest().body(apiError);
    }

    /**
     * Handle generic internal server exceptions that are unexpected
     *
     * @param ex      exception that occurred
     * @param request request made
     * @return {@link ResponseEntity} with 500 status and {@link ApiError} body containing:
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            WebRequest request) {
        LOGGER.error("Unexpected error occurred", ex);

        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred",
                request.getDescription(false).replace("uri=", ""),
                List.of("Please contact support")
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}
