package sg.gov.tech.gds_swe_challenge.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import sg.gov.tech.gds_swe_challenge.config.TestConfig;
import sg.gov.tech.gds_swe_challenge.constant.AppConstants;
import sg.gov.tech.gds_swe_challenge.entity.Restaurant;
import sg.gov.tech.gds_swe_challenge.entity.Session;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
class RestaurantRepositoryTest {
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private TestEntityManager entityManager;
    private Session testSession;

    @BeforeEach
    void setUp() {
        testSession = new Session();
        testSession.setName("Test Session");
        entityManager.persistAndFlush(testSession);
    }

    @Test
    void shouldSaveRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Kopitiam");
        restaurant.setSession(testSession);

        Restaurant saved = restaurantRepository.save(restaurant);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Kopitiam");
        assertThat(saved.getSession()).isEqualTo(testSession);
        assertThat(saved.getCreatedBy()).isEqualTo(AppConstants.SYSTEM);
    }

    @Test
    void shouldFindById() {
        Restaurant restaurant = createTestRestaurant("Din Tai Fung");
        Long restaurantId = restaurant.getId();

        Restaurant found = restaurantRepository.findById(restaurantId).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Din Tai Fung");
        assertThat(found.getSession()).isEqualTo(testSession);
        assertThat(found.getCreatedBy()).isEqualTo(AppConstants.SYSTEM);
    }

    @Test
    void shouldDeleteRestaurant() {
        Restaurant restaurant = createTestRestaurant("Burger King");
        Long restaurantId = restaurant.getId();

        restaurantRepository.deleteById(restaurantId);
        entityManager.flush();

        assertThat(restaurantRepository.findById(restaurantId)).isEmpty();
    }

    private Restaurant createTestRestaurant(String name) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setSession(testSession);
        return entityManager.persistAndFlush(restaurant);
    }
}
