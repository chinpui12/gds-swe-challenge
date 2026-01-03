package sg.gov.tech.gds_swe_challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sg.gov.tech.gds_swe_challenge.entity.Restaurant;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    /**
     * Efficient random selection for specific session using JOIN.
     */
    @Query(value = """
            SELECT r.* FROM restaurant r
            INNER JOIN restaurant_session s ON r.session_name = s.session_name
            WHERE s.id = :sessionId
            ORDER BY RANDOM()
            LIMIT 1
            """, nativeQuery = true)
    Optional<Restaurant> findRandomRestaurantBySession(@Param("sessionId") long sessionId);
}