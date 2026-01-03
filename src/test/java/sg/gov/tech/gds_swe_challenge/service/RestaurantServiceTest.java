package sg.gov.tech.gds_swe_challenge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.gov.tech.gds_swe_challenge.constant.AppConstants;
import sg.gov.tech.gds_swe_challenge.dto.SubmitRestaurantRequest;
import sg.gov.tech.gds_swe_challenge.entity.Restaurant;
import sg.gov.tech.gds_swe_challenge.entity.Session;
import sg.gov.tech.gds_swe_challenge.repository.RestaurantRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {
    @Mock
    private RestaurantRepository repository;
    @Mock
    private SessionService sessionService;

    private RestaurantService sut;

    @BeforeEach
    void setup() {
        sut = new RestaurantService(repository, sessionService);
    }

    @Test
    void addRestaurant_ShouldCreateAndSaveRestaurant() {
        String sessionName = AppConstants.GLOBAL_SESSION_NAME;
        SubmitRestaurantRequest request = new SubmitRestaurantRequest("Kopitiam");

        Session savedSession = new Session();
        savedSession.setName(sessionName);
        savedSession.setId(AppConstants.GLOBAL_SESSION_ID);
        when(sessionService.getOrCreateSession(AppConstants.GLOBAL_SESSION_ID, sessionName, "alice"))
                .thenReturn(savedSession);

        sut.addRestaurant(request, "alice");

        ArgumentCaptor<Restaurant> restaurantCaptor = ArgumentCaptor.forClass(Restaurant.class);
        verify(repository).saveAndFlush(restaurantCaptor.capture());
        Restaurant savedRestaurant = restaurantCaptor.getValue();
        assertThat(savedRestaurant.getName()).isEqualTo("Kopitiam");
        assertThat(savedRestaurant.getSession()).isSameAs(savedSession);

        verify(sessionService).getOrCreateSession(AppConstants.GLOBAL_SESSION_ID, sessionName, "alice");
        verify(repository).saveAndFlush(argThat(restaurant ->
                restaurant.getName().equals("Kopitiam") &&
                        restaurant.getSession().equals(savedSession)));
    }


    @Test
    void addRestaurant_RepositoryThrowsException_ShouldPropagate() {
        String name = "KFC";
        when(repository.saveAndFlush(any(Restaurant.class)))
                .thenThrow(new RuntimeException("Database error"));
        when(sessionService.getOrCreateSession(anyLong(), anyString(), anyString())).thenReturn(null);

        assertThatThrownBy(() -> sut.addRestaurant(new SubmitRestaurantRequest(name), "alice"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(repository, times(1)).saveAndFlush(any(Restaurant.class));
    }

    @Test
    void getRandomRestaurant_ShouldReturnRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Kopitiam");

        when(repository.findRandomRestaurantBySession(1L)).thenReturn(Optional.of(restaurant));

        Restaurant result = sut.getRandomRestaurant(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Kopitiam");
        verify(repository, times(1)).findRandomRestaurantBySession(1L);
    }

    @Test
    void getRandomRestaurant_NoRestaurants_ShouldReturnNull() {
        long sessionId = 1L;
        when(repository.findRandomRestaurantBySession(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.getRandomRestaurant(sessionId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No restaurants available in session: 1");


        verify(sessionService).isSessionClosed(sessionId);
        verify(repository).findRandomRestaurantBySession(sessionId);
        verify(sessionService, never()).closeSession(anyLong(), anyString());
    }
}
