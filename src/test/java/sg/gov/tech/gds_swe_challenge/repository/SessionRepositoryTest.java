package sg.gov.tech.gds_swe_challenge.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import sg.gov.tech.gds_swe_challenge.config.TestConfig;
import sg.gov.tech.gds_swe_challenge.entity.Session;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
class SessionRepositoryTest {
    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Session openSession;
    private Session closedSession;

    @BeforeEach
    void setUp() {
        openSession = new Session();
        openSession.setName("open-session-1");
        openSession.setClosed(false);
        entityManager.persistAndFlush(openSession);

        closedSession = new Session();
        closedSession.setName("closed-session-1");
        closedSession.setClosed(true);
        entityManager.persistAndFlush(closedSession);
    }

    @Test
    void shouldSaveSession() {
        Session newSession = new Session();
        newSession.setName("new-session");
        newSession.setClosed(false);

        Session saved = sessionRepository.save(newSession);
        entityManager.flush();

        assertThat(saved.getName()).isEqualTo("new-session");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.isClosed()).isFalse();
        assertThat(saved.getCreatedBy()).isNotNull();
    }

    @Test
    void shouldFindByIdAndIsClosedFalse_openSession() {
        Long openSessionId = openSession.getId();

        Optional<Session> found = sessionRepository.findByIdAndIsClosedFalse(openSessionId);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("open-session-1");
        assertThat(found.get().isClosed()).isFalse();
    }

    @Test
    void shouldNotFindByIdAndIsClosedFalse_closedSession() {
        Long closedSessionId = closedSession.getId();

        Optional<Session> found = sessionRepository.findByIdAndIsClosedFalse(closedSessionId);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindById_normalFindById() {
        Long openSessionId = openSession.getId();

        Optional<Session> found = sessionRepository.findById(openSessionId);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("open-session-1");
    }

    @Test
    void shouldUpdateSession() {
        Long openSessionId = openSession.getId();
        openSession.setClosed(true);

        sessionRepository.save(openSession);
        entityManager.flush();

        Session found = sessionRepository.findById(openSessionId).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.isClosed()).isTrue();
        assertThat(found.getUpdatedBy()).isNotNull(); // Auditing
    }

    @Test
    void shouldDeleteSession() {
        Long openSessionId = openSession.getId();

        sessionRepository.deleteById(openSessionId);
        entityManager.flush();

        assertThat(sessionRepository.findById(openSessionId)).isEmpty();
    }

    @Test
    void findByIdAndIsClosedFalse_nonExistentId_returnsEmpty() {
        Optional<Session> found = sessionRepository.findByIdAndIsClosedFalse(999L);

        assertThat(found).isEmpty();
    }
}
