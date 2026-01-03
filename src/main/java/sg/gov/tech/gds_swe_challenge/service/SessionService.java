package sg.gov.tech.gds_swe_challenge.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sg.gov.tech.gds_swe_challenge.entity.Session;
import sg.gov.tech.gds_swe_challenge.repository.SessionRepository;

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
                    return sessionRepository.save(newSession);
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
        sessionRepository.save(session);
    }

    /**
     * Gets session by id (returns null if not found).
     */
    @Transactional(readOnly = true)
    public Session getOpenSession(long id) {
        return sessionRepository.findByIdAndIsClosedFalse(id).orElse(null);
    }

    /**
     * Checks if session is closed.
     */
    public boolean isSessionClosed(long id) {
        Session session = getOpenSession(id);
        return session != null && session.isClosed();
    }
}
