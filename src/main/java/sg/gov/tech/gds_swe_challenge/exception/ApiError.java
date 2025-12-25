package sg.gov.tech.gds_swe_challenge.exception;

import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@NullMarked
public record ApiError(
        LocalDateTime timestamp,
        HttpStatus status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public ApiError(HttpStatus status, String error, String message, String path, List<String> details) {
        this(LocalDateTime.now(), status, error, message, path, details);
    }
}
