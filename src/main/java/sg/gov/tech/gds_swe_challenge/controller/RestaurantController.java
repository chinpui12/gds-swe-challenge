package sg.gov.tech.gds_swe_challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sg.gov.tech.gds_swe_challenge.constant.AppConstants;
import sg.gov.tech.gds_swe_challenge.dto.SubmitRestaurantRequest;
import sg.gov.tech.gds_swe_challenge.entity.Restaurant;
import sg.gov.tech.gds_swe_challenge.service.RestaurantService;

/**
 * REST controller for restaurant submission operations.
 * Handles lunch voting restaurant submissions with username tracking.
 */
@RestController
@RequestMapping(value = "/restaurant")
@NullMarked
@Tag(name = "Restaurant API", description = "Restaurant submission and selection")
public class RestaurantController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestaurantController.class);
    private final RestaurantService service;

    public RestaurantController(RestaurantService service) {
        this.service = service;
    }

    /**
     * Submits a restaurant choice for lunch voting. Defaults to global session if session name is not provided in request
     * <p>
     * Requires valid {@link SubmitRestaurantRequest} and {@code X-Username} header.
     * Delegates to {@link RestaurantService#addRestaurant} and returns created restaurant.
     * </p>
     *
     * @param request  validated restaurant submission data
     * @param username submitter username from X-Username header
     * @return {@link ResponseEntity} with 201 Created + {@link Restaurant} or error response
     */
    @Operation(
            summary = "Submit restaurant to session",
            description = "Adds restaurant to specified session (creates if new)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Restaurant created"),
            @ApiResponse(responseCode = "400", description = "Validation failed/Session closed")
    })
    @PostMapping("/submit")
    public ResponseEntity<Restaurant> submitRestaurant(
            @Valid @RequestBody SubmitRestaurantRequest request,
            @RequestHeader(AppConstants.HEADER_X_USERNAME) String username) {
        LOGGER.info("submitRestaurant [request: {}, username: {}]", request, username);
        Restaurant restaurant = service.addRestaurant(request, username);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(restaurant);
    }

    /**
     * Retrieves a **random restaurant** from the database based on the selected session.
     *
     * @param sessionId session id (defaults to GLOBAL_SESSION_ID if not provided)
     * @param username  X-Username header
     * @return {@link ResponseEntity}
     */
    @Operation(summary = "Get random restaurant")
    @GetMapping("/random")
    public ResponseEntity<Restaurant> getRandomRestaurant(
            @Parameter(description = "Session ID (default: GLOBAL=0)")
            @RequestParam(value = "sessionId", defaultValue = AppConstants.GLOBAL_SESSION_ID_STR) String sessionId,
            @RequestHeader(AppConstants.HEADER_X_USERNAME) String username) {
        LOGGER.info("getRandomRestaurant [sessionId: {}, username: {}]", sessionId, username);
        Restaurant restaurant = service.getRandomRestaurant(Long.parseLong(sessionId), username);
        return restaurant != null
                ? ResponseEntity.ok(restaurant)
                : ResponseEntity.notFound().build();
    }
}