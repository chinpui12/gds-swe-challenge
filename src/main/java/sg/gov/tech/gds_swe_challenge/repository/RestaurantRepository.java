package sg.gov.tech.gds_swe_challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sg.gov.tech.gds_swe_challenge.entity.Restaurant;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    /**
     * Efficient random selection using random() function.
     */
    @Query(value = """
            SELECT * FROM restaurant
            ORDER BY RANDOM()
            LIMIT 1
            """, nativeQuery = true)
    Optional<Restaurant> findRandomRestaurant();
}