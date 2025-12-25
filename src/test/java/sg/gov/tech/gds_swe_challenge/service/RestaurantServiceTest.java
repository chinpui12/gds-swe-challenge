package sg.gov.tech.gds_swe_challenge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.gov.tech.gds_swe_challenge.entity.Restaurant;
import sg.gov.tech.gds_swe_challenge.repository.RestaurantRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {
    @Mock
    private RestaurantRepository repository;

    private RestaurantService sut;

    @BeforeEach
    void setup() {
        sut = new RestaurantService(repository);
    }

    @Test
    void addRestaurant_ShouldCreateAndSaveRestaurant() {
        String name = "Kopitiam";
        String submittedBy = "Test User";
        Restaurant savedRestaurant = new Restaurant();
        savedRestaurant.setId(1L);
        savedRestaurant.setName("Kopitiam");
        savedRestaurant.setSubmittedBy("Test User");

        when(repository.save(any(Restaurant.class))).thenReturn(savedRestaurant);

        Restaurant result = sut.addRestaurant(name, submittedBy);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isOne();
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getSubmittedBy()).isEqualTo(submittedBy);

        verify(repository, times(1)).save(argThat(restaurant ->
                restaurant.getName().equals(name) &&
                        restaurant.getSubmittedBy().equals(submittedBy)
        ));
    }

    @Test
    void addRestaurant_ShouldSetCorrectFields() {
        String name = "Pizza Hut";
        String submittedBy = "Test User";

        when(repository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> {
                    Restaurant restaurant = invocation.getArgument(0);
                    restaurant.setId(2L);
                    return restaurant;
                });

        Restaurant result = sut.addRestaurant(name, submittedBy);

        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getSubmittedBy()).isEqualTo(submittedBy);
        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    void addRestaurant_RepositoryThrowsException_ShouldPropagate() {
        String name = "KFC";
        String submittedBy = "Test User";
        when(repository.save(any(Restaurant.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> sut.addRestaurant(name, submittedBy))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(repository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void getRandomRestaurant_ShouldReturnRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Kopitiam");

        when(repository.findRandomRestaurant()).thenReturn(Optional.of(restaurant));

        Restaurant result = sut.getRandomRestaurant();

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Kopitiam");
        verify(repository, times(1)).findRandomRestaurant();
    }

    @Test
    void getRandomRestaurant_NoRestaurants_ShouldReturnNull() {
        when(repository.findRandomRestaurant()).thenReturn(Optional.empty());

        Restaurant result = sut.getRandomRestaurant();

        assertThat(result).isNull();
        verify(repository, times(1)).findRandomRestaurant();
    }
}
