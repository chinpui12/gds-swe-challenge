package sg.gov.tech.gds_swe_challenge.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sg.gov.tech.gds_swe_challenge.constant.AppConstants;
import sg.gov.tech.gds_swe_challenge.entity.Session;
import sg.gov.tech.gds_swe_challenge.entity.User;
import sg.gov.tech.gds_swe_challenge.repository.SessionRepository;

import java.util.List;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;
    private final UserService userService;

    public SessionService(SessionRepository sessionRepository,
                          UserService userService) {
        this.sessionRepository = sessionRepository;
        this.userService = userService;
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

    /**
     * Invite user - only by creator
     */
    @Transactional
    public Session inviteUser(Long sessionId, String inviterUsername, List<String> invitedUsernames) {
        Session session = getSession(sessionId);
        User inviter = userService.getUser(inviterUsername);

        if (!session.isCreator(inviter)) {
            throw new IllegalStateException("Only session creator can invite users");
        }

        if (session.isClosed()) {
            throw new IllegalStateException("Cannot invite to closed session");
        }

        invitedUsernames
                .stream()
                .distinct()
                .forEach(invitedUsername -> {
                    User invitedUser = userService.getUser(invitedUsername);
                    session.addInvitedUser(invitedUser);
                });

        return sessionRepository.saveAndFlush(session);
    }

    private Session getSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("Session not found: " + sessionId));
    }

    public void validateUserCanSubmit(Session session, String username) {
        if (session != null) {
            var sessionId = session.getId();
            try {
                if (sessionId.equals(AppConstants.GLOBAL_SESSION_ID)) {
                    return;
                }

                if (session.isClosed()) {
                    throw new IllegalStateException(
                            "Session %d is closed for submissions".formatted(sessionId));
                }

                if (!session.isUserInvited(username)) {
                    throw new IllegalStateException(
                            "User '%s' not invited to session '%s' (ID: %d)".formatted(
                                    username, session.getName(), sessionId));
                }
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Cannot validate submission for session %d: %s".formatted(
                                sessionId, e.getMessage()), e);
            }
        }
    }
}
