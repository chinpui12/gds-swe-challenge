package sg.gov.tech.gds_swe_challenge.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sg.gov.tech.gds_swe_challenge.entity.Session;
import sg.gov.tech.gds_swe_challenge.repository.SessionRepository;

import java.util.List;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Gets or creates a session for the given sessionId and sessionName.
     * Creates new session if it doesn't exist.
     * If session is closed for restaurant submissions, throw exception
     */
    @Transactional
    public Session getOrCreateSession(long sessionId, String sessionName) {
        return sessionRepository.findById(sessionId)
                .filter(foundSession -> {
                    if (foundSession.isClosed()) {
                        throw new IllegalStateException("Session is already closed for restaurant submissions");
                    }
                    return true;
                })
                .orElseGet(() -> {
                    Session newSession = new Session();
                    newSession.setName(sessionName);
                    return sessionRepository.saveAndFlush(newSession);
                });
    }

    /**
     * Closes session, no more restaurant submissions allowed
     */
    @Transactional
    public void closeSession(long id, String selectedRestaurant) {
        Session session = getOpenSession(id);
        if (session == null) {
            throw new IllegalStateException("Unable to find open session with id: %s".formatted(id));
        }
        session.setClosed(true);
        session.setSelectedRestaurant(selectedRestaurant);
        sessionRepository.saveAndFlush(session);
    }

    @Transactional
    public Session resetSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException(
                        "Session not found with id: " + sessionId));

        if (!session.isClosed()) {
            throw new IllegalStateException(
                    "Session with id " + sessionId + " is already open");
        }
        session.reset();

        return sessionRepository.saveAndFlush(session);
    }

    /**
     * Gets session by id (returns null if not found).
     */
    @Transactional(readOnly = true)
    public Session getOpenSession(long id) {
        return sessionRepository.findByIdAndIsClosedFalse(id).orElse(null);
    }

    /**
     * Get all sessions
     */
    @Transactional(readOnly = true)
    public List<Session> getSessions() {
        return sessionRepository.findAll();
    }

    /**
     * Checks if session is closed.
     */
    public boolean isSessionClosed(long id) {
        Session session = getOpenSession(id);
        return session != null && session.isClosed();
    }
}
