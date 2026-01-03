package sg.gov.tech.gds_swe_challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sg.gov.tech.gds_swe_challenge.dto.InviteUserRequest;
import sg.gov.tech.gds_swe_challenge.entity.Session;
import sg.gov.tech.gds_swe_challenge.service.SessionService;

import java.util.List;

/**
 * REST controller for session operations.
 * Handles operations related to the sessions.
 */
@RestController
@RequestMapping(value = "/session")
@NullMarked
@Tag(name = "Session API", description = "Session operations")
public class SessionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionController.class);
    private final SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
    }

    /**
     * Retrieves all sessions regardless of state.
     *
     * @return {@link ResponseEntity}
     */
    @Operation(
            summary = "Get all sessions",
            description = "Returns complete list of sessions including GLOBAL and custom sessions"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Sessions retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Session.class)))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping()
    public ResponseEntity<List<Session>> getSessions() {
        LOGGER.info("getSessions");
        List<Session> sessions = service.getSessions();
        return ResponseEntity.ok(sessions);
    }

    @Operation(
            summary = "Reset session to OPEN state",
            description = "Reopens closed session for new restaurant submissions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session reset successfully"),
            @ApiResponse(responseCode = "400", description = "Session already open"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{sessionId}/reset")
    public ResponseEntity<Session> resetSession(
            @Parameter(description = "Session ID to reset")
            @PathVariable(value = "sessionId") Long sessionId) {
        LOGGER.info("Resetting session [id: {}]", sessionId);
        Session resetSession = service.resetSession(sessionId);
        return ResponseEntity.ok(resetSession);
    }

    /**
     * ✅ Invite user to session (CREATOR ONLY)
     */
    @PostMapping("/invite")
    @Operation(summary = "Invite user to session",
            description = "Only session creator can invite. Session must be OPEN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users invited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid inviter/closed session"),
            @ApiResponse(responseCode = "403", description = "Not session creator"),
            @ApiResponse(responseCode = "404", description = "Session/User not found")
    })
    public ResponseEntity<Session> inviteUser(
            @RequestHeader("X-Username") String inviterUsername,
            @Valid @RequestBody InviteUserRequest request) {
        var session = service.inviteUser(request.sessionId(), inviterUsername, request.usernames());
        return ResponseEntity.ok(session);
    }
}