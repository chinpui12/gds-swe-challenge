package sg.gov.tech.gds_swe_challenge.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sg.gov.tech.gds_swe_challenge.entity.Restaurant;
import sg.gov.tech.gds_swe_challenge.repository.RestaurantRepository;

@Service
public class RestaurantService {
    private final RestaurantRepository repository;

    public RestaurantService(RestaurantRepository repository) {
        this.repository = repository;
    }

    public Restaurant addRestaurant(String name, String submittedBy) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setSubmittedBy(submittedBy);

        return repository.save(restaurant);
    }

    @Transactional(readOnly = true)
    public Restaurant getRandomRestaurant() {
        return repository.findRandomRestaurant().orElse(null);
    }
}
