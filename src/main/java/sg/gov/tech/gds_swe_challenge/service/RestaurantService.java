package sg.gov.tech.gds_swe_challenge.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sg.gov.tech.gds_swe_challenge.dto.SubmitRestaurantRequest;
import sg.gov.tech.gds_swe_challenge.entity.Restaurant;
import sg.gov.tech.gds_swe_challenge.repository.RestaurantRepository;

@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final SessionService sessionService;

    public RestaurantService(RestaurantRepository repository,
                             SessionService sessionService) {
        this.restaurantRepository = repository;
        this.sessionService = sessionService;
    }

    public Restaurant addRestaurant(SubmitRestaurantRequest request, String username) {
        var session = sessionService.getOrCreateSession(request.sessionId(), request.sessionName(), username);

        sessionService.validateUserCanSubmit(session, username);

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.name());
        restaurant.setSession(session);

        return restaurantRepository.saveAndFlush(restaurant);
    }

    @Transactional(readOnly = true)
    public Restaurant getRandomRestaurant(long sessionId) {
        if (sessionService.isSessionClosed(sessionId)) {
            throw new IllegalStateException("Session is already closed, a random restaurant has already been selected");
        }
        var randomRestaurant = restaurantRepository.findRandomRestaurantBySession(sessionId)
                .orElseThrow(() -> new IllegalStateException(
                        "No restaurants available in session: %s".formatted(sessionId)));
        sessionService.closeSession(sessionId, randomRestaurant.getName());

        return randomRestaurant;
    }
}
