package sg.gov.tech.gds_swe_challenge.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import sg.gov.tech.gds_swe_challenge.entity.Session;
import sg.gov.tech.gds_swe_challenge.service.SessionService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebMvcTest(SessionController.class)
class SessionControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SessionService sessionService;
    private RestTestClient client;

    @BeforeEach
    void setup() {
        client = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    void getSessions_allSessions() {
        List<Session> sessions = List.of(
                createSession(0L, "GLOBAL", false, null),
                createSession(1L, "team-alpha", true, "Kopitiam"),
                createSession(2L, "team-beta", false, null)
        );
        when(sessionService.getSessions()).thenReturn(sessions);

        List<Session> retrievedSessions = client.get().uri("/session")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<Session>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(retrievedSessions).hasSize(3);
        assertThat(retrievedSessions.get(0).getId()).isZero();
        assertThat(retrievedSessions.get(0).getName()).isEqualTo("GLOBAL");
        assertThat(retrievedSessions.get(1).getName()).isEqualTo("team-alpha");
        assertThat(retrievedSessions.get(1).isClosed()).isTrue();
        assertThat(retrievedSessions.get(1).getSelectedRestaurant()).isEqualTo("Kopitiam");
    }

    @Test
    void getSessions_emptyList() {
        when(sessionService.getSessions()).thenReturn(List.of());

        List<Session> retrievedSessions = client.get().uri("/session")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<Session>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(retrievedSessions).isEmpty();
    }

    @Test
    void resetSession_success() {
        long sessionId = 1L;
        Session resetSession = createSession(sessionId, "team-alpha", false, null);
        when(sessionService.resetSession(sessionId)).thenReturn(resetSession);

        Session retrievedSession = client.patch().uri("/session/{sessionId}/reset", sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Session.class)
                .returnResult()
                .getResponseBody();

        assertThat(retrievedSession).isNotNull();
        assertThat(retrievedSession.getId()).isOne();
        assertThat(retrievedSession.getName()).isEqualTo("team-alpha");
        assertThat(retrievedSession.isClosed()).isFalse();
        assertThat(retrievedSession.getSelectedRestaurant()).isNull();
    }

    @Test
    void resetSession_alreadyOpen_400() {
        long sessionId = 2L;
        when(sessionService.resetSession(sessionId))
                .thenThrow(new IllegalStateException("Session is already open"));

        Map<String, Object> errorResponse = client.patch().uri("/session/{sessionId}/reset", sessionId)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(errorResponse)
                .containsEntry("error", "Business Rule Violation")
                .containsEntry("message", "Session is already open");
    }

    @Test
    void resetSession_notFound_400() {
        long sessionId = 3L;
        when(sessionService.resetSession(sessionId))
                .thenThrow(new IllegalStateException("Session not found"));

        Map<String, Object> errorResponse = client.patch().uri("/session/{sessionId}/reset", sessionId)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(errorResponse).containsEntry("message", "Session not found");
    }

    @Test
    void getSessions_serviceThrows500() {
        when(sessionService.getSessions())
                .thenThrow(new RuntimeException("Database failure"));

        client.get().uri("/session")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void resetSession_serviceThrows500() {
        when(sessionService.resetSession(4L))
                .thenThrow(new RuntimeException("Transaction rollback"));

        client.patch().uri("/session/{sessionId}/reset", 4L)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    private Session createSession(Long id, String name, boolean closed, String selectedRestaurant) {
        Session session = new Session();
        session.setId(id);
        session.setName(name);
        session.setClosed(closed);
        session.setSelectedRestaurant(selectedRestaurant);
        return session;
    }
}
