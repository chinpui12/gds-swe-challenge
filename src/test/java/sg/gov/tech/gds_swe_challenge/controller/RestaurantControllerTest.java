package sg.gov.tech.gds_swe_challenge.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import sg.gov.tech.gds_swe_challenge.constant.AppConstants;
import sg.gov.tech.gds_swe_challenge.dto.SubmitRestaurantRequest;
import sg.gov.tech.gds_swe_challenge.entity.Restaurant;
import sg.gov.tech.gds_swe_challenge.service.RestaurantService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebMvcTest(RestaurantController.class)
class RestaurantControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private RestaurantService restaurantService;

    private RestTestClient client;

    @BeforeEach
    void setup() {
        client = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    void submitRestaurant() {
        SubmitRestaurantRequest request = new SubmitRestaurantRequest("Kopitiam");
        Restaurant savedRestaurant = new Restaurant();
        savedRestaurant.setId(1L);
        savedRestaurant.setName("Kopitiam");
        savedRestaurant.setCreatedBy("Test User");
        when(restaurantService.addRestaurant(request, "Test User"))
                .thenReturn(savedRestaurant);

        client.post().uri("/restaurant/submit")
                .header("X-Username", "Test User")
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Restaurant.class)
                .value(restaurant -> {
                    assertThat(restaurant).isNotNull();
                    assertThat(restaurant.getId()).isOne();
                    assertThat(restaurant.getName()).isEqualTo("Kopitiam");
                    assertThat(restaurant.getCreatedBy()).isEqualTo("Test User");
                });
    }

    @Test
    void badRequestWithMissingUsername() {
        SubmitRestaurantRequest request = new SubmitRestaurantRequest("Pizza Hut");

        client.post().uri("/restaurant/submit")
                .body(request)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void handleUnexpectedServiceException() {
        SubmitRestaurantRequest request = new SubmitRestaurantRequest("KFC");
        when(restaurantService.addRestaurant(request, "Test User"))
                .thenThrow(new RuntimeException("Unexpected service error"));

        client.post().uri("/restaurant/submit")
                .header("X-Username", "Test User")
                .body(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getRandomRestaurant() {
        Restaurant savedRestaurant = new Restaurant();
        savedRestaurant.setId(1L);
        savedRestaurant.setName("Pizza Hut");
        when(restaurantService.getRandomRestaurant(AppConstants.GLOBAL_SESSION_ID, "Test User")).thenReturn(savedRestaurant);

        client.get()
                .uri("/restaurant/random")
                .header("X-Username", "Test User")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Restaurant.class)
                .value(restaurant -> {
                    assertThat(restaurant).isNotNull();
                    assertThat(restaurant.getName()).isEqualTo("Pizza Hut");
                    assertThat(restaurant.getId()).isOne();
                });
    }

    @Test
    void getRandomRestaurant_NoRestaurants() {
        when(restaurantService.getRandomRestaurant(AppConstants.GLOBAL_SESSION_ID, "Test User")).thenReturn(null);

        client.get()
                .uri("/restaurant/random")
                .header("X-Username", "Test User")
                .exchange()
                .expectStatus().isNotFound();
    }
}
