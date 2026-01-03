package sg.gov.tech.gds_swe_challenge.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import sg.gov.tech.gds_swe_challenge.controller.RestaurantController;
import sg.gov.tech.gds_swe_challenge.dto.SubmitRestaurantRequest;
import sg.gov.tech.gds_swe_challenge.service.RestaurantService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

/**
 * Use RestaurantController as base to test the exceptions caught by the global exception handler
 */
@WebMvcTest({RestaurantController.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private RestaurantService restaurantService;

    private RestTestClient client;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    void handleMethodArgumentNotValid() {
        SubmitRestaurantRequest invalidRequest = new SubmitRestaurantRequest("");

        client.post()
                .uri("/restaurant/submit")
                .header("X-Username", "Test User")
                .contentType(MediaType.APPLICATION_JSON)
                .body(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ApiError.class)
                .value(apiError -> {
                    assertThat(apiError).isNotNull();
                    assertThat(apiError.error()).isEqualTo("Validation Error");
                    assertThat(apiError.message()).isEqualTo("Invalid request data");
                    assertThat(apiError.status()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(apiError.details()).containsExactly("[field: name, error: must not be blank]");
                    assertThat(apiError.timestamp().toString()).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{1,9}");
                    assertThat(apiError.path()).contains("/restaurant/submit");
                });
    }

    @Test
    void handleBusinessLogicExceptions_IllegalArgument() {
        doThrow(new IllegalArgumentException("Duplicate restaurant name")).when(restaurantService).addRestaurant(any(SubmitRestaurantRequest.class));

        client.post()
                .uri("/restaurant/submit")
                .header("X-Username", "Test User")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SubmitRestaurantRequest("Kopitiam"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ApiError.class)
                .value(apiError -> {
                    assertThat(apiError).isNotNull();
                    assertThat(apiError.error()).isEqualTo("Business Rule Violation");
                    assertThat(apiError.message()).isEqualTo("Duplicate restaurant name");
                    assertThat(apiError.status()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(apiError.details()).isEmpty();
                    assertThat(apiError.path()).contains("/restaurant/submit");
                });
    }

    @Test
    void handleBusinessLogicExceptions_IllegalState() {
        // Given: Service throws IllegalStateException
        doThrow(new IllegalStateException("Restaurant submission closed")).when(restaurantService).addRestaurant(any(SubmitRestaurantRequest.class));

        // When + Then
        client.post()
                .uri("/restaurant/submit")
                .header("X-Username", "Test User")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SubmitRestaurantRequest("Pizza Hut"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ApiError.class)
                .value(apiError -> {
                    assertThat(apiError).isNotNull();
                    assertThat(apiError.error()).isEqualTo("Business Rule Violation");
                    assertThat(apiError.message()).isEqualTo("Restaurant submission closed");
                });
    }

    @Test
    void handleBusinessLogicExceptions_EmptyHeader() {
        client.post()
                .uri("/restaurant/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SubmitRestaurantRequest("KFC"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ApiError.class)
                .value(apiError -> {
                    assertThat(apiError).isNotNull();
                    assertThat(apiError.error()).isEqualTo("Missing Header");
                    assertThat(apiError.message()).contains("Required header 'X-Username' is missing");
                });
    }

    @Test
    void handleGenericException_InternalServerError() {
        doThrow(new RuntimeException("Database connection failed")).when(restaurantService).addRestaurant(any(SubmitRestaurantRequest.class));

        client.post()
                .uri("/restaurant/submit")
                .header("X-Username", "Test User")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SubmitRestaurantRequest("McDonald's"))
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ApiError.class)
                .value(apiError -> {
                    assertThat(apiError).isNotNull();
                    assertThat(apiError.error()).isEqualTo("Internal Server Error");
                    assertThat(apiError.message()).isEqualTo("An unexpected error occurred");
                    assertThat(apiError.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(apiError.details()).containsExactly("Please contact support");
                });
    }
}
