package sg.gov.tech.gds_swe_challenge.controller;

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
import org.springframework.web.bind.annotation.RestController;
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
public class RestaurantController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestaurantController.class);
    private final RestaurantService service;

    public RestaurantController(RestaurantService service) {
        this.service = service;
    }

    /**
     * Submits a restaurant choice for lunch voting.
     * <p>
     * Requires valid {@link SubmitRestaurantRequest} and {@code X-Username} header.
     * Delegates to {@link RestaurantService#addRestaurant} and returns created restaurant.
     * </p>
     *
     * @param request  validated restaurant submission data
     * @param username submitter username from X-Username header
     * @return {@link ResponseEntity} with 201 Created + {@link Restaurant} or error response
     */
    @PostMapping("/submit")
    public ResponseEntity<Restaurant> submitRestaurant(
            @Valid @RequestBody SubmitRestaurantRequest request,
            @RequestHeader("X-Username") String username) {
        LOGGER.info("submitRestaurant [request: {}, username: {}]", request, username);
        if (username.trim().isEmpty()) {
            throw new IllegalArgumentException("X-Username header cannot be empty");
        }
        Restaurant restaurant = service.addRestaurant(request.name(), username);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(restaurant);
    }

    /**
     * Retrieves a **random restaurant** from the database.
     *
     * @return {@link ResponseEntity} containing:
     */
    @GetMapping("/random")
    public ResponseEntity<Restaurant> getRandomRestaurant() {
        LOGGER.info("getRandomRestaurant");
        Restaurant restaurant = service.getRandomRestaurant();
        return restaurant != null
                ? ResponseEntity.ok(restaurant)
                : ResponseEntity.notFound().build();
    }
}